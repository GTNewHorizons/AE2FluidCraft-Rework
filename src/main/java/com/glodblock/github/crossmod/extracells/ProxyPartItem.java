package com.glodblock.github.crossmod.extracells;

import java.util.function.Function;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import appeng.api.definitions.IItemDefinition;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;

public class ProxyPartItem extends ProxyItem implements IPartItem {

    /**
     * Creates a ProxyPartItem associated with the ProxyPart instance. This instance is reused on each call to
     * {@link #createPartFromItemStack(ItemStack)}.
     *
     * @param ec2itemName extra cells internal name
     */
    public ProxyPartItem(String ec2itemName) {
        super(ec2itemName);
    }

    protected void addItemPart(int srcMeta, Item replacement, Function<ProxyPartItem, ProxyPart> part) {
        this.replacements.put(srcMeta, new PartReplacement(replacement, part));
    }

    protected void addItemPart(int srcMeta, IItemDefinition replacement, Function<ProxyPartItem, ProxyPart> part) {
        ItemStack stack = replacement.maybeStack(1).get();
        this.replacements.put(srcMeta, new PartReplacement(stack.getItem(), stack.getItemDamage(), part));
    }

    @Nullable
    @Override
    public IPart createPartFromItemStack(ItemStack is) {
        final int meta = is.getItemDamage();
        if (this.replacements.get(meta) instanceof PartReplacement part) {
            return part.proxyPart.apply(this);
        }
        return null;
    }
}

/**
 * Item replacement entry for ItemParts. All targets replace into 0.
 */
class PartReplacement extends ProxyItem.ProxyItemEntry {

    Function<ProxyPartItem, ProxyPart> proxyPart;

    PartReplacement(Item replacement, Function<ProxyPartItem, ProxyPart> proxyPart) {
        this(replacement, 0, proxyPart);
    }

    PartReplacement(Item replacement, int meta, Function<ProxyPartItem, ProxyPart> proxyPart) {
        super(replacement, meta);
        this.proxyPart = proxyPart;
    }
}
