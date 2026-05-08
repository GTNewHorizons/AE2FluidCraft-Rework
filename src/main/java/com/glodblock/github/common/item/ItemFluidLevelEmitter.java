package com.glodblock.github.common.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.util.NameConst;

import appeng.api.AEApi;
import appeng.client.texture.TextureUtils;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemFluidLevelEmitter extends FCBaseItem {

    public ItemFluidLevelEmitter() {
        this.setMaxStackSize(64);
        this.setUnlocalizedName(NameConst.ITEM_PART_FLUID_LEVEL_EMITTER);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float xOffset, float yOffset, float zOffset) {
        return AEApi.instance().partHelper().placeBus(stack, x, y, z, side, player, world);
    }

    @Override
    public ItemFluidLevelEmitter register() {
        GameRegistry.registerItem(this, NameConst.ITEM_PART_FLUID_LEVEL_EMITTER, FluidCraft.MODID);
        return this;
    }

    @Override
    public IIcon getIconIndex(ItemStack is) {
        return TextureUtils.getMissingBlock();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getSpriteNumber() {
        return 0;
    }

    @Override
    public void addInformation(final ItemStack stack, final EntityPlayer player, final List<String> lines,
            final boolean displayMoreInfo) {
        lines.add("§4Deprecated, use regular level emitter, hold CTRL for get fluid of container.");
        super.addInformation(stack, player, lines, displayMoreInfo);
    }
}
