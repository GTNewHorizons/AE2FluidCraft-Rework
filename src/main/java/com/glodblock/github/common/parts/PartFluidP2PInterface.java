package com.glodblock.github.common.parts;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

import com.glodblock.github.client.FluidInterfaceButtons;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.AEFluidInventory;
import com.glodblock.github.inventory.IAEFluidTank;
import com.glodblock.github.inventory.IDualHost;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.DualityFluidInterface;
import com.glodblock.github.util.Util;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.IConfigManager;
import appeng.helpers.DualityInterface;
import appeng.helpers.ICustomButtonDataObject;
import appeng.helpers.ICustomButtonProvider;
import appeng.parts.p2p.PartP2PInterface;
import appeng.parts.p2p.PartP2PTunnel;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartFluidP2PInterface extends PartP2PInterface implements IDualHost, ICustomButtonProvider {

    private final DualityFluidInterface dualityFluid = new DualityFluidInterface(this.getProxy(), this);
    private final AppEngInternalAEInventory config = new AppEngInternalAEInventory(this, 6);

    private ICustomButtonDataObject customButtonDataObject;

    public PartFluidP2PInterface(ItemStack is) {
        super(is);

        this.customButtonDataObject = new FluidInterfaceButtons(false);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getTypeTexture() {
        return ItemAndBlockHolder.INTERFACE.getBlockTextureFromSide(0);
    }

    @Override
    public PartP2PTunnel<?> applyMemoryCard(EntityPlayer player, IMemoryCard memoryCard, ItemStack is) {
        PartP2PTunnel<?> newTunnel = super.applyMemoryCard(player, memoryCard, is);
        if (Platform.isClient()) return newTunnel;
        NBTTagCompound data = memoryCard.getData(is);
        if (newTunnel instanceof PartFluidP2PInterface p2PInterface) {
            p2PInterface.duality.getConfigManager().readFromNBT(data);
        }
        return newTunnel;
    }

    @Override
    protected void copySettings(final PartP2PTunnel<?> from) {
        if (from instanceof PartFluidP2PInterface fromInterface) {
            DualityInterface newDuality = this.duality;

            IConfigManager config = fromInterface.duality.getConfigManager();
            config.getSettings()
                    .forEach(setting -> newDuality.getConfigManager().putSetting(setting, config.getSetting(setting)));
        }
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        TickingRequest item = duality.getTickingRequest(node);
        TickingRequest fluid = dualityFluid.getTickingRequest(node);
        return new TickingRequest(
                Math.min(item.minTickRate, fluid.minTickRate),
                Math.max(item.maxTickRate, fluid.maxTickRate),
                item.isSleeping && fluid.isSleeping,
                true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        TickRateModulation item = duality.tickingRequest(node, ticksSinceLastCall);
        TickRateModulation fluid = dualityFluid.tickingRequest(node, ticksSinceLastCall);
        if (item.ordinal() >= fluid.ordinal()) {
            return item;
        } else {
            return fluid;
        }
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        return dualityFluid.fill(from, resource, doFill);
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        return dualityFluid.drain(from, resource, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return dualityFluid.drain(from, maxDrain, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return dualityFluid.canFill(from, fluid);
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return dualityFluid.canDrain(from, fluid);
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return dualityFluid.getTankInfo(from);
    }

    @Override
    public void onFluidInventoryChanged(IAEFluidTank inv, int slot) {
        saveChanges();
        getTileEntity().markDirty();
        dualityFluid.onFluidInventoryChanged(inv, slot);
    }

    @Override
    public AEFluidInventory getInternalFluid() {
        return dualityFluid.getInternalFluid();
    }

    @Override
    public DualityFluidInterface getDualityFluid() {
        return dualityFluid;
    }

    @Override
    public AppEngInternalAEInventory getConfig() {
        Util.mirrorFluidToPacket(config, dualityFluid.getConfig());
        return config;
    }

    @Override
    public void setConfig(int id, IAEFluidStack fluid) {
        if (id >= 0 && id < 6) {
            config.setInventorySlotContents(
                    id,
                    ItemFluidPacket.newDisplayStack(fluid == null ? null : fluid.getFluidStack()));
            dualityFluid.getConfig().setFluidInSlot(id, dualityFluid.getStandardFluid(fluid));
        }
    }

    @Override
    public void setFluidInv(int id, IAEFluidStack fluid) {
        if (id >= 0 && id < 6) {
            dualityFluid.getInternalFluid().setFluidInSlot(id, fluid);
        }
    }

    @Override
    public ItemStack getPrimaryGuiIcon() {
        return ItemAndBlockHolder.FLUID_INTERFACE.stack();
    }

    @Override
    public void writeCustomButtonData() {}

    @Override
    public void readCustomButtonData() {}

    @Override
    public void initCustomButtons(int guiLeft, int guiTop, int xSize, int ySize, int xOffset, int yOffset,
            List<GuiButton> buttonList) {
        if (customButtonDataObject != null)
            customButtonDataObject.initCustomButtons(guiLeft, guiTop, xSize, ySize, xOffset, yOffset, buttonList);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean actionPerformedCustomButtons(final GuiButton btn) {
        return customButtonDataObject != null && customButtonDataObject.actionPerformedCustomButtons(btn);
    }

    @Override
    public ICustomButtonDataObject getDataObject() {
        return customButtonDataObject;
    }

    @Override
    public void setDataObject(ICustomButtonDataObject dataObject) {
        customButtonDataObject = dataObject;
    }
}
