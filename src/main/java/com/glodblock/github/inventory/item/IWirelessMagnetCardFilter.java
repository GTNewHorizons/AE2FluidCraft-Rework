package com.glodblock.github.inventory.item;

import com.glodblock.github.common.item.ItemMagnetCard;

public interface IWirelessMagnetCardFilter extends IWirelessTerminal, IItemTerminal {

    ItemMagnetCard.ListMode getListMode();

    boolean getNBTMode();

    boolean getMetaMode();

    boolean getOreMode();

    void setListMode(ItemMagnetCard.ListMode mode);

    void setNBTMode(boolean ignoreNBT);

    void setMetaMode(boolean ignoreMeta);

    void setOreMode(boolean useOre);

    default void clearConfig() {};

}
