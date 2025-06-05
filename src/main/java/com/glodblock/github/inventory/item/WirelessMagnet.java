package com.glodblock.github.inventory.item;

import static com.glodblock.github.common.Config.magnetRange;

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;

import com.glodblock.github.common.item.ItemWirelessUltraTerminal;

import appeng.entity.EntityFloatingItem;
import appeng.util.Platform;

public class WirelessMagnet {

    public static String modeKey = "MagnetMode";
    public static String filterKey = "MagnetFilter";
    public static String filterConfigKey = "MagnetConfig";

    public enum Mode {

        Off,
        Inv,
        ME;

        private static final Mode[] MODES = Mode.values();

        public static Mode[] getModes() {
            return MODES;
        }
    }

    public enum ListMode {
        WhiteList,
        BlackList
    }

    private static <T> List<T> getEntitiesInRange(Class<T> entityType, World world, int x, int y, int z, int distance) {
        return world.getEntitiesWithinAABB(
                entityType,
                AxisAlignedBB.getBoundingBox(
                        x - distance,
                        y - distance,
                        z - distance,
                        x + distance,
                        y + distance,
                        z + distance));
    }

    public static boolean isConfigured(ItemStack wirelessTerm) {
        NBTTagCompound data = Platform.openNbtData(wirelessTerm);
        return data.hasKey(modeKey);
    }

    public static void doMagnet(ItemStack wirelessTerm, EntityPlayer player) {
        if (player.ticksExisted % 5 != 0 || player.isSneaking() || !isConfigured(wirelessTerm)) return;

        World world = player.worldObj;
        final List<EntityItem> items = getEntitiesInRange(
                EntityItem.class,
                world,
                (int) player.posX,
                (int) player.posY,
                (int) player.posZ,
                magnetRange);
        final boolean skipPlayerCheck = world.playerEntities.size() < 2;
        boolean playSound = false;

        for (EntityItem itemToGet : items) {
            if (itemToGet.getEntityItem() == null || itemToGet instanceof EntityFloatingItem) {
                continue;
            }

            if (!skipPlayerCheck) {
                EntityPlayer closestPlayer = world.getClosestPlayerToEntity(itemToGet, magnetRange);
                if (closestPlayer == null || closestPlayer != player) continue;
            }

            if (itemToGet.delayBeforeCanPickup > 0) {
                itemToGet.delayBeforeCanPickup = 0;
            }
            playSound = true;
            itemToGet.motionX = 0;
            itemToGet.motionY = 0;
            itemToGet.motionZ = 0;
            itemToGet.setPosition(
                    player.posX - 0.2 + (world.rand.nextDouble() * 0.4),
                    player.posY - 0.6,
                    player.posZ - 0.2 + (world.rand.nextDouble() * 0.4));
        }

        if (playSound) {
            world.playSoundAtEntity(
                    player,
                    "random.orb",
                    0.1F,
                    0.5F * ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 2F));
        }

        if (!world.isRemote) {
            // xp
            final List<EntityXPOrb> xpOrbs = getEntitiesInRange(
                    EntityXPOrb.class,
                    world,
                    (int) player.posX,
                    (int) player.posY,
                    (int) player.posZ,
                    magnetRange);

            for (EntityXPOrb xpToGet : xpOrbs) {
                if (xpToGet.field_70532_c == 0 && xpToGet.isEntityAlive()) {
                    if (!skipPlayerCheck) {
                        EntityPlayer closestPlayer = world.getClosestPlayerToEntity(xpToGet, magnetRange);
                        if (closestPlayer == null || closestPlayer != player) continue;
                    }

                    if (MinecraftForge.EVENT_BUS.post(new PlayerPickupXpEvent(player, xpToGet))) continue;
                    world.playSoundAtEntity(
                            player,
                            "random.orb",
                            0.1F,
                            0.5F * ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.8F));
                    player.onItemPickup(xpToGet, 1);
                    player.addExperience(xpToGet.xpValue);
                    xpToGet.setDead();
                }
            }
        }
    }

    public static Mode getMode(ItemStack is) {
        if (is != null && is.getItem() instanceof ItemWirelessUltraTerminal) {
            NBTTagCompound data = Platform.openNbtData(is);
            if (data.hasKey(modeKey)) {
                return Mode.getModes()[data.getInteger(modeKey)];
            }
            setMode(is, Mode.Off);
        }
        return Mode.Off;
    }

    public static void setMode(ItemStack is, Mode mode) {
        if (is != null && is.getItem() instanceof ItemWirelessUltraTerminal) {
            NBTTagCompound data = Platform.openNbtData(is);
            data.setInteger(modeKey, mode.ordinal());
        }
    }
}
