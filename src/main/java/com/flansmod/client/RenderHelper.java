package com.flansmod.client;

import com.flansmod.common.utils.RGBA;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

public final class RenderHelper {

    private static void preRender() {
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
    }

    private static void postRender() {
        GL11.glEnable(3553);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawLine(float x1, float y1, float x2, float y2, float thickness, RGBA colour, boolean smooth) {
        drawLines(new float[]{x1, y1, x2, y2}, thickness, colour, smooth);
    }

    public static void drawRectangle(float x1, float y1, float x2, float y2, float thickness, RGBA colour, boolean smooth) {
        drawLines(new float[]{x1, y1, x2, y1, x2, y1, x2, y2, x1, y2, x2, y2, x1, y1, x1, y2}, thickness, colour, smooth);
    }

    public static void drawBorderedRectangle(float x1, float y1, float x2, float y2, float borderThickness, RGBA borderColour, RGBA fillColour, boolean smooth) {
        drawFilledRectangle(x1, y1, x2, y2, fillColour, smooth);
        drawRectangle(x1, y1, x2, y2, borderThickness, borderColour, smooth);
    }

    public static void drawLines(float[] points, float thickness, RGBA colour, boolean smooth) {
        preRender();
        if (smooth) GL11.glEnable(2848);
        else GL11.glDisable(2848);

        GL11.glLineWidth(thickness);
        GL11.glColor4f((float) colour.getRed() / 255.0F, (float) colour.getGreen() / 255.0F, (float) colour.getBlue() / 255.0F, (float) colour.getOpacity() / 255.0F);
        GL11.glBegin(1);

        for (int i = 0; i < points.length; i += 2) {
            GL11.glVertex2f(points[i], points[i + 1]);
        }

        GL11.glEnd();
        postRender();
    }

    public static void drawFilledRectangle(float x1, float y1, float x2, float y2, RGBA colour, boolean smooth) {
        drawFilledShape(new float[]{x1, y1, x1, y2, x2, y2, x2, y1}, colour, smooth);
    }

    public static void drawFilledShape(float[] points, RGBA colour, boolean smooth) {
        preRender();
        if (smooth) {
            GL11.glEnable(2848);
        } else {
            GL11.glDisable(2848);
        }

        GL11.glColor4f((float) colour.getRed() / 255.0F, (float) colour.getGreen() / 255.0F, (float) colour.getBlue() / 255.0F, (float) colour.getOpacity() / 255.0F);
        GL11.glBegin(9);

        for (int i = 0; i < points.length; i += 2) {
            GL11.glVertex2f(points[i], points[i + 1]);
        }

        GL11.glEnd();
        postRender();
    }

    public static void drawCircle(float x, float y, float radius, float thickness, RGBA colour, boolean smooth) {
        drawPartialCircle(x, y, radius, 0, 360, thickness, colour, smooth);
    }

    public static void drawPartialCircle(float x, float y, float radius, int startAngle, int endAngle, float thickness, RGBA colour, boolean smooth) {
        preRender();
        if (startAngle > endAngle) {
            int ratio = startAngle;
            startAngle = endAngle;
            endAngle = ratio;
        }

        if (startAngle < 0) {
            startAngle = 0;
        }

        if (endAngle > 360) {
            endAngle = 360;
        }

        if (smooth) {
            GL11.glEnable(2848);
        } else {
            GL11.glDisable(2848);
        }

        GL11.glLineWidth(thickness);
        GL11.glColor4f((float) colour.getRed() / 255.0F, (float) colour.getGreen() / 255.0F, (float) colour.getBlue() / 255.0F, (float) colour.getOpacity() / 255.0F);
        GL11.glBegin(3);
        float var11 = 0.017453292F;

        for (int i = startAngle; i <= endAngle; ++i) {
            float radians = (float) (i - 90) * var11;
            GL11.glVertex2f(x + (float) Math.cos((double) radians) * radius, y + (float) Math.sin((double) radians) * radius);
        }

        GL11.glEnd();
        postRender();
    }

    public static void drawString(String text, int x, int y, RGBA colour) {
        GL11.glColor4f((float) colour.getRed() / 255.0F, (float) colour.getGreen() / 255.0F, (float) colour.getBlue() / 255.0F, (float) colour.getOpacity() / 255.0F);
        Minecraft.getMinecraft().fontRenderer.drawString(text, x, y, 0);
    }

    public static int getTextWidth(String text) {
        return Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
    }

    public static void drawModalRectWithCustomSizedTexture(double screenX, double screenY, double textureU, double textureV,
                                                           float width, float height, float textureWidth, float textureHeight) {
        float f4 = 1.0F / textureWidth;
        float f5 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(screenX, (screenY + height), 0.0D, (textureU * f4), ((textureV + height) * f5));
        tessellator.addVertexWithUV((screenX + width), (screenY + height), 0.0D, ((textureU + width) * f4), ((textureV + height) * f5));
        tessellator.addVertexWithUV((screenX + width), screenY, 0.0D, ((textureU + width) * f4), (textureV * f5));
        tessellator.addVertexWithUV(screenX, screenY, 0.0D, (textureU * f4), (textureV * f5));
        tessellator.draw();
    }

    public static void drawScaledCustomSizeModalRect(double screenX, double screenY, double textureU, double textureV,
                                                     float uWidth, float uHeight,
                                                     float width, float height, float tileWidth, float tileHeight) {
        float f4 = 1.0F / tileWidth;
        float f5 = 1.0F / tileHeight;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(screenX, (screenY + height), 0.0D, (textureU * f4), ((textureV + uHeight) * f5));
        tessellator.addVertexWithUV((screenX + width), (screenY + height), 0.0D, ((textureU + uWidth) * f4), ((textureV + uHeight) * f5));
        tessellator.addVertexWithUV((screenX + width), screenY, 0.0D, ((textureU + uWidth) * f4), (textureV * f5));
        tessellator.addVertexWithUV(screenX, screenY, 0.0D, (textureU * f4), (textureV * f5));
        tessellator.draw();
    }
}
