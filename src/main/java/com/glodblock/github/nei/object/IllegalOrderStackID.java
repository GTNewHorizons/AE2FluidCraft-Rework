package com.glodblock.github.nei.object;

public class IllegalOrderStackID extends RuntimeException {

    private static final long serialVersionUID = -3782749325355674453L;

    public IllegalOrderStackID(int id) {
        super("Illegal type id: " + id);
    }
}
