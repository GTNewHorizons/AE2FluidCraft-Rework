package com.glodblock.github.nei.recipes;

import java.util.Collection;

import com.glodblock.github.nei.recipes.extractor.AvaritiaRecipeExtractor;
import com.glodblock.github.nei.recipes.extractor.EnderIORecipeExtractor;
import com.glodblock.github.nei.recipes.extractor.ForestryRecipeExtractor;
import com.glodblock.github.nei.recipes.extractor.GTPPRecipeExtractor;
import com.glodblock.github.nei.recipes.extractor.GregTech5RecipeExtractor;
import com.glodblock.github.nei.recipes.extractor.GregTech6RecipeExtractor;
import com.glodblock.github.nei.recipes.extractor.IndustrialCraftRecipeExtractor;
import com.glodblock.github.nei.recipes.extractor.VanillaRecipeExtractor;
import com.glodblock.github.util.ModAndClassUtil;

import forestry.factory.recipes.nei.NEIHandlerBottler;
import forestry.factory.recipes.nei.NEIHandlerCarpenter;
import forestry.factory.recipes.nei.NEIHandlerCentrifuge;
import forestry.factory.recipes.nei.NEIHandlerFabricator;
import forestry.factory.recipes.nei.NEIHandlerFermenter;
import forestry.factory.recipes.nei.NEIHandlerMoistener;
import forestry.factory.recipes.nei.NEIHandlerSqueezer;
import forestry.factory.recipes.nei.NEIHandlerStill;
import gregapi.recipes.Recipe;

public class DefaultExtractorLoader implements Runnable {

    @Override
    public void run() {
        FluidRecipe.addRecipeMap("smelting", new VanillaRecipeExtractor(false));
        FluidRecipe.addRecipeMap("brewing", new VanillaRecipeExtractor(false));
        FluidRecipe.addRecipeMap("crafting", new VanillaRecipeExtractor(true));
        FluidRecipe.addRecipeMap("crafting2x2", new VanillaRecipeExtractor(true));

        if (ModAndClassUtil.GT5) {
            try {
                Class<?> recipeMapClazz = Class.forName("gregtech.api.util.GTRecipe$GT_Recipe_Map");
                Collection<?> sMappings = (Collection<?>) recipeMapClazz.getDeclaredField("sMappings").get(null);
                for (Object tMap : sMappings) {
                    String mNEIName = (String) recipeMapClazz.getDeclaredField("mNEIName").get(tMap);
                    FluidRecipe.addRecipeMap(
                            mNEIName,
                            new GregTech5RecipeExtractor(
                                    mNEIName.equals("gt.recipe.scanner")
                                            || mNEIName.equals("gt.recipe.fakeAssemblylineProcess")));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (ModAndClassUtil.GT6) {
            for (Recipe.RecipeMap tMap : Recipe.RecipeMap.RECIPE_MAPS.values()) {
                FluidRecipe.addRecipeMap(tMap.mNameNEI, new GregTech6RecipeExtractor(tMap));
            }
        }

        if (ModAndClassUtil.GTPP && !ModAndClassUtil.GT5NH) {
            try {
                Class<?> gtRecipeMapClazz = Class.forName("gregtech.api.util.GT_Recipe$GT_Recipe_Map");
                Class<?> gtppRecipeMapClazz = Class.forName("gregtech.api.util.GTPP_Recipe$GTPP_Recipe_Map_Internal");
                Collection<?> sMappingsEx = (Collection<?>) gtppRecipeMapClazz.getDeclaredField("sMappingsEx")
                        .get(null);
                for (Object gtppMap : sMappingsEx) {
                    boolean mNEIAllowed = gtRecipeMapClazz.getDeclaredField("mNEIAllowed").getBoolean(gtppMap);
                    if (mNEIAllowed) {
                        String mNEIName = (String) gtRecipeMapClazz.getDeclaredField("mNEIName").get(gtppMap);
                        FluidRecipe.addRecipeMap(mNEIName, new GTPPRecipeExtractor());
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (ModAndClassUtil.EIO) {
            FluidRecipe.addRecipeMap("EIOEnchanter", new EnderIORecipeExtractor());
            FluidRecipe.addRecipeMap("EnderIOAlloySmelter", new EnderIORecipeExtractor());
            FluidRecipe.addRecipeMap("EnderIOSagMill", new EnderIORecipeExtractor());
            FluidRecipe.addRecipeMap("EnderIOSliceAndSplice", new EnderIORecipeExtractor());
            FluidRecipe.addRecipeMap("EnderIOSoulBinder", new EnderIORecipeExtractor());
            FluidRecipe.addRecipeMap("EnderIOVat", new EnderIORecipeExtractor());
        }

        if (ModAndClassUtil.AVARITIA) {
            FluidRecipe.addRecipeMap("extreme", new AvaritiaRecipeExtractor());
        }

        if (ModAndClassUtil.FTR) {
            // GTNH
            FluidRecipe.addRecipeMap("forestry.bottler", new ForestryRecipeExtractor(new NEIHandlerBottler()));
            FluidRecipe.addRecipeMap("forestry.carpenter", new ForestryRecipeExtractor(new NEIHandlerCarpenter()));
            FluidRecipe.addRecipeMap("forestry.centrifuge", new ForestryRecipeExtractor(new NEIHandlerCentrifuge()));
            FluidRecipe.addRecipeMap("forestry.fabricator", new ForestryRecipeExtractor(new NEIHandlerFabricator()));
            FluidRecipe.addRecipeMap("forestry.fermenter", new ForestryRecipeExtractor(new NEIHandlerFermenter()));
            FluidRecipe.addRecipeMap("forestry.moistener", new ForestryRecipeExtractor(new NEIHandlerMoistener()));
            FluidRecipe.addRecipeMap("forestry.squeezer", new ForestryRecipeExtractor(new NEIHandlerSqueezer()));
            FluidRecipe.addRecipeMap("forestry.still", new ForestryRecipeExtractor(new NEIHandlerStill()));

            // LEGACY
            FluidRecipe.addRecipeMap(null, new ForestryRecipeExtractor(new NEIHandlerBottler()));
            FluidRecipe.addRecipeMap(null, new ForestryRecipeExtractor(new NEIHandlerCarpenter()));
            FluidRecipe.addRecipeMap(null, new ForestryRecipeExtractor(new NEIHandlerCentrifuge()));
            FluidRecipe.addRecipeMap(null, new ForestryRecipeExtractor(new NEIHandlerFabricator()));
            FluidRecipe.addRecipeMap(null, new ForestryRecipeExtractor(new NEIHandlerFermenter()));
            FluidRecipe.addRecipeMap(null, new ForestryRecipeExtractor(new NEIHandlerMoistener()));
            FluidRecipe.addRecipeMap(null, new ForestryRecipeExtractor(new NEIHandlerSqueezer()));
            FluidRecipe.addRecipeMap(null, new ForestryRecipeExtractor(new NEIHandlerStill()));
        }

        if (ModAndClassUtil.IC2) {
            FluidRecipe.addRecipeMap("blastfurnace", new IndustrialCraftRecipeExtractor());
            FluidRecipe.addRecipeMap("BlockCutter", new IndustrialCraftRecipeExtractor());
            FluidRecipe.addRecipeMap("centrifuge", new IndustrialCraftRecipeExtractor());
            FluidRecipe.addRecipeMap("compressor", new IndustrialCraftRecipeExtractor());
            FluidRecipe.addRecipeMap("extractor", new IndustrialCraftRecipeExtractor());
            FluidRecipe.addRecipeMap("fluidcanner", new IndustrialCraftRecipeExtractor());
            FluidRecipe.addRecipeMap("macerator", new IndustrialCraftRecipeExtractor());
            FluidRecipe.addRecipeMap("metalformer", new IndustrialCraftRecipeExtractor());
            FluidRecipe.addRecipeMap("oreWashing", new IndustrialCraftRecipeExtractor());
            FluidRecipe.addRecipeMap("solidcanner", new IndustrialCraftRecipeExtractor());
        }
    }
}
