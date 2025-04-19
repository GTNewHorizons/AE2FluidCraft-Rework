package com.glodblock.github.inventory.item;

public interface IWirelessMagnetFilter extends IWirelessTerminal, IItemTerminal {

    WirelessMagnet.ListMode getListMode();

    boolean getNBTMode();

    boolean getMetaMode();

    boolean getOreMode();

    boolean getOreDictMode();

    String getOreDictFilter();

    void setListMode(WirelessMagnet.ListMode mode);

    void setNBTMode(boolean ignoreNBT);

    void setMetaMode(boolean ignoreMeta);

    void setOreMode(boolean useOre);

    void setOreDictMode(boolean useOreDict);

    void setOreDictFilter(String str);

    default void clearConfig() {};

}
