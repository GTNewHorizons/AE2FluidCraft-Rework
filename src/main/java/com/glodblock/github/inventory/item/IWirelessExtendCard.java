package com.glodblock.github.inventory.item;

import com.glodblock.github.common.item.ItemMagnetCard;

import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.data.IAEStack;

public interface IWirelessExtendCard {

    default void setMagnetCardMode(ItemMagnetCard.Mode mode) {}

    ItemMagnetCard.Mode getMagnetCardMode();

    PlayerSource getActionSource();

    IAEStack injectItems(IAEStack aeStack);

    IAEStack extractItems(IAEStack aeStack);

    default void setMagnetCardNextMode() {};

    void setRestock(boolean val);

    default boolean isRestock() {
        return false;
    }

}
