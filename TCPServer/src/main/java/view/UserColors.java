package view;

import javafx.scene.paint.Color;

public final class UserColors {
    private UserColors() {}

    private static double hue(String name) {
        return Math.abs(name.hashCode() % 360);
    }

    /** Light version, used as a background */
    public static String pastelHex(String name) {
        Color c = Color.hsb(hue(name), 0.30, 1.0);
        return String.format("#%02x%02x%02x",
                Math.round(c.getRed() * 255),
                Math.round(c.getGreen() * 255),
                Math.round(c.getBlue() * 255));
    }

    /** Dark version of the same hue, used for text */
    public static Color dark(String name) {
        return Color.hsb(hue(name), 0.85, 0.55);
    }
}