package main;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable {
    public static final int width = 1280;
    public static final int height = 720;
    private final int FPS = 60;
    Thread gameThread;
    PlayManager pm;

    public GamePanel(){
        this.setPreferredSize(new Dimension(width, height));
        this.setBackground(Color.black);
        this.setLayout(null);
        //movement
        this.addKeyListener(new KeyHandler());
        this.setFocusable(true);
        //This fixes the key handler not connected to the game window
        // Request focus AFTER the panel is visible
        this.addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {
                requestFocusInWindow();
                System.out.println("GamePanel gained focus!");
            }
            @Override
            public void ancestorRemoved(javax.swing.event.AncestorEvent event) {}
            @Override
            public void ancestorMoved(javax.swing.event.AncestorEvent event) {}
        });

        pm = new PlayManager();
    }
    public void launch(){
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1_000_000_000/FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;
            if(delta >= 1){
                update();
                repaint();
                delta--;
            }
        }
    }

    private void update(){
        if(!KeyHandler.pause && !pm.gameover) pm.update();
    }
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        pm.draw(g2d);
    }
}
