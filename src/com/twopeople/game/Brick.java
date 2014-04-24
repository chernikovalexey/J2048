package com.twopeople.game;

import java.awt.*;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Alexey
 * At 2:27 PM on 4/4/14
 */

public class Brick implements Serializable {
    public static final int MOVING_FRAMES = 6;
    public static final int SWELL_FRAMES = 12;

    public Animation xAnim, yAnim, swellAnim1, swellAnim2, opacityAnim;

    public int value = 2;
    public double opacity = 1.0;
    public double x, y;
    public int size;
    public int swell = 0;

    private transient final static HashMap<Integer, Color> colors = new HashMap<Integer, Color>() {{
        put(2, new Color(208, 188, 167));
        put(4, new Color(197, 171, 122));
        put(8, new Color(205, 158, 96));
        put(16, new Color(205, 158, 96));
        put(32, new Color(216, 155, 73));
        put(64, new Color(216, 155, 73));
        put(128, new Color(217, 145, 49));
        put(256, new Color(205, 158, 96));
        put(512, new Color(205, 158, 96));
        put(1024, new Color(205, 158, 96));
        put(2048, new Color(255, 146, 0));
    }};

    public Brick(double x, double y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;

        moveTo(x, y);
        setSwell(0);
        opacityAnim = new Animation(1.0, 1.0, MOVING_FRAMES);
    }

    public void update(double delta) {
        x = xAnim.next();
        y = yAnim.next();

        if (!swellAnim1.isFinished) {
            swell = (int) swellAnim1.next();
        } else if (swellAnim1.isFinished && !swellAnim2.isFinished) {
            swell = (int) swellAnim2.next();
        }

        opacity = opacityAnim.next();
    }

    public void moveTo(double nx, double ny) {
        xAnim = new Animation(x, nx, MOVING_FRAMES);
        yAnim = new Animation(y, ny, MOVING_FRAMES);
    }

    public void setSwell(int s) {
        swellAnim1 = new Animation(0, s, SWELL_FRAMES / 2);
        swellAnim2 = new Animation(s, 0, SWELL_FRAMES / 2);
    }

    public void fadeOut() {
        opacityAnim = new Animation(1.0, 0.0, 2);
    }

    public boolean isMoving() {
        double min = 0.85;
        return (xAnim.frame / xAnim.numFrames < min) || (yAnim.frame / yAnim.numFrames < min);
    }

    public void render(Graphics g) {
        int ix = (int) x;
        int iy = (int) y;

        g.setColor(colors.get(value > 2048 ? 2048 : value));
        g.fillRoundRect(ix - swell / 2, iy - swell / 2, size + swell, size + swell, GameState.ROUND_RADIUS, GameState.ROUND_RADIUS);

        g.setColor(Color.white);

        int fontSize = 60;
        if (value >= 128) {
            fontSize = 24;
        } else if (value >= 16) {
            fontSize = 42;
        }

        Font font = new Font("Tahoma", Font.BOLD, fontSize);
        FontMetrics metrics = g.getFontMetrics(font);

        String _val = "" + value;

        int w = metrics.stringWidth(_val);
        int h = metrics.getHeight();

        g.setFont(font);
        g.drawString(_val, ix + size / 2 - w / 2, iy + (size - h) / 2 - h / 5 + metrics.getDescent() + metrics.getAscent());
    }

    public boolean hasEqualValue(Brick brick) {
        return brick != null && brick.value == value;
    }
}