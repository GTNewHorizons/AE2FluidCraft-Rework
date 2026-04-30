package com.glodblock.github.client.textures;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import com.glodblock.github.util.NameConst;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public enum FCPartsTexture {

    PartFluidTerminal_Bright("fluid_terminal_bright"),
    PartFluidTerminal_Dark("fluid_terminal_dark"),
    PartFluidTerminal_Colored("fluid_terminal_medium"),
    PartFluidPatternTerminal_Bright("pattern_terminal_bright"),
    PartFluidPatternTerminal_Dark("pattern_terminal_dark"),
    PartFluidPatternTerminal_Colored("pattern_terminal_medium"),
    PartLevelTerminal_Bright("level_terminal_bright"),
    PartLevelTerminal_Dark("level_terminal_dark"),
    PartLevelTerminal_Colored("level_terminal_medium"),
    PartTerminalBroad("terminal_broad"),
    PartFluidImportBus("fluid_import_face"),
    PartFluidExportBus("fluid_export_face"),
    PartFluidStorageBus("fluid_storage_bus"),
    BlockFluidInterfaceAlternate_Arrow("fluid_interface_arrow"),
    BlockInterfaceAlternate("fluid_interface_a"),
    BlockInterface_Face("fluid_interface"),
    PartFluidLevelEmitter("fluid_level_emitter"),
    BlockLevelMaintainer("level_maintainer"),
    BlockLevelMaintainer_Active("level_maintainer_active"),
    PartFluidFormationPlane("fluid_formation_plane"),
    PartFluidAnnihilationPlane("fluid_annihilation_plane"),
    PartFluidFormationPlaneOn("fluid_formation_plane_on"),
    PartFluidAnnihilationPlaneOn("fluid_annihilation_plane_on"),;

    private final String name;
    public net.minecraft.util.IIcon IIcon;

    FCPartsTexture(final String name) {
        this.name = name;
    }

    public static ResourceLocation GuiTexture(final String string) {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public static IIcon getMissing() {
        return ((TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture))
                .getAtlasSprite("missingno");
    }

    public String getName() {
        return this.name;
    }

    public IIcon getIcon() {
        return this.IIcon;
    }

    public void registerIcon(final TextureMap map) {
        this.IIcon = map.registerIcon(NameConst.RES_KEY + this.name);
    }
}
