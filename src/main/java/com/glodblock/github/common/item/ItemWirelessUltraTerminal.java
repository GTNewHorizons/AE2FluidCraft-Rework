package com.glodblock.github.common.item;

import static appeng.util.Platform.nextEnum;
import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.inventory.item.WirelessLevelTerminalInventory;
import com.glodblock.github.inventory.item.WirelessMagnet;
import com.glodblock.github.inventory.item.WirelessMagnetCardFilterInventory;
import com.glodblock.github.loader.IRegister;
import com.glodblock.github.network.CPacketSwitchGuis;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.UltraTerminalModes;
import com.glodblock.github.util.Util;

import appeng.api.AEApi;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.networking.IGridNode;
import appeng.core.features.AEFeature;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.WirelessPatternTerminalGuiObject;
import appeng.util.Platform;
import baubles.api.BaublesApi;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemWirelessUltraTerminal extends ItemBaseWirelessTerminal
        implements IRegister<ItemWirelessUltraTerminal>, IGuiItem {

    public final static String MODE = "mode";

    public ItemWirelessUltraTerminal() {
        super(null);
        AEApi.instance().registries().wireless().registerWirelessHandler(this);
        this.setFeature(EnumSet.of(AEFeature.WirelessAccessTerminal, AEFeature.PoweredTools));
        setUnlocalizedName(NameConst.ITEM_WIRELESS_ULTRA_TERMINAL);
        setTextureName(FluidCraft.resource(NameConst.ITEM_WIRELESS_ULTRA_TERMINAL).toString());
    }

    @Override
    public ItemWirelessUltraTerminal register() {
        GameRegistry.registerItem(this, NameConst.ITEM_WIRELESS_ULTRA_TERMINAL, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addCheckedInformation(ItemStack stack, EntityPlayer player, List<String> lines,
            boolean displayMoreInfo) {
        super.addCheckedInformation(stack, player, lines, displayMoreInfo);
        if (isShiftKeyDown()) {
            lines.add(StatCollector.translateToLocal(NameConst.TT_ULTRA_TERMINAL));
            lines.add(StatCollector.translateToLocal(NameConst.TT_ULTRA_TERMINAL + "." + getMode(stack)));
            lines.add(NameConst.i18n(NameConst.TT_ULTRA_TERMINAL_TIPS));
            lines.addAll(Arrays.asList(NameConst.i18n(NameConst.TT_ULTRA_TERMINAL_TIPS_DESC).split("\\\\n")));
        } else {
            lines.add(NameConst.i18n(NameConst.TT_SHIFT_FOR_MORE));
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StatCollector.translateToLocalFormatted("item.wireless_ultra_terminal." + getMode(stack) + ".name");
    }

    @Override
    public Object getInventory(ItemStack stack, World world, int x, int y, int z, EntityPlayer player) {
        try {
            final IGridNode gridNode = Util.getWirelessGrid(stack);

            // wireless magnet
            if (Util.GuiHelper.decodeType(y).getLeft() == Util.GuiHelper.GuiType.ITEM && z == -1) {
                return new WirelessMagnetCardFilterInventory(stack, x, gridNode, player);
            }

            if (getMode(stack) == UltraTerminalModes.LEVEL) {
                return new WirelessLevelTerminalInventory(stack, x, gridNode, player);
            }
        } catch (Exception e) {
            player.addChatMessage(PlayerMessages.OutOfRange.toChat());
        }
        return null;
    }

    @Override
    public ItemStack onItemRightClick(final ItemStack is, final World w, final EntityPlayer player) {
        if (player.isSneaking()) {
            setMode(is, nextEnum(getMode(is)));
            return is;
        }
        return super.onItemRightClick(is, w, player);
    }

    public static void switchTerminal(EntityPlayer player, UltraTerminalModes mode) {
        ImmutablePair<Integer, ItemStack> temp = Util.getUltraWirelessTerm(player);
        if (temp == null) return;
        ItemStack term = temp.getRight();
        if (term != null && term.getItem() instanceof ItemWirelessUltraTerminal) {
            if (mode != null) setMode(term, mode);
            else mode = getMode(term);

            if (Platform.isClient()) FluidCraft.proxy.netHandler.sendToServer(new CPacketSwitchGuis(mode, true));
            else openGui(term, player.worldObj, player);
        }
    }

    public static boolean hasInfinityBoosterCard(EntityPlayer player) {
        ImmutablePair<Integer, ItemStack> term = Util.getUltraWirelessTerm(player);
        if (term == null) return false;
        if (term.getRight().getItem() instanceof ItemWirelessUltraTerminal) {
            return Util.hasInfinityBoosterCard(term.getRight());
        }
        return false;
    }

    @Override
    public void onWornTick(ItemStack itemStack, EntityLivingBase entityLivingBase) {
        if (Platform.isServer() && entityLivingBase instanceof EntityPlayer player) {
            IInventory handler = BaublesApi.getBaubles(player);
            if (handler != null) {
                for (int i = 0; i < handler.getSizeInventory(); ++i) {
                    if (handler.getStackInSlot(i) == itemStack) {
                        onUpdate(
                                itemStack,
                                null,
                                player,
                                Util.GuiHelper.encodeType(i, Util.GuiHelper.InvType.PLAYER_BAUBLES),
                                false);
                    }
                }
            }
        }
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int slot, boolean p_77663_5_) {
        if (entityIn instanceof EntityPlayer player) {
            if (WirelessMagnet.getMode(stack) != WirelessMagnet.Mode.Off) {
                WirelessMagnet.doMagnet(stack, player);
            }
        }
    }

    @Override
    public IGuiItemObject getGuiObject(ItemStack is, World world, EntityPlayer p, int x, int y, int z) {
        final IWirelessTermHandler wh = AEApi.instance().registries().wireless().getWirelessTerminalHandler(is);
        if (wh == null) return null;
        return new WirelessPatternTerminalGuiObject(wh, is, p, world, x, y, z);
    }
}
