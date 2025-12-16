package com.glodblock.github.loader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.lwjgl.input.Keyboard;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.network.CPacketSwitchGuis;
import com.glodblock.github.network.CPacketValueConfig;
import com.glodblock.github.util.UltraTerminalModes;
import com.glodblock.github.util.Util;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeybindLoader implements Runnable {

    public static KeyBinding openTerminal;
    public static KeyBinding openCraftingTerminal;
    public static KeyBinding openLevelTerminal;
    public static KeyBinding openInterfaceTerminal;
    public static KeyBinding openPatternTerminal;
    public static KeyBinding openPatternExTerminal;
    public static KeyBinding restock;

    @Override
    public void run() {
        openTerminal = new KeyBinding(FluidCraft.MODID + ".key.OpenTerminal", Keyboard.CHAR_NONE, "itemGroup.ae2fc");
        openCraftingTerminal = new KeyBinding(
                FluidCraft.MODID + ".key.OpenCraftingTerminal",
                Keyboard.CHAR_NONE,
                "itemGroup.ae2fc");
        openLevelTerminal = new KeyBinding(
                FluidCraft.MODID + ".key.OpenLevelTerminal",
                Keyboard.CHAR_NONE,
                "itemGroup.ae2fc");
        openInterfaceTerminal = new KeyBinding(
                FluidCraft.MODID + ".key.OpenInterfaceTerminal",
                Keyboard.CHAR_NONE,
                "itemGroup.ae2fc");
        openPatternTerminal = new KeyBinding(
                FluidCraft.MODID + ".key.OpenPatternTerminal",
                Keyboard.CHAR_NONE,
                "itemGroup.ae2fc");
        openPatternExTerminal = new KeyBinding(
                FluidCraft.MODID + ".key.OpenPatternExTerminal",
                Keyboard.CHAR_NONE,
                "itemGroup.ae2fc");
        restock = new KeyBinding(FluidCraft.MODID + ".key.Restock", Keyboard.CHAR_NONE, "itemGroup.ae2fc");
        ClientRegistry.registerKeyBinding(openTerminal);
        ClientRegistry.registerKeyBinding(openCraftingTerminal);
        ClientRegistry.registerKeyBinding(openLevelTerminal);
        ClientRegistry.registerKeyBinding(openInterfaceTerminal);
        ClientRegistry.registerKeyBinding(openPatternTerminal);
        ClientRegistry.registerKeyBinding(openPatternExTerminal);
        ClientRegistry.registerKeyBinding(restock);
        FMLCommonHandler.instance().bus().register(this);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        handleKeybindings();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        handleKeybindings();
    }

    private void handleKeybindings() {
        if (openTerminal.isPressed()) {
            handleOpenTerminalKey(null);
        } else if (openCraftingTerminal.isPressed()) {
            handleOpenTerminalKey(UltraTerminalModes.CRAFTING);
        } else if (openLevelTerminal.isPressed()) {
            handleOpenTerminalKey(UltraTerminalModes.LEVEL);
        } else if (openInterfaceTerminal.isPressed()) {
            handleOpenTerminalKey(UltraTerminalModes.INTERFACE);
        } else if (openPatternTerminal.isPressed()) {
            handleOpenTerminalKey(UltraTerminalModes.PATTERN);
        } else if (openPatternExTerminal.isPressed()) {
            handleOpenTerminalKey(UltraTerminalModes.PATTERN_EX);
        } else if (restock.isPressed()) {
            handleRestockKey();
        }
    }

    private void handleOpenTerminalKey(UltraTerminalModes mode) {
        if (Minecraft.getMinecraft().currentScreen != null) return;
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        ImmutablePair<Integer, ItemStack> temp = Util.getUltraWirelessTerm(player);
        if (temp == null) return;
        if (mode != null) ItemWirelessUltraTerminal.setMode(temp.getRight(), mode);
        FluidCraft.proxy.netHandler.sendToServer(new CPacketSwitchGuis(mode, true));
    }

    private void handleRestockKey() {
        if (Minecraft.getMinecraft().currentScreen != null) return;
        EntityClientPlayerMP p = Minecraft.getMinecraft().thePlayer;
        if (p.openContainer == null) {
            return;
        }

        ImmutablePair<Integer, ItemStack> term = Util.getUltraWirelessTerm(p);
        if (term != null) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketValueConfig(0, 1));
        }
    }
}
