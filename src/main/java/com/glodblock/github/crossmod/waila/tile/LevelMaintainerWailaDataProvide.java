package com.glodblock.github.crossmod.waila.tile;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.glodblock.github.common.tile.TileLevelMaintainer;
import com.glodblock.github.crossmod.waila.Tooltip;

import appeng.api.storage.data.IAEItemStack;
import appeng.integration.modules.waila.BaseWailaDataProvider;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public class LevelMaintainerWailaDataProvide extends BaseWailaDataProvider {

    @Override
    public List<String> getWailaBody(final ItemStack itemStack, final List<String> currentToolTip,
            final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        final TileEntity te = accessor.getTileEntity();
        if (te instanceof TileLevelMaintainer) {
            te.readFromNBT(accessor.getNBTData());
            for (int i = 0; i < TileLevelMaintainer.REQ_COUNT; i++) {
                IAEItemStack ias = ((TileLevelMaintainer) te).requests.getRequestStacks().getStack(i);
                if (ias != null) {
                    currentToolTip.add(
                            Tooltip.tileLevelMaintainerFormat(
                                    ias.getItemStack().getDisplayName(),
                                    ((TileLevelMaintainer) te).requests.getQuantity(i),
                                    ((TileLevelMaintainer) te).requests.getBatchSize(i),
                                    ((TileLevelMaintainer) te).requests.isEnable(i)));
                }
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
