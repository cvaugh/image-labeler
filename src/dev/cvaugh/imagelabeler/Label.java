package dev.cvaugh.imagelabeler;

import java.awt.Color;

public enum Label {
    FACE(Color.GREEN),
    NOT_FACE(Color.RED),
    AMBIGUOUS(Color.MAGENTA),
    NONE(Color.LIGHT_GRAY);

    public Color color;

    Label(Color color) {
        this.color = color;
    }
}
