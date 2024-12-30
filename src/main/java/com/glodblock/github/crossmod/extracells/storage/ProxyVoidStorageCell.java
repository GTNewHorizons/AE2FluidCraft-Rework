package com.glodblock.github.crossmod.extracells.storage;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.items.storage.ItemVoidStorageCell;

public class ProxyVoidStorageCell extends ItemVoidStorageCell {

    private final String name = AEApi.instance().definitions().items().cellVoid().maybeStack(1).get()
            .getUnlocalizedName();

    public ProxyVoidStorageCell() {
        this.setUnlocalizedName("ec2placeholder.storage.physical.void");
    }

    @Override
    public void addCheckedInformation(ItemStack stack, EntityPlayer player, List<String> info,
            boolean displayMoreInfo) {
        info.add(EnumChatFormatting.RED + "Extra Cells Placeholder for:");
        info.add(EnumChatFormatting.AQUA + name);
        info.add(EnumChatFormatting.GOLD + "Put in your inventory to get a replacement.");
        info.add(EnumChatFormatting.GOLD + "It will disappear if no replacement was found.");
        info.add(EnumChatFormatting.RED + "Report missing conversions on the GitHub.");
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int p_77663_4_, boolean p_77663_5_) {
        if (!worldIn.isRemote && entityIn instanceof EntityPlayer player) {
            for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
                ItemStack s = player.inventory.getStackInSlot(i);
                if (s == stack) {
                    ItemStack replaceStack = AEApi.instance().definitions().items().cellVoid().maybeStack(1).get();
                    player.inventory.setInventorySlotContents(i, replaceStack);
                    break;
                }
            }
        }

    }
}
