package com.glodblock.github.loader;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.world.World;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.network.CPacketCraftRequest;
import com.glodblock.github.network.CPacketDumpTank;
import com.glodblock.github.network.CPacketEncodePattern;
import com.glodblock.github.network.CPacketFluidPatternTermBtns;
import com.glodblock.github.network.CPacketFluidUpdate;
import com.glodblock.github.network.CPacketInventoryAction;
import com.glodblock.github.network.CPacketLevelMaintainer;
import com.glodblock.github.network.CPacketPatternValueSet;
import com.glodblock.github.network.CPacketRenamer;
import com.glodblock.github.network.CPacketSwitchGuis;
import com.glodblock.github.network.CPacketTransferRecipe;
import com.glodblock.github.network.CPacketValueConfig;
import com.glodblock.github.network.SPacketFluidUpdate;
import com.glodblock.github.network.SPacketMEFluidInvUpdate;
import com.glodblock.github.network.SPacketMEItemInvUpdate;
import com.glodblock.github.network.SPacketSetItemAmount;
import com.glodblock.github.network.SPacketStringUpdate;

import cpw.mods.fml.relauncher.Side;

public class ChannelLoader implements Runnable {

    public static final ChannelLoader INSTANCE = new ChannelLoader();

    @Override
    @SuppressWarnings("all")
    public void run() {
        int id = 0;
        FluidCraft.proxy.netHandler
                .registerMessage(new CPacketSwitchGuis.Handler(), CPacketSwitchGuis.class, id++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(
                new CPacketFluidPatternTermBtns.Handler(),
                CPacketFluidPatternTermBtns.class,
                id++,
                Side.SERVER);
        FluidCraft.proxy.netHandler
                .registerMessage(new CPacketEncodePattern.Handler(), CPacketEncodePattern.class, id++, Side.SERVER);
        FluidCraft.proxy.netHandler
                .registerMessage(new SPacketMEItemInvUpdate.Handler(), SPacketMEItemInvUpdate.class, id++, Side.CLIENT);
        FluidCraft.proxy.netHandler.registerMessage(
                new SPacketMEFluidInvUpdate.Handler(),
                SPacketMEFluidInvUpdate.class,
                id++,
                Side.CLIENT);
        FluidCraft.proxy.netHandler
                .registerMessage(new CPacketCraftRequest.Handler(), CPacketCraftRequest.class, id++, Side.SERVER);
        FluidCraft.proxy.netHandler
                .registerMessage(new CPacketInventoryAction.Handler(), CPacketInventoryAction.class, id++, Side.SERVER);
        FluidCraft.proxy.netHandler
                .registerMessage(new CPacketSwitchGuis.Handler(), CPacketSwitchGuis.class, id++, Side.CLIENT);
        FluidCraft.proxy.netHandler
                .registerMessage(new CPacketTransferRecipe.Handler(), CPacketTransferRecipe.class, id++, Side.SERVER);
        FluidCraft.proxy.netHandler
                .registerMessage(new CPacketDumpTank.Handler(), CPacketDumpTank.class, id++, Side.SERVER);
        FluidCraft.proxy.netHandler
                .registerMessage(new SPacketFluidUpdate.Handler(), SPacketFluidUpdate.class, id++, Side.CLIENT);
        FluidCraft.proxy.netHandler
                .registerMessage(new CPacketPatternValueSet.Handler(), CPacketPatternValueSet.class, id++, Side.SERVER);
        FluidCraft.proxy.netHandler
                .registerMessage(new CPacketValueConfig.Handler(), CPacketValueConfig.class, id++, Side.SERVER);
        FluidCraft.proxy.netHandler
                .registerMessage(new CPacketFluidUpdate.Handler(), CPacketFluidUpdate.class, id++, Side.SERVER);
        FluidCraft.proxy.netHandler
                .registerMessage(new CPacketLevelMaintainer.Handler(), CPacketLevelMaintainer.class, id++, Side.SERVER);
        FluidCraft.proxy.netHandler
                .registerMessage(new SPacketSetItemAmount.Handler(), SPacketSetItemAmount.class, id++, Side.CLIENT);
        FluidCraft.proxy.netHandler
                .registerMessage(new CPacketRenamer.Handler(), CPacketRenamer.class, id++, Side.SERVER);
        FluidCraft.proxy.netHandler
                .registerMessage(new SPacketStringUpdate.Handler(), SPacketStringUpdate.class, id++, Side.CLIENT);
    }

    public static void sendPacketToAllPlayers(Packet packet, World world) {
        for (Object player : world.playerEntities) {
            if (player instanceof EntityPlayerMP) {
                ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(packet);
            }
        }
    }
}
