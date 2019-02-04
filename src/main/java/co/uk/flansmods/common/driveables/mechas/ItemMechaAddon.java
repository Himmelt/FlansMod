package co.uk.flansmods.common.driveables.mechas;

import co.uk.flansmods.common.FlansMod;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ItemMechaAddon extends Item {
    public ItemMechaAddon(int i, MechaItemType type1) {
        super(i);
        type = type1;
        setMaxStackSize(1);
        type.item = this;
        setCreativeTab(FlansMod.tabFlanMechas);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean b) {
        if (type.description != null) {
            for (String s : type.description.split("_"))
                list.add(s);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack par1ItemStack, int par2) {
        return type.colour;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister icon) {
        itemIcon = icon.registerIcon("FlansMod:" + type.iconPath);
    }

    public MechaItemType type;
}
