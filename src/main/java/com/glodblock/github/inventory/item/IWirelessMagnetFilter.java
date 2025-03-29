package com.glodblock.github.inventory.item;

public interface IWirelessMagnetFilter extends IWirelessTerminal, IItemTerminal {

    WirelessMagnet.ListMode getListMode();

    boolean getNBTMode();

    boolean getMetaMode();

    boolean getOreMode();

    void setListMode(WirelessMagnet.ListMode mode);

    void setNBTMode(boolean ignoreNBT);

    void setMetaMode(boolean ignoreMeta);

    void setOreMode(boolean useOre);

    default void clearConfig() {};

}
