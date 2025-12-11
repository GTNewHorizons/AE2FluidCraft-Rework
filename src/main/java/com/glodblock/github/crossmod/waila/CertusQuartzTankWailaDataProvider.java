package com.glodblock.github.crossmod.waila;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidTankInfo;

import com.glodblock.github.common.tile.TileCertusQuartzTank;

import appeng.integration.modules.waila.BaseWailaDataProvider;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public class CertusQuartzTankWailaDataProvider extends BaseWailaDataProvider {

    @Override
    public List<String> getWailaBody(final ItemStack itemStack, final List<String> currentToolTip,
            final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        TileEntity tileEntity = accessor.getTileEntity();
        if (tileEntity instanceof TileCertusQuartzTank quartzTank) {
            FluidTankInfo info = quartzTank.getTankInfo(true)[0];
            if (info.fluid == null) {
                currentToolTip.add(Tooltip.tankEmptyFormat(info.capacity));
            } else {
                currentToolTip.add(
                        Tooltip.tankWithFluidFormat(info.fluid.getLocalizedName(), info.fluid.amount, info.capacity));
            }
        }
        return currentToolTip;
    }
}
