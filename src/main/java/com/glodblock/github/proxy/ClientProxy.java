package com.glodblock.github.proxy;

import static com.glodblock.github.common.Config.reStockTime;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.loader.KeybindLoader;
import com.glodblock.github.loader.ListenerLoader;
import com.glodblock.github.loader.RenderLoader;
import com.glodblock.github.nei.recipes.DefaultExtractorLoader;
import com.glodblock.github.network.CPacketValueConfig;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.Util;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class ClientProxy extends CommonProxy {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static long refreshTick = System.currentTimeMillis();

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
        if (refreshTick + reStockTime < System.currentTimeMillis()) {
            // try to stock items
            ImmutablePair<Integer, ItemStack> result = Util.getUltraWirelessTerm(mc.thePlayer);
            if (result != null && mc.currentScreen == null && Util.isRestock(result.getRight())) {
                FluidCraft.proxy.netHandler.sendToServer(new CPacketValueConfig(1, 0));
            }
            refreshTick = System.currentTimeMillis();
        }
    }
}
