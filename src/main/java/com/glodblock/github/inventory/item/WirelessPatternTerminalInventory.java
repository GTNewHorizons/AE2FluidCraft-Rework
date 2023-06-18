package com.glodblock.github.inventory.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.ItemBiggerAppEngInventory;
import com.glodblock.github.inventory.WirelessFluidPatternTerminalPatterns;
import com.glodblock.github.util.Util;

import appeng.api.config.*;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.MEMonitorHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.items.contents.WirelessTerminalViewCells;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.Platform;

public class WirelessPatternTerminalInventory extends MEMonitorHandler<IAEItemStack>
        implements IWirelessPatternTerminal {

    private final ItemStack target;
    private final IAEItemPowerStorage ips;
    private final int inventorySlot;
    private final AppEngInternalInventory viewCell;
    private final StorageChannel channel;
    private final IGridNode grid;

    protected AppEngInternalInventory crafting;
    protected AppEngInternalInventory output;
    protected final WirelessFluidPatternTerminalPatterns pattern;

    protected boolean craftingMode = true;
    protected boolean substitute = false;
    protected boolean combine = false;
    protected boolean prioritize = false;
    protected boolean inverted = false;
    protected boolean beSubstitute = false;
    protected boolean autoFillPattern = false;
    protected int activePage = 0;

    @SuppressWarnings("unchecked")
    public WirelessPatternTerminalInventory(final ItemStack is, final int slot, IGridNode gridNode,
            EntityPlayer player) {
        super(
                (IMEInventoryHandler<IAEItemStack>) Objects
                        .requireNonNull(Util.getWirelessInv(is, player, StorageChannel.ITEMS)));
        this.ips = (ToolWirelessTerminal) is.getItem();
        this.grid = gridNode;
        this.target = is;
        this.inventorySlot = slot;
        this.viewCell = new WirelessTerminalViewCells(is);
        this.channel = StorageChannel.ITEMS;
        this.pattern = new WirelessFluidPatternTerminalPatterns(is, this);
        this.crafting = new ItemBiggerAppEngInventory(is, "crafting", 9);
        this.output = new ItemBiggerAppEngInventory(is, "output", 3);
    }

    public StorageChannel getChannel() {
        return this.channel;
    }

    @Override
    public ItemStack getItemStack() {
        return this.target;
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier) {
        amt = usePowerMultiplier.multiply(amt);
        if (mode == Actionable.SIMULATE) {
            return usePowerMultiplier.divide(Math.min(amt, this.ips.getAECurrentPower(this.target)));
        }
        return usePowerMultiplier.divide(this.ips.extractAEPower(this.target, amt));
    }

    @Override
    public IMEMonitor<IAEItemStack> getItemInventory() {
        return this;
    }

    @Override
    public IMEMonitor<IAEFluidStack> getFluidInventory() {
        return null;
    }

    public void readFromNBT() {
        NBTTagCompound data = Platform.openNbtData(this.target);
        this.setCraftingRecipe(data.getBoolean("craftingMode"));
        this.setSubstitution(data.getBoolean("substitute"));
        this.setCombineMode(data.getBoolean("combine"));
        this.setBeSubstitute(data.getBoolean("beSubstitute"));
        this.setActivePage(data.getInteger("activePage"));
        this.setAutoFillPattern(data.getBoolean("autoFillPattern"));
    }

    public void writeToNBT() {
        NBTTagCompound data = Platform.openNbtData(this.target);
        data.setBoolean("craftingMode", this.craftingMode);
        data.setBoolean("substitute", this.substitute);
        data.setBoolean("combine", this.combine);
        data.setBoolean("beSubstitute", this.beSubstitute);
        data.setInteger("activePage", this.activePage);
        data.setBoolean("autoFillPattern", this.autoFillPattern);
    }

    @Override
    public IConfigManager getConfigManager() {
        final ConfigManager out = new ConfigManager((manager, settingName, newValue) -> {
            final NBTTagCompound data = Platform.openNbtData(this.target);
            manager.writeToNBT(data);
        });
        out.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        out.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        out.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
        out.registerSetting(Settings.TYPE_FILTER, TypeFilter.ALL);
        out.readFromNBT((NBTTagCompound) Platform.openNbtData(this.target).copy());
        this.readFromNBT();
        return out;
    }

    @Override
    public int getInventorySlot() {
        return this.inventorySlot;
    }

    @Override
    public IInventory getViewCellStorage() {
        return this.viewCell;
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        return this.grid;
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {
        return AECableType.NONE;
    }

    @Override
    public void securityBreak() {
        this.getGridNode(ForgeDirection.UNKNOWN).getMachine().securityBreak();
    }

    @Override
    public void saveChanges() {
        // NO-OP
    }

    @Override
    public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack,
            ItemStack newStack) {
        if (inv == this.pattern && slot == 1) {
            final ItemStack is = inv.getStackInSlot(1);
            if (is != null && is.getItem() instanceof final ICraftingPatternItem craftingPatternItem) {
                final ICraftingPatternDetails details = craftingPatternItem
                        .getPatternForItem(is, this.getActionableNode().getWorld());
                if (details != null) {
                    final IAEItemStack[] inItems = details.getInputs();
                    final IAEItemStack[] outItems = details.getOutputs();

                    this.setCraftingRecipe(details.isCraftable());
                    this.setSubstitution(details.canSubstitute());
                    if (newStack != null) {
                        NBTTagCompound data = newStack.getTagCompound();
                        this.setCombineMode(data.getInteger("combine") == 1);
                        this.setBeSubstitute(details.canBeSubstitute());
                    }
                    for (int i = 0; i < this.crafting.getSizeInventory(); i++) {
                        this.crafting.setInventorySlotContents(i, null);
                    }

                    for (int i = 0; i < this.output.getSizeInventory(); i++) {
                        this.output.setInventorySlotContents(i, null);
                    }

                    for (int i = 0; i < this.crafting.getSizeInventory() && i < inItems.length; i++) {
                        if (inItems[i] != null) {
                            final IAEItemStack item = inItems[i];
                            if (item != null && item.getItem() instanceof ItemFluidDrop) {
                                ItemStack packet = ItemFluidPacket
                                        .newStack(ItemFluidDrop.getFluidStack(item.getItemStack()));
                                this.crafting.setInventorySlotContents(i, packet);
                            } else this.crafting.setInventorySlotContents(i, item == null ? null : item.getItemStack());
                        }
                    }

                    for (int i = 0; i < this.output.getSizeInventory() && i < outItems.length; i++) {
                        if (outItems[i] != null) {
                            final IAEItemStack item = outItems[i];
                            if (item != null && item.getItem() instanceof ItemFluidDrop) {
                                ItemStack packet = ItemFluidPacket
                                        .newStack(ItemFluidDrop.getFluidStack(item.getItemStack()));
                                this.output.setInventorySlotContents(i, packet);
                            } else this.output.setInventorySlotContents(i, item == null ? null : item.getItemStack());
                        }
                    }
                }
            }
        }
        if (inv == this.crafting) {
            this.fixCraftingRecipes();
        }
    }

    private void fixCraftingRecipes() {
        if (this.craftingMode) {
            for (int x = 0; x < this.crafting.getSizeInventory(); x++) {
                final ItemStack is = this.crafting.getStackInSlot(x);
                if (is != null) {
                    is.stackSize = 1;
                }
            }
        }
    }

    @Override
    public boolean isInverted() {
        return false;
    }

    @Override
    public IInventory getInventoryByName(String name) {
        if (name.equals("crafting")) {
            return this.crafting;
        }

        if (name.equals("output")) {
            return this.output;
        }

        if (name.equals("pattern")) {
            return this.pattern;
        }

        return null;
    }

    @Override
    public boolean canBeSubstitute() {
        return this.beSubstitute;
    }

    @Override
    public boolean isPrioritize() {
        return false;
    }

    @Override
    public boolean isSubstitution() {
        return this.substitute;
    }

    @Override
    public boolean shouldCombine() {
        return this.combine;
    }

    @Override
    public void onChangeCrafting(IAEItemStack[] newCrafting, IAEItemStack[] newOutput) {
        IInventory crafting = this.getInventoryByName("crafting");
        IInventory output = this.getInventoryByName("output");
        if (crafting instanceof AppEngInternalInventory && output instanceof AppEngInternalInventory) {
            for (int x = 0; x < crafting.getSizeInventory() && x < newCrafting.length; x++) {
                final IAEItemStack item = newCrafting[x];
                crafting.setInventorySlotContents(x, item == null ? null : item.getItemStack());
            }
            for (int x = 0; x < output.getSizeInventory() && x < newOutput.length; x++) {
                final IAEItemStack item = newOutput[x];
                output.setInventorySlotContents(x, item == null ? null : item.getItemStack());
            }
        }
    }

    @Override
    public void setCraftingRecipe(boolean craftingMode) {
        this.craftingMode = craftingMode;
        this.fixCraftingRecipes();
        // force update crafting mode
        ((ItemBiggerAppEngInventory) this.crafting).setCraftingMode(craftingMode);
    }

    @Override
    public void setSubstitution(boolean canSubstitute) {
        this.substitute = canSubstitute;
    }

    @Override
    public void setBeSubstitute(boolean canBeSubstitute) {
        this.beSubstitute = canBeSubstitute;
    }

    @Override
    public void setCombineMode(boolean shouldCombine) {
        this.combine = shouldCombine;
    }

    @Override
    public void setPrioritization(boolean canPrioritize) {
        this.prioritize = canPrioritize;
    }

    @Override
    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    @Override
    public int getActivePage() {
        return this.activePage;
    }

    @Override
    public void setActivePage(int activePage) {
        this.activePage = activePage;
    }

    @Override
    public boolean isCraftingRecipe() {
        return this.craftingMode;
    }

    @Override
    public void sortCraftingItems() {
        List<ItemStack> items = new ArrayList<>();
        List<ItemStack> fluids = new ArrayList<>();
        for (ItemStack is : this.crafting) {
            if (is == null) continue;
            if (is.getItem() instanceof ItemFluidPacket) {
                fluids.add(is);
            } else {
                items.add(is);
            }
        }
        if (this.prioritize) {
            fluids.addAll(items);
            items.clear();
        } else {
            items.addAll(fluids);
            fluids.clear();
        }

        for (int i = 0; i < this.crafting.getSizeInventory(); i++) {
            if (this.crafting.getStackInSlot(i) == null) break;
            if (items.isEmpty()) {
                this.crafting.setInventorySlotContents(i, fluids.get(i));
            } else {
                this.crafting.setInventorySlotContents(i, items.get(i));
            }
        }
        saveChanges();
    }

    @Override
    public boolean isAutoFillPattern() {
        return this.autoFillPattern;
    }

    @Override
    public void setAutoFillPattern(boolean canFill) {
        this.autoFillPattern = canFill;
    }

    @Override
    public void saveSettings() {
        this.writeToNBT();
    }

    @Override
    public IGridNode getActionableNode() {
        return this.grid;
    }

}
