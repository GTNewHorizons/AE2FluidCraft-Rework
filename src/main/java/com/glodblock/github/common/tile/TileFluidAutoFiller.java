package com.glodblock.github.common.tile;

import static com.glodblock.github.loader.RecipeLoader.BUCKET;

import javax.annotation.Nonnull;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.lang3.tuple.MutablePair;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.util.Util;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.helpers.Reflected;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;

public class TileFluidAutoFiller extends AENetworkInvTile
        implements ICraftingProvider, IMEMonitorHandlerReceiver<IAEFluidStack>, IGridTickable {

    private final AppEngInternalInventory inventory = new AppEngInternalInventory(this, 1);
    private final BaseActionSource source = new MachineSource(this);
    private IItemList<IAEFluidStack> fluids = AEApi.instance().storage().createFluidList();
    private final Item encodedPattern = AEApi.instance().definitions().items().encodedPattern().maybeItem().orNull();
    private IAEItemStack returnStack;
    private boolean isPowered;

    public IInventory getInventory() {
        return inventory;
    }

    @Reflected
    public TileFluidAutoFiller() {
        getProxy().setIdlePowerUsage(1D);
        getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
        inventory.setInventorySlotContents(0, BUCKET);
    }

    public ItemStack getContainerItem() {
        return getInventory().getStackInSlot(0);
    }

    public void setContainerItem(ItemStack is) {
        getInventory().setInventorySlotContents(0, is);
    }

    @Override
    public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added) {}

    @Override
    public int[] getAccessibleSlotsBySide(ForgeDirection whichSide) {
        return new int[0];
    }

    @Override
    public boolean canBeRotated() {
        return false;
    }

    public void updatePattern() {
        ItemStack is = inventory.getStackInSlot(0);
        if (is == null) return;
        this.setContainerItem(is);
        postEvent();
    }

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void readFromNBTEvent(NBTTagCompound data) {
        inventory.readFromNBT(data, "Inv");
        if (inventory.getStackInSlot(0) == null) {
            inventory.setInventorySlotContents(0, BUCKET);
        }
    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public NBTTagCompound writeToNBTEvent(NBTTagCompound data) {
        inventory.writeToNBT(data, "Inv");
        return data;
    }

    @Nonnull
    @Override
    public IInventory getInternalInventory() {
        return inventory;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {
        return AECableType.DENSE;
    }

    private IStorageGrid getStorageGrid() {
        try {
            return this.getProxy().getGrid().getCache(IStorageGrid.class);
        } catch (GridAccessException e) {
            return null;
        }
    }

    @Override
    public void provideCrafting(ICraftingProviderHelper craftingTracker) {
        IStorageGrid storage = getStorageGrid();
        if (storage == null) return;
        if (this.fluids.isEmpty()) {
            this.fluids = storage.getFluidInventory().getStorageList();
        }
        for (IAEFluidStack fluidStack : this.fluids) {
            Fluid fluid = fluidStack.getFluid();
            if (fluid == null) continue;
            int maxCapacity = Util.FluidUtil.getCapacity(this.getContainerItem(), fluid);
            if (maxCapacity == 0) continue;
            MutablePair<Integer, ItemStack> filled = Util.FluidUtil
                    .fillStack(this.getContainerItem().copy(), new FluidStack(fluid, maxCapacity));
            if (filled.right == null) continue;
            ItemStack pattern = getPattern(this.getContainerItem(), filled.right);
            ICraftingPatternItem patter = (ICraftingPatternItem) pattern.getItem();
            craftingTracker.addCraftingOption(this, patter.getPatternForItem(pattern, getWorldObj()));
        }
    }

    private ItemStack getPattern(ItemStack emptyContainer, ItemStack filledContainer) {
        NBTTagList in = new NBTTagList();
        NBTTagList out = new NBTTagList();
        in.appendTag(emptyContainer.writeToNBT(new NBTTagCompound()));
        ItemStack fluidDrop = ItemFluidDrop.newStack(Util.FluidUtil.getFluidFromContainer(filledContainer));
        in.appendTag(createItemTag(fluidDrop));
        out.appendTag(filledContainer.writeToNBT(new NBTTagCompound()));
        NBTTagCompound itemTag = new NBTTagCompound();
        itemTag.setTag("in", in);
        itemTag.setTag("out", out);
        itemTag.setBoolean("crafting", false);
        ItemStack pattern = new ItemStack(this.encodedPattern);
        pattern.setTagCompound(itemTag);
        return pattern;
    }

    protected NBTBase createItemTag(final ItemStack i) {
        final NBTTagCompound c = new NBTTagCompound();
        if (i != null) {
            Util.writeItemStackToNBT(i, c);
        }
        return c;
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
        this.returnStack = AEApi.instance().storage()
                .createItemStack(patternDetails.getCondensedOutputs()[0].getItemStack());
        try {
            this.getProxy().getTick().alertDevice(this.getProxy().getNode());
        } catch (GridAccessException ignored) {

        }
        return true;
    }

    @Override
    public boolean isBusy() {
        return this.returnStack != null;
    }

    @Override
    public boolean isValid(Object verificationToken) {
        return true;
    }

    @Override
    public void postChange(IBaseMonitor<IAEFluidStack> monitor, Iterable<IAEFluidStack> change,
            BaseActionSource source) {
        if (this.getProxy().isActive() && this.getStorageGrid() != null) {
            boolean hasChanged = false;
            for (IAEFluidStack tmp : change) {
                if (this.fluids.findPrecise(tmp) == null) {
                    hasChanged = true;
                    this.fluids.add(tmp);
                }
            }
            if (hasChanged) postEvent();
        }
    }

    private boolean postEvent() {
        try {
            this.getProxy().getGrid()
                    .postEvent(new MENetworkCraftingPatternChange(this, getGridNode(ForgeDirection.UNKNOWN)));
            return true;
        } catch (GridAccessException ignored) {
            return false;
        }
    }

    @Override
    public void onListUpdate() {}

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkPowerStatusChange p) {
        this.updatePowerState();
    }

    @MENetworkEventSubscribe
    public final void bootingRender(final MENetworkBootingStatusChange c) {
        this.updatePowerState();
    }

    private void updatePowerState() {
        boolean newState = false;
        try {
            newState = this.getProxy().isActive()
                    && this.getProxy().getEnergy().extractAEPower(1, Actionable.SIMULATE, PowerMultiplier.CONFIG)
                            > 0.0001;
            if (newState != this.isPowered && newState) {
                this.getProxy().getStorage().getFluidInventory().addListener(this, null);
            }
        } catch (final GridAccessException ignored) {}
        if (newState != this.isPowered) {
            this.isPowered = newState;
            this.markForUpdate();
        }
    }

    @Override
    public void gridChanged() {
        try {
            this.getProxy().getStorage().getFluidInventory().removeListener(this);
        } catch (final GridAccessException ignored) {}
        super.gridChanged();
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 5, this.returnStack == null, true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
        if (this.getStorageGrid() == null) {
            return TickRateModulation.SLOWER;
        }
        IAEItemStack nodAdded = getStorageGrid().getItemInventory()
                .injectItems(this.returnStack, Actionable.SIMULATE, this.source);
        if (nodAdded == null) {
            getStorageGrid().getItemInventory().injectItems(this.returnStack, Actionable.MODULATE, this.source);
            this.returnStack = null;
            return TickRateModulation.SLEEP;
        } else {
            return TickRateModulation.SLOWER;
        }
    }

    @Override
    public boolean dropItems() {
        return false;
    }
}
