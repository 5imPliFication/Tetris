package main;

import mino.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class PlayManager {
    // play area setting
    final int width = 360;
    final int height = 600;
    public static int leftX, rightX, topY, bottomY;

    //minos
    Mino currentMino;
    final int minoStartX, minoStartY;
    Mino nextMino;
    final int nextStartX, nextStartY;
    public static ArrayList<Block> staticList = new ArrayList<>();

    //others
    public static int dropInterval = 60;

    public PlayManager() {
        leftX = (GamePanel.width / 2) - (width / 2);
        rightX = leftX + width;
        topY = 50;
        bottomY = topY + height;
        minoStartX = leftX + (width / 2) - Block.size;
        minoStartY = topY + Block.size;
        nextStartX = rightX + 175;
        nextStartY = topY + 500;

        //set the starting mino
        currentMino = pickMino();
        currentMino.setXY(minoStartX, minoStartY);
        nextMino = pickMino();
        nextMino.setXY(nextStartX, nextStartY);
    }

    public Mino pickMino() {
        Mino mino = null;
        int i = new Random().nextInt(7);
        mino = switch (i) {
            case 0 -> new Mino_L1();
            case 1 -> new Mino_L2();
            case 2 -> new Mino_Bar();
            case 3 -> new Mino_Square();
            case 4 -> new Mino_T();
            case 5 -> new Mino_Z1();
            case 6 -> new Mino_Z2();
            default -> mino;
        };
        return mino;
    }

    public void update() {
        if (!currentMino.active) {
            staticList.add(currentMino.b[0]);
            staticList.add(currentMino.b[1]);
            staticList.add(currentMino.b[2]);
            staticList.add(currentMino.b[3]);

            currentMino.deactivate = false;

            currentMino = nextMino;
            currentMino.setXY(minoStartX, minoStartY);
            nextMino = pickMino();
            nextMino.setXY(nextStartX, nextStartY);

            /// check if line is deletable
            deleteLine();
        }
        currentMino.update();
    }

    private void deleteLine() {
        int x = leftX;
        int y = topY;
        int blockCount = 0;
        while (x < rightX && y < bottomY) {
            x += Block.size;
            for (Block block : staticList) {
                if (block.x == x && block.y == y) {
                    //count the already placed block
                    blockCount++;
                }
            }
            if (x == rightX) {

                if (blockCount == 12) {
                    for (int i = staticList.size() - 1; i > -1; i--) {
                        //remove all block if the line is filled
                        if (staticList.get(i).y == y) {
                            staticList.remove(i);
                        }
                    }
                    for (int i = 0; i < staticList.size(); i++) {
                        // when line removed, blocks from above need to be place down to the bottom
                        if (staticList.get(i).y == y) {
                            staticList.get(i).y += Block.size;
                        }
                    }
                }

                blockCount = 0;
                x = leftX;
                y += Block.size;
            }
        }
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
        nextMino.draw(g2d);
        //draw static block
        for (Block block : staticList) {
            block.draw(g2d);
        }

        //pause
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 50));
        if (KeyHandler.pause) {
            x = leftX + 70;
            y = topY + 320;
            g2d.drawString("PAUSED!", x, y);
        }
    }
}
