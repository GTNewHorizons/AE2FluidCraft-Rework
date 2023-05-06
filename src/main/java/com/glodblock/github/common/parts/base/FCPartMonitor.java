package com.glodblock.github.common.parts.base;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.glodblock.github.client.textures.FCPartsTexture;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.util.Util;

import appeng.api.implementations.parts.IPartStorageMonitor;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.client.ClientHelper;
import appeng.client.texture.CableBusTextures;
import appeng.core.AELog;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.Reflected;
import appeng.hooks.TickHandler;
import appeng.me.GridAccessException;
import appeng.parts.reporting.AbstractPartDisplay;
import appeng.parts.reporting.PartPanel;
import appeng.util.IWideReadableNumberConverter;
import appeng.util.Platform;
import appeng.util.ReadableNumberConverter;
import appeng.util.item.AEFluidStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public abstract class FCPartMonitor extends AbstractPartDisplay implements IPartStorageMonitor, IStackWatcherHost {

    private static final IWideReadableNumberConverter NUMBER_CONVERTER = ReadableNumberConverter.INSTANCE;

    private IAEFluidStack configuredFluid;
    private String lastHumanReadableText;
    private boolean isLocked;
    private IStackWatcher myWatcher;

    @SideOnly(Side.CLIENT)
    private boolean updateList;

    @SideOnly(Side.CLIENT)
    private Integer dspList;

    @Reflected
    public FCPartMonitor(ItemStack is) {
        super(is);
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);

        this.isLocked = data.getBoolean("isLocked");

        final NBTTagCompound myItem = data.getCompoundTag("configuredItem");
        this.configuredFluid = Util.loadFluidStackFromNBT(myItem);
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);

        data.setBoolean("isLocked", this.isLocked);

        final NBTTagCompound myItem = new NBTTagCompound();
        if (this.configuredFluid != null) {
            this.configuredFluid.writeToNBT(myItem);
        }

        data.setTag("configuredItem", myItem);
    }

    @Override
    public void writeToStream(final ByteBuf data) throws IOException {
        super.writeToStream(data);

        data.writeBoolean(this.isLocked);
        data.writeBoolean(this.configuredFluid != null);
        if (this.configuredFluid != null) {
            this.configuredFluid.writeToPacket(data);
        }
    }

    @Override
    public boolean readFromStream(final ByteBuf data) throws IOException {
        boolean needRedraw = super.readFromStream(data);

        final boolean isLocked = data.readBoolean();
        needRedraw |= this.isLocked != isLocked;

        this.isLocked = isLocked;

        final boolean val = data.readBoolean();
        if (val) {
            needRedraw |= this.configuredFluid == null;
            this.configuredFluid = AEFluidStack.loadFluidStackFromPacket(data);
        } else {
            needRedraw |= this.configuredFluid != null;
            this.configuredFluid = null;
        }

        this.updateList = true;

        return needRedraw;
    }

    @Override
    public boolean onPartActivate(final EntityPlayer player, final Vec3 pos) {
        if (Platform.isClient()) {
            return true;
        }

        if (!this.getProxy().isActive()) {
            return false;
        }

        if (!Platform.hasPermissions(this.getLocation(), player)) {
            return false;
        }

        final TileEntity te = this.getTile();
        final ItemStack eq = player.getCurrentEquippedItem();

        if (Platform.isWrench(player, eq, te.xCoord, te.yCoord, te.zCoord)) {
            this.isLocked = !this.isLocked;
            player.addChatMessage((this.isLocked ? PlayerMessages.isNowLocked : PlayerMessages.isNowUnlocked).get());
            this.getHost().markForUpdate();
        } else if (!this.isLocked) {
            this.configuredFluid = AEFluidStack.create(Util.FluidUtil.getFluidFromContainer(eq));
            this.configureWatchers();
            this.getHost().markForUpdate();
        } else {
            this.extractItem(player);
        }

        return true;
    }

    // update the system...
    private void configureWatchers() {
        if (this.myWatcher != null) {
            this.myWatcher.clear();
        }

        try {
            if (this.configuredFluid != null) {
                if (this.myWatcher != null) {
                    this.myWatcher.add(this.configuredFluid);
                }

                this.updateReportingValue(this.getProxy().getStorage().getFluidInventory());
            }
        } catch (final GridAccessException e) {
            // >.>
        }
    }

    protected void extractItem(final EntityPlayer player) {}

    private void updateReportingValue(final IMEMonitor<IAEFluidStack> fluidInventory) {
        if (this.configuredFluid != null) {
            final IAEFluidStack result = fluidInventory.getStorageList().findPrecise(this.configuredFluid);
            if (result == null) {
                this.configuredFluid.setStackSize(0);
            } else {
                this.configuredFluid.setStackSize(result.getStackSize());
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void finalize() throws Throwable {
        super.finalize();

        if (this.dspList != null) {
            TickHandler.INSTANCE.scheduleCallListDelete(this.dspList);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderDynamic(final double x, final double y, final double z, final IPartRenderHelper rh,
            final RenderBlocks renderer) {
        if (this.dspList == null) {
            this.dspList = GLAllocation.generateDisplayLists(1);
        }

        final Tessellator tess = Tessellator.instance;

        if ((this.getClientFlags() & (PartPanel.POWERED_FLAG | PartPanel.CHANNEL_FLAG))
                != (PartPanel.POWERED_FLAG | PartPanel.CHANNEL_FLAG)) {
            return;
        }

        final IAEFluidStack ais = (IAEFluidStack) this.getDisplayed();

        if (ais != null) {
            GL11.glPushMatrix();
            GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

            if (this.updateList) {
                this.updateList = false;
                GL11.glNewList(this.dspList, GL11.GL_COMPILE_AND_EXECUTE);
                this.tesrRenderScreen(tess, ais);
                GL11.glEndList();
            } else {
                GL11.glCallList(this.dspList);
            }

            GL11.glPopMatrix();
        }
    }

    @Override
    public boolean requireDynamicRender() {
        return true;
    }

    @Override
    public IAEStack<?> getDisplayed() {
        return this.configuredFluid;
    }

    private void tesrRenderScreen(final Tessellator tess, final IAEFluidStack ais) {
        // GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );

        final ForgeDirection d = this.getSide();

        GL11.glTranslated(d.offsetX * 0.77, d.offsetY * 0.77, d.offsetZ * 0.77);

        switch (d) {
            case UP:
                GL11.glScalef(1.0f, -1.0f, 1.0f);
                GL11.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
                GL11.glRotatef(this.getSpin() * 90.0F, 0, 0, 1);
                break;
            case DOWN:
                GL11.glScalef(1.0f, -1.0f, 1.0f);
                GL11.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
                GL11.glRotatef(this.getSpin() * -90.0F, 0, 0, 1);
                break;
            case EAST:
                GL11.glScalef(-1.0f, -1.0f, -1.0f);
                GL11.glRotatef(-90.0f, 0.0f, 1.0f, 0.0f);
                break;
            case WEST:
                GL11.glScalef(-1.0f, -1.0f, -1.0f);
                GL11.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
                break;
            case NORTH:
                GL11.glScalef(-1.0f, -1.0f, -1.0f);
                break;
            case SOUTH:
                GL11.glScalef(-1.0f, -1.0f, -1.0f);
                GL11.glRotatef(180.0f, 0.0f, 1.0f, 0.0f);
                break;

            default:
                break;
        }
        final ItemStack sis = ItemFluidPacket.newDisplayStack(ais.getFluidStack());

        try {
            final int br = 16 << 20 | 16 << 4;
            final int var11 = br % 65536;
            final int var12 = br / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, var11 * 0.8F, var12 * 0.8F);

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            // RenderHelper.enableGUIStandardItemLighting();
            tess.setColorOpaque_F(1.0f, 1.0f, 1.0f);

            ClientHelper.proxy.doRenderItem(sis, this.getTile().getWorldObj());
        } catch (final Exception e) {
            AELog.debug(e);
        } finally {
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        }
        if (sis != null) {
            GL11.glTranslatef(0.0f, 0.14f, -0.24f);
            GL11.glScalef(1.0f / 62.0f, 1.0f / 62.0f, 1.0f / 62.0f);

            final long stackSize = ais.getStackSize();
            final String renderedStackSize = NUMBER_CONVERTER.toWideReadableForm(stackSize);

            final FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
            final int width = fr.getStringWidth(renderedStackSize);
            GL11.glTranslatef(-0.5f * width, 0.0f, -1.0f);
            fr.drawString(renderedStackSize, 0, 0, 0);
        }
        // GL11.glPopAttrib();
    }

    @Override
    public boolean isLocked() {
        return this.isLocked;
    }

    @Override
    public void updateWatcher(final IStackWatcher newWatcher) {
        this.myWatcher = newWatcher;
        this.configureWatchers();
    }

    @Override
    public void onStackChange(final IItemList o, final IAEStack fullStack, final IAEStack diffStack,
            final BaseActionSource src, final StorageChannel chan) {
        if (this.configuredFluid != null) {
            if (fullStack == null) {
                this.configuredFluid.setStackSize(0);
            } else {
                this.configuredFluid.setStackSize(fullStack.getStackSize());
            }

            final long stackSize = this.configuredFluid.getStackSize();
            final String humanReadableText = NUMBER_CONVERTER.toWideReadableForm(stackSize);

            if (!humanReadableText.equals(this.lastHumanReadableText)) {
                this.lastHumanReadableText = humanReadableText;
                this.getHost().markForUpdate();
            }
        }
    }

    @Override
    public boolean showNetworkInfo(final MovingObjectPosition where) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventory(final IPartRenderHelper rh, final RenderBlocks renderer) {
        rh.setBounds(2, 2, 14, 14, 14, 16);

        final IIcon sideTexture = CableBusTextures.PartMonitorSides.getIcon();
        final IIcon backTexture = CableBusTextures.PartMonitorBack.getIcon();

        rh.setTexture(
                sideTexture,
                sideTexture,
                backTexture,
                FCPartsTexture.PartTerminalBroad.getIcon(),
                sideTexture,
                sideTexture);
        rh.renderInventoryBox(renderer);

        rh.setInvColor(this.getColor().whiteVariant);
        rh.renderInventoryFace(this.getFrontBright().getIcon(), ForgeDirection.SOUTH, renderer);

        rh.setInvColor(this.getColor().mediumVariant);
        rh.renderInventoryFace(this.getFrontDark().getIcon(), ForgeDirection.SOUTH, renderer);

        rh.setInvColor(this.getColor().blackVariant);
        rh.renderInventoryFace(this.getFrontColored().getIcon(), ForgeDirection.SOUTH, renderer);

        rh.setBounds(4, 4, 13, 12, 12, 14);
        rh.renderInventoryBox(renderer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderStatic(final int x, final int y, final int z, final IPartRenderHelper rh,
            final RenderBlocks renderer) {
        this.setRenderCache(rh.useSimplifiedRendering(x, y, z, this, this.getRenderCache()));

        final IIcon sideTexture = CableBusTextures.PartMonitorSides.getIcon();
        final IIcon backTexture = CableBusTextures.PartMonitorBack.getIcon();

        rh.setTexture(
                sideTexture,
                sideTexture,
                backTexture,
                FCPartsTexture.PartTerminalBroad.getIcon(),
                sideTexture,
                sideTexture);

        rh.setBounds(2, 2, 14, 14, 14, 16);
        rh.renderBlock(x, y, z, renderer);

        if (this.getLightLevel() > 0) {
            final int l = 13;
            Tessellator.instance.setBrightness(l << 20 | l << 4);
        }

        renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = this
                .getSpin();

        Tessellator.instance.setColorOpaque_I(this.getColor().whiteVariant);
        rh.renderFace(x, y, z, this.getFrontBright().getIcon(), ForgeDirection.SOUTH, renderer);

        Tessellator.instance.setColorOpaque_I(this.getColor().mediumVariant);
        rh.renderFace(x, y, z, this.getFrontDark().getIcon(), ForgeDirection.SOUTH, renderer);

        Tessellator.instance.setColorOpaque_I(this.getColor().blackVariant);
        rh.renderFace(x, y, z, this.getFrontColored().getIcon(), ForgeDirection.SOUTH, renderer);

        renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;

        final IIcon sideStatusTexture = CableBusTextures.PartMonitorSidesStatus.getIcon();

        rh.setTexture(
                sideStatusTexture,
                sideStatusTexture,
                backTexture,
                FCPartsTexture.PartTerminalBroad.getIcon(),
                sideStatusTexture,
                sideStatusTexture);

        rh.setBounds(4, 4, 13, 12, 12, 14);
        rh.renderBlock(x, y, z, renderer);

        final boolean hasChan = (this.getClientFlags() & (PartPanel.POWERED_FLAG | PartPanel.CHANNEL_FLAG))
                == (PartPanel.POWERED_FLAG | PartPanel.CHANNEL_FLAG);
        final boolean hasPower = (this.getClientFlags() & PartPanel.POWERED_FLAG) == PartPanel.POWERED_FLAG;

        if (hasChan) {
            final int l = 14;
            Tessellator.instance.setBrightness(l << 20 | l << 4);
            Tessellator.instance.setColorOpaque_I(this.getColor().blackVariant);
        } else if (hasPower) {
            final int l = 9;
            Tessellator.instance.setBrightness(l << 20 | l << 4);
            Tessellator.instance.setColorOpaque_I(this.getColor().whiteVariant);
        } else {
            Tessellator.instance.setBrightness(0);
            Tessellator.instance.setColorOpaque_I(0x000000);
        }

        final IIcon sideStatusLightTexture = CableBusTextures.PartMonitorSidesStatusLights.getIcon();

        rh.renderFace(x, y, z, sideStatusLightTexture, ForgeDirection.EAST, renderer);
        rh.renderFace(x, y, z, sideStatusLightTexture, ForgeDirection.WEST, renderer);
        rh.renderFace(x, y, z, sideStatusLightTexture, ForgeDirection.UP, renderer);
        rh.renderFace(x, y, z, sideStatusLightTexture, ForgeDirection.DOWN, renderer);
    }

    public IAEFluidStack getAEStoreFluidStack() {
        return this.configuredFluid;
    }
}
