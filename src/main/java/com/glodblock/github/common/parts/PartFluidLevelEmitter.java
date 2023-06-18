package com.glodblock.github.common.parts;

import java.util.Objects;
import java.util.Random;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.Util;

import appeng.api.config.*;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.*;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.client.texture.CableBusTextures;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.helpers.Reflected;
import appeng.me.GridAccessException;
import appeng.parts.automation.PartUpgradeable;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartFluidLevelEmitter extends PartUpgradeable
        implements IStackWatcherHost, ICraftingWatcherHost, IMEMonitorHandlerReceiver<IAEFluidStack>, IGridTickable {

    private static final int FLAG_ON = 8;

    private final AppEngInternalAEInventory config = new AppEngInternalAEInventory(this, 1);

    private boolean prevState = false;

    private long lastReportedValue = 0;
    private long reportingValue = 0;

    private IStackWatcher myWatcher;
    private double centerX;
    private double centerY;
    private double centerZ;

    private int lastWorkingTick = 0;
    private boolean delayedUpdatesQueued = false;

    @Reflected
    public PartFluidLevelEmitter(final ItemStack is) {
        super(is);

        this.getConfigManager().registerSetting(Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL);

        // Workaround the emitter randomly breaking on world load
        if (MinecraftServer.getServer() != null) {
            delayedUpdatesQueued = true;
            lastWorkingTick = MinecraftServer.getServer().getTickCounter();
        }
    }

    public long getReportingValue() {
        return this.reportingValue;
    }

    public void setReportingValue(final long v) {
        this.reportingValue = v;
        this.updateState();
    }

    @MENetworkEventSubscribe
    public void powerChanged(final MENetworkPowerStatusChange c) {
        this.updateState();
    }

    private void updateState() {
        final boolean isOn = this.isLevelEmitterOn();
        if (this.prevState != isOn) {
            this.getHost().markForUpdate();
            final TileEntity te = this.getHost().getTile();
            this.prevState = isOn;
            Platform.notifyBlocksOfNeighbors(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord);
            Platform.notifyBlocksOfNeighbors(
                    te.getWorldObj(),
                    te.xCoord + this.getSide().offsetX,
                    te.yCoord + this.getSide().offsetY,
                    te.zCoord + this.getSide().offsetZ);
        }
    }

    private boolean isLevelEmitterOn() {
        if (Platform.isClient()) {
            return (this.getClientFlags() & FLAG_ON) == FLAG_ON;
        }

        if (!this.getProxy().isActive()) {
            return false;
        }

        final Enum<?> redstoneEmitterSetting = this.getConfigManager().getSetting(Settings.REDSTONE_EMITTER);
        final boolean result;

        if (redstoneEmitterSetting == RedstoneMode.LOW_SIGNAL) {
            result = this.reportingValue >= this.lastReportedValue + 1;
        } else {
            result = this.reportingValue < this.lastReportedValue + 1;
        }

        return result;
    }

    @MENetworkEventSubscribe
    public void channelChanged(final MENetworkChannelsChanged c) {
        this.updateState();
    }

    @Override
    protected int populateFlags(final int cf) {
        return cf | (this.prevState ? FLAG_ON : 0);
    }

    @Override
    public IIcon getBreakingTexture() {
        return this.getItemStack().getIconIndex();
    }

    @Override
    public void updateWatcher(final ICraftingWatcher newWatcher) {
        this.configureWatchers();
    }

    @Override
    public void onRequestChange(final ICraftingGrid craftingGrid, final IAEItemStack what) {
        this.updateState();
    }

    private IAEFluidStack getIAEFluidStack() {
        final FluidStack fs = ItemFluidPacket.getFluidStack(this.config.getAEStackInSlot(0));
        if (fs != null) return Util.FluidUtil.createAEFluidStack(fs);
        return null;
    }

    // update the system...
    private void configureWatchers() {
        final IAEFluidStack myStack = this.getIAEFluidStack();

        if (this.myWatcher != null) {
            this.myWatcher.clear();
        }

        try {
            this.getProxy().getStorage().getFluidInventory().removeListener(this);
            if (this.myWatcher != null && myStack != null) {
                this.myWatcher.add(myStack);
            }
            this.updateReportingValue(this.getProxy().getStorage().getFluidInventory());
        } catch (final GridAccessException ignored) {}
    }

    private void updateReportingValue(final IMEMonitor<IAEFluidStack> monitor) {
        final IAEFluidStack myStack = this.getIAEFluidStack();

        if (myStack == null) {
            this.lastReportedValue = 0;
            for (final IAEFluidStack st : monitor.getStorageList()) {
                this.lastReportedValue += st.getStackSize();
            }
        } else {
            final IAEFluidStack r = monitor.getStorageList().findPrecise(myStack);
            if (r == null) {
                this.lastReportedValue = 0;
            } else {
                this.lastReportedValue = r.getStackSize();
            }
        }

        this.updateState();
    }

    @Override
    public void updateWatcher(final IStackWatcher newWatcher) {
        this.myWatcher = newWatcher;
        this.configureWatchers();
    }

    @Override
    public void onStackChange(final IItemList o, final IAEStack fullStack, final IAEStack diffStack,
            final BaseActionSource src, final StorageChannel chan) {
        if (chan == StorageChannel.FLUIDS && fullStack.equals(this.getIAEFluidStack())) {
            this.lastReportedValue = fullStack.getStackSize();
            this.updateState();
        }
    }

    @Override
    public boolean isValid(final Object effectiveGrid) {
        try {
            return this.getProxy().getGrid() == effectiveGrid;
        } catch (final GridAccessException e) {
            return false;
        }
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(
                AEConfig.instance.levelEmitterDelay / 2,
                AEConfig.instance.levelEmitterDelay,
                !delayedUpdatesQueued,
                true);
    }

    private boolean canDoWork() {
        int currentTick = MinecraftServer.getServer().getTickCounter();
        return (currentTick - lastWorkingTick) > AEConfig.instance.levelEmitterDelay;
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
        if (delayedUpdatesQueued && canDoWork()) {
            delayedUpdatesQueued = false;
            lastWorkingTick = MinecraftServer.getServer().getTickCounter();
            this.onListUpdate();
        }
        return delayedUpdatesQueued ? TickRateModulation.IDLE : TickRateModulation.SLEEP;
    }

    @Override
    public void postChange(final IBaseMonitor<IAEFluidStack> monitor, final Iterable<IAEFluidStack> change,
            final BaseActionSource actionSource) {
        if (canDoWork()) {
            if (delayedUpdatesQueued) {
                delayedUpdatesQueued = false;
                try {
                    this.getProxy().getTick().sleepDevice(this.getProxy().getNode());
                } catch (GridAccessException e) {
                    AELog.error(e, "Couldn't put level emitter to sleep after cancelling delayed updates");
                }
            }
            lastWorkingTick = MinecraftServer.getServer().getTickCounter();
            this.updateReportingValue((IMEMonitor<IAEFluidStack>) monitor);
        } else if (!delayedUpdatesQueued) {
            delayedUpdatesQueued = true;
            try {
                this.getProxy().getTick().alertDevice(this.getProxy().getNode());
            } catch (GridAccessException e) {
                AELog.error(e, "Couldn't wake up level emitter for delayed updates");
            }
        }
    }

    @Override
    public void onListUpdate() {
        try {
            this.updateReportingValue(this.getProxy().getStorage().getFluidInventory());
        } catch (final GridAccessException ignored) {}
    }

    @Override
    public AECableType getCableConnectionType(final ForgeDirection dir) {
        return AECableType.SMART;
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(7, 7, 11, 9, 9, 16);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventory(final IPartRenderHelper rh, final RenderBlocks renderer) {
        rh.setTexture(this.getItemStack().getIconIndex());
        Tessellator.instance.startDrawingQuads();
        this.renderTorchAtAngle(0, -0.5, 0);
        Tessellator.instance.draw();
    }

    private void renderTorchAtAngle(double baseX, double baseY, double baseZ) {
        final boolean isOn = this.isLevelEmitterOn();
        final IIcon offTexture = this.getItemStack().getIconIndex();
        final IIcon IIcon = (isOn ? CableBusTextures.LevelEmitterTorchOn.getIcon() : offTexture);
        this.centerX = baseX + 0.5;
        this.centerY = baseY + 0.5;
        this.centerZ = baseZ + 0.5;

        baseY += 7.0 / 16.0;

        final float var16 = IIcon.getMinU();
        final float var17 = IIcon.getMaxU();
        final float var18 = IIcon.getMinV();
        final float var19 = IIcon.getMaxV();

        final double var20b = offTexture.getInterpolatedU(7.0D);
        final double var24b = offTexture.getInterpolatedU(9.0D);

        final double var20 = IIcon.getInterpolatedU(7.0D);
        final double var24 = IIcon.getInterpolatedU(9.0D);
        final double var22 = IIcon.getInterpolatedV(6.0D + (isOn ? 0 : 1.0D));
        final double var26 = IIcon.getInterpolatedV(8.0D + (isOn ? 0 : 1.0D));
        final double var28 = IIcon.getInterpolatedU(7.0D);
        final double var30 = IIcon.getInterpolatedV(13.0D);
        final double var32 = IIcon.getInterpolatedU(9.0D);
        final double var34 = IIcon.getInterpolatedV(15.0D);

        final double var22b = IIcon.getInterpolatedV(9.0D);
        final double var26b = IIcon.getInterpolatedV(11.0D);

        baseX += 0.5D;
        baseZ += 0.5D;
        final double var36 = baseX - 0.5D;
        final double var38 = baseX + 0.5D;
        final double var40 = baseZ - 0.5D;
        final double var42 = baseZ + 0.5D;

        double toff = 0.0d;

        if (!isOn) {
            toff = 1.0d / 16.0d;
        }

        final Tessellator var12 = Tessellator.instance;
        if (isOn) {
            var12.setColorOpaque_F(1.0F, 1.0F, 1.0F);
            var12.setBrightness(11 << 20 | 11 << 4);
        }

        final double TorchLen = 0.625D;
        final double var44 = 0.0625D;
        final double Zero = 0;
        final double par10 = 0;

        final double x = baseX + Zero * (1.0D - TorchLen) - var44;
        final double x1 = baseX + Zero * (1.0D - TorchLen) + var44;

        final double y = baseY + TorchLen - toff;
        final double var422 = 0.1915D + 1.0 / 16.0;
        final double y1 = baseY + var422;

        final double z = baseZ + par10 * (1.0D - TorchLen) - var44;
        final double z1 = baseZ + par10 * (1.0D - TorchLen) + var44;

        this.addVertexWithUV(x, y, z, var20, var22);
        this.addVertexWithUV(x, y, z1, var20, var26);
        this.addVertexWithUV(x1, y, z1, var24, var26);
        this.addVertexWithUV(x1, y, z, var24, var22);

        this.addVertexWithUV(x1, y1, z, var24b, var22b);
        this.addVertexWithUV(x1, y1, z1, var24b, var26b);
        this.addVertexWithUV(x, y1, z1, var20b, var26b);
        this.addVertexWithUV(x, y1, z, var20b, var22b);

        this.addVertexWithUV(baseX + var44 + Zero, baseY, baseZ - var44 + par10, var32, var30);
        this.addVertexWithUV(baseX + var44 + Zero, baseY, baseZ + var44 + par10, var32, var34);
        this.addVertexWithUV(baseX - var44 + Zero, baseY, baseZ + var44 + par10, var28, var34);
        this.addVertexWithUV(baseX - var44 + Zero, baseY, baseZ - var44 + par10, var28, var30);

        this.addVertexWithUV(baseX - var44, baseY + 1.0D, var40, var16, var18);
        this.addVertexWithUV(baseX - var44 + Zero, baseY + 0.0D, var40 + par10, var16, var19);
        this.addVertexWithUV(baseX - var44 + Zero, baseY + 0.0D, var42 + par10, var17, var19);
        this.addVertexWithUV(baseX - var44, baseY + 1.0D, var42, var17, var18);

        this.addVertexWithUV(baseX + var44, baseY + 1.0D, var42, var16, var18);
        this.addVertexWithUV(baseX + Zero + var44, baseY + 0.0D, var42 + par10, var16, var19);
        this.addVertexWithUV(baseX + Zero + var44, baseY + 0.0D, var40 + par10, var17, var19);
        this.addVertexWithUV(baseX + var44, baseY + 1.0D, var40, var17, var18);

        this.addVertexWithUV(var36, baseY + 1.0D, baseZ + var44, var16, var18);
        this.addVertexWithUV(var36 + Zero, baseY + 0.0D, baseZ + var44 + par10, var16, var19);
        this.addVertexWithUV(var38 + Zero, baseY + 0.0D, baseZ + var44 + par10, var17, var19);
        this.addVertexWithUV(var38, baseY + 1.0D, baseZ + var44, var17, var18);

        this.addVertexWithUV(var38, baseY + 1.0D, baseZ - var44, var16, var18);
        this.addVertexWithUV(var38 + Zero, baseY + 0.0D, baseZ - var44 + par10, var16, var19);
        this.addVertexWithUV(var36 + Zero, baseY + 0.0D, baseZ - var44 + par10, var17, var19);
        this.addVertexWithUV(var36, baseY + 1.0D, baseZ - var44, var17, var18);
    }

    private void addVertexWithUV(double x, double y, double z, final double u, final double v) {
        final Tessellator var12 = Tessellator.instance;

        x -= this.centerX;
        y -= this.centerY;
        z -= this.centerZ;

        if (this.getSide() == ForgeDirection.DOWN) {
            y = -y;
            z = -z;
        }

        if (this.getSide() == ForgeDirection.EAST) {
            final double m = x;
            x = y;
            y = m;
            y = -y;
        }

        if (this.getSide() == ForgeDirection.WEST) {
            final double m = x;
            x = -y;
            y = m;
        }

        if (this.getSide() == ForgeDirection.SOUTH) {
            final double m = z;
            z = y;
            y = m;
            y = -y;
        }

        if (this.getSide() == ForgeDirection.NORTH) {
            final double m = z;
            z = -y;
            y = m;
        }

        x += this.centerX; // + orientation.offsetX * 0.4;
        y += this.centerY; // + orientation.offsetY * 0.4;
        z += this.centerZ; // + orientation.offsetZ * 0.4;

        var12.addVertexWithUV(x, y, z, u, v);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderStatic(final int x, final int y, final int z, final IPartRenderHelper rh,
            final RenderBlocks renderer) {
        rh.setTexture(this.getItemStack().getIconIndex());

        renderer.renderAllFaces = true;

        final Tessellator tess = Tessellator.instance;
        tess.setBrightness(rh.getBlock().getMixedBrightnessForBlock(this.getHost().getTile().getWorldObj(), x, y, z));
        tess.setColorOpaque_F(1.0F, 1.0F, 1.0F);

        this.renderTorchAtAngle(x, y, z);

        renderer.renderAllFaces = false;

        rh.setBounds(7, 7, 11, 9, 9, 12);
        this.renderLights(x, y, z, rh, renderer);
    }

    @Override
    public int isProvidingStrongPower() {
        return this.prevState ? 15 : 0;
    }

    @Override
    public int isProvidingWeakPower() {
        return this.prevState ? 15 : 0;
    }

    @Override
    public void randomDisplayTick(final World world, final int x, final int y, final int z, final Random r) {
        if (this.isLevelEmitterOn()) {
            final ForgeDirection d = this.getSide();

            final double d0 = d.offsetX * 0.45F + (r.nextFloat() - 0.5F) * 0.2D;
            final double d1 = d.offsetY * 0.45F + (r.nextFloat() - 0.5F) * 0.2D;
            final double d2 = d.offsetZ * 0.45F + (r.nextFloat() - 0.5F) * 0.2D;

            world.spawnParticle("reddust", 0.5 + x + d0, 0.5 + y + d1, 0.5 + z + d2, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public int cableConnectionRenderTo() {
        return 16;
    }

    @Override
    public boolean onPartActivate(final EntityPlayer player, final Vec3 pos) {
        if (!player.isSneaking()) {
            if (Platform.isClient()) {
                return true;
            }
            InventoryHandler.openGui(
                    player,
                    this.getHost().getTile().getWorldObj(),
                    new BlockPos(this.getHost().getTile()),
                    Objects.requireNonNull(getSide()),
                    GuiType.FLUID_LEVEL_EMITTER);
            return true;
        }

        return false;
    }

    @Override
    public void updateSetting(final IConfigManager manager, final Enum settingName, final Enum newValue) {
        this.configureWatchers();
    }

    @Override
    public void onChangeInventory(final IInventory inv, final int slot, final InvOperation mc,
            final ItemStack removedStack, final ItemStack newStack) {
        if (inv == this.config) {
            this.configureWatchers();
        }
        super.onChangeInventory(inv, slot, mc, removedStack, newStack);
    }

    @Override
    public void upgradesChanged() {
        this.configureWatchers();
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.lastReportedValue = data.getLong("lastReportedValue");
        this.reportingValue = data.getLong("reportingValue");
        this.prevState = data.getBoolean("prevState");
        this.config.readFromNBT(data, "config");
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        data.setLong("lastReportedValue", this.lastReportedValue);
        data.setLong("reportingValue", this.reportingValue);
        data.setBoolean("prevState", this.prevState);
        this.config.writeToNBT(data, "config");
    }

    public void setFluidInSlot(int id, IAEFluidStack fluid) {
        ItemStack tmp = ItemFluidPacket.newDisplayStack(fluid == null ? null : fluid.getFluidStack());
        this.config.setInventorySlotContents(id, tmp);
    }

    @Override
    public IInventory getInventoryByName(final String name) {
        if (name.equals("config")) {
            return this.config;
        }

        return super.getInventoryByName(name);
    }
}
