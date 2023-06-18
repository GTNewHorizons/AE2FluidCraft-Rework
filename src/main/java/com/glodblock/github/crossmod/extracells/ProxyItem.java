package com.glodblock.github.crossmod.extracells;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.FCBaseItem;

import cpw.mods.fml.common.registry.GameRegistry;

/**
 * EC2ProxyItem - Item that "holds" the data of a missing item.
 */
public class ProxyItem extends FCBaseItem {

    protected final String name;

    /**
     * Maps a metadata value to a replacement.
     */
    protected final Map<Integer, ProxyItemEntry> replacements;

    public ProxyItem(String ec2itemName) {
        this.name = ec2itemName;
        this.replacements = new HashMap<>();
        setUnlocalizedName("ec2placeholder." + name);
    }

    @Override
    public FCBaseItem register() {
        GameRegistry.registerItem(this, "ec2placeholder." + name, FluidCraft.MODID);
        return this;
    }

    protected void addMetaReplacement(int srcMeta, Item replacement, int targetMeta) {
        this.replacements.put(srcMeta, new ProxyItemEntry(replacement, targetMeta));
    }

    protected void addMetaReplacement(int srcMeta, ProxyItemEntry replacement) {
        this.replacements.put(srcMeta, replacement);
    }

    @Override
    public boolean getHasSubtypes() {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List info, boolean p_77624_4_) {
        int meta = stack.getItemDamage();
        ProxyItemEntry itemRepl = replacements.get(meta);
        if (itemRepl != null) {
            info.add(EnumChatFormatting.RED + "Extra Cells Placeholder for:");
            info.add(EnumChatFormatting.AQUA + itemRepl.replacement.getUnlocalizedName());
            info.add(EnumChatFormatting.GOLD + "Put in your inventory to get a replacement.");
            info.add(EnumChatFormatting.GOLD + "It will disappear if no replacement was found.");
            info.add(EnumChatFormatting.RED + "Report missing conversions on the Github.");
        }
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int p_77663_4_, boolean p_77663_5_) {
        if (!worldIn.isRemote && entityIn instanceof EntityPlayer player) {
            for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
                ItemStack s = player.inventory.getStackInSlot(i);
                if (s == stack) {
                    int meta = stack.getItemDamage();
                    ProxyItemEntry r = replacements.get(meta);
                    if (r == null) {
                        player.inventory.setInventorySlotContents(i, null);
                    } else {
                        ItemStack replaceStack = new ItemStack(r.replacement, stack.stackSize, r.replacementMeta);
                        if (stack.hasTagCompound()) {
                            replaceStack.setTagCompound(r.replaceNBT(stack.getTagCompound()));
                        }
                        player.inventory.setInventorySlotContents(i, replaceStack);
                    }
                    break;
                }
            }
        }
    }

    /**
     * Base class for a mapping from source (Item, meta) -> replacement (Item, meta). Optionally, override the
     * {@link ProxyItemEntry#replaceNBT(NBTTagCompound)} method for custom NBT migration.
     */
    protected static class ProxyItemEntry {

        protected final Item replacement;
        protected final int replacementMeta;

        /**
         * Creates a Proxy Replacement.
         *
         * @param replacement     Item that will replace the other.
         * @param replacementMeta Metadata/damage value of the replacement
         */
        protected ProxyItemEntry(Item replacement, int replacementMeta) {
            this.replacement = replacement;
            this.replacementMeta = replacementMeta;
        }

        NBTTagCompound replaceNBT(NBTTagCompound compound) {
            System.out.println(compound);
            return compound;
        }
    }

    protected static class ProxyStorageEntry extends ProxyItemEntry {

        public final long maxBytes;
        public final int bytesPerType;
        public final double idleDrain;
        public final int types;

        protected ProxyStorageEntry(Item replacement, int meta, long kilobytes, int bytesPerType, double idleDrain) {
            super(replacement, meta);
            this.maxBytes = kilobytes * 1024;
            this.bytesPerType = bytesPerType;
            this.idleDrain = idleDrain;
            this.types = 63;
        }

        protected ProxyStorageEntry(Item replacement, long kilobytes, int bytesPerType, double idleDrain, int types) {
            super(replacement, 0);
            this.maxBytes = kilobytes * 1024;
            this.bytesPerType = bytesPerType;
            this.idleDrain = idleDrain;
            this.types = types;
        }

        @Override
        NBTTagCompound replaceNBT(NBTTagCompound compound) {
            if (compound != null && compound.hasKey("ecc")) {
                compound.removeTag("ecc");
            }
            return compound;
        }
    }
}
