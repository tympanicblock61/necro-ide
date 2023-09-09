package org.ide.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public class ColorUtils {
    public static Integer rgbaToHex(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF));
    }

    public static int colorToHex(Color color) {
        return rgbaToHex(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static List<Integer> hexToRgba(int hex) {
        int red = ((hex >> 16) & 0xFF);
        int green = ((hex >> 8) & 0xFF);
        int blue = (hex & 0xFF);
        int alpha = ((hex >> 24) & 0xFF);
        List<Integer> rgba = new ArrayList<>(4);
        rgba.add(red);
        rgba.add(green);
        rgba.add(blue);
        rgba.add(alpha);
        return rgba;
    }
}
