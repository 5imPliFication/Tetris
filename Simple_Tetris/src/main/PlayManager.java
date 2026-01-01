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
    public boolean gameover = false;

    //minos
    Mino currentMino;
    final int minoStartX, minoStartY;
    Mino nextMino;
    final int nextStartX, nextStartY;
    public static ArrayList<Block> staticList = new ArrayList<>();

    //others
    public static int dropInterval = 60;

    //effects
    public boolean effectEnable;
    public int effectCounter;
    public ArrayList<Integer> effectYList = new ArrayList<>();

    //score
    int level = 1;
    int lines, score;

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

            //check game over
            if (currentMino.b[0].x == minoStartX && currentMino.b[0].y == minoStartY) {
                gameover = true;
            }

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
        int lineCount = 0;
        while (x < rightX && y < bottomY) {
            for (int i = 0; i < staticList.size(); i++) {
                if (staticList.get(i).x == x && staticList.get(i).y == y) {
                    //count the already placed block
                    blockCount++;
                }
            }
            x += Block.size;
            if (x == rightX) {
                if (blockCount == 12) {
                    effectEnable = true;
                    effectYList.add(y);
                    for (int i = staticList.size() - 1; i > -1; i--) {
                        //remove all block if the line is filled
                        if (staticList.get(i).y == y) {
                            staticList.remove(i);
                        }
                    }
                    lineCount++;
                    lines++;
                    //drop speed, score mechanic
                    if (lines % 10 == 0 && dropInterval > 1) {
                        level++;
                        if (dropInterval > 10) {
                            dropInterval -= 5;
                        } else {
                            dropInterval--;
                        }
                    }
                    for (int i = 0; i < staticList.size(); i++) {
                        // when line removed, blocks from above need to be place down to the bottom
                        if (staticList.get(i).y < y) {
                            staticList.get(i).y += Block.size;
                        }
                    }
                }

                blockCount = 0;
                x = leftX;
                y += Block.size;
            }
        }
        if (lineCount > 0) {
            int singleLineScore = 10 * level;
            score += singleLineScore * lineCount;
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

        //draw score board
        g2d.drawRect(x, topY, 250, 300);
        x += 40;
        y = topY + 90;
        g2d.drawString("Level: " + level, x, y);
        y += 70;
        g2d.drawString("Lines: " + lines, x, y);
        y += 70;
        g2d.drawString("Score: " + score, x, y);

        // draw the current mino brick
        if (currentMino != null) {
            currentMino.draw(g2d);
        }
        nextMino.draw(g2d);
        //draw static block
        for (int i = 0; i < staticList.size(); i++) {
            staticList.get(i).draw(g2d);
        }

        //pause
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 50));
        if (gameover) {
            x = leftX + 25;
            y = topY + 320;
            g2d.drawString("GAME OVER", x, y);
        }
        if (KeyHandler.pause) {
            x = leftX + 70;
            y = topY + 320;
            g2d.drawString("PAUSED!", x, y);
        }

        //effect
        if (effectEnable) {
            effectCounter++;
            g2d.setColor(Color.white);
            for (int i = 0; i < effectYList.size(); i++) {
                g2d.fillRect(leftX, effectYList.get(i), width, Block.size);
            }
            if (effectCounter == 12) {
                effectEnable = false;
                effectCounter = 0;
                effectYList.clear();
            }
        }

        //draw game title
        x = 35;
        y = topY + 320;
        g2d.setColor(Color.white);
        g2d.setFont(new Font("Arial", Font.ITALIC, 60));
        g2d.drawString("Tetris", x, y);
    }
}
