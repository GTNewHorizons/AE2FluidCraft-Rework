package com.glodblock.github.nei;

import net.minecraft.util.EnumChatFormatting;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.GuiFluidCraftingWireless;
import com.glodblock.github.client.gui.GuiFluidPatternExWireless;
import com.glodblock.github.client.gui.GuiFluidPatternTerminal;
import com.glodblock.github.client.gui.GuiFluidPatternTerminalEx;
import com.glodblock.github.client.gui.GuiFluidPatternWireless;
import com.glodblock.github.nei.recipes.FluidRecipe;
import com.glodblock.github.util.ModAndClassUtil;

import codechicken.nei.SearchField;
import codechicken.nei.SearchTokenParser;
import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

@SuppressWarnings("unused")
public class NEI_FC_Config implements IConfigureNEI {

    @Override
    public void loadConfig() {
        API.registerNEIGuiHandler(new AE2FC_NEIGuiHandler());
        API.addSearchProvider(
                new SearchField.SearchParserProvider(
                        '\0',
                        "cellSearch",
                        EnumChatFormatting.RESET,
                        CellSearchFilter::new) {

                    @Override
                    public SearchTokenParser.SearchMode getSearchMode() {
                        return SearchTokenParser.SearchMode.ALWAYS;
                    }
                });
        API.registerStackStringifyHandler(new FCStackStringifyHandler());
        API.registerUsageHandler(new NEICellViewHandler());

        if (ModAndClassUtil.AVARITIA) {
            API.registerGuiOverlay(GuiFluidPatternWireless.class, "extreme", null);
            API.registerGuiOverlay(GuiFluidPatternExWireless.class, "extreme", null);
            API.registerGuiOverlay(GuiFluidPatternTerminal.class, "extreme", null);
            API.registerGuiOverlay(GuiFluidPatternTerminalEx.class, "extreme", null);
        }

        for (String identifier : FluidRecipe.getSupportRecipes()) {
            // that NEE handlers take priority
            if (!API.hasGuiOverlayHandler(GuiFluidCraftingWireless.class, identifier)) {
                API.registerGuiOverlayHandler(
                        GuiFluidCraftingWireless.class,
                        FluidCraftingTransferHandler.INSTANCE,
                        identifier);
            }
            if (!API.hasGuiOverlayHandler(GuiFluidPatternWireless.class, identifier)) {
                API.registerGuiOverlayHandler(
                        GuiFluidPatternWireless.class,
                        FluidPatternTerminalRecipeTransferHandler.INSTANCE,
                        identifier);
            }
            if (!API.hasGuiOverlayHandler(GuiFluidPatternExWireless.class, identifier)) {
                API.registerGuiOverlayHandler(
                        GuiFluidPatternExWireless.class,
                        FluidPatternTerminalRecipeTransferHandler.INSTANCE,
                        identifier);
            }
            if (!API.hasGuiOverlayHandler(GuiFluidPatternTerminal.class, identifier)) {
                API.registerGuiOverlayHandler(
                        GuiFluidPatternTerminal.class,
                        FluidPatternTerminalRecipeTransferHandler.INSTANCE,
                        identifier);
            }
            if (!API.hasGuiOverlayHandler(GuiFluidPatternTerminalEx.class, identifier)) {
                API.registerGuiOverlayHandler(
                        GuiFluidPatternTerminalEx.class,
                        FluidPatternTerminalRecipeTransferHandler.INSTANCE,
                        identifier);
            }
        }
    }

    @Override
    public String getName() {
        return FluidCraft.MODNAME;
    }

    @Override
    public String getVersion() {
        return FluidCraft.VERSION;
    }
}
