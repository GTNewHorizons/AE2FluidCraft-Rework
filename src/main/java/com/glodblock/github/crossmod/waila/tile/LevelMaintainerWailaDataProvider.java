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

    @Override
    public List<String> getWailaBody(final ItemStack itemStack, final List<String> currentToolTip,
            final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        final TileEntity te = accessor.getTileEntity();
        if (te instanceof TileLevelMaintainer tileLevelMaintainer) {
            NBTTagCompound data = accessor.getNBTData();
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
                                        request.getAEItemStack().getItemStack().getDisplayName(),
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
        if (te instanceof TileLevelMaintainer) {
            te.writeToNBT(tag);
        }
        return tag;
    }
}
