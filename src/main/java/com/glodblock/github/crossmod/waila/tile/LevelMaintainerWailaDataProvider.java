package com.glodblock.github.crossmod.waila.tile;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import com.glodblock.github.common.tile.TileLevelMaintainer;
import com.glodblock.github.crossmod.waila.Tooltip;

import appeng.integration.modules.waila.BaseWailaDataProvider;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public class LevelMaintainerWailaDataProvider extends BaseWailaDataProvider {

    /** Refresh interval in effect, in ticks. Sent even when the block follows the config default. */
    private static final String NBT_RATE = "ae2fc_rate";
    /** World time the next check is due at, or 0 before the tile has been ticked. */
    private static final String NBT_NEXT = "ae2fc_next";

    @Override
    public List<String> getWailaBody(final ItemStack itemStack, final List<String> currentToolTip,
            final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        final TileEntity te = accessor.getTileEntity();
        if (te instanceof TileLevelMaintainer tileLevelMaintainer) {
            NBTTagCompound data = accessor.getNBTData();
            final long due = data.getLong(NBT_NEXT);
            // Without a deadline the tile has not been ticked yet, and "next in 0s" would be a lie.
            if (due > 0) {
                currentToolTip.add(
                        Tooltip.tileLevelMaintainerRateFormat(
                                ticksUntilNextCheck(due, accessor),
                                data.getInteger(NBT_RATE)));
            }
            if (data.hasKey(TileLevelMaintainer.NBT_REQUESTS)) {
                NBTTagList tagList = data.getTagList(TileLevelMaintainer.NBT_REQUESTS, Constants.NBT.TAG_COMPOUND);
                for (int i = 0; i < tagList.tagCount(); i++) {
                    NBTTagCompound tag = tagList.getCompoundTagAt(i);
                    if (tag == null || !tag.hasKey(TileLevelMaintainer.NBT_STACK)) continue;

                    try {
                        TileLevelMaintainer.RequestInfo request = new TileLevelMaintainer.RequestInfo(
                                tag,
                                tileLevelMaintainer);
                        currentToolTip.add(
                                Tooltip.tileLevelMaintainerFormat(
                                        request.getAEStack().getDisplayName(),
                                        request.getQuantity(),
                                        request.getBatchSize(),
                                        request.isEnable()));
                    } catch (Exception ignored) {}
                }
            }
        }
        return currentToolTip;
    }

    /** Ticks left until the next check, counted down client side against the deadline the server sent. */
    private static long ticksUntilNextCheck(final long due, final IWailaDataAccessor accessor) {
        final World world = accessor.getWorld();
        if (world == null) return 0L;
        return Math.max(0L, due - world.getTotalWorldTime());
    }

    @Override
    public NBTTagCompound getNBTData(final EntityPlayerMP player, final TileEntity te, final NBTTagCompound tag,
            final World world, final int x, final int y, final int z) {
        if (te instanceof TileLevelMaintainer tile) {
            tile.writeToNBT(tag);
            // The tile tag only carries the refresh rate when the block overrides the config, so always send the
            // effective
            // one along with the world time the next check is due at.
            tag.setInteger(NBT_RATE, tile.getRefreshTicks());
            tag.setLong(NBT_NEXT, tile.getNextCheckTick());
        }
        return tag;
    }
}
