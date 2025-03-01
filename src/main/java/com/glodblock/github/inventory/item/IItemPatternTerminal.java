package com.glodblock.github.inventory.item;

public interface IItemPatternTerminal extends IItemTerminal {

    boolean isInverted();

    boolean canBeSubstitute();

    boolean isPrioritize();

    boolean isSubstitution();

    boolean shouldCombine();

    void setCraftingRecipe(final boolean craftingMode);

    void setSubstitution(boolean canSubstitute);

    void setBeSubstitute(boolean canBeSubstitute);

    void setCombineMode(boolean shouldCombine);

    void setPrioritization(boolean canPrioritize);

    void setInverted(boolean inverted);

    int getActivePage();

    void setActivePage(int activePage);

    boolean isCraftingRecipe();

    void sortCraftingItems();

    boolean isAutoFillPattern();

    void setAutoFillPattern(boolean canFill);
}
