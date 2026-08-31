package mino;

import java.awt.*;

public class Block extends Rectangle {
    public int x, y;
    public static final int size = 30;
    public Color color;

    public Block(Color color) {
        this.color = color;
    }

    public void draw(Graphics2D g2d) {
        drawAt(g2d, this.x, this.y);
    }

    public void drawAt(Graphics2D g2d, int drawX, int drawY) {
        if (color == null) return;

        int margin = 2;
        int bx = drawX + margin;
        int by = drawY + margin;
        int bSize = size - (margin * 2);

        // Flat filled block
        g2d.setColor(color);
        g2d.fillRect(bx, by, bSize, bSize);

        // Thin dark border
        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.drawRect(bx, by, bSize, bSize);
    }

    public void drawGhost(Graphics2D g2d, int drawX, int drawY) {
        if (color == null) return;

        int margin = 2;
        int bx = drawX + margin;
        int by = drawY + margin;
        int bSize = size - (margin * 2);

        // Brighten the color so dark minos (e.g. blue) are visible on black bg
        int r = Math.min(255, color.getRed() + 100);
        int gc = Math.min(255, color.getGreen() + 100);
        int b = Math.min(255, color.getBlue() + 100);

        // Semi-transparent fill
        g2d.setColor(new Color(r, gc, b, 40));
        g2d.fillRect(bx, by, bSize, bSize);

        // Solid outline
        g2d.setColor(new Color(r, gc, b, 160));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRect(bx, by, bSize, bSize);
    }
}
