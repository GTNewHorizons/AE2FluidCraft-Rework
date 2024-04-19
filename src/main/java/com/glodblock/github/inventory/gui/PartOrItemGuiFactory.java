package com.glodblock.github.inventory.gui;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.glodblock.github.inventory.item.IItemInventory;
import com.glodblock.github.util.Util;

import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;

public abstract class PartOrItemGuiFactory<T> extends PartGuiFactory<T> {

    PartOrItemGuiFactory(Class<T> invClass) {
        super(invClass);
    }

    @Nullable
    protected T getItemInventory(Object inv) {
        return invClass.isInstance(inv) ? invClass.cast(inv) : null;
    }

    @Nullable
    @Override
    public Object createServerGui(EntityPlayer player, World world, int x, int y, int z, ForgeDirection face) {
        ImmutablePair<Util.GuiHelper.GuiType, Integer> result = Util.GuiHelper.decodeType(y);
        if (result.getLeft() == Util.GuiHelper.GuiType.ITEM) {
            ItemStack item = Util.Wireless.getWirelessTerminal(player, x);
            if (item == null || !(item.getItem() instanceof IItemInventory)) {
                return null;
            }
            T inv = getItemInventory(
                    ((IItemInventory) item.getItem()).getInventory(item, world, x, result.getRight(), z, player));
            if (inv == null) {
                return null;
            }
            Object gui = createServerGui(player, inv);
            if (gui instanceof AEBaseContainer) {
                ContainerOpenContext ctx = new ContainerOpenContext(inv);
                ctx.setWorld(world);
                ctx.setX(x);
                ctx.setY(y);
                ctx.setZ(z);
                ctx.setSide(face);
                ((AEBaseContainer) gui).setOpenContext(ctx);
            }
            return gui;
        } else {
            return super.createServerGui(player, world, x, result.getRight(), z, face);
        }

    }

    @Nullable
    @Override
    public Object createClientGui(EntityPlayer player, World world, int x, int y, int z, ForgeDirection face) {
        ImmutablePair<Util.GuiHelper.GuiType, Integer> result = Util.GuiHelper.decodeType(y);
        if (result.left == Util.GuiHelper.GuiType.ITEM) {
            ItemStack item = Util.Wireless.getWirelessTerminal(player, x);
            if (item == null || !(item.getItem() instanceof IItemInventory)) {
                return null;
            }
            T inv = getItemInventory(
                    ((IItemInventory) item.getItem()).getInventory(item, world, x, result.getRight(), z, player));
            return inv != null ? createClientGui(player, inv) : null;
        } else {
            return super.createClientGui(player, world, x, result.getRight(), z, face);
        }

    }

}
