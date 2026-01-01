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
        g2d.setColor(color);
        g2d.fillRect(x + 2, y + 2, size - 4, size - 4);
    }

}
