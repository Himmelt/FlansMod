package co.uk.flansmods.common.guns;

import co.uk.flansmods.common.FlansMod;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ItemAttachment extends Item {
    public AttachmentType type;

    public ItemAttachment(int id, AttachmentType t) {
        super(id);
        type = t;
        type.item = this;
        maxStackSize = t.maxStackSize;
        setCreativeTab(FlansMod.tabFlanGuns);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getColorFromItemStack(ItemStack par1ItemStack, int par2) {
        return type.colour;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister icon) {
        itemIcon = icon.registerIcon("FlansMod:" + type.iconPath);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean b) {
        if (type.description != null) {
            for (String s : type.description.split("_"))
                lines.add(s);
        }
    }
}
