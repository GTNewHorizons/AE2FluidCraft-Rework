package com.glodblock.github.network;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.minecraft.entity.player.EntityPlayerMP;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;

import com.glodblock.github.FluidCraft;

/**
 * Only should exist on the server side!
 */
public class SPacketMEUpdateBuffer {

    private static ScheduledExecutorService executor;
    private static ScheduledFuture<?> task;

    private static final Map<EntityPlayerMP, LinkedHashMap<IAEItemStack, IAEItemStack>> itemBuffer = new HashMap<>();

    private static final Map<EntityPlayerMP, Map<IAEFluidStack, IAEFluidStack>> fluidBuffer = new HashMap<>();

    public static void init() {
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setName("AE2FC Network worker");
            thread.setDaemon(true);
            thread.setPriority(6);
            return thread;
        });
        task = executor.scheduleAtFixedRate(SPacketMEUpdateBuffer::sendBuffer, 0, 50, TimeUnit.MILLISECONDS);
    }

    public static void disable() {
        task.cancel(false);
        executor.shutdown();
    }

    public static void scheduleItemUpdate(EntityPlayerMP player, List<IAEItemStack> stacks) {
        synchronized (itemBuffer) {
            if (!itemBuffer.containsKey(player)) {
                itemBuffer.put(player, new LinkedHashMap<>(1024));
            }
            Map<IAEItemStack, IAEItemStack> buffer = itemBuffer.get(player);
            stacks.forEach(s -> buffer.put(s, s));
        }
    }

    public static void scheduleFluidUpdate(EntityPlayerMP player, List<IAEFluidStack> stacks) {
        synchronized (itemBuffer) {
            if (!fluidBuffer.containsKey(player)) {
                fluidBuffer.put(player, new LinkedHashMap<>(1024));
            }
            Map<IAEFluidStack, IAEFluidStack> buffer = fluidBuffer.get(player);
            stacks.forEach(s -> buffer.put(s, s));
        }
    }

    public static void sendBuffer() {
        synchronized (itemBuffer) {
            itemBuffer.forEach((player, updates) -> {
                if (updates.isEmpty()) return;
                int i = 0;
                SPacketMEItemInvUpdate packet = new SPacketMEItemInvUpdate();
                Iterator<IAEItemStack> it = updates.keySet().iterator();
                while (it.hasNext()) {
                    if (i < 256) {
                        packet.appendItem(it.next());
                        it.remove();
                    } else {
                        packet.setResort(false);
                        break;
                    }
                    i++;
                }
                FluidCraft.proxy.netHandler.sendTo(packet, player);
            });
            fluidBuffer.forEach((player, updates) -> {
                if (updates.isEmpty()) return;
                int i = 0;
                SPacketMEFluidInvUpdate packet = new SPacketMEFluidInvUpdate();
                Iterator<IAEFluidStack> it = updates.keySet().iterator();
                while (it.hasNext()) {
                    if (i < 256) {
                        packet.appendFluid(it.next());
                        it.remove();
                    } else {
                        packet.setResort(false);
                        break;
                    }
                    i++;
                }
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
}
