package view;

import javafx.scene.paint.Color;

public final class UserColors {
    private UserColors() {}

    /** Dark color derived from the name, the same formula the server uses */
    public static Color dark(String name) {
        double hue = Math.abs(name.hashCode() % 360);
        return Color.hsb(hue, 0.85, 0.55);
    }
}