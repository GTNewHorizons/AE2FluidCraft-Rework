package com.glodblock.github.network;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.minecraft.entity.player.EntityPlayerMP;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.Config;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

/**
 * Only should exist on the server side! Schedules packets to be sent every `packetRate` ms.
 */
public class SPacketMEUpdateBuffer {

    private static ScheduledExecutorService executor;
    private static ScheduledFuture<?> task;

    private static final Map<EntityPlayerMP, LinkedHashSet<IAEItemStack>> itemBuffer = new HashMap<>();

    private static final Map<EntityPlayerMP, LinkedHashSet<IAEFluidStack>> fluidBuffer = new HashMap<>();

    public static void init() {
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setName("AE2FC Network worker");
            thread.setDaemon(true);
            thread.setPriority(6);
            return thread;
        });
        task = executor
                .scheduleAtFixedRate(SPacketMEUpdateBuffer::sendBuffer, 0, Config.packetRate, TimeUnit.MILLISECONDS);
    }

    public static void disable() {
        task.cancel(false);
        executor.shutdown();
    }

    public static void scheduleItemUpdate(EntityPlayerMP player, List<IAEItemStack> stacks) {
        synchronized (itemBuffer) {
            if (!itemBuffer.containsKey(player)) {
                itemBuffer.put(player, new LinkedHashSet<>(1024));
            }
            LinkedHashSet<IAEItemStack> buffer = itemBuffer.get(player);
            stacks.forEach(s -> buffer.add(s.copy()));
        }
    }

    public static void scheduleFluidUpdate(EntityPlayerMP player, List<IAEFluidStack> stacks) {
        synchronized (itemBuffer) {
            if (!fluidBuffer.containsKey(player)) {
                fluidBuffer.put(player, new LinkedHashSet<>(1024));
            }
            LinkedHashSet<IAEFluidStack> buffer = fluidBuffer.get(player);
            stacks.forEach(s -> buffer.add(s.copy()));
        }
    }

    public static void sendBuffer() {
        synchronized (itemBuffer) {
            itemBuffer.forEach((player, updates) -> {
                if (updates.isEmpty()) return;
                int i = 0;
                SPacketMEItemInvUpdate packet = new SPacketMEItemInvUpdate(false);
                Iterator<IAEItemStack> it = updates.iterator();
                while (it.hasNext()) {
                    if (i < Config.packetSize) {
                        packet.appendItem(it.next());
                        it.remove();
                        i++;
                    } else {
                        FluidCraft.proxy.netHandler.sendTo(packet, player);
                        packet = new SPacketMEItemInvUpdate(false);
                        i = 0;
                    }
                }
                packet.setResort(true);
                FluidCraft.proxy.netHandler.sendTo(packet, player);
            });
            fluidBuffer.forEach((player, updates) -> {
                if (updates.isEmpty()) return;
                int i = 0;
                SPacketMEFluidInvUpdate packet = new SPacketMEFluidInvUpdate(false);
                Iterator<IAEFluidStack> it = updates.iterator();
                while (it.hasNext()) {
                    if (i < Config.packetSize) {
                        packet.appendFluid(it.next());
                        it.remove();
                        i++;
                    } else {
                        FluidCraft.proxy.netHandler.sendTo(packet, player);
                        packet = new SPacketMEFluidInvUpdate(false);
                        i = 0;
                    }
                }
                packet.setResort(true);
                FluidCraft.proxy.netHandler.sendTo(packet, player);
            });

        }
    }

    public static void clear(EntityPlayerMP player) {
        synchronized (itemBuffer) {
            if (itemBuffer.containsKey(player)) {
                itemBuffer.get(player).clear();
            }
            if (fluidBuffer.containsKey(player)) {
                fluidBuffer.get(player).clear();
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            clear((EntityPlayerMP) event.player);
        }
    }
}
