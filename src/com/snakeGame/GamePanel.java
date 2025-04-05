package com.snakeGame;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / UNIT_SIZE;
    static final int DELAY = 75;

    final int[] x = new int[GAME_UNITS];
    final int[] y = new int[GAME_UNITS];
    int bodyParts = 6;
    int applesEaten;
    int appleX;
    int appleY;
    char direction = 'R';
    boolean running = false;
    boolean gameStarted = false;
    Timer timer;
    Random random;

    // Color and Font Definitions
    Color snakeHeadColor = new Color(0, 180, 0);
    Color snakeBodyColor = new Color(45, 180, 0);
    Color appleColor = Color.red;
    Color gridColor = new Color(50, 50, 50);
    Color backgroundColor = Color.black;
    Color textColor = Color.white;
    
    // Video game-style fonts
    Font scoreFont = new Font("Courier New", Font.BOLD, 20);
    Font gameOverFont = new Font("Impact", Font.BOLD, 60);
    Font instructionsFont = new Font("Courier New", Font.BOLD, 16);
    Font titleFont = new Font("Impact", Font.BOLD, 30);
    Font startScreenFont = new Font("Arial", Font.BOLD, 50);
    Font startInstructionsFont = new Font("Courier New", Font.BOLD, 20);

    GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(backgroundColor);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        // Instead of immediately starting the game, we initialize everything but wait for user input
        initializeGame();
    }

    private void initializeGame() {
        resetGame();
        newApple();
        // Game is initialized but not running yet - waiting at start screen
        running = false;
        gameStarted = false;
        timer = new Timer(DELAY, this);
        // Start the timer just for drawing the start screen with animations
        timer.start();
    }

    private void startGame() {
        resetGame();
        newApple();
        running = true;
        gameStarted = true;
        timer.setDelay(DELAY);
        timer.start();
    }

    private void resetGame() {
        applesEaten = 0;
        bodyParts = 6;
        direction = 'R';
        // Initialize snake position at center
        int startX = SCREEN_WIDTH / 2;
        int startY = SCREEN_HEIGHT / 2;
        for (int i = 0; i < bodyParts; i++) {
            x[i] = startX - i * UNIT_SIZE;
            y[i] = startY;
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw((Graphics2D) g);
    }

    public void draw(Graphics2D g) {
        // Enable antialiasing for smoother rendering
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (!gameStarted) {
            gameStart(g);
        } else if (running) {
            // Draw grid
            g.setColor(gridColor);
            for (int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++) {
                g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, SCREEN_HEIGHT);
                g.drawLine(0, i * UNIT_SIZE, SCREEN_WIDTH, i * UNIT_SIZE);
            }

            // Draw apple with gradient effect
            RadialGradientPaint appleGradient = new RadialGradientPaint(
                appleX + UNIT_SIZE / 2,
                appleY + UNIT_SIZE / 2,
                UNIT_SIZE / 2,
                new float[]{0.0f, 1.0f},
                new Color[]{new Color(255, 50, 50), appleColor}
            );
            g.setPaint(appleGradient);
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

            // Draw snake with slight gradient effect
            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    // Snake head with eyes
                    g.setColor(snakeHeadColor);
                    g.fillRoundRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE, 10, 10);
                    drawSnakeEyes(g, i);
                } else {
                    // Snake body with slight color change
                    Color bodyColor = new Color(
                        snakeBodyColor.getRed(),
                        Math.max(10, snakeBodyColor.getGreen() - i * 3),
                        snakeBodyColor.getBlue()
                    );
                    g.setColor(bodyColor);
                    g.fillRoundRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE, 8, 8);
                }
            }

            // Draw score with arcade-style appearance
            // Black outline for visibility
            g.setColor(Color.black);
            g.setFont(scoreFont);
            FontMetrics metrics = getFontMetrics(g.getFont());
            String scoreText = "SCORE: " + applesEaten;
            
            // Draw text shadow/outline for video game effect
            for (int xOffset = -2; xOffset <= 2; xOffset += 2) {
                for (int yOffset = -2; yOffset <= 2; yOffset += 2) {
                    if (xOffset != 0 || yOffset != 0) {
                        g.drawString(scoreText, 
                            (SCREEN_WIDTH - metrics.stringWidth(scoreText)) / 2 + xOffset, 
                            30 + yOffset);
                    }
                }
            }
            
            // Main score text
            g.setColor(new Color(255, 255, 0)); // Bright yellow for arcade feel
            g.drawString(scoreText, (SCREEN_WIDTH - metrics.stringWidth(scoreText)) / 2, 30);

        } else {
            gameOver(g);
        }
    }

    public void gameStart(Graphics2D g) {
        // Draw animated background effect
        drawAnimatedBackground(g);
        
        // Semi-transparent overlay like in gameOver
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        
        
        // Main title with arcade-style glow effect
        g.setColor(new Color(0, 200, 0));
        g.setFont(startScreenFont);
        String titleText = "SNAKE GAME";
        FontMetrics titleMetrics = getFontMetrics(g.getFont());
        
        // Create a text glow/shadow effect like in gameOver
        for (int offset = 5; offset > 0; offset--) {
            g.setColor(new Color(0, 100, 0, 80));
            g.drawString(titleText, 
                (SCREEN_WIDTH - titleMetrics.stringWidth(titleText)) / 2 + offset, 
                SCREEN_HEIGHT / 2 - 30 + offset);
            g.drawString(titleText, 
                (SCREEN_WIDTH - titleMetrics.stringWidth(titleText)) / 2 - offset, 
                SCREEN_HEIGHT / 2 - 30 - offset);
        }
        
        // Main title text
        g.setColor(new Color(0, 255, 0));
        g.drawString(titleText, (SCREEN_WIDTH - titleMetrics.stringWidth(titleText)) / 2, SCREEN_HEIGHT / 2 - 30);
        
        // Instructions with blinking effect like in gameOver
        long time = System.currentTimeMillis() % 1000;
        if (time > 500) { // Blink every half second
            g.setColor(new Color(255, 255, 0)); // Yellow color
            g.setFont(startInstructionsFont);
            String instructions = "PRESS SPACE TO START";
            FontMetrics instructMetrics = getFontMetrics(g.getFont());
            g.drawString(instructions, 
                (SCREEN_WIDTH - instructMetrics.stringWidth(instructions)) / 2, 
                SCREEN_HEIGHT * 2/3);
        }
        
        // Draw game controls help text
        g.setColor(new Color(200, 200, 200));
        g.setFont(instructionsFont);
        String controlsText = "USE ARROW KEYS TO MOVE";
        FontMetrics controlsMetrics = getFontMetrics(g.getFont());
        g.drawString(controlsText, 
            (SCREEN_WIDTH - controlsMetrics.stringWidth(controlsText)) / 2, 
            SCREEN_HEIGHT * 3/4);
            
        // Draw version or credits
        g.setColor(new Color(150, 150, 150));
        g.setFont(new Font("Courier New", Font.PLAIN, 12));
        String versionText = "V1.0";
        g.drawString(versionText, SCREEN_WIDTH - 50, SCREEN_HEIGHT - 20);
    }
    
    
    private void drawAnimatedBackground(Graphics2D g) {
        // Draw some snake-like patterns in background for visual interest
        long time = System.currentTimeMillis();
        int segmentLength = 20;
        
        for (int s = 0; s < 3; s++) { // Draw 3 snakes
            double speed = 0.001 + (s * 0.0005);
            double amplitude = 50 + (s * 20);
            double wavelength = 0.02 - (s * 0.003);
            
            // Each snake has different color
            Color snakeColor = new Color(0, 80 + s * 30, 0, 50);
            g.setColor(snakeColor);
            
            // Starting position for this snake
            int startX = 50 + s * 150;
            int startY = 100 + s * 100;
            
            for (int i = 0; i < segmentLength; i++) {
                // Calculate wave-like movement
                double factor = wavelength * i + speed * time;
                int x = startX + i * 20;
                int y = startY + (int)(amplitude * Math.sin(factor));
                
                // Draw snake segment
                g.fillRoundRect(x, y, 15, 15, 5, 5);
            }
        }
    }

    private void drawSnakeEyes(Graphics2D g, int i) {
        int eyeSize = UNIT_SIZE / 5;
        g.setColor(Color.WHITE);
        switch (direction) {
            case 'R':
                g.fillOval(x[i] + UNIT_SIZE - eyeSize * 2, y[i] + eyeSize, eyeSize, eyeSize);
                g.fillOval(x[i] + UNIT_SIZE - eyeSize * 2, y[i] + UNIT_SIZE - eyeSize * 2, eyeSize, eyeSize);
                break;
            case 'L':
                g.fillOval(x[i] + eyeSize, y[i] + eyeSize, eyeSize, eyeSize);
                g.fillOval(x[i] + eyeSize, y[i] + UNIT_SIZE - eyeSize * 2, eyeSize, eyeSize);
                break;
            case 'U':
                g.fillOval(x[i] + eyeSize, y[i] + eyeSize, eyeSize, eyeSize);
                g.fillOval(x[i] + UNIT_SIZE - eyeSize * 2, y[i] + eyeSize, eyeSize, eyeSize);
                break;
            case 'D':
                g.fillOval(x[i] + eyeSize, y[i] + UNIT_SIZE - eyeSize * 2, eyeSize, eyeSize);
                g.fillOval(x[i] + UNIT_SIZE - eyeSize * 2, y[i] + UNIT_SIZE - eyeSize * 2, eyeSize, eyeSize);
                break;
        }
        
        // Add pupils to eyes for more character
        g.setColor(Color.BLACK);
        int pupilSize = eyeSize / 2;
        switch (direction) {
            case 'R':
                g.fillOval(x[i] + UNIT_SIZE - eyeSize * 2 + pupilSize/2, y[i] + eyeSize + pupilSize/2, pupilSize, pupilSize);
                g.fillOval(x[i] + UNIT_SIZE - eyeSize * 2 + pupilSize/2, y[i] + UNIT_SIZE - eyeSize * 2 + pupilSize/2, pupilSize, pupilSize);
                break;
            case 'L':
                g.fillOval(x[i] + eyeSize + pupilSize/2, y[i] + eyeSize + pupilSize/2, pupilSize, pupilSize);
                g.fillOval(x[i] + eyeSize + pupilSize/2, y[i] + UNIT_SIZE - eyeSize * 2 + pupilSize/2, pupilSize, pupilSize);
                break;
            case 'U':
                g.fillOval(x[i] + eyeSize + pupilSize/2, y[i] + eyeSize + pupilSize/2, pupilSize, pupilSize);
                g.fillOval(x[i] + UNIT_SIZE - eyeSize * 2 + pupilSize/2, y[i] + eyeSize + pupilSize/2, pupilSize, pupilSize);
                break;
            case 'D':
                g.fillOval(x[i] + eyeSize + pupilSize/2, y[i] + UNIT_SIZE - eyeSize * 2 + pupilSize/2, pupilSize, pupilSize);
                g.fillOval(x[i] + UNIT_SIZE - eyeSize * 2 + pupilSize/2, y[i] + UNIT_SIZE - eyeSize * 2 + pupilSize/2, pupilSize, pupilSize);
                break;
        }
    }

    public void newApple() {
        appleX = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
        appleY = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;

        // Ensure the apple doesn't spawn on the snake
        for (int i = 0; i < bodyParts; i++) {
            if ((appleX == x[i]) && (appleY == y[i])) {
                newApple(); // Recursively try a new position
                break;
            }
        }
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U':
                y[0] = y[0] - UNIT_SIZE;
                break;
            case 'D':
                y[0] = y[0] + UNIT_SIZE;
                break;
            case 'L':
                x[0] = x[0] - UNIT_SIZE;
                break;
            case 'R':
                x[0] = x[0] + UNIT_SIZE;
                break;
        }
    }

    public void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
        }
    }

    public void checkCollisions() {
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
            }
        }

        // Check border collisions (wrap-around style)
        if (x[0] < 0) {
            x[0] = SCREEN_WIDTH - UNIT_SIZE;
        }
        if (x[0] >= SCREEN_WIDTH) {
            x[0] = 0;
        }
        if (y[0] < 0) {
            y[0] = SCREEN_HEIGHT - UNIT_SIZE;
        }
        if (y[0] >= SCREEN_HEIGHT) {
            y[0] = 0;
        }

        if (!running && gameStarted) {
            timer.stop();
        }
    }

    public void gameOver(Graphics g) {
        // Semi-transparent overlay for Game Over
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // Game Over Title with arcade-style appearance
        g.setColor(new Color(255, 0, 0));
        g.setFont(gameOverFont);
        FontMetrics metrics = getFontMetrics(g.getFont());
        String gameOverText = "GAME OVER";
        
        // Create a text glow/shadow effect typical of arcade games
        for (int offset = 5; offset > 0; offset--) {
            g.setColor(new Color(150, 0, 0, 80));
            g.drawString(gameOverText, 
                (SCREEN_WIDTH - metrics.stringWidth(gameOverText)) / 2 + offset, 
                SCREEN_HEIGHT / 2 - 30 + offset);
            g.drawString(gameOverText, 
                (SCREEN_WIDTH - metrics.stringWidth(gameOverText)) / 2 - offset, 
                SCREEN_HEIGHT / 2 - 30 - offset);
        }
        
        // Main game over text
        g.setColor(new Color(255, 0, 0));
        g.drawString(gameOverText, (SCREEN_WIDTH - metrics.stringWidth(gameOverText)) / 2, SCREEN_HEIGHT / 2 - 30);

        // Score display
        g.setColor(new Color(255, 255, 0));
        g.setFont(titleFont);
        FontMetrics scoreMetrics = getFontMetrics(g.getFont());
        String scoreText = "FINAL SCORE: " + applesEaten;
        g.drawString(scoreText, (SCREEN_WIDTH - scoreMetrics.stringWidth(scoreText)) / 2, SCREEN_HEIGHT / 2 + 40);

        // Restart Instructions with a blinking effect
        long time = System.currentTimeMillis() % 1000;
        if (time > 500) { // Blink every half second
            g.setColor(new Color(50, 255, 50));
            g.setFont(instructionsFont);
            String instructions = "PRESS SPACE TO RESTART";
            FontMetrics instructMetrics = getFontMetrics(g.getFont());
            g.drawString(instructions, (SCREEN_WIDTH - instructMetrics.stringWidth(instructions)) / 2, SCREEN_HEIGHT / 2 + 100);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (direction != 'R') direction = 'L';
                    break;
                case KeyEvent.VK_RIGHT:
                    if (direction != 'L') direction = 'R';
                    break;
                case KeyEvent.VK_DOWN:
                    if (direction != 'U') direction = 'D';
                    break;
                case KeyEvent.VK_UP:
                    if (direction != 'D') direction = 'U';
                    break;
                case KeyEvent.VK_SPACE:
                    if (!gameStarted) {
                        startGame();
                    } else if (!running) {
                        startGame();
                    }
                    break;
            }
        }
    }
}