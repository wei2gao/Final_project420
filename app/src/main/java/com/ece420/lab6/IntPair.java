package com.ece420.lab6;

public class IntPair {
    public int x,y;

    public IntPair() {
        this(0,0);
    }

    public IntPair(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object other){
        if (!(other instanceof IntPair)) return false;
        IntPair p = (IntPair) other;
        return (this.x == p.x) && (this.y == p.y);
    }
}