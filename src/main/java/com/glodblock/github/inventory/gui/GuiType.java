package com.glodblock.github.inventory.gui;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;

import com.glodblock.github.client.gui.GuiFluidAutoFiller;
import com.glodblock.github.client.gui.GuiFluidInterface;
import com.glodblock.github.client.gui.GuiFluidPacketDecoder;
import com.glodblock.github.client.gui.GuiFluidPatternEncoder;
import com.glodblock.github.client.gui.GuiIngredientBuffer;
import com.glodblock.github.client.gui.GuiLargeIngredientBuffer;
import com.glodblock.github.client.gui.GuiLevelEmitterProxy;
import com.glodblock.github.client.gui.GuiLevelMaintainer;
import com.glodblock.github.client.gui.GuiLevelTerminal;
import com.glodblock.github.client.gui.GuiLevelWireless;
import com.glodblock.github.client.gui.GuiMagnetFilter;
import com.glodblock.github.client.gui.GuiOCPatternEditor;
import com.glodblock.github.client.gui.GuiSuperStockReplenisher;
import com.glodblock.github.client.gui.container.ContainerFluidAutoFiller;
import com.glodblock.github.client.gui.container.ContainerFluidInterface;
import com.glodblock.github.client.gui.container.ContainerFluidPacketDecoder;
import com.glodblock.github.client.gui.container.ContainerFluidPatternEncoder;
import com.glodblock.github.client.gui.container.ContainerIngredientBuffer;
import com.glodblock.github.client.gui.container.ContainerLargeIngredientBuffer;
import com.glodblock.github.client.gui.container.ContainerLevelMaintainer;
import com.glodblock.github.client.gui.container.ContainerLevelTerminal;
import com.glodblock.github.client.gui.container.ContainerLevelWireless;
import com.glodblock.github.client.gui.container.ContainerMagnetFilter;
import com.glodblock.github.client.gui.container.ContainerOCPatternEditor;
import com.glodblock.github.client.gui.container.ContainerSuperStockReplenisher;
import com.glodblock.github.common.tile.TileFluidAutoFiller;
import com.glodblock.github.common.tile.TileFluidPacketDecoder;
import com.glodblock.github.common.tile.TileFluidPatternEncoder;
import com.glodblock.github.common.tile.TileIngredientBuffer;
import com.glodblock.github.common.tile.TileLargeIngredientBuffer;
import com.glodblock.github.common.tile.TileLevelMaintainer;
import com.glodblock.github.common.tile.TileOCPatternEditor;
import com.glodblock.github.common.tile.TileSuperStockReplenisher;
import com.glodblock.github.inventory.IDualHost;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.google.common.collect.ImmutableList;

import appeng.api.parts.ILevelEmitter;
import appeng.api.storage.ITerminalHost;
import appeng.container.implementations.ContainerLevelEmitter;

public enum GuiType {

    FLUID_AUTO_FILLER(new TileGuiFactory<>(TileFluidAutoFiller.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, TileFluidAutoFiller inv) {
            return new ContainerFluidAutoFiller(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, TileFluidAutoFiller inv) {
            return new GuiFluidAutoFiller(player.inventory, inv);
        }
    }),

    LEVEL_EMITTER_PROXY(new PartGuiFactory<>(ILevelEmitter.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ILevelEmitter inv) {
            return new ContainerLevelEmitter(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ILevelEmitter inv) {
            return new GuiLevelEmitterProxy(player.inventory, inv);
        }
    }),

    LEVEL_MAINTAINER(new TileGuiFactory<>(TileLevelMaintainer.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, TileLevelMaintainer inv) {
            return new ContainerLevelMaintainer(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, TileLevelMaintainer inv) {
            return new GuiLevelMaintainer(player.inventory, inv);
        }
    }),

    OC_PATTERN_EDITOR(new TileGuiFactory<>(TileOCPatternEditor.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, TileOCPatternEditor inv) {
            return new ContainerOCPatternEditor(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, TileOCPatternEditor inv) {
            return new GuiOCPatternEditor(player.inventory, inv);
        }
    }),

    DUAL_INTERFACE_FLUID(new TileOrPartGuiFactory<>(IDualHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, IDualHost inv) {
            return new ContainerFluidInterface(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IDualHost inv) {
            return new GuiFluidInterface(player.inventory, inv);
        }
    }),

    INGREDIENT_BUFFER(new TileGuiFactory<>(TileIngredientBuffer.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, TileIngredientBuffer inv) {
            return new ContainerIngredientBuffer(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, TileIngredientBuffer inv) {
            return new GuiIngredientBuffer(player.inventory, inv);
        }
    }),

    LARGE_INGREDIENT_BUFFER(new TileGuiFactory<>(TileLargeIngredientBuffer.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, TileLargeIngredientBuffer inv) {
            return new ContainerLargeIngredientBuffer(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, TileLargeIngredientBuffer inv) {
            return new GuiLargeIngredientBuffer(player.inventory, inv);
        }
    }),

    LEVEL_TERMINAL(new TileOrPartGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerLevelTerminal(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiLevelTerminal(player.inventory, inv);
        }
    }),

    WIRELESS_LEVEL_TERMINAL(new ItemGuiFactory<>(IWirelessTerminal.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, IWirelessTerminal inv) {
            return new ContainerLevelWireless(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IWirelessTerminal inv) {
            return new GuiLevelWireless(player.inventory, inv);
        }
    }),

    FLUID_PATTERN_ENCODER(new TileGuiFactory<>(TileFluidPatternEncoder.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, TileFluidPatternEncoder inv) {
            return new ContainerFluidPatternEncoder(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, TileFluidPatternEncoder inv) {
            return new GuiFluidPatternEncoder(player.inventory, inv);
        }
    }),

    FLUID_PACKET_DECODER(new TileGuiFactory<>(TileFluidPacketDecoder.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, TileFluidPacketDecoder inv) {
            return new ContainerFluidPacketDecoder(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, TileFluidPacketDecoder inv) {
            return new GuiFluidPacketDecoder(player.inventory, inv);
        }
    }),

    WIRELESS_MAGNET_FILTER(new ItemGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerMagnetFilter(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiMagnetFilter(player.inventory, inv);
        }
    }),

    GUI_SUPER_STOCK_REPLENISHER(new TileGuiFactory<>(TileSuperStockReplenisher.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, TileSuperStockReplenisher inv) {
            return new ContainerSuperStockReplenisher(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, TileSuperStockReplenisher inv) {
            return new GuiSuperStockReplenisher(player.inventory, inv);
        }
    });

    public static final List<GuiType> VALUES = ImmutableList.copyOf(values());

    @Nullable
    public static GuiType getByOrdinal(int ordinal) {
        return ordinal < 0 || ordinal >= VALUES.size() ? null : VALUES.get(ordinal);
    }

    public final IGuiFactory guiFactory;

    GuiType(IGuiFactory guiFactory) {
        this.guiFactory = guiFactory;
    }
}
