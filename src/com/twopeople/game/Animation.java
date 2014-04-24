package com.twopeople.game;

import java.io.Serializable;

/**
 * Created by Alexey
 * At 9:22 PM on 4/18/14
 */

public class Animation implements Serializable {
    boolean isFinished = false;

    int frame = 0;
    int numFrames;

    double start, end;
    double lastDelta;
    double lastValue;

    public Animation(double start, double end, int numFrames) {
        this.start = start;
        this.end = end;
        this.lastDelta = 0.0;
        this.lastValue = start;
        if (start == end) { isFinished = true; }
        this.numFrames = numFrames;
    }

    public double next() {
        isFinished = frame >= numFrames;

        if (isFinished) { return end; }

        double val = start + (end - start) * Math.sin((double) frame / (double) numFrames * Math.PI / 2);
        lastDelta = val - lastValue;
        lastValue = val;
        frame++;
        return val;
    }
}