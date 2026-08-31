package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {
    // Key hold states
    public boolean leftHeld;
    public boolean rightHeld;
    public boolean downHeld;

    // Frame counters for DAS (Delayed Auto Shift) and ARR (Auto Repeat Rate)
    private int leftHeldFrames = 0;
    private int rightHeldFrames = 0;
    private int downHeldFrames = 0;

    private static final int DAS = 10; // Frames before auto-repeat begins (~166ms at 60 FPS)
    private static final int ARR = 2;  // Repeat interval in frames (~33ms at 60 FPS)
    private static final int SOFT_DROP_INTERVAL = 2;

    // Single-trigger actions
    private boolean rotateCWPending = false;
    private boolean rotateCCWPending = false;
    private boolean hardDropPending = false;
    private boolean holdPending = false;
    private boolean pausePending = false;
    private boolean restartPending = false;
    private boolean mutePending = false;

    public void update() {
        if (leftHeld) {
            leftHeldFrames++;
        } else {
            leftHeldFrames = 0;
        }

        if (rightHeld) {
            rightHeldFrames++;
        } else {
            rightHeldFrames = 0;
        }

        if (downHeld) {
            downHeldFrames++;
        } else {
            downHeldFrames = 0;
        }
    }

    public boolean shouldMoveLeft() {
        if (!leftHeld) return false;
        if (leftHeldFrames == 1) return true;
        if (leftHeldFrames > DAS && (leftHeldFrames - DAS) % ARR == 0) return true;
        return false;
    }

    public boolean shouldMoveRight() {
        if (!rightHeld) return false;
        if (rightHeldFrames == 1) return true;
        if (rightHeldFrames > DAS && (rightHeldFrames - DAS) % ARR == 0) return true;
        return false;
    }

    public boolean shouldSoftDrop() {
        if (!downHeld) return false;
        return downHeldFrames % SOFT_DROP_INTERVAL == 0;
    }

    public boolean consumeRotateCW() {
        if (rotateCWPending) {
            rotateCWPending = false;
            return true;
        }
        return false;
    }

    public boolean consumeRotateCCW() {
        if (rotateCCWPending) {
            rotateCCWPending = false;
            return true;
        }
        return false;
    }

    public boolean consumeHardDrop() {
        if (hardDropPending) {
            hardDropPending = false;
            return true;
        }
        return false;
    }

    public boolean consumeHold() {
        if (holdPending) {
            holdPending = false;
            return true;
        }
        return false;
    }

    public boolean consumePause() {
        if (pausePending) {
            pausePending = false;
            return true;
        }
        return false;
    }

    public boolean consumeRestart() {
        if (restartPending) {
            restartPending = false;
            return true;
        }
        return false;
    }

    public boolean consumeMute() {
        if (mutePending) {
            mutePending = false;
            return true;
        }
        return false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
            leftHeld = true;
        } else if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
            rightHeld = true;
        } else if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
            downHeld = true;
        } else if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP || code == KeyEvent.VK_X) {
            rotateCWPending = true;
        } else if (code == KeyEvent.VK_Z) {
            rotateCCWPending = true;
        } else if (code == KeyEvent.VK_SPACE) {
            hardDropPending = true;
        } else if (code == KeyEvent.VK_C || code == KeyEvent.VK_SHIFT) {
            holdPending = true;
        } else if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_P) {
            pausePending = true;
        } else if (code == KeyEvent.VK_R || code == KeyEvent.VK_ENTER) {
            restartPending = true;
        } else if (code == KeyEvent.VK_M) {
            mutePending = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
            leftHeld = false;
            leftHeldFrames = 0;
        } else if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
            rightHeld = false;
            rightHeldFrames = 0;
        } else if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
            downHeld = false;
            downHeldFrames = 0;
        }
    }
}
