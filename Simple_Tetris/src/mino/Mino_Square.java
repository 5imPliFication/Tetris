package mino;

import java.awt.*;

public class Mino_Square extends Mino {
    public Mino_Square() {
        create(new Color(240, 240, 0)); // Guideline Yellow
    }

    @Override
    public void setXY(int x, int y) {
        b[0].x = x;
        b[0].y = y;
        b[1].x = b[0].x + Block.size;
        b[1].y = b[0].y;
        b[2].x = b[0].x;
        b[2].y = b[0].y + Block.size;
        b[3].x = b[0].x + Block.size;
        b[3].y = b[0].y + Block.size;
    }

    private void applySame() {
        tempB[0].x = b[0].x;
        tempB[0].y = b[0].y;
        tempB[1].x = b[0].x + Block.size;
        tempB[1].y = b[0].y;
        tempB[2].x = b[0].x;
        tempB[2].y = b[0].y + Block.size;
        tempB[3].x = b[0].x + Block.size;
        tempB[3].y = b[0].y + Block.size;
    }

    @Override
    public void getDirection1() {
        applySame();
    }

    @Override
    public void getDirection2() {
        applySame();
    }

    @Override
    public void getDirection3() {
        applySame();
    }

    @Override
    public void getDirection4() {
        applySame();
    }
}
