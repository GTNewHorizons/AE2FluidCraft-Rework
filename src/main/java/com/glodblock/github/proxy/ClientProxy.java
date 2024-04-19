package com.glodblock.github.proxy;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.ItemBaseWirelessTerminal;
import com.glodblock.github.loader.KeybindLoader;
import com.glodblock.github.loader.ListenerLoader;
import com.glodblock.github.loader.RenderLoader;
import com.glodblock.github.nei.recipes.DefaultExtractorLoader;
import com.glodblock.github.network.CPacketValueConfig;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.Util;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.ReadableNumberConverter;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class ClientProxy extends CommonProxy {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static long refreshTick = System.currentTimeMillis();
    private static final ReadableNumberConverter format = ReadableNumberConverter.INSTANCE;

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        (new ListenerLoader()).run();
        (new RenderLoader()).run();
        (new KeybindLoader()).run();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    @Override
    public void onLoadComplete(FMLLoadCompleteEvent event) {
        super.onLoadComplete(event);
        if (ModAndClassUtil.NEI) {
            new DefaultExtractorLoader().run();
        }
    }

    @SubscribeEvent
    public void tickEvent(TickEvent.PlayerTickEvent e) {
        super.tickEvent(e);
        if (refreshTick + 1000 < System.currentTimeMillis()) {
            // try to stock items
            ImmutablePair<Integer, ItemStack> result = Util.Wireless.getUltraWirelessTerm(mc.thePlayer);
            if (result == null || !Util.Wireless.isRestock(result.getRight()) || mc.currentScreen != null) return;
            FluidCraft.proxy.netHandler.sendToServer(new CPacketValueConfig(1, 0));
            refreshTick = System.currentTimeMillis();
        }
    }

    public static void postUpdate(List<IAEItemStack> list, final byte ref) {
        if (ref == 2) {
            setWirelessTerminalEffect(list);
        }
    }

    private static void setWirelessTerminalEffect(List<IAEItemStack> list) {
        ItemBaseWirelessTerminal.setEffect(true);
        for (IAEItemStack is : list) {
            mc.thePlayer.addChatMessage(
                    new ChatComponentText(
                            I18n.format(
                                    NameConst.TT_CRAFTING_COMPLETE,
                                    is.getItemStack().getDisplayName(),
                                    format.toWideReadableForm(is.getStackSize()))));
        }
    }
}
