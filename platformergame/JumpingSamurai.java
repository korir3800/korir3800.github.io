
import java.awt.*; // abstract window toolkit, used for drawing shapes, colors and fonts
import java.awt.event.*; // event-handling interfaces like keyListener
import java.awt.image.BufferedImage; // gives access to buffered image which you can load and manipulate
import java.io.IOException; // handles input/output exceptions, like when loading images
import java.net.URL; // used for loading resources like image paths (sprite images)
import java.util.ArrayList; // lets you use the arraylist class
import javax.imageio.ImageIO; // reading image files into bufferedimage objects
import javax.swing.*; // inclludes swing gui components like JPanel, JFrame, JButton, etc.

public class JumpingSamurai extends JPanel implements ActionListener, KeyListener {

    // Timer used for the main game loop (ticks every millisecond)
    Timer timer;

    // Current game state: "menu", "playing"
    String gameState = "menu";

    // Flag to indicate if the player has reached the final platform
    boolean playerWon = false;

    // GUI Buttons shown on menu screen
    JButton startButton, instructionsButton, creditsButton;

    // Player position and size
    int playerX = 100, playerY = 500;
    int playerWidth = 100, playerHeight = 80;

    // Vertical speed, gravity force, and jump strength
    int velocityY = 0;
    int gravity = 1;
    int jumpStrength = -15;

    // Whether the player is standing on a platform
    boolean onGround = false;

    // Movement keys status
    boolean left = false, right = false, up = false;

    // Tracks which direction player is facing for sprite flipping
    boolean facingLeft = false;

    // List of platform rectangles for collision detection
    ArrayList<Rectangle> platforms = new ArrayList<>();

    // Sprite images for different terrain tiles
    BufferedImage grassSprite, dirtSprite, stoneSprite;

    // Idle animation sheet and current player sprite to draw
    BufferedImage idleSheet, playerSprite;

    // Running animation sheet and its frames
    BufferedImage runSheet;
    BufferedImage[] runFrames;

    // Running animation state trackers
    int runFrameIndex = 0;
    int runAnimationCounter = 0;
    int runAnimationDelay = 5; // Smaller = faster animation speed

    // Image used as the background for the menu
    BufferedImage menuBackground;

    // Game area constraints
    final int WORLD_BOTTOM = 1000;            // Y-position where the player resets if they fall
    final int HORIZONTAL_AREA_WIDTH = 800;    // Width of the level

    // Timer tracking for gameplay
    long startTime;        // Start timestamp
    int elapsedTime = 0;   // Time since game started (ms)
    double finalTime = 0;  // Time when player wins

    // Constructor: sets up game window, menu UI, loads assets, and builds level
    public JumpingSamurai() {
        setPreferredSize(new Dimension(800, 600)); 
        setFocusable(true);
        addKeyListener(this);
        setLayout(null); 

        // Create buttons
        startButton = new JButton("Start Game");
        instructionsButton = new JButton("Instructions");
        creditsButton = new JButton("Credits");

        // Position buttons on screen
        startButton.setBounds(60, 200, 200, 40);
        instructionsButton.setBounds(60, 250, 200, 40);
        creditsButton.setBounds(60, 300, 200, 40);

        // Add buttons to panel
        add(startButton);
        add(instructionsButton);
        add(creditsButton);

        // Style the buttons (modern font and hover effects)
        Font modernFont = new Font("Segoe UI", Font.BOLD, 18);
        Color baseColor = new Color(255, 255, 255, 200); 
        Color hoverColor = new Color(255, 255, 255, 255); 
        JButton[] buttons = { startButton, instructionsButton, creditsButton };

        for (JButton btn : buttons) {
            btn.setFont(modernFont);
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(false);
            btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            btn.setForeground(baseColor);

            // Add hover effect to each button
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    btn.setForeground(hoverColor);
                    btn.setBorder(BorderFactory.createLineBorder(hoverColor, 2));
                }

                public void mouseExited(MouseEvent e) {
                    btn.setForeground(baseColor);
                    btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
                }
            });
        }

        // "Start Game" button starts the game loop and hides menu
        startButton.addActionListener(e -> {
            gameState = "playing";
            startTime = System.currentTimeMillis();
            elapsedTime = 0;
            playerWon = false;
            finalTime = 0;
            startButton.setVisible(false);
            instructionsButton.setVisible(false);
            creditsButton.setVisible(false);
            timer.start();
            requestFocusInWindow(); // Refocus for key input
        });

        // Instructions dialog popup
        instructionsButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "Use your arrow keys and jump your way to the top!" +
                "\n\nSee how fast you can reach the last platform!\n\nPress ESC to restart!");
        });

        // Credits dialog popup
        creditsButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "Created by Ahmed, Chase and Naftali.\nAll sprites used are open-sourced and free to use!");
        });

        // Load terrain tiles from a tileset image
        try {
            BufferedImage fullTileset = ImageIO.read(new URL("https://www.dropbox.com/scl/fi/87jvtlt57oeq99x1yor37/world_tileset.png?rlkey=yymj0x7l1lm5zv44eux89ess6&st=h3od30ub&raw=1"));
            int tileWidth = 16, tileHeight = 16;
            grassSprite = fullTileset.getSubimage(0, 0, tileWidth, tileHeight);
            dirtSprite = fullTileset.getSubimage(16, 0, tileWidth, tileHeight);
            stoneSprite = fullTileset.getSubimage(128, 0, tileWidth, tileHeight);
        } catch (IOException e) { e.printStackTrace(); }

        // Load idle animation and extract first frame
        try {
            idleSheet = ImageIO.read(new URL("https://www.dropbox.com/scl/fi/3dkk9yqah8n05axcdeuvv/IDLE.png?rlkey=3xa1xfqj41f69n4fek9dsxv8e&st=2p7kkomt&raw=1"));
            int frameWidth = idleSheet.getWidth() / 8;
            playerSprite = idleSheet.getSubimage(0, 0, frameWidth, idleSheet.getHeight());
        } catch (IOException e) { e.printStackTrace(); }

        // Load and slice running animation frames
        try {
            runSheet = ImageIO.read(new URL("https://www.dropbox.com/scl/fi/xcfs1yi3682v3e9dn6kdx/RUN.png?rlkey=d0zy8fph01o5bo2sexz9duwvf&st=ij8p9qsl&raw=1"));
            int runFramesCount = 16;
            int frameWidth = runSheet.getWidth() / runFramesCount;
            runFrames = new BufferedImage[runFramesCount];
            for (int i = 0; i < runFramesCount; i++) {
                runFrames[i] = runSheet.getSubimage(i * frameWidth, 0, frameWidth, runSheet.getHeight());
            }
        } catch (IOException e) { e.printStackTrace(); }

        // Load menu background image
        try {
            menuBackground = ImageIO.read(new URL("https://www.dropbox.com/scl/fi/wg7hyt8wp1dny4rili35a/samurai.png?rlkey=sgos0ujycr4yadtx7r183s3lx&st=bbr87lrg&raw=1"));
        } catch (IOException e) { e.printStackTrace(); }

        // Define all platform positions
        platforms.add(new Rectangle(0, 550, 800, 64)); // Ground
        platforms.add(new Rectangle(150, 450, 100, 32));
        platforms.add(new Rectangle(300, 370, 100, 32));
        platforms.add(new Rectangle(450, 300, 100, 32));
        platforms.add(new Rectangle(600, 220, 100, 32));
        platforms.add(new Rectangle(400, 140, 100, 32));
        platforms.add(new Rectangle(200, 60, 100, 32));
        platforms.add(new Rectangle(150, -20, 100, 32));
        platforms.add(new Rectangle(350, -100, 100, 32));
        platforms.add(new Rectangle(420, -180, 100, 32));
        platforms.add(new Rectangle(250, -260, 100, 32));
        platforms.add(new Rectangle(450, -340, 100, 32));
        platforms.add(new Rectangle(650, -420, 100, 32)); // Win platform

        // Set timer to tick every millisecond
        timer = new Timer(1, this);
    }

    // Main game loop: runs every timer tick
    public void actionPerformed(ActionEvent e) {
        // Skip logic if not playing or already won
        if (!gameState.equals("playing") || playerWon) return;

        // Horizontal movement
        if (left) playerX -= 5;
        if (right) playerX += 5;
        playerX = Math.max(0, Math.min(playerX, HORIZONTAL_AREA_WIDTH - playerWidth));

        // Apply gravity and move player
        velocityY += gravity;
        playerY += velocityY;
        onGround = false;

        // Define collision box (smaller than sprite for better accuracy)
        Rectangle playerBounds = new Rectangle(playerX + 30, playerY + 20, 40, 40);

        // Check for collisions with platforms
        for (Rectangle platform : platforms) {
            if (playerBounds.intersects(platform)) {
                Rectangle intersection = playerBounds.intersection(platform);
                if (intersection.height < intersection.width && velocityY >= 0) {
                    playerY -= intersection.height;
                    velocityY = 0;
                    onGround = true;
                }
            }
        }

        // Jumping logic
        if (up && onGround) {
            velocityY = jumpStrength;
            onGround = false;
        }

        // If player falls off map, reset to start
        if (playerY > WORLD_BOTTOM) {
            playerX = 100;
            playerY = 500;
            velocityY = 0;
        }

        // Update animation frame if moving
        if (left || right) {
            runAnimationCounter++;
            if (runAnimationCounter >= runAnimationDelay) {
                runAnimationCounter = 0;
                runFrameIndex = (runFrameIndex + 1) % runFrames.length;
            }
            playerSprite = runFrames[runFrameIndex];
        } else {
            playerSprite = idleSheet.getSubimage(0, 0, idleSheet.getWidth() / 8, idleSheet.getHeight());
        }

        // Track elapsed time since game start
        elapsedTime = (int) (System.currentTimeMillis() - startTime);

        // Win condition: touch final platform
        Rectangle lastPlatform = platforms.get(platforms.size() - 1);
        if (playerBounds.intersects(lastPlatform)) {
            finalTime = elapsedTime / 1000.0;
            playerWon = true;
            timer.stop();
        }

        repaint(); // Re-render the screen
    }

    // Draw everything to the screen
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw menu background if in menu state
        if (gameState.equals("menu")) {
            if (menuBackground != null) {
                g.drawImage(menuBackground, 0, 0, getWidth(), getHeight(), null);
            } else {
                g.setColor(new Color(50, 50, 50));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            return;
        }

        Graphics2D g2d = (Graphics2D) g;

        // Camera positioning
        int cameraX = 0;
        int cameraY = playerY - getHeight() / 2 + playerHeight / 2;
        g2d.translate(-cameraX, -cameraY);

        // Sky gradient
        GradientPaint skyGradient = new GradientPaint(cameraX, cameraY, new Color(70, 130, 180),
                cameraX, cameraY + getHeight(), new Color(135, 206, 235));
        g2d.setPaint(skyGradient);
        g2d.fillRect(cameraX, cameraY, getWidth(), getHeight());

        // Draw player sprite, flipped if facing left
        if (playerSprite != null) {
            if (facingLeft) {
                g2d.drawImage(playerSprite, playerX + playerWidth, playerY, -playerWidth, playerHeight, null);
            } else {
                g2d.drawImage(playerSprite, playerX, playerY, playerWidth, playerHeight, null);
            }
        }

        // Draw platforms
        if (grassSprite != null && dirtSprite != null && stoneSprite != null) {
            int tileWidth = grassSprite.getWidth();
            int tileHeight = grassSprite.getHeight();
            for (int i = 0; i < platforms.size(); i++) {
                Rectangle r = platforms.get(i);
                if (i == 0) {
                    // Draw ground and fill underneath with dirt
                    int groundY = r.y;
                    int visibleBottom = cameraY + getHeight();
                    g2d.setColor(new Color(101, 51, 0));
                    g2d.fillRect(0, groundY, HORIZONTAL_AREA_WIDTH, visibleBottom - groundY);
                    for (int x = 0; x <= HORIZONTAL_AREA_WIDTH; x += tileWidth) {
                        g2d.drawImage(grassSprite, x, groundY, null);
                        for (int y = groundY + tileHeight; y < visibleBottom; y += tileHeight) {
                            g2d.drawImage(dirtSprite, x, y, null);
                        }
                    }
                } else {
                    // Draw floating stone platform
                    int yOffset = r.y + (r.height - tileHeight) / 2;
                    for (int x = r.x; x < r.x + r.width; x += tileWidth) {
                        g2d.drawImage(stoneSprite, x, yOffset, null);
                    }
                }
            }
        }

        // Display elapsed time
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString(String.format("Time: %.3fs", elapsedTime / 1000.0), cameraX + 20, cameraY + 40);

        // Display win message
        if (playerWon) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 36));
            g2d.drawString(String.format("You won in %.3f seconds!", finalTime), cameraX + getWidth() / 2 - 200, cameraY + getHeight() / 2);
            g2d.drawString("Press ESC to restart!", cameraX + getWidth() / 2 - 200, cameraY + getHeight() / 2 + 50);
            
        }
    }

    // Handle key press
    public void keyPressed(KeyEvent e) {
        if (!gameState.equals("playing")) return;
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) {
            left = true;
            facingLeft = true;
        }
        if (key == KeyEvent.VK_RIGHT) {
            right = true;
            facingLeft = false;
        }
        if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_UP) {
            up = true;
        }
        // If ESC is pressed, restart the game
        if (key == KeyEvent.VK_ESCAPE) {
            restartGame();
        }
    }

    // Handle key release
    public void keyReleased(KeyEvent e) {
        if (!gameState.equals("playing")) return;
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) left = false;
        if (key == KeyEvent.VK_RIGHT) right = false;
        if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_UP) up = false;
    }

    public void keyTyped(KeyEvent e) {} // Not used

    // Method to reset the game when ESC is pressed
    private void restartGame() {
        // Reset player position and velocity
        playerX = 100;
        playerY = 500;
        velocityY = 0;
        
        // Reset movement flags
        left = false;
        right = false;
        up = false;
        facingLeft = false;
        
        // Reset time tracking variables
        startTime = System.currentTimeMillis();
        elapsedTime = 0;
        finalTime = 0;
        
        // Reset game state and win flag
        gameState = "playing";
        playerWon = false;
        
        // Restart the timer if not running
        if (!timer.isRunning()) {
            timer.start();
        }
        
        // Refocus the game panel for key events
        requestFocusInWindow();
    }

    // Launch the game
    public static void main(String[] args) {
        JFrame frame = new JFrame("Jumping Samurai");
        JumpingSamurai game = new JumpingSamurai();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
