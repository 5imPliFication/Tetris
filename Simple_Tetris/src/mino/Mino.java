package mino;

import main.KeyHandler;
import main.PlayManager;

import java.awt.*;

public class Mino {
    public Block b[] = new Block[4];
    public Block tempB[] = new Block[4];
    int autoDropCounter = 0;
    public int direction = 1; //default position

    public void create(Color color) {
        b[0] = new Block(color);
        b[1] = new Block(color);
        b[2] = new Block(color);
        b[3] = new Block(color);
        tempB[0] = new Block(color);
        tempB[1] = new Block(color);
        tempB[2] = new Block(color);
        tempB[3] = new Block(color);
    }

    public void setXY(int x, int y) {
    }

    public void updateXY(int direction) {
        this.direction = direction;
        b[0].x = tempB[0].x;
        b[0].y = tempB[0].y;
        b[1].x = tempB[1].x;
        b[1].y = tempB[1].y;
        b[2].x = tempB[2].x;
        b[2].y = tempB[2].y;
        b[3].x = tempB[3].x;
        b[3].y = tempB[3].y;
    }

    public void getDirection1() {
    }

    public void getDirection2() {
    }

    public void getDirection3() {
    }

    public void getDirection4() {
    }

    public void update() {
        // control the mino
        if (KeyHandler.up) {
            System.out.println("CHANGE DIRECTION!");
            switch (direction) {
                case 1:
                    getDirection2();
                    break;
                case 2:
                    getDirection3();
                    break;
                case 3:
                    getDirection4();
                    break;
                case 4:
                    getDirection1();
                    break;
            }
            KeyHandler.up = false;
        }
        if (KeyHandler.down) {
            System.out.println("DOWN");
            b[0].y += Block.size;
            b[1].y += Block.size;
            b[2].y += Block.size;
            b[3].y += Block.size;
            autoDropCounter = 0;
            KeyHandler.down = false;
        }
        if (KeyHandler.left) {
            System.out.println("LEFT");
            b[0].x -= Block.size;
            b[1].x -= Block.size;
            b[2].x -= Block.size;
            b[3].x -= Block.size;
            KeyHandler.left = false;
        }
        if (KeyHandler.right) {
            System.out.println("RIGHT");
            b[0].x += Block.size;
            b[1].x += Block.size;
            b[2].x += Block.size;
            b[3].x += Block.size;
            KeyHandler.right = false;
        }

        // gravity
        autoDropCounter++;
        if (autoDropCounter == PlayManager.dropInterval) {
            b[0].y += Block.size;
            b[1].y += Block.size;
            b[2].y += Block.size;
            b[3].y += Block.size;
            autoDropCounter = 0;
        }
    }

    public void draw(Graphics2D g2d) {
        // a single mino brick
        g2d.setColor(b[0].color);
        int margin = 4;
        g2d.fillRect(b[0].x, b[0].y, Block.size - (margin), Block.size - (margin));
        g2d.fillRect(b[1].x, b[1].y, Block.size - (margin), Block.size - (margin));
        g2d.fillRect(b[2].x, b[2].y, Block.size - (margin), Block.size - (margin));
        g2d.fillRect(b[3].x, b[3].y, Block.size - (margin), Block.size - (margin));
    }
}
