package com.glodblock.github.common.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.textures.FCPartsTexture;
import com.glodblock.github.common.parts.PartFluidLevelEmitter;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.util.NameConst;

import appeng.api.AEApi;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemFluidLevelEmitter extends FCBaseItem implements IPartItem {

    public ItemFluidLevelEmitter() {
        this.setMaxStackSize(64);
        this.setUnlocalizedName(NameConst.ITEM_PART_FLUID_LEVEL_EMITTER);
        AEApi.instance().partHelper().setItemBusRenderer(this);
    }

    @Nullable
    @Override
    public IPart createPartFromItemStack(ItemStack is) {
        return new PartFluidLevelEmitter(is);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float xOffset, float yOffset, float zOffset) {
        return AEApi.instance().partHelper().placeBus(player.getHeldItem(), x, y, z, side, player, world);
    }

    @Override
    public ItemFluidLevelEmitter register() {
        GameRegistry.registerItem(this, NameConst.ITEM_PART_FLUID_LEVEL_EMITTER, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @Override
    public IIcon getIconIndex(ItemStack is) {
        return FCPartsTexture.PartFluidLevelEmitter.getIcon();
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
