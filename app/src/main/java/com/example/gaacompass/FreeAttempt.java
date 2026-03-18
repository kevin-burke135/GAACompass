package com.example.gaacompass;

public class FreeAttempt {
    public final float normX;
    public final float normY;
    public final boolean scored;
    public final long timestamp;

    public FreeAttempt(float normX, float normY, boolean scored, long timestamp) {
        this.normX = normX;
        this.normY = normY;
        this.scored = scored;
        this.timestamp = timestamp;
    }
}
