package com.glodblock.github.crossmod.waila;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import com.glodblock.github.crossmod.waila.part.FluidInvWailaDataProvider;
import com.glodblock.github.crossmod.waila.part.FluidMonitorWailaDataProvider;
import com.glodblock.github.crossmod.waila.part.SpeedWailaDataProvider;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import appeng.api.parts.IPart;
import appeng.integration.modules.waila.part.IPartWailaDataProvider;
import appeng.integration.modules.waila.part.PartAccessor;
import appeng.integration.modules.waila.part.Tracer;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;

public class PartWailaDataProvider implements IWailaDataProvider {

    private final List<IPartWailaDataProvider> providers;
    private final PartAccessor accessor = new PartAccessor();
    private final Tracer tracer = new Tracer();

    public PartWailaDataProvider() {
        final IPartWailaDataProvider speed = new SpeedWailaDataProvider();
        final IPartWailaDataProvider fluidInv = new FluidInvWailaDataProvider();
        final IPartWailaDataProvider fluidMonitor = new FluidMonitorWailaDataProvider();
        this.providers = Lists.newArrayList(speed, fluidInv, fluidMonitor);
    }

    @Override
    public ItemStack getWailaStack(final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        final TileEntity te = accessor.getTileEntity();
        final MovingObjectPosition mop = accessor.getPosition();

        final Optional<IPart> maybePart = this.accessor.getMaybePart(te, mop);

        if (maybePart.isPresent()) {
            final IPart part = maybePart.get();

            ItemStack wailaStack = null;

            for (final IPartWailaDataProvider provider : this.providers) {
                wailaStack = provider.getWailaStack(part, config, wailaStack);
                if (wailaStack != null) break;
            }
            return wailaStack;
        }

        return null;
    }

    @Override
    public List<String> getWailaHead(final ItemStack itemStack, final List<String> currentToolTip,
            final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        final TileEntity te = accessor.getTileEntity();
        final MovingObjectPosition mop = accessor.getPosition();

        final Optional<IPart> maybePart = this.accessor.getMaybePart(te, mop);

        if (maybePart.isPresent()) {
            final IPart part = maybePart.get();

            for (final IPartWailaDataProvider provider : this.providers) {
                provider.getWailaHead(part, currentToolTip, accessor, config);
            }
        }

        return currentToolTip;
    }

    @Override
    public List<String> getWailaBody(final ItemStack itemStack, final List<String> currentToolTip,
            final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        final TileEntity te = accessor.getTileEntity();
        final MovingObjectPosition mop = accessor.getPosition();

        final Optional<IPart> maybePart = this.accessor.getMaybePart(te, mop);

        if (maybePart.isPresent()) {
            final IPart part = maybePart.get();

            for (final IPartWailaDataProvider provider : this.providers) {
                provider.getWailaBody(part, currentToolTip, accessor, config);
            }
        }

        return currentToolTip;
    }

    @Override
    public List<String> getWailaTail(final ItemStack itemStack, final List<String> currentToolTip,
            final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        final TileEntity te = accessor.getTileEntity();
        final MovingObjectPosition mop = accessor.getPosition();

        final Optional<IPart> maybePart = this.accessor.getMaybePart(te, mop);

        if (maybePart.isPresent()) {
            final IPart part = maybePart.get();

            for (final IPartWailaDataProvider provider : this.providers) {
                provider.getWailaTail(part, currentToolTip, accessor, config);
            }
        }

        return currentToolTip;
    }

    @Override
    public NBTTagCompound getNBTData(final EntityPlayerMP player, final TileEntity te, final NBTTagCompound tag,
            final World world, final int x, final int y, final int z) {
        final MovingObjectPosition mop = this.tracer.retraceBlock(world, player, x, y, z);

        if (mop != null) {
            final Optional<IPart> maybePart = this.accessor.getMaybePart(te, mop);

            if (maybePart.isPresent()) {
                final IPart part = maybePart.get();

                for (final IPartWailaDataProvider provider : this.providers) {
                    provider.getNBTData(player, part, te, tag, world, x, y, z);
                }
            }
        }

        return tag;
    }
}
