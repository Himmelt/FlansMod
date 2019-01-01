package com.flansmod.client;

import net.minecraft.client.renderer.Tessellator;

public final class RenderHelper {
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
