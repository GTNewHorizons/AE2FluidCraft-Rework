package com.glodblock.github.common.item;

import static com.glodblock.github.loader.recipe.WirelessTerminalEnergyRecipe.getEnergyCard;
import static com.glodblock.github.loader.recipe.WirelessTerminalInfinityBoosterRecipe.getInfinityBoosterCard;
import static com.glodblock.github.loader.recipe.WirelessTerminalMagnetRecipe.getMagnetCard;
import static com.glodblock.github.util.Util.Wireless.hasEnergyCard;
import static com.glodblock.github.util.Util.Wireless.hasInfinityBoosterCard;
import static com.glodblock.github.util.Util.Wireless.hasMagnetCard;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;

import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IItemInventory;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.NameConst;

import appeng.api.AEApi;
import appeng.api.features.ILocatable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.features.IWirelessTermRegistry;
import appeng.core.localization.PlayerMessages;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBaseWirelessTerminal extends ToolWirelessTerminal implements IItemInventory {

    protected GuiType type;
    public static String infinityBoosterCard = "infinityBoosterCard";
    public static String magnetCard = "magnetCard";
    public static String restockItems = "restock";
    public static String infinityEnergyCard = "InfinityEnergyCard";
    public static String syncData = "syncData";
    private static boolean effect = false;

    public ItemBaseWirelessTerminal(GuiType t) {
        super();
        this.type = t;
    }

    public static void setEffect(boolean e) {
        effect = e;
    }

    public static boolean getEffect() {
        return effect;
    }

    @Override
    public boolean hasEffect(ItemStack par1ItemStack, int pass) {
        if (Platform.isClient()) {
            return getEffect();
        }
        return super.hasEffect(par1ItemStack, pass);
    }

    @Override
    public ItemStack onItemRightClick(final ItemStack item, final World w, final EntityPlayer player) {
        if (player.isSneaking()) return removeInfinityBoosterCard(player, item);
        if (ForgeEventFactory.onItemUseStart(player, item, 1) > 0) {
            if (Platform.isClient()) {
                setEffect(false);
                return item;
            }
            IWirelessTermRegistry term = AEApi.instance().registries().wireless();
            if (!term.isWirelessTerminal(item)) {
                player.addChatMessage(PlayerMessages.DeviceNotWirelessTerminal.get());
                return item;
            }
            final IWirelessTermHandler handler = term.getWirelessTerminalHandler(item);
            final String unparsedKey = handler.getEncryptionKey(item);
            if (unparsedKey.isEmpty()) {
                player.addChatMessage(PlayerMessages.DeviceNotLinked.get());
                return item;
            }
            final long parsedKey = Long.parseLong(unparsedKey);
            final ILocatable securityStation = AEApi.instance().registries().locatable().getLocatableBy(parsedKey);
            if (securityStation == null) {
                player.addChatMessage(PlayerMessages.StationCanNotBeLocated.get());
                return item;
            }
            if (handler.hasPower(player, 0.5, item)) {
                InventoryHandler.openGui(
                        player,
                        w,
                        new BlockPos(player.inventory.currentItem, 0, 0),
                        ForgeDirection.UNKNOWN,
                        this.guiGuiType(item));
            } else {
                player.addChatMessage(PlayerMessages.DeviceNotPowered.get());
            }
        }

        return item;
    }

    private ItemStack removeInfinityBoosterCard(final EntityPlayer player, ItemStack is) {
        if (hasInfinityBoosterCard(is)) {
            if (!player.inventory.addItemStackToInventory(getInfinityBoosterCard())) {
                player.entityDropItem(getInfinityBoosterCard(), 0);
            }
            is.getTagCompound().setBoolean(infinityBoosterCard, false);
        }
        return is;
    }

    private ItemStack removeMagnetCard(final EntityPlayer player, ItemStack is) {
        if (hasMagnetCard(is)) {
            if (!player.inventory.addItemStackToInventory(getMagnetCard())) {
                player.entityDropItem(getMagnetCard(), 0);
            }
            is.getTagCompound().setBoolean(magnetCard, false);
        }
        return is;
    }

    public static void toggleRestockItemsMode(ItemStack is, boolean state) {
        NBTTagCompound data = Platform.openNbtData(is);
        data.setBoolean(restockItems, state);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addCheckedInformation(ItemStack stack, EntityPlayer player, List<String> lines,
            boolean displayMoreInfo) {
        super.addCheckedInformation(stack, player, lines, displayMoreInfo);
        if (GuiScreen.isCtrlKeyDown()) {
            lines.add(NameConst.i18n(NameConst.TT_WIRELESS_INSTALLED));
            if (hasInfinityBoosterCard(stack)) {
                lines.add("  " + EnumChatFormatting.GOLD + getInfinityBoosterCard().getDisplayName());
            }
            if (hasMagnetCard(stack)) {
                lines.add("  " + EnumChatFormatting.GOLD + getMagnetCard().getDisplayName());
            }
            if (hasEnergyCard(stack)) {
                lines.add("  " + EnumChatFormatting.GOLD + getEnergyCard().getDisplayName());
            }
        } else {
            lines.add(NameConst.i18n(NameConst.TT_CTRL_FOR_MORE));
        }
    }

    @Override
    public boolean canHandle(final ItemStack is) {
        return is.getItem() instanceof ItemBaseWirelessTerminal;
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }

    @Override
    public Object getInventory(ItemStack stack, World world, int x, int y, int z, EntityPlayer player) {
        return null;
    }

    public GuiType guiGuiType(ItemStack stack) {
        return this.type;
    }

}
