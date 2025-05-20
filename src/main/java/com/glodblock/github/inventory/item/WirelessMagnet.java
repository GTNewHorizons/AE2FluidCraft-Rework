package com.glodblock.github.inventory.item;

import static com.glodblock.github.common.Config.magnetRange;

import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import com.glodblock.github.common.item.ItemWirelessUltraTerminal;

import appeng.util.Platform;

public class WirelessMagnet {

    public static String modeKey = "MagnetMode";
    public static String filterKey = "MagnetFilter";
    public static String filterConfigKey = "MagnetConfig";

    public enum Mode {
        Off,
        Inv,
        ME
    }

    public enum ListMode {
        WhiteList,
        BlackList
    }

    public static List<?> getEntitiesInRange(Class<?> entityType, World world, int x, int y, int z, int distance) {
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

    @SuppressWarnings("unchecked")

    public static boolean isConfigured(ItemStack wirelessTerm) {
        NBTTagCompound data = Platform.openNbtData(wirelessTerm);
        return data.hasKey(modeKey);
    }

    public static void doMagnet(ItemStack wirelessTerm, World world, EntityPlayer player) {
        if (wirelessTerm == null || player == null || player.isSneaking() || !isConfigured(wirelessTerm)) return;

        Iterator<?> iterator = getEntitiesInRange(
                EntityItem.class,
                world,
                (int) player.posX,
                (int) player.posY,
                (int) player.posZ,
                magnetRange).iterator();

        while (iterator.hasNext()) {
            EntityItem itemToGet = (EntityItem) iterator.next();
            EntityPlayer closestPlayer = world.getClosestPlayerToEntity(itemToGet, magnetRange);

            if (closestPlayer != null && closestPlayer == player) {
                NBTTagCompound itemNBT = new NBTTagCompound();
                itemToGet.writeEntityToNBT(itemNBT);

                if (itemToGet.func_145800_j() == null
                        || !itemToGet.func_145800_j().equals(player.getCommandSenderName()))
                    itemToGet.delayBeforeCanPickup = 0;

                if (itemToGet.delayBeforeCanPickup <= 0) {
                    itemNBT.setBoolean("attractable", true);
                    itemToGet.readEntityFromNBT(itemNBT);
                }

                if (itemNBT.getBoolean("attractable")) {
                    itemToGet.motionX = itemToGet.motionY = itemToGet.motionZ = 0;
                    itemToGet.setPosition(
                            player.posX - 0.2 + (world.rand.nextDouble() * 0.4),
                            player.posY - 0.6,
                            player.posZ - 0.2 + (world.rand.nextDouble() * 0.4));
                }
            }
        }

        // xp
        iterator = getEntitiesInRange(
                EntityXPOrb.class,
                world,
                (int) player.posX,
                (int) player.posY,
                (int) player.posZ,
                magnetRange).iterator();
        while (iterator.hasNext()) {
            EntityXPOrb xpToGet = (EntityXPOrb) iterator.next();
            EntityPlayer closestPlayer = world.getClosestPlayerToEntity(xpToGet, magnetRange);

            if (!xpToGet.isDead && !xpToGet.isInvisible() && closestPlayer != null && closestPlayer == player) {
                int xpAmount = xpToGet.xpValue;
                xpToGet.xpValue = 0;
                player.xpCooldown = 0;
                player.addExperience(xpAmount);
                xpToGet.setDead();
                xpToGet.setInvisible(true);
                world.playSoundAtEntity(
                        player,
                        "random.orb",
                        0.08F,
                        0.5F * ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.8F));
            }
        }
    }

    public static Mode getMode(ItemStack is) {
        if (is != null && is.getItem() instanceof ItemWirelessUltraTerminal) {
            NBTTagCompound data = Platform.openNbtData(is);
            if (data.hasKey(modeKey)) {
                return Mode.values()[data.getInteger(modeKey)];
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
