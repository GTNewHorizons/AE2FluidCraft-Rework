package com.glodblock.github.crossmod.waila.vanilla;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTankInfo;

import com.glodblock.github.common.tile.TileCertusQuartzTank;
import com.glodblock.github.crossmod.waila.Tooltip;
import com.glodblock.github.util.Util;

import appeng.integration.modules.waila.BaseWailaDataProvider;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public class FluidInvWailaDataProvider extends BaseWailaDataProvider {

    @Override
    public List<String> getWailaBody(final ItemStack itemStack, final List<String> currentToolTip,
            final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        final TileEntity te = accessor.getTileEntity();
        if (te instanceof TileCertusQuartzTank) {
            if (accessor.getNBTData().hasKey("fluidInv")) {
                NBTTagCompound data = (NBTTagCompound) accessor.getNBTData().getTag("fluidInv");
                for (FluidTankInfo info : Util.FluidUtil.fluidTankInfoReadFromNBT(data)) {
                    if (info.fluid == null) continue;
                    currentToolTip
                            .add(Tooltip.fluidFormat(info.fluid.getLocalizedName(), info.fluid.amount, info.capacity));
                }
            }
        }
        return currentToolTip;
    }

    @Override
    public NBTTagCompound getNBTData(final EntityPlayerMP player, final TileEntity te, final NBTTagCompound tag,
            final World world, final int x, final int y, final int z) {
        if (te instanceof TileCertusQuartzTank) {
            NBTTagCompound data = new NBTTagCompound();
            Util.FluidUtil.fluidTankInfoWriteToNBT(((TileCertusQuartzTank) te).getInternalFluid(), data);
            tag.setTag("fluidInv", data);
            te.writeToNBT(tag);
        }
        return tag;
    }
}
