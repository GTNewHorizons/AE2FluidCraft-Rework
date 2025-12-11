package com.glodblock.github.common.item;

import static com.glodblock.github.common.item.ItemWirelessUltraTerminal.MODE;
import static com.glodblock.github.loader.recipe.WirelessTerminalEnergyRecipe.getEnergyCard;
import static com.glodblock.github.loader.recipe.WirelessTerminalRecipe.getInfinityBoosterCard;
import static com.glodblock.github.util.Util.DimensionalCoordSide.hasEnergyCard;
import static com.glodblock.github.util.Util.hasInfinityBoosterCard;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IItemInventory;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.UltraTerminalModes;

import appeng.api.AEApi;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.core.sync.GuiBridge;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBaseWirelessTerminal extends ToolWirelessTerminal implements IItemInventory, IGuiItem {

    protected Object type;
    public static String infinityBoosterCard = "infinityBoosterCard";
    public static String infinityEnergyCard = "InfinityEnergyCard";

    public ItemBaseWirelessTerminal(Object t) {
        super();
        this.type = t;
    }

    @Override
    public ItemStack onItemRightClick(final ItemStack item, final World w, final EntityPlayer player) {
        if (player.isSneaking()) return removeInfinityBoosterCard(player, item); // todo: doesn't work in universal
                                                                                 // terminal
        return super.onItemRightClick(item, w, player);
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
            if (hasEnergyCard(stack)) {
                lines.add("  " + EnumChatFormatting.GOLD + getEnergyCard().getDisplayName());
            }
        } else {
            lines.add(NameConst.i18n(NameConst.TT_CTRL_FOR_MORE));
        }
    }

    @Override
    public boolean canHandle(final ItemStack is) {
        if (is == null) {
            return false;
        }
        return is.getItem() == this;
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }

    @Override
    public Object getInventory(ItemStack stack, World world, int x, int y, int z, EntityPlayer player) {
        return null;
    }

    public static UltraTerminalModes getMode(ItemStack is) {
        Item item = is.getItem();
        if (item instanceof ItemWirelessUltraTerminal) {
            if (!is.hasTagCompound()) is.setTagCompound(new NBTTagCompound());
            return UltraTerminalModes.values()[is.getTagCompound().getInteger(MODE)];
        } else if (item instanceof ItemWirelessPatternTerminal) {
            return UltraTerminalModes.PATTERN;
        } else if (item instanceof ItemWirelessInterfaceTerminal) {
            return UltraTerminalModes.INTERFACE;
        } else if (item instanceof ItemWirelessLevelTerminal) {
            return UltraTerminalModes.LEVEL;
        } else {
            return UltraTerminalModes.INTERFACE; // any wireless gui object will fit
        }
    }

    public static void setMode(ItemStack is, UltraTerminalModes utm) {
        is.getTagCompound().setInteger(MODE, utm.ordinal());
    }

    @Override
    public void openGui(final ItemStack is, final World w, final EntityPlayer player, final Object mode) {
        final GuiBridge aeGui;
        Item item = is.getItem();
        if (item instanceof ItemWirelessPatternTerminal) {
            aeGui = GuiBridge.GUI_PATTERN_TERMINAL;
        } else if (item instanceof ItemWirelessInterfaceTerminal) {
            aeGui = GuiBridge.GUI_INTERFACE_TERMINAL;
        } else if (item instanceof ItemWirelessLevelTerminal) {
            InventoryHandler.openGui(
                    player,
                    w,
                    new BlockPos(player.inventory.currentItem, 0, 0),
                    ForgeDirection.UNKNOWN,
                    GuiType.WIRELESS_LEVEL_TERMINAL);
            return;
        } else {
            aeGui = GuiBridge.GUI_INTERFACE_TERMINAL; // any wireless gui object will fit
        }
        Platform.openGUI(player, null, null, aeGui);
    }

    @Override
    public IGuiItemObject getGuiObject(ItemStack is, World world, EntityPlayer p, int x, int y, int z) {
        final IWirelessTermHandler wh = AEApi.instance().registries().wireless().getWirelessTerminalHandler(is);
        if (wh == null) return null;
        return new WirelessTerminalGuiObject(wh, is, p, world, x, y, z);
    }

    @Override
    public boolean hasInfinityPower(ItemStack is) {
        NBTTagCompound data = is.getTagCompound();
        return data != null && data.hasKey(infinityEnergyCard) && data.getBoolean(infinityEnergyCard);
    }

    @Override
    public boolean hasInfinityRange(ItemStack is) {
        NBTTagCompound data = is.getTagCompound();
        return data != null && data.hasKey(infinityBoosterCard) && data.getBoolean(infinityBoosterCard);
    }
}
