package main;

import mino.Block;
import mino.Mino;
import mino.Mino_L1;

import java.awt.*;

public class PlayManager {
    // play area setting
    final int width = 360;
    final int height = 600;
    public static int leftX, rightX, topY, bottomY;

    //minos
    Mino currentMino;
    final int minoStartX, minoStartY;

    //others
    public static int dropInterval = 60;

    public PlayManager() {
        leftX = (GamePanel.width / 2) - (width / 2);
        rightX = leftX + width;
        topY = 50;
        bottomY = topY + height;
        minoStartX = leftX + (width / 2) - Block.size;
        minoStartY = topY + Block.size;

        //set the starting mino
        currentMino = new Mino_L1();
        currentMino.setXY(minoStartX, minoStartY);
    }

    public void update() {
        currentMino.update();
    }

    public void draw(Graphics2D g2d) {
        //draw the main play area
        g2d.setColor(Color.white);
        g2d.setStroke(new BasicStroke(4f));
        g2d.drawRect(leftX - 4, topY - 4, width + 8, height + 8);

        //draw waiting box
        int x = rightX + 100;
        int y = bottomY - 200;
        g2d.drawRect(x, y, 200, 200);
        g2d.setFont(new Font("Arial", Font.PLAIN, 30));
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.drawString("NEXT", x + 60, y + 40);

        // draw the current mino brick
        if (currentMino != null) {
            currentMino.draw(g2d);
        }
    }
}
