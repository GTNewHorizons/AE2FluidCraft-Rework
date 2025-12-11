package com.glodblock.github.loader;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.network.CPacketCustomButtonUpdate;
import com.glodblock.github.network.CPacketDumpTank;
import com.glodblock.github.network.CPacketFluidPatternTermBtns;
import com.glodblock.github.network.CPacketLevelMaintainer;
import com.glodblock.github.network.CPacketLevelTerminalCommands;
import com.glodblock.github.network.CPacketSwitchGuis;
import com.glodblock.github.network.CPacketValueConfig;
import com.glodblock.github.network.SPacketCustomButtonUpdate;
import com.glodblock.github.network.SPacketFluidUpdate;
import com.glodblock.github.network.SPacketLevelMaintainerGuiUpdate;
import com.glodblock.github.network.SPacketLevelTerminalUpdate;
import com.glodblock.github.network.SPacketSuperStockReplenisherUpdate;
import com.glodblock.github.network.wrapper.FCNetworkWrapper;

import cpw.mods.fml.relauncher.Side;

public class ChannelLoader implements Runnable {

    public static final ChannelLoader INSTANCE = new ChannelLoader();

    @Override
    public void run() {
        int id = 0;
        FCNetworkWrapper netHandler = FluidCraft.proxy.netHandler;
        netHandler.registerMessage(new CPacketSwitchGuis.Handler(), CPacketSwitchGuis.class, id++, Side.SERVER);
        netHandler.registerMessage(
                new CPacketFluidPatternTermBtns.Handler(),
                CPacketFluidPatternTermBtns.class,
                id++,
                Side.SERVER);
        netHandler.registerMessage(new CPacketSwitchGuis.Handler(), CPacketSwitchGuis.class, id++, Side.CLIENT);
        netHandler.registerMessage(new CPacketDumpTank.Handler(), CPacketDumpTank.class, id++, Side.SERVER);
        netHandler.registerMessage(new SPacketFluidUpdate.Handler(), SPacketFluidUpdate.class, id++, Side.CLIENT);
        netHandler.registerMessage(
                new SPacketSuperStockReplenisherUpdate.Handler(),
                SPacketSuperStockReplenisherUpdate.class,
                id++,
                Side.CLIENT);
        netHandler.registerMessage(new CPacketValueConfig.Handler(), CPacketValueConfig.class, id++, Side.SERVER);
        netHandler
                .registerMessage(new CPacketLevelMaintainer.Handler(), CPacketLevelMaintainer.class, id++, Side.SERVER);
        netHandler.registerMessage(
                new SPacketLevelTerminalUpdate.Handler(),
                SPacketLevelTerminalUpdate.class,
                id++,
                Side.CLIENT);
        netHandler.registerMessage(
                new CPacketLevelTerminalCommands.Handler(),
                CPacketLevelTerminalCommands.class,
                id++,
                Side.SERVER);
        netHandler.registerMessage(
                new SPacketLevelMaintainerGuiUpdate.Handler(),
                SPacketLevelMaintainerGuiUpdate.class,
                id++,
                Side.CLIENT);
        netHandler.registerMessage(
                new SPacketCustomButtonUpdate.Handler(),
                SPacketCustomButtonUpdate.class,
                id++,
                Side.CLIENT);
        netHandler.registerMessage(
                new CPacketCustomButtonUpdate.Handler(),
                CPacketCustomButtonUpdate.class,
                id++,
                Side.SERVER);
    }
}
