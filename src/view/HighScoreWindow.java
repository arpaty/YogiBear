/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import database.HighScores;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;

/**
 *
 * @author Patrik Bogdan
 */
public class HighScoreWindow extends JDialog {

    private JTable table = null;

    public HighScoreWindow(JFrame parent) throws SQLException {
        super(parent, true);

        try {
            HighScores highScores = new HighScores(10);
            table = new JTable(new HighScoreTableModel(highScores.getTopTen()));
            table.setFillsViewportHeight(true);

            add(new JScrollPane(table));
            setSize(400, 400);
            setTitle("Leaderboard");
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            setLocationRelativeTo(null);
            setVisible(true);
        } catch (SQLException ex) {
            Logger.getLogger(YogiBearGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
