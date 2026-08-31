package main;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable {
    public static final int width = 1280;
    public static final int height = 720;
    private final int FPS = 60;
    private Thread gameThread;
    private final PlayManager pm;
    private final KeyHandler keyH;
    private boolean paused = false;

    public GamePanel() {
        this.setPreferredSize(new Dimension(width, height));
        this.setBackground(Color.BLACK);
        this.setLayout(null);
        this.setFocusable(true);

        keyH = new KeyHandler();
        this.addKeyListener(keyH);

        this.addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {
                requestFocusInWindow();
            }
            @Override
            public void ancestorRemoved(javax.swing.event.AncestorEvent event) {}
            @Override
            public void ancestorMoved(javax.swing.event.AncestorEvent event) {}
        });

        pm = new PlayManager();
    }

    public void launch() {
        gameThread = new Thread(this, "TetrisGameThread");
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {}
        }
    }

    private void update() {
        keyH.update();

        if (keyH.consumePause()) {
            paused = !paused;
        }

        if (!paused) {
            pm.update(keyH);
        } else {
            if (keyH.consumeRestart()) {
                pm.initGame();
                paused = false;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        pm.draw(g2d);

        // Title
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.ITALIC, 50));
        g2d.drawString("Tetris", 35, pm.topY + 320);

        // Pause overlay
        if (paused) {
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(pm.leftX, pm.topY, pm.playWidth, pm.playHeight);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 36));
            FontMetrics fm = g2d.getFontMetrics();
            String title = "PAUSED";
            g2d.drawString(title, pm.leftX + (pm.playWidth - fm.stringWidth(title)) / 2, pm.topY + 280);

            g2d.setFont(new Font("Arial", Font.PLAIN, 16));
            fm = g2d.getFontMetrics();
            String sub = "Press ESC to resume";
            g2d.drawString(sub, pm.leftX + (pm.playWidth - fm.stringWidth(sub)) / 2, pm.topY + 320);
        }
    }
}
