package co.uk.flansmods.client;

import co.uk.flansmods.common.InfoType;
import co.uk.flansmods.common.guns.GunType;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;

public class FlansModResourceHandler {
    private static HashMap<InfoType, ResourceLocation> iconMap = new HashMap<InfoType, ResourceLocation>();
    private static HashMap<InfoType, ResourceLocation> textureMap = new HashMap<InfoType, ResourceLocation>();
    private static HashMap<String, ResourceLocation> scopeMap = new HashMap<String, ResourceLocation>();
    private static HashMap<String, ResourceLocation> soundMap = new HashMap<String, ResourceLocation>();

    public static ResourceLocation getIcon(InfoType infoType) {
        if (iconMap.containsKey(infoType)) {
            return iconMap.get(infoType);
        }
        ResourceLocation resLoc = new ResourceLocation("flansmod", "textures/items/" + infoType.iconPath + ".png");
        iconMap.put(infoType, resLoc);
        return resLoc;
    }

    public static ResourceLocation getTexture(InfoType infoType) {
        if (textureMap.containsKey(infoType)) {
            return textureMap.get(infoType);
        }
        ResourceLocation resLoc = new ResourceLocation("flansmod", "skins/" + infoType.texture + ".png");
        textureMap.put(infoType, resLoc);
        return resLoc;
    }

    public static ResourceLocation getDeployableTexture(GunType gunType) {
        if (textureMap.containsKey(gunType)) {
            return textureMap.get(gunType);
        }
        ResourceLocation resLoc = new ResourceLocation("flansmod", "skins/" + gunType.deployableTexture + ".png");
        textureMap.put(gunType, resLoc);
        return resLoc;
    }

    public static ResourceLocation getScope(String scopeTexture) {
        if (scopeMap.containsKey(scopeTexture)) {
            return scopeMap.get(scopeTexture);
        }
        ResourceLocation resLoc = new ResourceLocation("flansmod", "gui/" + scopeTexture + ".png");
        scopeMap.put(scopeTexture, resLoc);
        return resLoc;
    }

    public static void getSound(String contentPack, String type, String sound) {
		/*if(soundMap.containsKey(contentPack + "." + sound))
		{
			return soundMap.get(contentPack + "." + sound);
		}
		ResourceLocation resLoc = new ResourceLocation("flansmod", "sounds/" + sound + ".wav");
		soundMap.put(contentPack + "." + sound, resLoc);*/
        FMLClientHandler.instance().getClient().sndManager.addSound("flansmod:" + sound + ".ogg");
    }
}
