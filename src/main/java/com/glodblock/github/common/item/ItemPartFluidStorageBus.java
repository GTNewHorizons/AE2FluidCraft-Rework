package com.glodblock.github.common.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.parts.PartFluidStorageBus;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.util.NameConst;

import appeng.api.AEApi;
import appeng.api.parts.IPartItem;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPartFluidStorageBus extends FCBaseItem implements IPartItem {

    public ItemPartFluidStorageBus() {
        this.setMaxStackSize(64);
        this.setUnlocalizedName(NameConst.ITEM_PART_FLUID_STORAGE_BUS);
        AEApi.instance().partHelper().setItemBusRenderer(this);
    }

    @Nullable
    @Override
    public PartFluidStorageBus createPartFromItemStack(ItemStack is) {
        return new PartFluidStorageBus(is);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float xOffset, float yOffset, float zOffset) {
        return AEApi.instance().partHelper().placeBus(player.getHeldItem(), x, y, z, side, player, world);
    }

    @Override
    public ItemPartFluidStorageBus register() {
        GameRegistry.registerItem(this, NameConst.ITEM_PART_FLUID_STORAGE_BUS, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @Override
    protected String getIconString() {
        return NameConst.RES_KEY + "fluid_storage_bus";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getSpriteNumber() {
        return 0;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> lines, boolean advanced) {
        super.addInformation(stack, player, lines, advanced);
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("priority")) {
            int priority = stack.getTagCompound().getInteger("priority");
            String priorityText = StatCollector
                    .translateToLocalFormatted("gui.tooltips.appliedenergistics2.PreconfiguredPriority", priority);
            lines.add(EnumChatFormatting.GRAY + priorityText);
        }
    }
}
