package com.flansmod.common.driveables.mechas;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.IFlanItem;
import com.flansmod.common.types.InfoType;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.Level;

import java.util.Collections;
import java.util.List;

public class ItemMechaAddon extends Item implements IFlanItem {
    public MechaItemType type;

    public ItemMechaAddon(MechaItemType type1) {
        type = type1;
        setMaxStackSize(1);
        type.item = this;
        setCreativeTab(FlansMod.tabFlanMechas);
        try {
            GameRegistry.registerItem(this, type.shortName, FlansMod.MODID);
        } catch (Throwable e) {
            FlansMod.log(Level.WARN, e.getMessage());
        }
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean b) {
        if (type.description != null) {
            Collections.addAll(list, type.description.split("_"));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack par1ItemStack, int par2) {
        return type.colour;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister icon) {
        itemIcon = icon.registerIcon("FlansMod:" + type.iconPath);
    }

    @Override
    public InfoType getInfoType() {
        return type;
    }
}
