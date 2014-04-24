package com.twopeople.game;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by Alexey
 * At 2:22 PM on 4/4/14
 */

public class Level {
    private int size = 5;
    private int brickSize;
    private int genQueue = 0;

    private Brick[][] bricks;
    private ArrayList<Brick> removeQueue = new ArrayList<Brick>();

    private GameState game;

    public Level(GameState game, int size) {
        this.game = game;

        if (!Main.hasSavedState() || size != 0) {
            setSize(size);
            generateTiles();
        } else {
            loadState();
        }
    }

    public void setSize(int size) {
        this.size = size;
        this.bricks = new Brick[size][size];
        this.brickSize = (401 - 15 * 2 - 15 * (size - 1)) / size;
        for (int x = 0; x < size; ++x) {
            for (int y = 0; y < size; ++y) {
                if (bricks[x][y] != null) {
                    bricks[x][y].size = brickSize;
                }
            }
        }
    }

    public void update(double delta) {
        Iterator<Brick> i = removeQueue.iterator();
        while (i.hasNext()) {
            Brick b = i.next();
            b.update(delta);
            if (b.opacity < 0.1) {
                i.remove();
            }
        }

        for (int x = 0; x < size; ++x) {
            for (int y = 0; y < size; ++y) {
                if (bricks[x][y] != null) {
                    bricks[x][y].update(delta);
                }
            }
        }

        if (genQueue > 0 && isMovingFinished()) {
            --genQueue;
            generateTiles();

            if (size == 5) {
                generateTiles();
                generateTiles();
            }
        }
    }

    public void render(Graphics g) {
        for (int x = 0; x < size; ++x) {
            for (int y = 0; y < size; ++y) {
                g.setColor(new Color(228, 228, 228));
                g.fillRoundRect(getCellX(x), getCellY(y), brickSize, brickSize, GameState.ROUND_RADIUS, GameState.ROUND_RADIUS);
            }
        }

        for (int x = 0; x < size; ++x) {
            for (int y = 0; y < size; ++y) {
                if (bricks[x][y] != null) {
                    bricks[x][y].render(g);
                }
            }
        }

        Iterator<Brick> i = removeQueue.iterator();
        while (i.hasNext()) {
            i.next().render(g);
        }
    }

    public void move(int xShift, int yShift) {
        if (xShift > 1 || yShift > 1 || !isMovingFinished()) {
            return;
        }

        boolean left = xShift < 0;
        boolean up = yShift < 0;

        boolean moved = stickTiles(left, up, xShift, yShift);
        boolean merged = false;

        for (int x = left ? 0 : size - 1; left ? x < size : x >= 0; x += left ? 1 : -1) {
            for (int y = up ? 0 : size - 1; up ? y < size : y >= 0; y += up ? 1 : -1) {
                int xp = x + xShift;
                int yp = y + yShift;

                if (bricks[x][y] != null && inBounds(xp, yp) && bricks[xp][yp] != null && bricks[xp][yp].hasEqualValue(bricks[x][y])) {
                    bricks[xp][yp].value *= 2;
                    bricks[xp][yp].setSwell(12);

                    double modifier = 1.0;

                    if (size == 5) {
                        modifier = 2.0;
                    } else if (size == 3) {
                        modifier = 0.5;
                    }

                    game.score += (int) (bricks[xp][yp].value / modifier);

                    if (game.score > game.main.bestScore) {
                        game.main.bestScore = game.score;
                    }

                    bricks[x][y].moveTo(getCellX(xp), getCellY(yp));
                    bricks[x][y].fadeOut();
                    removeQueue.add(bricks[x][y]);

                    bricks[x][y] = null;
                    merged = true;
                }
            }
        }

        moved |= stickTiles(left, up, xShift, yShift);

        if (moved || merged) {
            genQueue++;
        }
    }

    private void generateTiles() {
        ArrayList<Integer[]> emptyTiles = new ArrayList<Integer[]>();
        for (int x = 0; x < size; ++x) {
            for (int y = 0; y < size; ++y) {
                if (bricks[x][y] == null) {
                    emptyTiles.add(new Integer[]{x, y});
                }
            }
        }

        if (emptyTiles.size() > 0) {
            Random random = new Random();
            Integer[] pos = emptyTiles.get(random.nextInt(emptyTiles.size()));

            addBrick(pos[0], pos[1], 2);
            saveState();
        }
    }

    private boolean isMovingFinished() {
        for (int x = 0; x < size; ++x) {
            for (int y = 0; y < size; ++y) {
                if (bricks[x][y] != null && bricks[x][y].isMoving()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean stickTiles(boolean left, boolean up, int xShift, int yShift) {
        boolean moved = false;
        for (int x = left ? 0 : size - 1; left ? x < size : x >= 0; x += left ? 1 : -1) {
            for (int y = up ? 0 : size - 1; up ? y < size : y >= 0; y += up ? 1 : -1) {
                int xp = x + xShift;
                int yp = y + yShift;

                if (bricks[x][y] != null && inBounds(xp, yp)) {
                    boolean changed = false;
                    while (inBounds(xp, yp) && bricks[xp][yp] == null) {
                        xp += xShift;
                        yp += yShift;
                        changed = true;
                    }

                    if (changed) {
                        xp -= xShift;
                        yp -= yShift;

                        bricks[xp][yp] = bricks[x][y];
                        bricks[xp][yp].moveTo(getCellX(xp), getCellY(yp));
                        bricks[x][y] = null;
                        moved = true;
                    }
                }
            }
        }

        saveState();

        return moved;
    }

    private boolean inBounds(int xp, int yp) {
        return xp >= 0 && yp >= 0 && xp < size && yp < size;
    }

    public boolean isOver() {
        for (int x = 0; x < size; ++x) {
            for (int y = 0; y < size; ++y) {
                for (int i = x == 0 ? 1 : -1; i <= (x == size - 1 ? 0 : 1); ++i) {
                    int nx = x + i;
                    if (nx == x) { continue; }
                    if (bricks[x][y] == null || bricks[nx][y] == null || bricks[x][y].hasEqualValue(bricks[nx][y])) {
                        return false;
                    }
                }

                for (int j = y == 0 ? 1 : -1; j <= (y == size - 1 ? 0 : 1); ++j) {
                    int ny = y + j;
                    if (ny == y) { continue; }
                    if (bricks[x][y] == null || bricks[x][ny] == null || bricks[x][y].hasEqualValue(bricks[x][ny])) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public void addBrick(int xc, int yc, int val) {
        bricks[xc][yc] = new Brick(getCellX(xc), getCellY(yc), brickSize);
        bricks[xc][yc].value = val;
    }

    public int getCellX(int n) {
        return GameState.FIELD_X + (n + 1) * GameState.CELL_SPACING + n * brickSize;
    }

    public int getCellY(int n) {
        return GameState.FIELD_Y + (n + 1) * GameState.CELL_SPACING + n * brickSize;
    }

    public void restart() {
        bricks = new Brick[size][size];
        genQueue = 0;
        removeQueue.clear();
        generateTiles();
    }

    private void loadState() {
        try {
            FileInputStream in = new FileInputStream(Main.pm.getAbsolutePathForFile("game.dat"));
            ObjectInputStream objectIn = new ObjectInputStream(in);
            State read = (State) objectIn.readObject();
            if (read != null) {
                setSize(read.bricks.length);
                bricks = read.bricks;
                game.score = read.score;

                if (game.score > game.main.bestScore) {
                    game.main.bestScore = game.score;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveState() {
        try {
            FileOutputStream out = new FileOutputStream(Main.pm.getAbsolutePathForFile("game.dat"));
            ObjectOutputStream objectOut = new ObjectOutputStream(out);

            State state = new State();
            state.bricks = bricks;
            state.score = game.score;

            objectOut.writeObject(state);
            objectOut.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetState() {
        try {
            PrintWriter writer = new PrintWriter(Main.pm.getAbsolutePathForFile("game.dat"));
            writer.write("");
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}