# Modern Java Tetris

A complete, feature-rich Java Swing implementation of Tetris built according to modern Tetris guidelines.

## Features

- **2D Grid Engine**: Clean $20 \times 10$ array representation with $O(1)$ collision detection and bottom-up line clearing.
- **Tetris Guideline Colors**: Cyan (I), Blue (J), Orange (L), Yellow (O), Green (S), Purple (T), Red (Z).
- **SRS Wall Kicks & Rotation**: Smooth piece rotation with boundary and block kick handling.
- **Ghost Piece**: Real-time drop trajectory preview shadow showing exact landing location.
- **7-Bag Randomizer**: Fair piece distribution preventing droughts and repetitive piece streaks.
- **Hold Piece**: Store a tetromino in the hold queue using `C` or `Shift` (1 swap per turn).
- **Next Queue**: View the next 3 upcoming tetrominoes.
- **Hard Drop & Soft Drop**: Instant lock with `Space` or soft drop bonus scoring with `Down / S`.
- **Responsive Controls (DAS / ARR)**: Delayed Auto Shift and Auto Repeat Rate for snappy keyboard response.
- **Guideline Scoring & Leveling**: Scaled scoring ($100, 300, 500, 800 \times \text{Level}$) and speed progression.
- **High Score Persistence**: Automatically preserves your top score between game sessions.
- **Procedural 8-Bit Audio**: Built-in synthesized sound effects for movement, rotation, drops, line clears, and game over with mute toggle (`M`).
- **3D Beveled Visuals**: Polished block graphics with lighting highlights and drop shadows.

---

## Controls

| Key | Action |
| --- | --- |
| `←` / `A` | Move Left |
| `→` / `D` | Move Right |
| `↑` / `W` / `X` | Rotate Clockwise |
| `Z` | Rotate Counter-Clockwise |
| `↓` / `S` | Soft Drop |
| `SPACE` | Hard Drop (Instant) |
| `C` / `SHIFT` | Hold Piece |
| `ESC` / `P` | Pause / Resume |
| `R` / `ENTER` | Restart Game |
| `M` | Toggle Sound Mute |

---

## Running the Game

Compile and run from your IDE or the command line:

```bash
javac -d bin $(find Simple_Tetris/src -name "*.java")
java -cp bin main.Main
```
