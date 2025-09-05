package com.glodblock.github.loader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.lwjgl.input.Keyboard;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.network.CPacketSelectBlockWithdraw;
import com.glodblock.github.network.CPacketValueConfig;
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
    public static KeyBinding restock;
    public static KeyBinding selectBlock;

    @Override
    public void run() {
        openTerminal = new KeyBinding(FluidCraft.MODID + ".key.OpenTerminal", Keyboard.CHAR_NONE, "itemGroup.ae2fc");
        restock = new KeyBinding(FluidCraft.MODID + ".key.Restock", Keyboard.CHAR_NONE, "itemGroup.ae2fc");
        selectBlock = new KeyBinding(FluidCraft.MODID + ".key.SelectBlock", Keyboard.CHAR_NONE, "itemGroup.ae2fc");
        ClientRegistry.registerKeyBinding(openTerminal);
        ClientRegistry.registerKeyBinding(restock);
        ClientRegistry.registerKeyBinding(selectBlock);
        FMLCommonHandler.instance().bus().register(this);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (openTerminal.isPressed()) {
            handleOpenTerminalKey();
        } else if (restock.isPressed()) {
            handleRestockKey();
        } else if (selectBlock.isPressed()) {
            handleSelectBlockKey();
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        if (openTerminal.isPressed()) {
            handleOpenTerminalKey();
        } else if (restock.isPressed()) {
            handleRestockKey();
        } else if (selectBlock.isPressed()) {
            handleSelectBlockKey();
        }
    }

    private void handleOpenTerminalKey() {
        if (Minecraft.getMinecraft().currentScreen != null) return;
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        if (player.openContainer == null) {
            return;
        }

        ImmutablePair<Integer, ItemStack> term = Util.getUltraWirelessTerm(player);
        if (term != null && term.getRight().getItem() instanceof ItemWirelessUltraTerminal) {
            ItemWirelessUltraTerminal.switchTerminal(
                    player,
                    ((ItemWirelessUltraTerminal) term.getRight().getItem()).guiGuiType(term.getRight()));
        }
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

    private void handleSelectBlockKey() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.currentScreen != null) return; // Don't act if a GUI is open

        EntityClientPlayerMP player = minecraft.thePlayer;
        if (player == null) return;

        // Ensure the player has the wireless terminal
        ImmutablePair<Integer, ItemStack> terminalAndInventorySlot = Util.getUltraWirelessTerm(player);
        if (terminalAndInventorySlot == null) {
            player.addChatMessage(new ChatComponentText("Could not find wireless terminal."));
            return;
        }

        ItemStack terminal = terminalAndInventorySlot.getRight();
        if (terminal == null || !(terminal.getItem() instanceof ItemWirelessUltraTerminal)) {
            player.addChatMessage(new ChatComponentText("Terminal must be universal version."));
            return;
        }

        // Get the block the player is currently looking at
        MovingObjectPosition movingObject = minecraft.objectMouseOver;
        if (movingObject == null || movingObject.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return;
        }

        // Send packet to server with block coordinates
        FluidCraft.proxy.netHandler.sendToServer(
                new CPacketSelectBlockWithdraw(movingObject.blockX, movingObject.blockY, movingObject.blockZ));
    }
}
