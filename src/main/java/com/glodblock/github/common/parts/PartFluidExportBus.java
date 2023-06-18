package com.glodblock.github.common.parts;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.IFluidHandler;

import com.glodblock.github.client.textures.FCPartsTexture;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.parts.base.FCSharedFluidBus;
import com.glodblock.github.inventory.FluidConvertingInventoryAdaptor;
import com.google.common.collect.ImmutableSet;

import appeng.api.config.*;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.texture.CableBusTextures;
import appeng.core.AELog;
import appeng.helpers.MultiCraftingTracker;
import appeng.me.GridAccessException;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartFluidExportBus extends FCSharedFluidBus implements ICraftingRequester {

    private final BaseActionSource source;
    private final MultiCraftingTracker craftingTracker = new MultiCraftingTracker(this, 9);
    private int nextSlot = 0;
    private long fluidToSend = 1000;
    private boolean didSomething;

    public PartFluidExportBus(ItemStack is) {
        super(is);
        this.getConfigManager().registerSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        this.getConfigManager().registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.getConfigManager().registerSetting(Settings.CRAFT_ONLY, YesNo.NO);
        this.getConfigManager().registerSetting(Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT);
        this.source = new MachineSource(this);
    }

    @Override
    public IIcon getFaceIcon() {
        return FCPartsTexture.PartFluidExportBus.getIcon();
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(5, 40, this.isSleeping(), false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        return this.canDoBusWork() ? this.doBusWork() : TickRateModulation.IDLE;
    }

    @Override
    protected boolean canDoBusWork() {
        return this.getProxy().isActive();
    }

    private int getStartingSlot(final SchedulingMode schedulingMode, final int x) {
        if (schedulingMode == SchedulingMode.RANDOM) {
            return Platform.getRandom().nextInt(this.availableSlots());
        }

        if (schedulingMode == SchedulingMode.ROUNDROBIN) {
            return (this.nextSlot + x) % this.availableSlots();
        }

        return x;
    }

    private int availableSlots() {
        return Math.min(1 + this.getInstalledUpgrades(Upgrades.CAPACITY) * 4, this.getInv().getSizeInventory());
    }

    private IInventory getInv() {
        return this.getInventoryByName("config");
    }

    @Override
    protected TickRateModulation doBusWork() {
        if (!this.canDoBusWork()) {
            return TickRateModulation.IDLE;
        }
        this.didSomething = false;
        this.fluidToSend = this.calculateAmountToSend();
        final TileEntity te = this.getConnectedTE();

        if (te instanceof IFluidHandler) {
            try {
                final InventoryAdaptor destination = this.getHandler(te);
                final ICraftingGrid cg = this.getProxy().getCrafting();
                final IFluidHandler fh = (IFluidHandler) te;
                final IMEMonitor<IAEFluidStack> inv = this.getProxy().getStorage().getFluidInventory();
                int i;
                for (i = 0; i < this.availableSlots() && this.fluidToSend > 0; i++) {
                    final int slotToExport = this.getStartingSlot(this.getSchedulingMode(), i);
                    IAEFluidStack fluid = AEFluidStack
                            .create(ItemFluidPacket.getFluidStack(getInv().getStackInSlot(slotToExport)));
                    if (fluid != null) {
                        fluid.setStackSize(this.calculateAmountToSend());
                        if (this.craftOnly()) {
                            this.didSomething = this.craftingTracker.handleCrafting(
                                    i,
                                    fluid.getStackSize(),
                                    ItemFluidDrop.newAeStack(fluid),
                                    destination,
                                    this.getTile().getWorldObj(),
                                    this.getProxy().getGrid(),
                                    cg,
                                    this.source);
                            continue;
                        }
                        // Find out how much to push
                        final IAEFluidStack toPush = fluid.copy();
                        final int amtToInsert = fh.fill(this.getSide().getOpposite(), toPush.getFluidStack(), false);
                        toPush.setStackSize(amtToInsert);
                        // Extract from the ME system. This might not be the same amount we actually send...
                        final IAEFluidStack real = inv.extractItems(toPush, Actionable.MODULATE, this.source);
                        if (real != null) {
                            int realInserted = fh.fill(this.getSide().getOpposite(), real.getFluidStack(), true);
                            if (realInserted < real.getStackSize()) {
                                // Could not use the entirety of the amount we extracted, so put it back.
                                // This can result in voiding fluids if there's nowhere for the fluids to go.
                                fluid.setStackSize(real.getStackSize() - realInserted);
                                inv.injectItems(fluid, Actionable.MODULATE, this.source);
                            }
                            this.fluidToSend -= realInserted;
                            didSomething = true;
                        } else if (this.isCraftingEnabled()) {
                            // If we didn't send anything, try crafting it if we can.
                            this.didSomething = this.craftingTracker.handleCrafting(
                                    i,
                                    fluid.getStackSize(),
                                    ItemFluidDrop.newAeStack(fluid),
                                    destination,
                                    this.getTile().getWorldObj(),
                                    this.getProxy().getGrid(),
                                    cg,
                                    this.source);
                        }
                    }
                }
                this.updateSchedulingMode(this.getSchedulingMode(), i);
                return didSomething ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
            } catch (GridAccessException ignored) {}
        }

        return TickRateModulation.SLEEP;
    }

    private void updateSchedulingMode(final SchedulingMode schedulingMode, final int x) {
        if (schedulingMode == SchedulingMode.ROUNDROBIN) {
            this.nextSlot = (this.nextSlot + x) % this.availableSlots();
        }
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(4, 4, 12, 12, 12, 14);
        bch.addBox(5, 5, 14, 11, 11, 15);
        bch.addBox(6, 6, 15, 10, 10, 16);
        bch.addBox(6, 6, 11, 10, 10, 12);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventory(final IPartRenderHelper rh, final RenderBlocks renderer) {
        rh.setTexture(
                CableBusTextures.PartExportSides.getIcon(),
                CableBusTextures.PartExportSides.getIcon(),
                CableBusTextures.PartMonitorBack.getIcon(),
                this.getFaceIcon(),
                CableBusTextures.PartExportSides.getIcon(),
                CableBusTextures.PartExportSides.getIcon());

        rh.setBounds(4, 4, 12, 12, 12, 14);
        rh.renderInventoryBox(renderer);

        rh.setBounds(5, 5, 14, 11, 11, 15);
        rh.renderInventoryBox(renderer);

        rh.setBounds(6, 6, 15, 10, 10, 16);
        rh.renderInventoryBox(renderer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderStatic(final int x, final int y, final int z, final IPartRenderHelper rh,
            final RenderBlocks renderer) {
        this.setRenderCache(rh.useSimplifiedRendering(x, y, z, this, this.getRenderCache()));
        rh.setTexture(
                CableBusTextures.PartExportSides.getIcon(),
                CableBusTextures.PartExportSides.getIcon(),
                CableBusTextures.PartMonitorBack.getIcon(),
                this.getFaceIcon(),
                CableBusTextures.PartExportSides.getIcon(),
                CableBusTextures.PartExportSides.getIcon());

        rh.setBounds(4, 4, 12, 12, 12, 14);
        rh.renderBlock(x, y, z, renderer);

        rh.setBounds(5, 5, 14, 11, 11, 15);
        rh.renderBlock(x, y, z, renderer);

        rh.setBounds(6, 6, 15, 10, 10, 16);
        rh.renderBlock(x, y, z, renderer);

        rh.setTexture(
                CableBusTextures.PartMonitorSidesStatus.getIcon(),
                CableBusTextures.PartMonitorSidesStatus.getIcon(),
                CableBusTextures.PartMonitorBack.getIcon(),
                this.getFaceIcon(),
                CableBusTextures.PartMonitorSidesStatus.getIcon(),
                CableBusTextures.PartMonitorSidesStatus.getIcon());

        rh.setBounds(6, 6, 11, 10, 10, 12);
        rh.renderBlock(x, y, z, renderer);

        this.renderLights(x, y, z, rh, renderer);
    }

    @Override
    public RedstoneMode getRSMode() {
        return (RedstoneMode) this.getConfigManager().getSetting(Settings.REDSTONE_CONTROLLED);
    }

    public SchedulingMode getSchedulingMode() {
        return (SchedulingMode) this.getConfigManager().getSetting(Settings.SCHEDULING_MODE);
    }

    private boolean craftOnly() {
        return this.getConfigManager().getSetting(Settings.CRAFT_ONLY) == YesNo.YES;
    }

    private boolean isCraftingEnabled() {
        return this.getInstalledUpgrades(Upgrades.CRAFTING) > 0;
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return this.craftingTracker.getRequestedJobs();
    }

    protected InventoryAdaptor getHandler(TileEntity target) {
        return target != null ? FluidConvertingInventoryAdaptor.wrap(target, this.getSide().getOpposite()) : null;
    }

    @Override
    public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode) {
        final InventoryAdaptor d = this.getHandler(getConnectedTE());

        try {
            if (d != null && this.getProxy().isActive()) {
                final IEnergyGrid energy = this.getProxy().getEnergy();
                final double power = Math.ceil(items.getStackSize() / 1000D);

                if (energy.extractAEPower(power, mode, PowerMultiplier.CONFIG) > power - 0.01) {
                    ItemStack inputStack = items.getItemStack();

                    ItemStack remaining;

                    if (mode == Actionable.SIMULATE) {
                        remaining = d.simulateAdd(inputStack);
                    } else {
                        remaining = d.addItems(inputStack);
                    }

                    return AEItemStack.create(remaining);
                }
            }
        } catch (final GridAccessException e) {
            AELog.debug(e);
        }

        return items;
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        this.craftingTracker.jobStateChange(link);
    }
}
