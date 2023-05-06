package com.glodblock.github.crossmod.waila.part;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.crossmod.waila.Tooltip;
import com.glodblock.github.inventory.AEFluidInventory;
import com.glodblock.github.inventory.IAEFluidInventory;

import appeng.api.parts.IPart;
import appeng.integration.modules.waila.part.BasePartWailaDataProvider;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public class FluidInvWailaDataProvider extends BasePartWailaDataProvider {

    @Override
    public List<String> getWailaBody(final IPart part, final List<String> currentToolTip,
            final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        if (part instanceof IAEFluidInventory) {
            ((IAEFluidInventory) part).getInternalFluid().readFromNBT(accessor.getNBTData(), "fluidInv");
            AEFluidInventory fluidInventory = ((IAEFluidInventory) part).getInternalFluid();
            for (int i = 0; i < fluidInventory.getSlots(); i++) {
                FluidStack fs = fluidInventory.getFluidStackInSlot(i);
                if (fs == null) continue;
                currentToolTip.add(Tooltip.fluidFormat(fs.getFluid().getLocalizedName(), fs.amount));
            }
        }
        return currentToolTip;
    }

    @Override
    public NBTTagCompound getNBTData(final EntityPlayerMP player, final IPart part, final TileEntity te,
            final NBTTagCompound tag, final World world, final int x, final int y, final int z) {
        if (part instanceof IAEFluidInventory) {
            ((IAEFluidInventory) part).getInternalFluid().writeToNBT(tag, "fluidInv");
        }
        return tag;
    }
}
