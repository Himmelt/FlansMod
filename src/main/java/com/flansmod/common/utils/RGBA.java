package com.flansmod.common.utils;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RGBA {

    private int RED;
    private int GREEN;
    private int BLUE;
    private int OPACITY;

    public static final Pattern COLOR = Pattern.compile("rgba\\(\\d+,\\d+,\\d+,\\d+\\)");
    public static final RGBA WHITE = new RGBA(255, 255, 255, 255);


    public RGBA(int red, int green, int blue, int opacity) {
        this.RED = red;
        this.GREEN = green;
        this.BLUE = blue;
        this.OPACITY = opacity;
    }

    public static RGBA parseColor(String string, RGBA def) {
        Matcher matcher = COLOR.matcher(string.replaceAll(" ", ""));
        if (matcher.find()) {
            String[] ss = matcher.group().replaceAll("(rgba|\\(|\\))", "").split(",");
            return new RGBA(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]), Integer.parseInt(ss[2]), Integer.parseInt(ss[3]));
        }
        return def;
    }

    public String toString() {
        return "rgba(" + this.RED + ", " + this.GREEN + ", " + this.BLUE + ", " + this.OPACITY + ")";
    }

    public int getRed() {
        return this.RED;
    }

    public void setRed(int r) {
        this.RED = r;
    }

    public int getGreen() {
        return this.GREEN;
    }

    public void setGreen(int g) {
        this.GREEN = g;
    }

    public int getBlue() {
        return this.BLUE;
    }

    public void setBlue(int b) {
        this.BLUE = b;
    }

    public int getOpacity() {
        return this.OPACITY;
    }

    public void setOpacity(int o) {
        this.OPACITY = o;
    }
}
