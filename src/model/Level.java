/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.ArrayList;

/**
 *
 * @author Patrik Bogdan
 */
public class Level {

    private final int gameID;
    private final int rows;
    private final int cols;
    private LevelItem[][] field;
    private Position playerPosition = new Position(5, 8);
    private int numBaskets, numBasketsCollected;

    private ArrayList<Position> rangerPositions;

    public Level(ArrayList<String> fieldRows, int id) {
        this.gameID = id;
        int countCols = 0;

        for (String s : fieldRows) {
            if (s.length() > countCols) {
                countCols = s.length();
            }
        }

        rows = fieldRows.size();
        cols = countCols;

        field = new LevelItem[rows][cols];
        numBaskets = 0;
        numBasketsCollected = 0;

        rangerPositions = new ArrayList<>();

        for (int i = 0; i < rows; i++) {
            String s = fieldRows.get(i);
            for (int j = 0; j < s.length(); j++) {
                switch (s.charAt(j)) {
                    case '#':
                        field[i][j] = LevelItem.OBSTACLE;
                        break;
                    case 'y':
                        playerPosition = new Position(j, i);
                        field[i][j] = LevelItem.PLAYER;
                        break;
                    case 'r':
                        field[i][j] = LevelItem.RANGER;
                        rangerPositions.add(new Position(j, i));
                        break;
                    case 'b':
                        field[i][j] = LevelItem.BASKET;
                        numBaskets++;
                        break;
                    default:
                        field[i][j] = LevelItem.EMPTY;
                        break;
                }
            }
        }
    }

    protected boolean isValidPosition(Position p) {
        return (p.getX() >= 0 && p.getY() >= 0 && p.getX() < cols && p.getY() < rows);
    }

    protected boolean isFree(Position p) {
        if (!isValidPosition(p)) {
            return false;
        }
        LevelItem li = field[p.getY()][p.getX()];
        return (li == LevelItem.EMPTY);
    }

    /**
     *
     * @param p the current position
     * @return true if there is a basket on that position
     */
    protected boolean isBasket(Position p) {
        if (!isValidPosition(p)) {
            return false;
        }
        LevelItem li = field[p.getY()][p.getX()];
        return (li == LevelItem.BASKET);
    }

    /**
     *
     * @param nextPosition the next position
     * @return true if the player collected a basket
     */
    protected boolean collectedBasket(Position nextPosition) {
        if (isBasket(nextPosition)) {
            field[playerPosition.getY()][playerPosition.getX()] = LevelItem.EMPTY;
            playerPosition = nextPosition;
            field[playerPosition.getY()][playerPosition.getX()] = LevelItem.PLAYER;
            numBasketsCollected++;
            return true;
        }
        return false;
    }

    public ArrayList<Position> getRangerPositions() {
        ArrayList<Position> positions = rangerPositions;
        return positions;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getLevelId() {
        return gameID;
    }

    public LevelItem[][] getField() {
        return field;
    }

    public LevelItem getFieldValue(int row, int col) {
        return field[row][col];
    }

    public void setFieldValue(int row, int col, LevelItem li) {
        field[row][col] = li;
    }

    public Position getPlayerPosition() {
        return playerPosition;
    }

    public void setPlayerPosition(Position p) {
        this.playerPosition = p;
    }

    public boolean allBasketsCollected() {
        return numBasketsCollected == numBaskets;
    }

    public int getCollectedBaskets() {
        return numBasketsCollected;
    }

    public int getBaskets() {
        return numBaskets;
    }

    public void printLevel() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(field[i][j].representation);
            }
            System.out.println("");
        }
    }
}
