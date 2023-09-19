import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class Football extends JFrame implements ActionListener, Runnable, KeyListener {

    /** long representing serialVersionUID */
    private static final long serialVersionUID = 1L;

    /** screen width */
    private static final int WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;

    /** screen height */
    private static final int HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;

    /** program frames per second */
    private static final int FPS = 144;

    /** time between frames in nanoseconds */
    final long OPTIMAL_TIME = 1000000000 / FPS;

    /** image loading for background */
    private Image background;

    /** player images */
    private Image playerRight;
    private Image playerLeft;

    /** football items and images */
    private List<FootballItem> footballItems = new ArrayList<>();
    private Image football;

    /** UNC items and images */
    private List<UNCItem> uncItems = new ArrayList<>();
    private Image UNC;

    /** score */
    private int score = 0;

    /** start time */
    private long startTime = System.currentTimeMillis();
    private long remainingTime = 60000; // 60 seconds in milliseconds
    private boolean isGameOver = false;

    /** player position */
    private int playerX;
    private int playerY;

    /** player speed */
    private double playerSpeed;
    private boolean isMovingRight;
    private boolean isMovingLeft;

    /** player boost */
    int playerBoost = 0;

    /** movement controller */
    private double dx = 0;
    private double dy = 0;

    /** frame count */
    private long frameCount = 0;
    private long lastTime = System.currentTimeMillis();

    /** controls the number of football items */
    public static final int MAX_FOOTBALLS = 5;

    /** controls the number of UNC items */
    public static final int MAX_UNC = 7;

    /** controls the boost increase */
    public static final double BOOST_INC = 2.5;

    /** controls the boost decrease */
    public static final double BOOST_DEC = 1;

    /** controls the boost shift */
    public static final int BOOST_SHIFT = 5;

    /** controls the maximum speed */
    public static final int MAX_SPEED = 6;

    /** controls the minimum speed */
    public static final int MIN_SPEED = 5;

    /** controls the score gain */
    public static final int SCORE_GAIN = 10;

    /** controls the score loss */
    public static final int SCORE_LOOSE = 10;

    /** harvard red */
    Color harvardRed = new Color(165, 28, 48);

    /** close button */
    private JButton closeButton;

    /**
     * Constructor
     * 
     * loads images, sets posiion and speed, creates objects and starts the game.
     * scales frame as well
     */
    public Football() {
        super("Football");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Load resources
        background = new ImageIcon("resources/sprites/old/carterFinley16x9.png").getImage();
        playerRight = new ImageIcon("resources/sprites/old/rsz_playerRight.png").getImage();
        playerLeft = new ImageIcon("resources/sprites/old/rsz_playerLeft.png").getImage();
        football = new ImageIcon("resources/sprites/old/football.png").getImage();
        UNC = new ImageIcon("resources/sprites/old/UNC.png").getImage();

        // Set initial player position and speed
        playerX = WIDTH / 2;
        playerY = HEIGHT / 2;
        playerSpeed = 4.5;

        // Add 5 football items
        for (int i = 0; i < MAX_FOOTBALLS; i++) {
            footballItems.add(new FootballItem(getWidth(), getHeight()));
        }

        // Add 5 UNC items
        for (int i = 0; i < MAX_UNC; i++) {
            uncItems.add(new UNCItem(getWidth(), getHeight()));
        }

        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();

        // Create a restart/close button
        closeButton = new JButton("Close (Escape)");
        closeButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Start the game loop
        new Thread(this).start();
    }

    /**
     * Paints the game and the game over screen
     */
    public void paint(Graphics g) {
        // Create offscreen buffer
        Image buffer = createImage(getWidth(), getHeight());
        Graphics bg = buffer.getGraphics();

        // Draw the background
        bg.drawImage(background, 0, 0, getWidth(), getHeight(), this);

        // change background color if the game is over
        if (isGameOver) {
            // Draw game over message
            bg.setColor(harvardRed);
            bg.fillRect(0, 0, getWidth(), getHeight());
            bg.setFont(new Font("Arial", Font.BOLD, 70));
            bg.setColor(Color.WHITE);
            String gameOverMessage = "GAME OVER";
            int gameOverMessageWidth = bg.getFontMetrics().stringWidth(gameOverMessage);
            bg.drawString(gameOverMessage, (getWidth() - gameOverMessageWidth) / 2, getHeight() / 2 - 50);

            // Draw score
            bg.setColor(Color.WHITE);
            bg.setFont(new Font("Arial", Font.BOLD, 50));
            String scoreMessage = "Your score: " + score;
            int scoreMessageWidth = bg.getFontMetrics().stringWidth(scoreMessage);
            bg.drawString(scoreMessage, (getWidth() - scoreMessageWidth) / 2, getHeight() / 2 + 50);

            // Draw restart and close buttons
            int buttonWidth = 400;
            int buttonHeight = 50;
            int closeButtonX = (getWidth() - buttonWidth) / 2;
            int buttonY = getHeight() / 2 + 150;
            bg.setColor(Color.GREEN);
            bg.fillRect(closeButtonX, buttonY, buttonWidth, buttonHeight);
            bg.setColor(Color.BLACK);
            bg.setFont(new Font("Arial", Font.BOLD, 30));
            String closeButtonMessage = "Close (Escape)";
            int closeButtonMessageWidth = bg.getFontMetrics().stringWidth(closeButtonMessage);
            bg.drawString(closeButtonMessage, closeButtonX + (buttonWidth - closeButtonMessageWidth) / 2,
                    buttonY + buttonHeight / 2 + 10);
        } else {

            // Draw the player
            if (isMovingRight) {
                bg.drawImage(playerRight, playerX, playerY, playerRight.getWidth(this), playerRight.getHeight(this),
                        this);
            } else if (isMovingLeft) {
                bg.drawImage(playerLeft, playerX, playerY, playerLeft.getWidth(this), playerLeft.getHeight(this), this);
            } else {
                bg.drawImage(playerRight, playerX, playerY, playerRight.getWidth(this) - 5,
                        playerRight.getHeight(this) - 5,
                        this);
            }

            // Draw the football items
            for (FootballItem footballItem : footballItems) {
                if (!footballItem.isRemoved()) {
                    bg.drawImage(football, footballItem.getX(), footballItem.getY(), footballItem.getWidth(),
                            footballItem.getHeight(), this);
                }
            }

            // Draw the UNC items
            for (UNCItem uncItem : uncItems) {
                if (!uncItem.isRemoved()) {
                    bg.drawImage(UNC, uncItem.getX(), uncItem.getY(), uncItem.getWidth(),
                            uncItem.getHeight(), this);
                }
            }

            // Draw the score and timer
            bg.setColor(Color.WHITE);
            bg.setFont(new Font("Arial", Font.BOLD, 35));
            bg.drawString("Score: " + score, getWidth() - 180, 75);
            bg.drawString("Time: " + remainingTime / 1000 + "s", getWidth() - 180, 125);

            // Draw the buffer to the screen
            g.drawImage(buffer, 0, 0, this);

        }

        // Draw the buffer to the screen
        g.drawImage(buffer, 0, 0, this);
    }

    /**
     * outputs the current FPS to terminal
     */
    public void outputFPS() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastTime;
        if (elapsedTime >= 1000) {
            System.out.println("FPS: " + (frameCount / (elapsedTime / 1000.0)));
            // System.out.println(score);
            frameCount = 0;
            lastTime = currentTime;
        }
    }

    /**
     * calls to move player and check for collisions, updates frame time as well
     */
    public void run() {
        long lastFrameTime = System.nanoTime();
        while (true) {
            // Calculate elapsed time since last frame
            long currentTime = System.nanoTime();
            double elapsedTime = (currentTime - lastFrameTime) / 1e9;
            lastFrameTime = currentTime;

            double delta = elapsedTime / ((double) OPTIMAL_TIME);

            // Move the player
            movePlayer(delta);
            // playerX += (int) Math.round(dx * elapsedTime);
            // playerY += (int) Math.round(dy * elapsedTime);

            // Update the remaining time
            long now = System.currentTimeMillis();
            remainingTime = Math.max(0, startTime + 60000 - now);
            if (remainingTime == 0) {
                isGameOver = true;
            }

            // Repaint the screen
            repaint();

            // Output FPS to the console
            outputFPS();

            // Sleep for the desired frame rate
            try {
                Thread.sleep(1000 / FPS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Increment the frame count
            frameCount++;
        }
    }

    /**
     * Moves the player and checks for collisions
     * 
     * @param delta time since last frame
     */
    private void movePlayer(double delta) {
        // Move the player
        playerX += dx;
        playerY += dy;

        // Check if the player is going out of screen bounds
        if (playerX < 0) {
            playerX = 0;
        } else if (playerX > getWidth() - playerRight.getWidth(null)) {
            playerX = getWidth() - playerRight.getWidth(null);
        }

        if (playerY < 0) {
            playerY = 0;
        } else if (playerY > getHeight() - playerRight.getHeight(null)) {
            playerY = getHeight() - playerRight.getHeight(null);
        }

        // Create a rectangle that represents the player's position and size
        Rectangle playerRect = new Rectangle(playerX, playerY, playerRight.getWidth(this) - 5,
                playerRight.getHeight(this) - 5);

        // Check for collision with football items
        for (int i = 0; i < footballItems.size(); i++) {
            FootballItem footballItem = footballItems.get(i);
            if (!footballItem.isRemoved() && playerRect.intersects(footballItem.getBounds())) {
                // Player collided with football item
                footballItem.setRemoved(true);
                score += SCORE_GAIN;
                playerBoost++;
                footballItems.add(new FootballItem(getWidth(), getHeight()));
            }
        }

        // Check for collision with UNC items
        for (int i = 0; i < uncItems.size(); i++) {
            UNCItem uncItem = uncItems.get(i);
            if (!uncItem.isRemoved() && playerRect.intersects(uncItem.getBounds())) {
                // Player collided with football item
                uncItem.setRemoved(true);
                score -= SCORE_LOOSE;
                if (!(playerSpeed <= MIN_SPEED)) {
                    playerSpeed -= BOOST_DEC;
                }

                uncItems.add(new UNCItem(getWidth(), getHeight()));
            }
        }

        if (playerBoost >= BOOST_SHIFT && score != 0 && playerSpeed < MAX_SPEED) {
            playerSpeed += BOOST_INC;
            playerBoost = 0;
        }
    }

    /**
     * main method
     */
    public static void main(String[] args) {
        Football game = new Football();
        game.setVisible(true);
    }

    /**
     * Action performed
     * 
     * @param e event
     * @throws UnsupportedOperationException unimplemented
     */
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Unimplemented method 'keyTyped'");
    }

    /**
     * Key typed
     * 
     * @param e event
     * @throws UnsupportedOperationException unimplemented
     */
    @Override
    public void keyTyped(KeyEvent e) {
        throw new UnsupportedOperationException("Unimplemented method 'keyTyped'");
    }

    /**
     * Key pressed event. assigns direction based on input controls
     * 
     * @param e event
     * 
     */
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                dy = -playerSpeed;
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                dy = playerSpeed;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                dx = -playerSpeed;
                isMovingRight = false;
                isMovingLeft = true;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                dx = playerSpeed;
                isMovingLeft = false;
                isMovingRight = true;
                break;
            case KeyEvent.VK_F11:
                // toggleFullscreen();
                break;
            case KeyEvent.VK_ESCAPE:
                // toggleMinimize();
                System.out.println("exit");
                System.exit(0);
                break;
        }
    }

    /**
     * Key released event. resets direction based on input release
     * 
     * @param e event
     */
    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                dy = 0;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                dx = 0;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                dx = 0;
                break;
        }
    }

}
