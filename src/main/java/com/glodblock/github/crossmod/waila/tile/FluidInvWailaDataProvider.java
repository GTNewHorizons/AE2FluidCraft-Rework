package com.glodblock.github.crossmod.waila.tile;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.tile.TileFluidBuffer;
import com.glodblock.github.common.tile.TileFluidInterface;
import com.glodblock.github.common.tile.TileFluidPacketDecoder;
import com.glodblock.github.crossmod.waila.Tooltip;
import com.glodblock.github.inventory.IAEFluidInventory;
import com.glodblock.github.inventory.IAEFluidTank;

import appeng.integration.modules.waila.BaseWailaDataProvider;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public class FluidInvWailaDataProvider extends BaseWailaDataProvider {

    @Override
    public List<String> getWailaBody(final ItemStack itemStack, final List<String> currentToolTip,
            final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        final TileEntity te = accessor.getTileEntity();
        if (te instanceof TileFluidInterface) {
            ((IAEFluidInventory) te).getInternalFluid().readFromNBT(accessor.getNBTData(), "fluidInv");
            addTooltip(currentToolTip, ((IAEFluidInventory) te).getInternalFluid(), true);
        } else if (te instanceof IAEFluidInventory) {
            ((IAEFluidInventory) te).getInternalFluid().readFromNBT(accessor.getNBTData(), "fluidInv");
            addTooltip(currentToolTip, ((IAEFluidInventory) te).getInternalFluid());
        } else if (te instanceof TileFluidPacketDecoder) {
            te.readFromNBT(accessor.getNBTData());
            IInventory inv = ((TileFluidPacketDecoder) te).getInventory();
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack is = inv.getStackInSlot(i);
                if (is != null && is.getItem() instanceof ItemFluidPacket) {
                    FluidStack fs = ItemFluidPacket.getFluidStack(is);
                    currentToolTip.add(Tooltip.fluidFormat(fs.getLocalizedName(), fs.amount));
                }
            }
        }
        return currentToolTip;
    }

    private void addTooltip(List<String> currentToolTip, IAEFluidTank ft, boolean prefix) {
        for (int i = 0; i < ft.getSlots(); i++) {
            if (ft.getFluidInSlot(i) == null) continue;
            FluidStack fs = ft.getFluidInSlot(i).getFluidStack();
            if (prefix) {
                currentToolTip.add(Tooltip.tileFluidInterfaceFormat(fs.getLocalizedName(), fs.amount, i));
            } else {
                currentToolTip.add(Tooltip.fluidFormat(fs.getLocalizedName(), fs.amount));
            }
        }
    }

    private void addTooltip(List<String> currentToolTip, IAEFluidTank ft) {
        this.addTooltip(currentToolTip, ft, false);
    }

    @Override
    public NBTTagCompound getNBTData(final EntityPlayerMP player, final TileEntity te, final NBTTagCompound tag,
            final World world, final int x, final int y, final int z) {
        if (te instanceof TileFluidBuffer) {
            ((TileFluidBuffer) te).updateFluidStore();
        }
        if (te instanceof IAEFluidInventory) {
            ((IAEFluidInventory) te).getInternalFluid().writeToNBT(tag, "fluidInv");
        } else if (te instanceof TileFluidPacketDecoder) {
            te.writeToNBT(tag);
        }
        return tag;
    }
}
