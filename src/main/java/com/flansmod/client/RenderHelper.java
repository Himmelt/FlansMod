package com.flansmod.client;

import net.minecraft.client.renderer.Tessellator;

public final class RenderHelper {
    public static void drawNonStandardTexturedRect(int screenX, int screenY, int textureU, int textureV, int width, int height, int textureWidth, int textureHeight) {
        double f = 1F / (double) textureWidth;
        double f1 = 1F / (double) textureHeight;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(screenX, screenY + height, 0, textureU * f, (textureV + height) * f1);
        tessellator.addVertexWithUV(screenX + width, screenY + height, 0, (textureU + width) * f, (textureV + height) * f1);
        tessellator.addVertexWithUV(screenX + width, screenY, 0, (textureU + width) * f, textureV * f1);
        tessellator.addVertexWithUV(screenX, screenY, 0, textureU * f, textureV * f1);
        tessellator.draw();
    }
}
