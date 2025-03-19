package com.glodblock.github.loader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.lwjgl.input.Keyboard;

import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
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

    @Override
    public void run() {
        openTerminal = new KeyBinding("key.OpenTerminal", Keyboard.CHAR_NONE, "itemGroup.ae2fc");
        ClientRegistry.registerKeyBinding(openTerminal);
        FMLCommonHandler.instance().bus().register(this);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (openTerminal.isPressed()) {
            handleOpenTerminalKey();
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        if (openTerminal.isPressed()) {
            handleOpenTerminalKey();
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
}
