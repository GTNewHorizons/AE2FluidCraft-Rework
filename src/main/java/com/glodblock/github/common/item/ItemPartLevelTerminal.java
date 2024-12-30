package com.glodblock.github.common.item;

import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.parts.PartLevelTerminal;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.util.NameConst;

import appeng.api.AEApi;
import appeng.api.parts.IPartItem;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPartLevelTerminal extends FCBaseItem implements IPartItem {

    public ItemPartLevelTerminal() {
        this.setMaxStackSize(64);
        this.setUnlocalizedName(NameConst.ITEM_PART_LEVEL_TERMINAL);
        AEApi.instance().partHelper().setItemBusRenderer(this);
    }

    @Nullable
    @Override
    public PartLevelTerminal createPartFromItemStack(ItemStack is) {
        return new PartLevelTerminal(is);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float xOffset, float yOffset, float zOffset) {
        return AEApi.instance().partHelper().placeBus(player.getHeldItem(), x, y, z, side, player, world);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> toolTip, boolean flag) {
        if (isShiftKeyDown()) {
            toolTip.add(NameConst.i18n(NameConst.TT_LEVEL_TERMINAL));
        } else {
            toolTip.add(NameConst.i18n(NameConst.TT_SHIFT_FOR_MORE));
        }
    }

    @Override
    public ItemPartLevelTerminal register() {
        GameRegistry.registerItem(this, NameConst.ITEM_PART_LEVEL_TERMINAL, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @Override
    public void registerIcons(IIconRegister _iconRegister) {}

    @Override
    @SideOnly(Side.CLIENT)
    public int getSpriteNumber() {
        return 0;
    }
}
