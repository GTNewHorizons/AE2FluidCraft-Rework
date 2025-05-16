package com.glodblock.github.crossmod.waila.tile;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.glodblock.github.common.tile.TileLevelMaintainer;
import com.glodblock.github.crossmod.waila.Tooltip;

import appeng.integration.modules.waila.BaseWailaDataProvider;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public class LevelMaintainerWailaDataProvider extends BaseWailaDataProvider {

    @Override
    public List<String> getWailaBody(final ItemStack itemStack, final List<String> currentToolTip,
            final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        final TileEntity te = accessor.getTileEntity();
        if (te instanceof TileLevelMaintainer tileLevelMaintainer) {
            te.readFromNBT(accessor.getNBTData());
            for (int i = 0; i < TileLevelMaintainer.REQ_COUNT; i++) {
                TileLevelMaintainer.RequestInfo request = tileLevelMaintainer.requests[i];
                if (request == null) continue;
                currentToolTip.add(
                        Tooltip.tileLevelMaintainerFormat(
                                request.getAEItemStack().getItemStack().getDisplayName(),
                                request.getQuantity(),
                                request.getBatchSize(),
                                request.isEnable()));

            }
        }
        return currentToolTip;
    }

    @Override
    public NBTTagCompound getNBTData(final EntityPlayerMP player, final TileEntity te, final NBTTagCompound tag,
            final World world, final int x, final int y, final int z) {
        if (te instanceof TileLevelMaintainer) {
            te.writeToNBT(tag);
        }
        return tag;
    }
}
