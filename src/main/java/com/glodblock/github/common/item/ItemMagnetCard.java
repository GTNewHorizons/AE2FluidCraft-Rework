package com.glodblock.github.common.item;

import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.inventory.ItemBiggerAppEngInventory;
import com.glodblock.github.inventory.item.WirelessMagnetCardFilterInventory;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.RenderUtil;
import com.glodblock.github.util.Util;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemMagnetCard extends FCBaseItem {

    public static String modeKey = "MagnetMode";
    public static String filterKey = "MagnetFilter";
    public static String filterConfigKey = "MagnetConfig";
    public static final int distanceFromPlayer = 16;

    public ItemMagnetCard() {
        super();
        this.setMaxStackSize(64);
        this.setUnlocalizedName(NameConst.ITEM_MAGNET_CARD);
        this.setTextureName(FluidCraft.resource(NameConst.ITEM_MAGNET_CARD).toString());
    }

    public static List<? extends Entity> getEntitiesInRange(Class<?> entityType, World world, int x, int y, int z,
            int distance) {
        return world.getEntitiesWithinAABB(
                entityType,
                AxisAlignedBB.getBoundingBox(
                        x - distance,
                        y - distance,
                        z - distance,
                        x + distance,
                        y + distance,
                        z + distance));
    }

    @SuppressWarnings("unchecked")
    public static List<ItemStack> getFilteredItems(ItemStack wirelessTerm) {
        if (wirelessTerm == null) {
            return null;
        }
        if (wirelessTerm.getItem() instanceof ItemWirelessUltraTerminal) {
            if (wirelessTerm.hasTagCompound()) {
                NBTTagCompound data = Platform.openNbtData(wirelessTerm);
                if (!data.hasKey(filterKey)) {
                    return Collections.emptyList();
                }
                AppEngInternalInventory filterList = new ItemBiggerAppEngInventory(wirelessTerm, filterKey, 27);
                ArrayList<ItemStack> list = new ArrayList<>();
                filterList.iterator().forEachRemaining(list::add);
                return list;
            }
        }
        return Collections.emptyList();
    }

    public static boolean isConfigured(ItemStack wirelessTerm) {
        NBTTagCompound data = Platform.openNbtData(wirelessTerm);
        return data.hasKey(modeKey);
    }

    public static void doMagnet(ItemStack wirelessTerm, World world, EntityPlayer player,
            WirelessMagnetCardFilterInventory inv) {
        if (Platform.isClient() || wirelessTerm == null
                || ItemMagnetCard.getMode(wirelessTerm) == Mode.Off
                || player == null
                || player.isSneaking()
                || !isConfigured(wirelessTerm))
            return;

        Iterator<? extends Entity> iterator = getEntitiesInRange(
                EntityItem.class,
                world,
                (int) player.posX,
                (int) player.posY,
                (int) player.posZ,
                distanceFromPlayer).iterator();
        IItemList<IAEItemStack> filteredList = inv.getAEFilteredItems();
        while (iterator.hasNext()) {
            EntityItem itemToGet = (EntityItem) iterator.next();
            if (itemToGet.func_145800_j() != null && itemToGet.func_145800_j().equals(player.getCommandSenderName())) {
                continue;
            }

            EntityPlayer closestPlayer = world.getClosestPlayerToEntity(itemToGet, distanceFromPlayer);

            if (closestPlayer != null && closestPlayer != player) {
                continue;
            }
            ItemStack stack = itemToGet.getEntityItem();
            if ((inv.getListMode() == ListMode.WhiteList && !filteredList.isEmpty()
                    && inv.isItemFiltered(stack, filteredList))
                    || (inv.getListMode() == ListMode.BlackList && !inv.isItemFiltered(stack, filteredList))) {
                if (itemToGet.delayBeforeCanPickup > 0) {
                    itemToGet.delayBeforeCanPickup = 0;
                }
                itemToGet.motionX = itemToGet.motionY = itemToGet.motionZ = 0;
                itemToGet.setPosition(
                        player.posX - 0.2 + (world.rand.nextDouble() * 0.4),
                        player.posY - 0.6,
                        player.posZ - 0.2 + (world.rand.nextDouble() * 0.4));
            }
        }

        // xp
        iterator = getEntitiesInRange(
                EntityXPOrb.class,
                world,
                (int) player.posX,
                (int) player.posY,
                (int) player.posZ,
                distanceFromPlayer).iterator();
        while (iterator.hasNext()) {
            EntityXPOrb xpToGet = (EntityXPOrb) iterator.next();
            if (xpToGet.isDead || xpToGet.isInvisible()) {
                continue;
            }
            int xpAmount = xpToGet.xpValue;
            xpToGet.xpValue = 0;
            player.xpCooldown = 0;
            player.addExperience(xpAmount);
            xpToGet.setDead();
            xpToGet.setInvisible(true);
            world.playSoundAtEntity(
                    player,
                    "random.orb",
                    0.08F,
                    0.5F * ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.8F));
        }
    }

    @Override
    public ItemMagnetCard register() {
        GameRegistry.registerItem(this, NameConst.ITEM_MAGNET_CARD, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List toolTip, boolean flag) {
        if (isShiftKeyDown()) {
            toolTip.addAll(RenderUtil.listFormattedStringToWidth(NameConst.i18n(NameConst.TT_MAGNET_CARD_DESC)));
        } else {
            toolTip.add(NameConst.i18n(NameConst.TT_SHIFT_FOR_MORE));
        }
    }

    public enum Mode {
        Off,
        Inv,
        ME
    }

    public enum ListMode {
        WhiteList,
        BlackList
    }

    public static Mode getMode(ItemStack is) {
        if (is != null && Util.Wireless.hasMagnetCard(is) && is.getItem() instanceof ItemWirelessUltraTerminal) {
            NBTTagCompound data = Platform.openNbtData(is);
            if (data.hasKey(modeKey)) {
                return Mode.values()[data.getInteger(modeKey)];
            }
            setMode(is, Mode.Off);
        }
        return Mode.Off;
    }

    public static void setMode(ItemStack is, Mode mode) {
        if (is != null && Util.Wireless.hasMagnetCard(is) && is.getItem() instanceof ItemWirelessUltraTerminal) {
            NBTTagCompound data = Platform.openNbtData(is);
            data.setInteger(modeKey, mode.ordinal());
        }
    }

}
