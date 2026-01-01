package mino;

import main.KeyHandler;
import main.PlayManager;

import java.awt.*;

public class Mino {
    public Block b[] = new Block[4];
    public Block tempB[] = new Block[4];
    int autoDropCounter = 0;
    public int direction = 1; //default position
    private boolean leftCollision, rightCollision, downCollision;
    public boolean active = true, deactivate;
    int deactivateCounter = 0;


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
        checkRotationCollision();
        if (!leftCollision && !rightCollision && !downCollision) {
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
    }

    public void getDirection1() {
    }

    public void getDirection2() {
    }

    public void getDirection3() {
    }

    public void getDirection4() {
    }

    public void checkStaticBlockCollision() {
        for (int i = 0; i < PlayManager.staticList.size(); i++) {
            Block targetBlock = PlayManager.staticList.get(i);
            int targetX = targetBlock.x;
            int targetY = targetBlock.y;
            //check down
            for (Block block : b) {
                if (block.x == targetX && block.y + Block.size == targetY) {
                    downCollision = true;
                    break;
                }
            }
            //check left
            for (Block block : b) {
                if (block.x - Block.size == targetX && block.y == targetY) {
                    leftCollision = true;
                    break;
                }
            }
            //check right
            for (Block block : b) {
                if (block.x + Block.size == targetX && block.y == targetY) {
                    downCollision = true;
                    break;
                }
            }
        }
    }

    public void checkMovementCollision() {
        leftCollision = false;
        rightCollision = false;
        downCollision = false;
        checkStaticBlockCollision();
        //check collision
        //left wall
        for (int i = 0; i < b.length; i++) {
            if (b[i].x == PlayManager.leftX) {
                leftCollision = true;
            }
            if (b[i].x + Block.size == PlayManager.rightX) {
                rightCollision = true;
            }
            if (b[i].y + Block.size == PlayManager.bottomY) {
                downCollision = true;
            }
        }
    }

    public void checkRotationCollision() {
        leftCollision = false;
        rightCollision = false;
        downCollision = false;
        checkStaticBlockCollision();
        //check collision
        //left wall
        for (int i = 0; i < b.length; i++) {
            if (tempB[i].x < PlayManager.leftX) {
                leftCollision = true;
            }
            if (tempB[i].x + Block.size > PlayManager.rightX) {
                rightCollision = true;
            }
            if (tempB[i].y + Block.size > PlayManager.bottomY) {
                downCollision = true;
            }
        }
    }

    public void update() {
        // control the mino
        if(deactivate){
            deactivating();
        }
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
        checkMovementCollision();
        if (KeyHandler.down) {
            if (!downCollision) {
                System.out.println("DOWN");
                b[0].y += Block.size;
                b[1].y += Block.size;
                b[2].y += Block.size;
                b[3].y += Block.size;
                autoDropCounter = 0;
                KeyHandler.down = false;
            }
        }
        if (KeyHandler.left) {
            if (!leftCollision) {
                System.out.println("LEFT");
                b[0].x -= Block.size;
                b[1].x -= Block.size;
                b[2].x -= Block.size;
                b[3].x -= Block.size;
                KeyHandler.left = false;
            }
        }
        if (KeyHandler.right) {
            if (!rightCollision) {
                System.out.println("RIGHT");
                b[0].x += Block.size;
                b[1].x += Block.size;
                b[2].x += Block.size;
                b[3].x += Block.size;
                KeyHandler.right = false;
            }
        }
        if (downCollision) {
            deactivate = true;
        } else {
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
    }

    private void deactivating() {
        deactivateCounter++;
        // wait 45 frame until deactivate
        if(deactivateCounter == 45){
            deactivateCounter = 0;
            checkMovementCollision();
            if(downCollision){
                active = false;
            }
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
