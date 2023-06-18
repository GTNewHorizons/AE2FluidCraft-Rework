package com.glodblock.github.crossmod.extracells;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.networking.IGridNode;
import appeng.api.parts.*;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.registry.GameRegistry;
import io.netty.buffer.ByteBuf;

/**
 * ProxyPart is expected to be immediately replaced with the replacement part on load.
 */
public class ProxyPart implements IPart, IPartDeprecated {

    /**
     * Prevent memory leaks by having one of the instances be a weak ref.
     */
    private final WeakReference<ProxyPartItem> item;

    /**
     * Name for debug
     */
    private final String name;

    public ProxyPart(ProxyPartItem item) {
        this.item = new WeakReference<>(item);
        this.name = item.name;
    }

    @Override
    public ItemStack getItemStack(PartItemStack type) {
        return null;
    }

    @Override
    public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer) {}

    @Override
    public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer) {}

    @Override
    public void renderDynamic(double x, double y, double z, IPartRenderHelper rh, RenderBlocks renderer) {}

    @Override
    public IIcon getBreakingTexture() {
        return null;
    }

    @Override
    public boolean requireDynamicRender() {
        return false;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public boolean canConnectRedstone() {
        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {}

    @Override
    public void readFromNBT(NBTTagCompound data) {}

    @Override
    public int getLightLevel() {
        return 0;
    }

    @Override
    public boolean isLadder(EntityLivingBase entity) {
        return false;
    }

    @Override
    public void onNeighborChanged() {}

    @Override
    public int isProvidingStrongPower() {
        return 0;
    }

    @Override
    public int isProvidingWeakPower() {
        return 0;
    }

    @Override
    public void writeToStream(ByteBuf data) {}

    @Override
    public boolean readFromStream(ByteBuf data) {
        return false;
    }

    @Override
    public IGridNode getGridNode() {
        return null;
    }

    @Override
    public void onEntityCollision(Entity entity) {}

    @Override
    public void removeFromWorld() {}

    @Override
    public void addToWorld() {}

    @Override
    public IGridNode getExternalFacingNode() {
        return null;
    }

    @Override
    public void setPartHostInfo(ForgeDirection side, IPartHost host, TileEntity tile) {}

    @Override
    public boolean onActivate(EntityPlayer player, Vec3 pos) {
        return false;
    }

    @Override
    public boolean onShiftActivate(EntityPlayer player, Vec3 pos) {
        return false;
    }

    @Override
    public void getDrops(List<ItemStack> drops, boolean wrenched) {}

    @Override
    public int cableConnectionRenderTo() {
        return 0;
    }

    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random r) {

    }

    @Override
    public void onPlacement(EntityPlayer player, ItemStack held, ForgeDirection side) {}

    @Override
    public boolean canBePlacedOn(BusSupport what) {
        return false;
    }

    @Override
    public void getBoxes(IPartCollisionHelper boxes) {}

    @Override
    public NBTTagCompound transformPart(NBTTagCompound def) {
        int meta = def.getShort("Damage");
        if (item.get() == null) {
            throw new NullPointerException("Umm, this wasn't supposed to happen. The ref item " + name + " was GC'd.");
        }
        ProxyItem.ProxyItemEntry r = item.get().replacements.get(meta);
        def.setInteger("id", Item.getIdFromItem(r.replacement));
        def.setShort("Damage", (short) r.replacementMeta);
        return def;
    }

    @Nonnull
    @Override
    public NBTTagCompound transformNBT(NBTTagCompound extra) {
        return extra;
    }

    /**
     * Creates a new fluid display NBT tag from the fluid name
     */
    public static NBTTagCompound createFluidDisplayTag(String fluidName) {
        NBTTagCompound fluidDisplay = new NBTTagCompound();
        Fluid fluid = FluidRegistry.getFluid(fluidName);
        if (fluid != null) {
            ItemStack fluidPacket = new ItemStack(GameRegistry.findItem("ae2fc", "fluid_packet"), 1, 0);
            NBTTagCompound fluidPacketTag = new NBTTagCompound();
            // FluidStack
            FluidStack fluidStack = new FluidStack(fluid, 1000);
            NBTTagCompound fluidStackNbt = new NBTTagCompound();
            fluidStack.writeToNBT(fluidStackNbt);
            fluidPacketTag.setTag("FluidStack", fluidStackNbt);
            // FluidPacket
            fluidPacketTag.setBoolean("DisplayOnly", true);
            fluidPacket.setTagCompound(fluidPacketTag);
            // Final Item
            IAEItemStack aeStack = AEItemStack.create(fluidPacket);
            aeStack.writeToNBT(fluidDisplay);
        }
        return fluidDisplay;
    }

    public static NBTTagCompound createFluidNBT(String fluidName, long amount) {
        NBTTagCompound fluid = new NBTTagCompound();
        fluid.setString("FluidName", fluidName);
        fluid.setBoolean("Craft", false);
        fluid.setLong("Req", 0);
        fluid.setLong("Cnt", amount);
        return fluid;
    }
}
