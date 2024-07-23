package com.glodblock.github.common.item;

import java.text.NumberFormat;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.api.FluidCraftAPI;
import com.glodblock.github.common.storage.CellType;
import com.glodblock.github.common.storage.FluidCellInventoryHandler;
import com.glodblock.github.common.storage.IFluidCellInventory;
import com.glodblock.github.common.storage.IStorageFluidCell;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.NameConst;
import com.google.common.base.Optional;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.ReadableNumberConverter;

public abstract class FCBaseItemCell extends AEBaseItem implements IStorageFluidCell {

    protected CellType component;
    protected long totalBytes;
    protected int perType;
    protected double idleDrain;
    protected int maxType = 1;
    private final ReadableNumberConverter format = ReadableNumberConverter.INSTANCE;

    @SuppressWarnings("Guava")
    public FCBaseItemCell(long bytes, int perType, int totalType, double drain) {
        super(Optional.of(bytes / 1024 + "k"));
        this.setFeature(EnumSet.of(AEFeature.StorageCells));
        this.setMaxStackSize(1);
        this.totalBytes = bytes;
        this.perType = perType;
        this.idleDrain = drain;
        this.maxType = totalType;
        this.component = null;
    }

    @SuppressWarnings("all")
    public FCBaseItemCell(final Optional<String> subName) {
        super(subName);
    }

    public ItemStack getHousing() {
        return ItemAndBlockHolder.CELL_HOUSING.stack();
    }

    public ItemStack getComponent() {
        return component.stack(1);
    }

    @Override
    public long getBytes(ItemStack cellItem) {
        return this.totalBytes;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return this.perType;
    }

    @Override
    public boolean isBlackListed(ItemStack cellItem, IAEFluidStack requestedAddition) {
        return requestedAddition == null || requestedAddition.getFluid() == null
                || FluidCraftAPI.instance().isBlacklistedInStorage(requestedAddition.getFluid().getClass());
    }

    @Override
    public void addCheckedInformation(final ItemStack stack, final EntityPlayer player, final List<String> lines,
            final boolean displayMoreInfo) {
        final IMEInventoryHandler<?> inventory = AEApi.instance().registries().cell()
                .getCellInventory(stack, null, StorageChannel.FLUIDS);

        if (inventory instanceof final FluidCellInventoryHandler handler) {
            final IFluidCellInventory cellInventory = handler.getCellInv();

            if (cellInventory != null) {
                lines.add(
                        EnumChatFormatting.WHITE + NumberFormat.getInstance().format(cellInventory.getUsedBytes())
                                + EnumChatFormatting.GRAY
                                + " "
                                + GuiText.Of.getLocal()
                                + " "
                                + EnumChatFormatting.DARK_GREEN
                                + NumberFormat.getInstance().format(cellInventory.getTotalBytes())
                                + " "
                                + EnumChatFormatting.GRAY
                                + GuiText.BytesUsed.getLocal());
                lines.add(
                        EnumChatFormatting.WHITE + NumberFormat.getInstance().format(cellInventory.getStoredFluidTypes())
                                + EnumChatFormatting.GRAY
                                + " "
                                + GuiText.Of.getLocal()
                                + " "
                                + EnumChatFormatting.DARK_GREEN
                                + cellInventory.getTotalFluidTypes()
                                + " "
                                + EnumChatFormatting.GRAY
                                + GuiText.Types.getLocal());

                if (GuiScreen.isCtrlKeyDown()) {
                    if (cellInventory.getStoredFluidTypes() > 0) {
                        lines.add(StatCollector.translateToLocal(NameConst.TT_CELL_CONTENTS));
                        for (IAEFluidStack fluid : cellInventory.getContents()) {
                            if (fluid != null) {
                                lines.add(
                                        String.format(
                                                "  %s x%s mB",
                                                fluid.getFluidStack().getLocalizedName(),
                                                format.toWideReadableForm(fluid.getStackSize())));
                            }
                        }
                    } else {
                        lines.add(StatCollector.translateToLocal(NameConst.TT_CELL_EMPTY));
                    }
                } else {
                    lines.add(StatCollector.translateToLocal(NameConst.TT_CTRL_FOR_MORE));
                }

                if (handler.isPreformatted()) {
                    final String list = (handler.getIncludeExcludeMode() == IncludeExclude.WHITELIST ? GuiText.Included
                            : GuiText.Excluded).getLocal();
                    lines.add(GuiText.Partitioned.getLocal() + " - " + list + ' ' + GuiText.Precise.getLocal());

                    if (GuiScreen.isShiftKeyDown()) {
                        lines.add(GuiText.Filter.getLocal() + ": ");
                        for (IAEFluidStack aeFluidStack : handler.getPartitionInv()) {
                            if (aeFluidStack != null) lines.add("  " + aeFluidStack.getFluidStack().getLocalizedName());
                        }
                    } else {
                        lines.add(StatCollector.translateToLocal(NameConst.TT_SHIFT_FOR_MORE));
                    }
                    if (handler.getSticky()) {
                        lines.add(GuiText.Sticky.getLocal());
                    }
                }
            }
        }
    }

    @Override
    public boolean storableInStorageCell() {
        return false;
    }

    @Override
    public boolean isStorageCell(ItemStack i) {
        return true;
    }

    @Override
    public double getIdleDrain(ItemStack is) {
        return this.idleDrain;
    }

    @Override
    public int getTotalTypes(final ItemStack cellItem) {
        return this.maxType;
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return true;
    }

    @Override
    public IInventory getUpgradesInventory(ItemStack is) {
        return new CellUpgrades(is, 2);
    }

    @Override
    public IInventory getConfigInventory(ItemStack is) {
        return new CellConfig(is);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        final String fz = Platform.openNbtData(is).getString("FuzzyMode");
        try {
            return FuzzyMode.valueOf(fz);
        } catch (final Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
        Platform.openNbtData(is).setString("FuzzyMode", fzMode.name());
    }

    @SuppressWarnings("unchecked")
    protected boolean disassembleDrive(final ItemStack stack, final World world, final EntityPlayer player) {
        if (player.isSneaking()) {
            if (Platform.isClient()) {
                return false;
            }
            final InventoryPlayer playerInventory = player.inventory;
            final IMEInventoryHandler<?> inv = AEApi.instance().registries().cell()
                    .getCellInventory(stack, null, StorageChannel.FLUIDS);
            if (inv != null && playerInventory.getCurrentItem() == stack) {
                final InventoryAdaptor ia = InventoryAdaptor.getAdaptor(player, ForgeDirection.UNKNOWN);
                final IItemList<IAEFluidStack> list = inv.getAvailableItems(StorageChannel.FLUIDS.createList());
                if (list.isEmpty() && ia != null) {
                    playerInventory.setInventorySlotContents(playerInventory.currentItem, null);

                    // drop core
                    final ItemStack extraB = ia.addItems(this.component.stack(1));
                    if (extraB != null) {
                        player.dropPlayerItemWithRandomChoice(extraB, false);
                    }

                    // drop upgrades
                    final IInventory upgradesInventory = this.getUpgradesInventory(stack);
                    for (int upgradeIndex = 0; upgradeIndex < upgradesInventory.getSizeInventory(); upgradeIndex++) {
                        final ItemStack upgradeStack = upgradesInventory.getStackInSlot(upgradeIndex);
                        final ItemStack leftStack = ia.addItems(upgradeStack);
                        if (leftStack != null && upgradeStack.getItem() instanceof IUpgradeModule) {
                            player.dropPlayerItemWithRandomChoice(upgradeStack, false);
                        }
                    }

                    // drop empty storage cell case
                    final ItemStack extraA = ia.addItems(this.getHousing());
                    if (extraA != null) {
                        player.dropPlayerItemWithRandomChoice(this.getHousing(), false);
                    }
                    if (player.inventoryContainer != null) {
                        player.inventoryContainer.detectAndSendChanges();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public ItemStack stack(int size) {
        return new ItemStack(this, size);
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }
}
