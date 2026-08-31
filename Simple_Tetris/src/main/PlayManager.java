package main;

import mino.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

public class PlayManager {
    // Grid settings
    public static final int COLS = 10;
    public static final int ROWS = 20;
    public final int leftX;
    public final int rightX;
    public final int topY;
    public final int bottomY;
    public final int playWidth;
    public final int playHeight;

    // 2D Grid holding placed blocks
    public Block[][] grid = new Block[ROWS][COLS];

    // Minos
    public Mino currentMino;
    public final int minoStartX;
    public final int minoStartY;
    public List<Mino> nextQueue = new ArrayList<>();
    public Mino holdMino = null;
    public boolean canHold = true;

    // 7-Bag Randomizer
    private final List<Integer> bag = new ArrayList<>();

    // Gravity / Timing
    public int autoDropCounter = 0;
    public int dropInterval = 60;

    // Line clear animation
    public boolean effectActive = false;
    public int effectCounter = 0;
    public List<Integer> effectYList = new ArrayList<>();
    private List<Integer> pendingClearRows = new ArrayList<>();

    // Score & Stats
    public int score = 0;
    public int highScore = 0;
    public int level = 1;
    public int lines = 0;
    public boolean gameOver = false;

    // High Score persistence
    private final Preferences prefs = Preferences.userNodeForPackage(PlayManager.class);
    private static final String HIGH_SCORE_KEY = "tetris_high_score";

    public PlayManager() {
        playWidth = COLS * Block.size;
        playHeight = ROWS * Block.size;
        leftX = (GamePanel.width / 2) - (playWidth / 2);
        rightX = leftX + playWidth;
        topY = 60;
        bottomY = topY + playHeight;

        minoStartX = leftX + (4 * Block.size);
        minoStartY = topY + (1 * Block.size);

        highScore = prefs.getInt(HIGH_SCORE_KEY, 0);

        initGame();
    }

    public void initGame() {
        grid = new Block[ROWS][COLS];
        score = 0;
        level = 1;
        lines = 0;
        gameOver = false;
        dropInterval = 60;
        autoDropCounter = 0;
        holdMino = null;
        canHold = true;
        effectActive = false;
        effectCounter = 0;
        effectYList.clear();
        pendingClearRows.clear();
        bag.clear();
        nextQueue.clear();

        // Fill next queue with 1 mino
        nextQueue.add(drawNextMino());

        spawnNextMino();
    }

    private Mino drawNextMino() {
        if (bag.isEmpty()) {
            for (int i = 0; i < 7; i++) {
                bag.add(i);
            }
            Collections.shuffle(bag);
        }
        int minoId = bag.remove(0);
        return createMinoById(minoId);
    }

    private Mino createMinoById(int id) {
        return switch (id) {
            case 0 -> new Mino_Bar();
            case 1 -> new Mino_L1();
            case 2 -> new Mino_L2();
            case 3 -> new Mino_Square();
            case 4 -> new Mino_T();
            case 5 -> new Mino_Z1();
            case 6 -> new Mino_Z2();
            default -> new Mino_Square();
        };
    }

    public void spawnNextMino() {
        currentMino = nextQueue.remove(0);
        currentMino.setXY(minoStartX, minoStartY);
        nextQueue.add(drawNextMino());
        canHold = true;
        autoDropCounter = 0;

        // Check if spawn position collides -> Game Over
        for (int i = 0; i < 4; i++) {
            if (isCellOccupied(currentMino.b[i].x, currentMino.b[i].y)) {
                gameOver = true;
                SoundManager.getInstance().playGameOver();
                return;
            }
        }
    }

    public void holdPiece() {
        if (!canHold || gameOver || effectActive) return;

        SoundManager.getInstance().playHold();
        if (holdMino == null) {
            holdMino = createMinoById(getMinoId(currentMino));
            spawnNextMino();
        } else {
            Mino temp = holdMino;
            holdMino = createMinoById(getMinoId(currentMino));
            currentMino = temp;
            currentMino.setXY(minoStartX, minoStartY);
        }
        canHold = false;
    }

    private int getMinoId(Mino m) {
        if (m instanceof Mino_Bar) return 0;
        if (m instanceof Mino_L1) return 1;
        if (m instanceof Mino_L2) return 2;
        if (m instanceof Mino_Square) return 3;
        if (m instanceof Mino_T) return 4;
        if (m instanceof Mino_Z1) return 5;
        if (m instanceof Mino_Z2) return 6;
        return 3;
    }

    public boolean isInsidePlayArea(int pixelX, int pixelY) {
        return pixelX >= leftX && pixelX + Block.size <= rightX && pixelY + Block.size <= bottomY;
    }

    public boolean isCellOccupied(int pixelX, int pixelY) {
        int col = (pixelX - leftX) / Block.size;
        int row = (pixelY - topY) / Block.size;

        if (row < 0) return false;
        if (row >= ROWS || col < 0 || col >= COLS) return true;

        return grid[row][col] != null;
    }

    public void update(KeyHandler keyH) {
        if (gameOver) {
            if (keyH.consumeRestart()) {
                initGame();
            }
            return;
        }

        // Handle line clear flash animation
        if (effectActive) {
            effectCounter++;
            if (effectCounter >= 8) {
                effectActive = false;
                effectCounter = 0;
                applyLineClears();
                spawnNextMino();
            }
            return;
        }

        // Key actions
        if (keyH.consumeMute()) {
            SoundManager.getInstance().toggleMute();
        }

        if (keyH.consumeHold()) {
            holdPiece();
            return;
        }

        if (keyH.consumeRotateCW()) {
            currentMino.rotate(this, true);
        } else if (keyH.consumeRotateCCW()) {
            currentMino.rotate(this, false);
        }

        if (keyH.shouldMoveLeft()) {
            currentMino.moveLeft(this);
        }

        if (keyH.shouldMoveRight()) {
            currentMino.moveRight(this);
        }

        if (keyH.consumeHardDrop()) {
            currentMino.hardDrop(this);
        }

        if (!currentMino.active) {
            lockCurrentMino();
            return;
        }

        // Soft drop & gravity
        if (keyH.shouldSoftDrop()) {
            if (currentMino.moveDown(this)) {
                score += 1;
                autoDropCounter = 0;
            }
        } else {
            autoDropCounter++;
            if (autoDropCounter >= dropInterval) {
                currentMino.moveDown(this);
                autoDropCounter = 0;
            }
        }

        currentMino.update(this);

        if (!currentMino.active) {
            lockCurrentMino();
        }
    }

    private void lockCurrentMino() {
        boolean outOfBounds = false;
        for (int i = 0; i < 4; i++) {
            int col = (currentMino.b[i].x - leftX) / Block.size;
            int row = (currentMino.b[i].y - topY) / Block.size;
            if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
                grid[row][col] = currentMino.b[i];
            } else {
                outOfBounds = true;
            }
        }

        if (outOfBounds) {
            gameOver = true;
            SoundManager.getInstance().playGameOver();
            return;
        }

        checkLinesToClear();

        if (pendingClearRows.isEmpty()) {
            spawnNextMino();
        }
    }

    private void checkLinesToClear() {
        pendingClearRows.clear();
        effectYList.clear();

        for (int r = ROWS - 1; r >= 0; r--) {
            boolean full = true;
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] == null) {
                    full = false;
                    break;
                }
            }
            if (full) {
                pendingClearRows.add(r);
                effectYList.add(topY + r * Block.size);
            }
        }

        if (!pendingClearRows.isEmpty()) {
            effectActive = true;
            effectCounter = 0;
            int count = pendingClearRows.size();
            if (count >= 4) {
                SoundManager.getInstance().playTetrisClear();
            } else {
                SoundManager.getInstance().playLineClear();
            }
        }
    }

    private void applyLineClears() {
        int linesCleared = pendingClearRows.size();
        if (linesCleared == 0) return;

        // Rebuild grid: copy non-cleared rows to the bottom, shifting them down
        java.util.Set<Integer> clearedSet = new java.util.HashSet<>(pendingClearRows);
        Block[][] newGrid = new Block[ROWS][COLS];
        int destRow = ROWS - 1;
        for (int srcRow = ROWS - 1; srcRow >= 0; srcRow--) {
            if (!clearedSet.contains(srcRow)) {
                for (int c = 0; c < COLS; c++) {
                    newGrid[destRow][c] = grid[srcRow][c];
                    if (newGrid[destRow][c] != null) {
                        newGrid[destRow][c].y = topY + destRow * Block.size;
                    }
                }
                destRow--;
            }
        }
        grid = newGrid;

        // Update score & level
        int basePoints = switch (linesCleared) {
            case 1 -> 100;
            case 2 -> 300;
            case 3 -> 500;
            case 4 -> 800;
            default -> 100 * linesCleared;
        };
        score += basePoints * level;
        lines += linesCleared;

        level = (lines / 10) + 1;
        dropInterval = Math.max(2, 60 - ((level - 1) * 5));

        if (score > highScore) {
            highScore = score;
            prefs.putInt(HIGH_SCORE_KEY, highScore);
        }

        pendingClearRows.clear();
    }

    public void draw(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawPlayArea(g2d);
        drawHoldBox(g2d);
        drawNextBox(g2d);
        drawStatsBox(g2d);

        // Active mino & ghost piece
        if (currentMino != null && !gameOver && !effectActive) {
            currentMino.drawGhost(g2d, this);
            currentMino.draw(g2d);
        }

        // Line clear flash
        if (effectActive) {
            g2d.setColor(Color.WHITE);
            for (int effectY : effectYList) {
                g2d.fillRect(leftX, effectY, playWidth, Block.size);
            }
        }

        if (gameOver) {
            drawGameOverOverlay(g2d);
        }
    }

    private void drawPlayArea(Graphics2D g2d) {
        // Playfield background
        g2d.setColor(Color.BLACK);
        g2d.fillRect(leftX, topY, playWidth, playHeight);

        // Subtle grid lines
        g2d.setColor(new Color(40, 40, 40));
        g2d.setStroke(new BasicStroke(1f));
        for (int c = 1; c < COLS; c++) {
            int gx = leftX + c * Block.size;
            g2d.drawLine(gx, topY, gx, bottomY);
        }
        for (int r = 1; r < ROWS; r++) {
            int gy = topY + r * Block.size;
            g2d.drawLine(leftX, gy, rightX, gy);
        }

        // Playfield border
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRect(leftX - 1, topY - 1, playWidth + 2, playHeight + 2);

        // Placed blocks
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] != null) {
                    grid[r][c].draw(g2d);
                }
            }
        }
    }

    private void drawHoldBox(Graphics2D g2d) {
        int hx = leftX - 160;
        int hy = topY;
        int hw = 140;
        int hh = 120;

        // Border
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRect(hx, hy, hw, hh);

        // Label
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        g2d.drawString("HOLD", hx + 10, hy + 22);

        // Draw held piece
        if (holdMino != null) {
            holdMino.drawPreview(g2d, hx + hw / 2, hy + hh / 2 + 12);
        }
    }

    private void drawNextBox(Graphics2D g2d) {
        int nx = rightX + 20;
        int ny = topY;
        int nw = 140;
        int nh = 120;

        // Border
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRect(nx, ny, nw, nh);

        // Label
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        g2d.drawString("NEXT", nx + 10, ny + 22);

        // Draw 1 next piece
        if (!nextQueue.isEmpty()) {
            nextQueue.get(0).drawPreview(g2d, nx + nw / 2, ny + nh / 2 + 12);
        }
    }

    private void drawStatsBox(Graphics2D g2d) {
        int sx = rightX + 20;
        int sy = topY + 150;

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));

        g2d.drawString("Score: " + score, sx, sy);
        sy += 35;
        g2d.drawString("High:  " + highScore, sx, sy);
        sy += 35;
        g2d.drawString("Level: " + level, sx, sy);
        sy += 35;
        g2d.drawString("Lines: " + lines, sx, sy);

        // Sound indicator
        sy += 50;
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.setColor(new Color(180, 180, 180));
        boolean muted = SoundManager.getInstance().isMuted();
        g2d.drawString("Sound (M): " + (muted ? "OFF" : "ON"), sx, sy);
    }

    private void drawGameOverOverlay(Graphics2D g2d) {
        // Dark overlay on playfield
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(leftX, topY, playWidth, playHeight);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 34));
        FontMetrics fm = g2d.getFontMetrics();
        String title = "GAME OVER";
        g2d.drawString(title, leftX + (playWidth - fm.stringWidth(title)) / 2, topY + 260);

        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        fm = g2d.getFontMetrics();
        String sub = "Score: " + score;
        g2d.drawString(sub, leftX + (playWidth - fm.stringWidth(sub)) / 2, topY + 300);

        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        fm = g2d.getFontMetrics();
        String prompt = "Press R to restart";
        g2d.drawString(prompt, leftX + (playWidth - fm.stringWidth(prompt)) / 2, topY + 350);
    }
}
