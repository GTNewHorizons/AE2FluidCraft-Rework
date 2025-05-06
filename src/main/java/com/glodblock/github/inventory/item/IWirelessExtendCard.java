package com.glodblock.github.inventory.item;

import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.data.IAEStack;

public interface IWirelessExtendCard {

    default void setMagnetCardMode(WirelessMagnet.Mode mode) {}

    WirelessMagnet.Mode getMagnetCardMode();

    PlayerSource getActionSource();

    IAEStack injectItems(IAEStack aeStack);

    IAEStack extractItems(IAEStack aeStack);

    default void setMagnetCardNextMode() {};

    void setRestock(boolean val);

    default boolean isRestock() {
        return false;
    }

}
