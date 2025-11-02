package com.glodblock.github.common.block;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidContainerItem;

import com.glodblock.github.common.item.ItemCertusQuartzTank;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.common.tile.TileCertusQuartzTank;
import com.glodblock.github.loader.IRegister;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.NameConst;

import appeng.api.implementations.items.IAEWrench;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCertusQuartzTank extends BaseBlockContainer implements IRegister<BlockCertusQuartzTank> {

    IIcon breakIcon;
    IIcon topIcon;
    IIcon bottomIcon;
    IIcon sideIcon;
    IIcon sideMiddleIcon;
    IIcon sideTopIcon;
    IIcon sideBottomIcon;

    public BlockCertusQuartzTank() {
        super(Material.glass);
        setBlockName(NameConst.BLOCK_CERTUS_QUARTZ_TANK);
        setResistance(10.0F);
        setHardness(2.0F);
        setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 1.0F, 0.9375F);
    }

    @Override
    public boolean canRenderInPass(int pass) {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        return new TileCertusQuartzTank();
    }

    public ItemStack getDropWithNBT(World world, int x, int y, int z) {
        NBTTagCompound tileEntity = new NBTTagCompound();
        TileEntity worldTE = world.getTileEntity(x, y, z);
        if (worldTE instanceof TileCertusQuartzTank) {
            ItemStack dropStack = new ItemStack(ItemAndBlockHolder.CERTUS_QUARTZ_TANK, 1);

            ((TileCertusQuartzTank) worldTE).writeToNBTWithoutCoords(tileEntity);

            if (!tileEntity.hasKey("Empty")) {
                dropStack.setTagCompound(new NBTTagCompound());
                dropStack.stackTagCompound.setTag("tileEntity", tileEntity);
            }
            return dropStack;

        }
        return null;
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return switch (meta) {
            case 1 -> this.sideTopIcon;
            case 2 -> this.sideBottomIcon;
            case 3 -> this.sideMiddleIcon;
            default -> side == 0 ? this.bottomIcon : side == 1 ? this.topIcon : this.sideIcon;
        };
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        return getDropWithNBT(world, x, y, z);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileCertusQuartzTank tank) {
            return tank.getFluidLightLevel();
        }
        return super.getLightValue(world, x, y, z);
    }

    @Override
    public boolean onBlockActivated(World worldObj, int x, int y, int z, EntityPlayer player, int side, float offsetX,
            float offsetY, float offsetZ) {
        ItemStack itemInHand = player.inventory.getCurrentItem();

        if (itemInHand == null || itemInHand.getItem() == null) {
            return false;
        }

        // In case if we're holding a IFluidContainerItem which is also a block we'll just allow to place a block
        // instead of trying to fill or drain it. In other cases we don't need to handle blocks either way
        if (itemInHand.getItem() instanceof ItemBlock) {
            return false;
        }

        if (player.isSneaking()) {
            if (itemInHand.getItem() instanceof IAEWrench wrench && wrench.canWrench(itemInHand, player, x, y, z)) {
                dropBlockAsItem(worldObj, x, y, z, getDropWithNBT(worldObj, x, y, z));
                worldObj.setBlockToAir(x, y, z);
                return true;
            }
        }

        TileCertusQuartzTank tank = (TileCertusQuartzTank) worldObj.getTileEntity(x, y, z);
        boolean isCreativeMode = player.capabilities.isCreativeMode;

        if (FluidContainerRegistry.isFilledContainer(itemInHand)) {
            // Fill the tank from the container in hand
            FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(itemInHand);

            if (fluid == null) {
                return false;
            }

            int amountFilled = tank.fill(ForgeDirection.UNKNOWN, fluid, true);
            if (amountFilled > 0 && !isCreativeMode) {
                ItemStack empty = FluidContainerRegistry.drainFluidContainer(itemInHand);
                replaceItemInInventory(worldObj, player, itemInHand, empty);
            }

            return true;
        } else if (FluidContainerRegistry.isEmptyContainer(itemInHand)) {
            // Fill the container in hand from the tank
            FluidStack available = tank.getTankInfo(true)[0].fluid;
            ItemStack filled = FluidContainerRegistry.fillFluidContainer(available, itemInHand);
            FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(filled);

            if (fluid == null) {
                return false;
            }

            tank.drain(ForgeDirection.UNKNOWN, fluid.amount, true);
            if (!isCreativeMode) {
                replaceItemInInventory(worldObj, player, itemInHand, filled);
            }

            return true;
        } else if (itemInHand.getItem() instanceof IFluidContainerItem) {
            ItemStack singleContainerItem = itemInHand.copy();
            singleContainerItem.stackSize = 1;

            IFluidContainerItem fluidContainer = (IFluidContainerItem) singleContainerItem.getItem();
            assert fluidContainer != null;

            FluidStack fluid = fluidContainer.getFluid(itemInHand);

            if (fluid != null && fluid.amount > 0) {
                // Fill the tank from the container in hand
                int filled = tank.fill(ForgeDirection.UNKNOWN, fluid, true);
                if (filled > 0 && !isCreativeMode) {
                    fluidContainer.drain(singleContainerItem, filled, true);
                    replaceItemInInventory(worldObj, player, itemInHand, singleContainerItem);
                }
            } else {
                // Fill the container in hand from the tank
                int containerCapacity = fluidContainer.getCapacity(itemInHand);
                FluidStack drained = tank.drain(ForgeDirection.UNKNOWN, containerCapacity, true);
                if (drained != null && drained.amount > 0 && !isCreativeMode) {
                    fluidContainer.fill(singleContainerItem, drained, true);
                    replaceItemInInventory(worldObj, player, itemInHand, singleContainerItem);
                }
            }

            return true;
        }

        return false;
    }

    private void replaceItemInInventory(World world, EntityPlayer player, ItemStack item, ItemStack replacement) {
        if (item.stackSize > 1) {
            item.stackSize--;
            // This method meant to be called both from client and server side, and trying to drop an item
            // from the client side would create a doubled ghost item, so we don't allow to do that
            if (!player.inventory.addItemStackToInventory(replacement) && !world.isRemote) {
                player.entityDropItem(replacement, 0);
            }
        } else {
            player.inventory.setInventorySlotContents(player.inventory.currentItem, replacement);
        }
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        if (stack != null && stack.hasTagCompound()) {
            if (FluidStack.loadFluidStackFromNBT(stack.getTagCompound().getCompoundTag("tileEntity")) != null) list.add(
                    FluidStack.loadFluidStackFromNBT(stack.getTagCompound().getCompoundTag("tileEntity")).amount
                            + "mB");
        }
    }

    @Override
    public void registerBlockIcons(IIconRegister iconregister) {
        String folder = NameConst.RES_KEY + "certus_quartz_tank/";
        this.breakIcon = iconregister.registerIcon(folder + "cube");
        this.topIcon = iconregister.registerIcon(folder + "top");
        this.bottomIcon = iconregister.registerIcon(folder + "bottom");
        this.sideIcon = iconregister.registerIcon(folder + "side");
        this.sideMiddleIcon = iconregister.registerIcon(folder + "side_mid");
        this.sideTopIcon = iconregister.registerIcon(folder + "side_top");
        this.sideBottomIcon = iconregister.registerIcon(folder + "side_bottom");
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public BlockCertusQuartzTank register() {
        GameRegistry.registerBlock(this, ItemCertusQuartzTank.class, NameConst.BLOCK_CERTUS_QUARTZ_TANK);
        GameRegistry.registerTileEntity(TileCertusQuartzTank.class, NameConst.BLOCK_CERTUS_QUARTZ_TANK);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride(World worldIn, int x, int y, int z, int side) {
        TileEntity tileEntity = worldIn.getTileEntity(x, y, z);
        if (tileEntity instanceof TileCertusQuartzTank tileCertusQuartzTank) {
            FluidTankInfo info = tileCertusQuartzTank.getTankInfo(false)[0];
            if (info.fluid != null) {
                int nonEmptyBump = info.fluid.amount > 0 ? 1 : 0;
                return (int) (14.0 * info.fluid.amount / info.capacity) + nonEmptyBump;
            }
        }
        return 0;
    }
}
