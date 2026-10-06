package com.glodblock.github.crossmod.waila.tile;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import com.glodblock.github.common.tile.TileLevelMaintainer;
import com.glodblock.github.crossmod.waila.Tooltip;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.ITickManager;
import appeng.integration.modules.waila.BaseWailaDataProvider;
import appeng.me.GridAccessException;
import appeng.me.cache.TickManagerCache;
import appeng.me.cache.helpers.TickTracker;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public class LevelMaintainerWailaDataProvider extends BaseWailaDataProvider {

    /** World-time deadline derived from AE2's actual tracker, rather than the configured idle maximum. */
    private static final String NBT_NEXT = "ae2fc_next_stock_check";
    private static final Field AWAKE_TRACKERS = findAwakeTrackers();

    @Override
    public List<String> getWailaBody(final ItemStack itemStack, final List<String> currentToolTip,
            final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        final TileEntity te = accessor.getTileEntity();
        if (te instanceof TileLevelMaintainer tileLevelMaintainer) {
            NBTTagCompound data = accessor.getNBTData();
            if (data.hasKey(NBT_NEXT) && accessor.getWorld() != null) {
                final long ticksLeft = Math.max(0L, data.getLong(NBT_NEXT) - accessor.getWorld().getTotalWorldTime());
                currentToolTip.add(Tooltip.tileLevelMaintainerRateFormat(ticksLeft));
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

    @Override
    public NBTTagCompound getNBTData(final EntityPlayerMP player, final TileEntity te, final NBTTagCompound tag,
            final World world, final int x, final int y, final int z) {
        if (te instanceof TileLevelMaintainer tile) {
            tile.writeToNBT(tag);
            // Waila can reuse the tag. Remove any old deadline when the node no longer has a schedule.
            tag.removeTag(NBT_NEXT);
            try {
                final long ticksLeft = ticksUntilNextCheck(tile.getProxy().getTick(), tile.getProxy().getNode());
                if (ticksLeft >= 0L) {
                    tag.setLong(NBT_NEXT, world.getTotalWorldTime() + ticksLeft);
                }
            } catch (GridAccessException ignored) {}
        }
        return tag;
    }

    private static Field findAwakeTrackers() {
        try {
            // ITickManager exposes no schedule query. Keep the AE2 implementation access here and fail quietly if
            // a different version does not expose the expected field.
            final Field field = TickManagerCache.class.getDeclaredField("awake");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException | SecurityException ignored) {
            return null;
        }
    }

    /** Remaining grid ticks, or -1 when no reliable schedule is available. */
    private static long ticksUntilNextCheck(ITickManager tickManager, IGridNode node) {
        if (AWAKE_TRACKERS == null || node == null || !(tickManager instanceof TickManagerCache manager)) return -1L;
        try {
            final Object trackers = AWAKE_TRACKERS.get(manager);
            if (!(trackers instanceof Map<?, ?>awake)) return -1L;
            final Object entry = awake.get(node);
            if (!(entry instanceof TickTracker tracker)) return -1L;
            return Math.max(0L, tracker.getLastTick() + tracker.getCurrentRate() - manager.getCurrentTick());
        } catch (IllegalAccessException ignored) {
            return -1L;
        }
    }

}
