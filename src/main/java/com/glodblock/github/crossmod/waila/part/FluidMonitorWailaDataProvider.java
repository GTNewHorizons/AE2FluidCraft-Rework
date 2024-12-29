package com.glodblock.github.crossmod.waila.part;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.glodblock.github.common.parts.base.FCPartMonitor;
import com.glodblock.github.crossmod.waila.Tooltip;
import com.glodblock.github.util.Util;

import appeng.api.parts.IPart;
import appeng.api.storage.data.IAEFluidStack;
import appeng.integration.modules.waila.part.BasePartWailaDataProvider;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public class FluidMonitorWailaDataProvider extends BasePartWailaDataProvider {

    private final String key = "fluidStore";

    @Override
    public List<String> getWailaBody(final IPart part, final List<String> currentToolTip,
            final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        if (part instanceof FCPartMonitor && accessor.getNBTData().hasKey(key)) {
            IAEFluidStack iaeFluidStack = Util.loadFluidStackFromNBT(accessor.getNBTData().getCompoundTag(key));
            if (iaeFluidStack != null) currentToolTip.add(
                    Tooltip.fluidFormat(
                            iaeFluidStack.getFluidStack().getLocalizedName(),
                            iaeFluidStack.getStackSize()));
        }
        return currentToolTip;
    }

    @Override
    public NBTTagCompound getNBTData(final EntityPlayerMP player, final IPart part, final TileEntity te,
            final NBTTagCompound tag, final World world, final int x, final int y, final int z) {
        if (part instanceof FCPartMonitor && ((FCPartMonitor) part).getAEStoreFluidStack() != null) {
            NBTTagCompound data = new NBTTagCompound();
            ((FCPartMonitor) part).getAEStoreFluidStack().writeToNBT(data);
            tag.setTag(key, data);
        }
        return tag;
    }
}
