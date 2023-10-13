/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import database.HighScores;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import res.ResourceLoader;
import view.YogiBearGUI;

/**
 *
 * @author Patrik Bogdan
 */
public final class GameEngine extends JPanel {

    private ArrayList<ArrayList<String>> levels;
    private Level currentLevel = null;

    private final Image obstacle;
    private final Image basket;
    private final Image player;
    private final Image ranger;
    private final Image empty;

    private final Timer newFrameTimer;

    private int playerLives = 3;
    private int playerScore = 0;

    private final int tileSize = 48;

    private final int currentMap;

    public GameEngine(String mapToLoad) throws IOException, SQLException {
        super();
        readLevels();

        obstacle = ResourceLoader.loadImage("res/obstacle.png");
        basket = ResourceLoader.loadImage("res/basket.png");
        player = ResourceLoader.loadImage("res/player.png");
        ranger = ResourceLoader.loadImage("res/ranger.png");
        empty = ResourceLoader.loadImage("res/empty.png");

        currentMap = Integer.parseInt(mapToLoad.replaceAll("\\D+", ""));
        loadLevel(currentMap);

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, false), "pressed A");
        this.getActionMap().put("pressed A", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                step(Direction.LEFT);
            }
        });

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, false), "pressed W");
        this.getActionMap().put("pressed W", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                step(Direction.UP);
            }
        });

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, false), "pressed S");
        this.getActionMap().put("pressed S", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                step(Direction.DOWN);
            }
        });

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, false), "pressed D");
        this.getActionMap().put("pressed D", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                step(Direction.RIGHT);
            }
        });

        newFrameTimer = new Timer(500, new NewFrameListener());
        newFrameTimer.start();
    }

    /**
     * reads the game levels and stores them
     */
    protected void readLevels() throws FileNotFoundException, IOException {
        levels = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            String path = "src/res/level" + (i + 1) + ".txt";
            BufferedReader br = new BufferedReader(new FileReader(path));
            ArrayList<String> fieldRows = new ArrayList<>();
            String line = br.readLine();

            if (line != null) {
                fieldRows.add(line);
                while ((line = br.readLine()) != null) {
                    fieldRows.add(line);
                }
                levels.add(fieldRows);
            }
        }
    }

    /**
     *
     * paints the game board
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D gr = (Graphics2D) g;

        int cols = currentLevel.getCols();
        int rows = currentLevel.getRows();

        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                Image img = null;
                LevelItem li = currentLevel.getFieldValue(x, y);

                switch (li) {
                    case OBSTACLE ->
                        img = obstacle;
                    case BASKET ->
                        img = basket;
                    case PLAYER ->
                        img = player;
                    case RANGER ->
                        img = ranger;
                    case EMPTY ->
                        img = empty;
                }
                gr.drawImage(img, y * tileSize, x * tileSize, tileSize, tileSize, null);
            }
        }
        paintStats(gr);
    }

    /**
     *
     * Paints the game statistics
     */
    protected void paintStats(Graphics g) {
        Graphics2D gr = (Graphics2D) g;

        gr.drawRect(10, 490, 140, 20);
        gr.setColor(Color.BLUE);
        gr.fillRect(10, 490, 140, 20);
        String s = "Baskets collected: " + currentLevel.getCollectedBaskets()
                + "/" + currentLevel.getBaskets();
        gr.setColor(Color.WHITE);
        gr.drawString(s, 20, 505);

        gr.drawRect(160, 490, 140, 20);
        gr.setColor(Color.BLUE);
        gr.fillRect(160, 490, 140, 20);
        String lives = "Remaining lives: " + playerLives;
        gr.setColor(Color.WHITE);
        gr.drawString(lives, 180, 505);

        gr.drawRect(310, 490, 100, 20);
        gr.setColor(Color.BLUE);
        gr.fillRect(310, 490, 100, 20);
        String score = "Score: " + playerScore;
        gr.setColor(Color.WHITE);
        gr.drawString(score, 340, 505);
    }

    class NewFrameListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            if (!isGameOver()) {
                moveRangers();
                repaint();

                if (currentLevel.allBasketsCollected() && !completedAllLevels()) {
                    loadLevel(currentLevel.getLevelId() + 1);
                }

                if (completedAllLevels()) {
                    JLabel nameLabel = new JLabel("Name: ");
                    JTextArea name = new JTextArea("");
                    JComponent[] arr = {nameLabel, name};
                    int input = JOptionPane.showOptionDialog(null, arr, "Victory!",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.INFORMATION_MESSAGE, null, null, null);

                    if (input == 0) {
                        String playerName = name.getText();
                        try {
                            HighScores highScore = new HighScores(100);
                            highScore.putHighScore(playerName, playerScore);
                            newFrameTimer.stop();
                            closeCurrentGame();
                        } catch (SQLException ex) {
                            System.out.println("Error while adding result to database!");
                        }
                    }
                }
            } else {
                JLabel nameLabel = new JLabel("Name: ");
                JTextArea name = new JTextArea("");
                JComponent[] arr = {nameLabel, name};
                int input = JOptionPane.showOptionDialog(null, arr, "Save highscore",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE, null, null, null);

                if (input == 0) {
                    try {
                        newFrameTimer.stop();
                        closeCurrentGame();
                        HighScores highScore = new HighScores(100);
                        String playerName = name.getText();
                        highScore.putHighScore(playerName, playerScore);
                    } catch (SQLException ex) {
                        System.out.println("Error while adding result to database!");
                    }
                } else {
                    newFrameTimer.stop();
                    closeCurrentGame();
                }
            }
        }
    }

    /**
     * closes the current window and opens the main menu
     */
    private void closeCurrentGame() {
        Component comp = SwingUtilities.getRoot(this);
        ((Window) comp).dispose();
        YogiBearGUI menu = new YogiBearGUI();
        menu.setVisible(true);
    }

    /**
     *
     * @param d the direction the player moves to
     * @return whether the player can move in that direction
     */
    public boolean movePlayer(Direction d) {
        Position current = currentLevel.getPlayerPosition();

        Position nextPosition = current.translate(d);

        if (currentLevel.isFree(nextPosition)) {
            currentLevel.setFieldValue(current.getY(), current.getX(),
                    LevelItem.EMPTY);
            currentLevel.setPlayerPosition(nextPosition);

            currentLevel.setFieldValue(nextPosition.getY(),
                    nextPosition.getX(), LevelItem.PLAYER);

            if (isInRange(nextPosition, LevelItem.RANGER)) {
                playerLives--;
                if (playerLives > 0) {
                    respawn();
                }
            }
            return true;
        } else if (currentLevel.collectedBasket(nextPosition)) {
            playerScore++;
        }

        return false;
    }

    /**
     *
     * @param p the position in question
     * @param li the level item to check
     * @return whether the current levelitem is in range of the position
     */
    private boolean isInRange(Position p, LevelItem li) {
        if (!currentLevel.isValidPosition(p)) {
            return false;
        }

        HashMap<Direction, Position> positions = new HashMap<>();

        positions.put(Direction.LEFT, p.translate(Direction.LEFT));
        positions.put(Direction.RIGHT, p.translate(Direction.RIGHT));
        positions.put(Direction.UP, p.translate(Direction.UP));
        positions.put(Direction.DOWN, p.translate(Direction.DOWN));

        for (Position pos : positions.values()) {
            if (currentLevel.getFieldValue(pos.getY(), pos.getX()) == li) {
                return true;
            }
        }

        return false;
    }

    /**
     * moves the rangers randomly
     */
    private void moveRangers() {
        Random rndDirection = new Random();
        int cntr = 0;

        for (int i = 0; i < currentLevel.getRangerPositions().size(); i++) {
            HashMap<Direction, Position> possibleSteps = fillRangerSteps(i);
            int direction = rndDirection.nextInt(possibleSteps.size());

            Position newPosition = null;

            for (Direction curr : possibleSteps.keySet()) {
                if (cntr == direction) {
                    newPosition = possibleSteps.get(curr);
                }
                cntr++;
            }

            Position old = currentLevel.getRangerPositions().get(i);
            currentLevel.setFieldValue(old.getY(), old.getX(), LevelItem.EMPTY);

            currentLevel.getRangerPositions().set(i, newPosition);
            currentLevel.setFieldValue(newPosition.getY(), newPosition.getX(),
                    LevelItem.RANGER);

            if (isInRange(newPosition, LevelItem.PLAYER)) {
                playerLives--;
                if (playerLives > 0) {
                    respawn();
                }
            }
            cntr = 0;
        }
    }

    /**
     *
     * @param i the current ranger's index
     * @return the possible steps the current ranger could move to
     */
    private HashMap<Direction, Position> fillRangerSteps(int i) {
        HashMap<Direction, Position> possibleSteps = new HashMap<>();

        ArrayList<Direction> directions = new ArrayList<>();
        directions.add(Direction.LEFT);
        directions.add(Direction.RIGHT);
        directions.add(Direction.UP);
        directions.add(Direction.DOWN);

        for (Direction d : directions) {
            Position stepOption = currentLevel.getRangerPositions().get(i).translate(d);
            if (currentLevel.isFree(stepOption)) {
                possibleSteps.put(d, stepOption);
            }
        }

        return possibleSteps;
    }

    /**
     * sets the player's position to the starting point
     */
    private void respawn() {
        Position old = currentLevel.getPlayerPosition();
        currentLevel.setFieldValue(old.getY(), old.getX(), LevelItem.EMPTY);

        currentLevel.getPlayerPosition().setX(5);
        currentLevel.getPlayerPosition().setY(8);

        Position newPosition = currentLevel.getPlayerPosition();

        currentLevel.setFieldValue(newPosition.getY(), newPosition.getX(),
                LevelItem.PLAYER);
    }

    private boolean step(Direction d) {
        return movePlayer(d);
    }

    private void loadLevel(int level) {
        currentLevel = new Level(levels.get(level - 1), level);
    }

    private boolean completedAllLevels() {
        return currentLevel.getLevelId() == 10 && currentLevel.allBasketsCollected();
    }

    private boolean isGameOver() {
        return playerLives == 0;
    }

    public boolean isLevelLoaded() {
        return currentLevel != null;
    }

    public Position getPlayerPosition() {
        return new Position(currentLevel.getPlayerPosition().getX(),
                currentLevel.getPlayerPosition().getY());
    }

    public Level getLevel() {
        return currentLevel;
    }
}
