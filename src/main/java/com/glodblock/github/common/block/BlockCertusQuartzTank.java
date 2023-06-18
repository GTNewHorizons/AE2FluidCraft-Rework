package com.glodblock.github.common.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
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

import com.glodblock.github.common.item.ItemCertusQuartzTank;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.common.tile.TileCertusQuartzTank;
import com.glodblock.github.loader.ChannelLoader;
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
    public boolean onBlockActivated(World worldObj, int x, int y, int z, EntityPlayer entityplayer, int blockID,
            float offsetX, float offsetY, float offsetZ) {
        ItemStack current = entityplayer.inventory.getCurrentItem();

        if (entityplayer.isSneaking() && current != null) {
            if (current.getItem() instanceof IAEWrench
                    && ((IAEWrench) current.getItem()).canWrench(current, entityplayer, x, y, z)) {
                dropBlockAsItem(worldObj, x, y, z, getDropWithNBT(worldObj, x, y, z));
                worldObj.setBlockToAir(x, y, z);
                return true;
            }
        }
        if (current != null) {
            FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(current);
            TileCertusQuartzTank tank = (TileCertusQuartzTank) worldObj.getTileEntity(x, y, z);

            if (liquid != null) {
                int amountFilled = tank.fill(ForgeDirection.UNKNOWN, liquid, true);
                if (amountFilled != 0 && !entityplayer.capabilities.isCreativeMode && current.getItem() != null) {
                    ItemStack empty;
                    if (FluidContainerRegistry.isContainer(current)) {
                        empty = FluidContainerRegistry.drainFluidContainer(current);
                    } else {
                        // Try this as a fallback.
                        entityplayer.inventory.addItemStackToInventory(current.getItem().getContainerItem(current));
                        empty = current.getItem().getContainerItem(current);
                    }
                    if (current.stackSize > 1) {
                        entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem].stackSize -= 1;
                        entityplayer.inventory.addItemStackToInventory(empty);
                    } else {
                        entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem] = empty;
                    }
                }
                return true;
            } else {
                FluidStack available = tank.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid;
                if (available != null) {
                    ItemStack filled = FluidContainerRegistry.fillFluidContainer(available, current);

                    liquid = FluidContainerRegistry.getFluidForFilledItem(filled);

                    if (liquid != null) {
                        tank.drain(ForgeDirection.UNKNOWN, liquid.amount, true);
                        if (!entityplayer.capabilities.isCreativeMode) {
                            if (current.stackSize == 1) {
                                entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem] = filled;
                            } else {
                                entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem].stackSize--;
                                if (!entityplayer.inventory.addItemStackToInventory(filled))
                                    entityplayer.entityDropItem(filled, 0);
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlock) {
        if (!world.isRemote) {
            ChannelLoader.sendPacketToAllPlayers(world.getTileEntity(x, y, z).getDescriptionPacket(), world);
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
}
