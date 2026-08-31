package mino;

import main.PlayManager;
import main.SoundManager;

import java.awt.*;

public class Mino {
    public Block[] b = new Block[4];
    public Block[] tempB = new Block[4];
    public int direction = 1;
    public boolean active = true;
    public boolean deactivate = false;
    public int deactivateCounter = 0;
    public static final int MAX_LOCK_DELAY = 35;

    public void create(Color color) {
        for (int i = 0; i < 4; i++) {
            b[i] = new Block(color);
            tempB[i] = new Block(color);
        }
    }

    public void setXY(int x, int y) {
    }

    public void getDirection1() {}
    public void getDirection2() {}
    public void getDirection3() {}
    public void getDirection4() {}

    public void applyDirection(int dir) {
        switch (dir) {
            case 1 -> getDirection1();
            case 2 -> getDirection2();
            case 3 -> getDirection3();
            case 4 -> getDirection4();
        }
    }

    public boolean rotate(PlayManager pm, boolean clockwise) {
        int nextDir;
        if (clockwise) {
            nextDir = (direction == 4) ? 1 : direction + 1;
        } else {
            nextDir = (direction == 1) ? 4 : direction - 1;
        }

        // Generate tempB positions for candidate rotation
        applyDirection(nextDir);

        // Wall kick offsets to test: (0,0), (-1,0), (+1,0), (-2,0), (+2,0), (0,-1)
        int[][] kickOffsets = {
            {0, 0},
            {-Block.size, 0},
            {Block.size, 0},
            {-Block.size * 2, 0},
            {Block.size * 2, 0},
            {0, -Block.size}
        };

        for (int[] offset : kickOffsets) {
            if (isValidPlacement(pm, offset[0], offset[1])) {
                this.direction = nextDir;
                for (int i = 0; i < 4; i++) {
                    b[i].x = tempB[i].x + offset[0];
                    b[i].y = tempB[i].y + offset[1];
                }
                if (deactivate) {
                    deactivateCounter = 0; // reset lock delay on successful rotation
                }
                SoundManager.getInstance().playRotate();
                return true;
            }
        }

        // Rotation blocked: restore tempB to current direction
        applyDirection(direction);
        return false;
    }

    private boolean isValidPlacement(PlayManager pm, int offsetX, int offsetY) {
        for (int i = 0; i < 4; i++) {
            int tx = tempB[i].x + offsetX;
            int ty = tempB[i].y + offsetY;
            if (!pm.isInsidePlayArea(tx, ty) || pm.isCellOccupied(tx, ty)) {
                return false;
            }
        }
        return true;
    }

    public boolean canMoveLeft(PlayManager pm) {
        for (int i = 0; i < 4; i++) {
            int tx = b[i].x - Block.size;
            int ty = b[i].y;
            if (!pm.isInsidePlayArea(tx, ty) || pm.isCellOccupied(tx, ty)) {
                return false;
            }
        }
        return true;
    }

    public boolean canMoveRight(PlayManager pm) {
        for (int i = 0; i < 4; i++) {
            int tx = b[i].x + Block.size;
            int ty = b[i].y;
            if (!pm.isInsidePlayArea(tx, ty) || pm.isCellOccupied(tx, ty)) {
                return false;
            }
        }
        return true;
    }

    public boolean canMoveDown(PlayManager pm) {
        for (int i = 0; i < 4; i++) {
            int tx = b[i].x;
            int ty = b[i].y + Block.size;
            if (!pm.isInsidePlayArea(tx, ty) || pm.isCellOccupied(tx, ty)) {
                return false;
            }
        }
        return true;
    }

    public void moveLeft(PlayManager pm) {
        if (canMoveLeft(pm)) {
            for (int i = 0; i < 4; i++) {
                b[i].x -= Block.size;
            }
            if (deactivate) deactivateCounter = 0;
            SoundManager.getInstance().playMove();
        }
    }

    public void moveRight(PlayManager pm) {
        if (canMoveRight(pm)) {
            for (int i = 0; i < 4; i++) {
                b[i].x += Block.size;
            }
            if (deactivate) deactivateCounter = 0;
            SoundManager.getInstance().playMove();
        }
    }

    public boolean moveDown(PlayManager pm) {
        if (canMoveDown(pm)) {
            for (int i = 0; i < 4; i++) {
                b[i].y += Block.size;
            }
            return true;
        } else {
            deactivate = true;
            return false;
        }
    }

    public void hardDrop(PlayManager pm) {
        int distance = getDropDistance(pm);
        for (int i = 0; i < 4; i++) {
            b[i].y += distance * Block.size;
        }
        active = false;
        SoundManager.getInstance().playHardDrop();
    }

    public int getDropDistance(PlayManager pm) {
        int distance = 0;
        while (true) {
            int testYOffset = (distance + 1) * Block.size;
            boolean canFall = true;
            for (int i = 0; i < 4; i++) {
                int tx = b[i].x;
                int ty = b[i].y + testYOffset;
                if (!pm.isInsidePlayArea(tx, ty) || pm.isCellOccupied(tx, ty)) {
                    canFall = false;
                    break;
                }
            }
            if (canFall) {
                distance++;
            } else {
                break;
            }
        }
        return distance;
    }

    public void update(PlayManager pm) {
        if (deactivate) {
            deactivateCounter++;
            if (!canMoveDown(pm)) {
                if (deactivateCounter >= MAX_LOCK_DELAY) {
                    active = false;
                }
            } else {
                deactivate = false;
                deactivateCounter = 0;
            }
        }
    }

    public void draw(Graphics2D g2d) {
        for (int i = 0; i < 4; i++) {
            b[i].draw(g2d);
        }
    }

    public void drawGhost(Graphics2D g2d, PlayManager pm) {
        int distance = getDropDistance(pm);
        if (distance > 0) {
            int yOffset = distance * Block.size;
            for (int i = 0; i < 4; i++) {
                b[i].drawGhost(g2d, b[i].x, b[i].y + yOffset);
            }
        }
    }

    public void drawPreview(Graphics2D g2d, int centerX, int centerY) {
        // Ensure shape geometry is initialized (setXY may not have been called on preview minos)
        setXY(0, 0);

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (int i = 0; i < 4; i++) {
            minX = Math.min(minX, b[i].x);
            maxX = Math.max(maxX, b[i].x + Block.size);
            minY = Math.min(minY, b[i].y);
            maxY = Math.max(maxY, b[i].y + Block.size);
        }
        int shapeW = maxX - minX;
        int shapeH = maxY - minY;
        int offsetX = centerX - (minX + shapeW / 2);
        int offsetY = centerY - (minY + shapeH / 2);

        for (int i = 0; i < 4; i++) {
            b[i].drawAt(g2d, b[i].x + offsetX, b[i].y + offsetY);
        }
    }
}
