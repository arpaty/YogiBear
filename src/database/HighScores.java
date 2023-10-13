/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Patrik Bogdan
 */
public class HighScores {

    private final int maxScores;
    private PreparedStatement insertStatement;
    private PreparedStatement updateStatement;
    private PreparedStatement deleteStatement;
    private final Connection connection;

    public HighScores(int maxScores) throws SQLException {
        this.maxScores = maxScores;
        String dbURL = "jdbc:derby://localhost:1527/highscores;";
        connection = DriverManager.getConnection(dbURL);

        String insertQuery = "INSERT INTO HIGHSCORES (NAME, SCORE) VALUES (?, ?)";
        insertStatement = connection.prepareStatement(insertQuery);

        String updateQuery = "UPDATE HIGHSCORES SET SCORE=? WHERE NAME=?";
        updateStatement = connection.prepareStatement(updateQuery);

        String deleteQuery = "DELETE FROM HIGHSCORES WHERE SCORE=?";
        deleteStatement = connection.prepareStatement(deleteQuery);
    }

    public ArrayList<HighScore> getHighScores() throws SQLException {
        String query = "SELECT * FROM HIGHSCORES ORDER BY SCORE DESC";
        ArrayList<HighScore> highScores = new ArrayList<>();
        Statement stmt = connection.createStatement();
        ResultSet results = stmt.executeQuery(query);

        while (results.next()) {
            String name = results.getString("NAME");
            int score = results.getInt("SCORE");
            highScores.add(new HighScore(name, score));
        }
        return highScores;
    }

    /**
     * inserts a highscore into the table
     *
     * @param name the player's name
     * @param score the player's score
     */
    public void putHighScore(String name, int score) throws SQLException {
        ArrayList<HighScore> highScores = getHighScores();

        String[] names = new String[highScores.size()];

        for (int i = 0; i < names.length; i++) {
            names[i] = highScores.get(i).getName();
        }

        if (highScores.size() < maxScores) {

            for (int i = 0; i < highScores.size(); i++) {
                if (highScores.get(i).getName().equals(name) && score > highScores.get(i).getScore()) {
                    updateScore(name, score);
                }
            }

            if (Arrays.asList(names).indexOf(name) == -1 && score != 0) {
                insertScore(name, score);
            }
        } else {
            int leastScore = highScores.get(highScores.size() - 1).getScore();
            if (leastScore < score) {
                deleteScores(leastScore);
                insertScore(name, score);
            }
        }
    }

    /**
     * @return the collection of the top ten highscores
     */
    public ArrayList<HighScore> getTopTen() throws SQLException {
        ArrayList<HighScore> highScores = new ArrayList<>();
        Statement stmt = connection.createStatement();
        String topTenQuery = "SELECT * FROM HIGHSCORES ORDER BY SCORE DESC FETCH NEXT 10 ROWS ONLY";
        ResultSet results = stmt.executeQuery(topTenQuery);

        while (results.next()) {
            String name = results.getString("NAME");
            int score = results.getInt("SCORE");
            highScores.add(new HighScore(name, score));
        }

        return highScores;
    }

    private void insertScore(String name, int score) throws SQLException {
        insertStatement.setString(1, name);
        insertStatement.setInt(2, score);
        insertStatement.executeUpdate();
    }

    private void updateScore(String name, int score) throws SQLException {
        updateStatement.setInt(1, score);
        updateStatement.setString(2, name);
        updateStatement.executeUpdate();
    }

    private void deleteScores(int score) throws SQLException {
        deleteStatement.setInt(1, score);
        deleteStatement.executeUpdate();
    }
}
