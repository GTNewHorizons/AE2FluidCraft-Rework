package com.glodblock.github.inventory.gui;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;

import com.glodblock.github.client.gui.GuiCraftingStatus;
import com.glodblock.github.client.gui.GuiDualInterface;
import com.glodblock.github.client.gui.GuiEssentiaTerminal;
import com.glodblock.github.client.gui.GuiFCPriority;
import com.glodblock.github.client.gui.GuiFluidAutoFiller;
import com.glodblock.github.client.gui.GuiFluidCraftAmount;
import com.glodblock.github.client.gui.GuiFluidCraftConfirm;
import com.glodblock.github.client.gui.GuiFluidCraftingWireless;
import com.glodblock.github.client.gui.GuiFluidIO;
import com.glodblock.github.client.gui.GuiFluidInterface;
import com.glodblock.github.client.gui.GuiFluidLevelEmitter;
import com.glodblock.github.client.gui.GuiFluidOptimizePatterns;
import com.glodblock.github.client.gui.GuiFluidPacketDecoder;
import com.glodblock.github.client.gui.GuiFluidPatternEncoder;
import com.glodblock.github.client.gui.GuiFluidPatternExWireless;
import com.glodblock.github.client.gui.GuiFluidPatternTerminal;
import com.glodblock.github.client.gui.GuiFluidPatternTerminalEx;
import com.glodblock.github.client.gui.GuiFluidPatternWireless;
import com.glodblock.github.client.gui.GuiFluidPortableCell;
import com.glodblock.github.client.gui.GuiFluidStorageBus;
import com.glodblock.github.client.gui.GuiFluidTerminal;
import com.glodblock.github.client.gui.GuiIngredientBuffer;
import com.glodblock.github.client.gui.GuiInterfaceWireless;
import com.glodblock.github.client.gui.GuiLargeIngredientBuffer;
import com.glodblock.github.client.gui.GuiLevelMaintainer;
import com.glodblock.github.client.gui.GuiLevelTerminal;
import com.glodblock.github.client.gui.GuiLevelWireless;
import com.glodblock.github.client.gui.GuiOCPatternEditor;
import com.glodblock.github.client.gui.GuiPatternItemRenamer;
import com.glodblock.github.client.gui.GuiPatternMulti;
import com.glodblock.github.client.gui.GuiPatternValueAmount;
import com.glodblock.github.client.gui.GuiRenamer;
import com.glodblock.github.client.gui.container.ContainerCraftingWireless;
import com.glodblock.github.client.gui.container.ContainerDualInterface;
import com.glodblock.github.client.gui.container.ContainerEssentiaMonitor;
import com.glodblock.github.client.gui.container.ContainerFluidAutoFiller;
import com.glodblock.github.client.gui.container.ContainerFluidCraftConfirm;
import com.glodblock.github.client.gui.container.ContainerFluidIO;
import com.glodblock.github.client.gui.container.ContainerFluidInterface;
import com.glodblock.github.client.gui.container.ContainerFluidLevelEmitter;
import com.glodblock.github.client.gui.container.ContainerFluidMonitor;
import com.glodblock.github.client.gui.container.ContainerFluidOptimizePatterns;
import com.glodblock.github.client.gui.container.ContainerFluidPacketDecoder;
import com.glodblock.github.client.gui.container.ContainerFluidPatternEncoder;
import com.glodblock.github.client.gui.container.ContainerFluidPatternExWireless;
import com.glodblock.github.client.gui.container.ContainerFluidPatternTerminal;
import com.glodblock.github.client.gui.container.ContainerFluidPatternTerminalEx;
import com.glodblock.github.client.gui.container.ContainerFluidPatternWireless;
import com.glodblock.github.client.gui.container.ContainerFluidPortableCell;
import com.glodblock.github.client.gui.container.ContainerFluidStorageBus;
import com.glodblock.github.client.gui.container.ContainerIngredientBuffer;
import com.glodblock.github.client.gui.container.ContainerInterfaceWireless;
import com.glodblock.github.client.gui.container.ContainerLargeIngredientBuffer;
import com.glodblock.github.client.gui.container.ContainerLevelMaintainer;
import com.glodblock.github.client.gui.container.ContainerLevelTerminal;
import com.glodblock.github.client.gui.container.ContainerLevelWireless;
import com.glodblock.github.client.gui.container.ContainerOCPatternEditor;
import com.glodblock.github.client.gui.container.ContainerPatternMulti;
import com.glodblock.github.client.gui.container.ContainerPatternValueAmount;
import com.glodblock.github.client.gui.container.ContainerRenamer;
import com.glodblock.github.common.parts.PartFluidLevelEmitter;
import com.glodblock.github.common.parts.PartFluidStorageBus;
import com.glodblock.github.common.parts.base.FCPart;
import com.glodblock.github.common.parts.base.FCSharedFluidBus;
import com.glodblock.github.common.tile.TileFluidAutoFiller;
import com.glodblock.github.common.tile.TileFluidPacketDecoder;
import com.glodblock.github.common.tile.TileFluidPatternEncoder;
import com.glodblock.github.common.tile.TileIngredientBuffer;
import com.glodblock.github.common.tile.TileLargeIngredientBuffer;
import com.glodblock.github.common.tile.TileLevelMaintainer;
import com.glodblock.github.common.tile.TileOCPatternEditor;
import com.glodblock.github.inventory.IDualHost;
import com.glodblock.github.inventory.item.IFluidPortableCell;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.google.common.collect.ImmutableList;

import appeng.api.storage.ITerminalHost;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.container.implementations.ContainerCraftingStatus;
import appeng.container.implementations.ContainerPatternItemRenamer;
import appeng.container.implementations.ContainerPriority;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;

public enum GuiType {

    RENAMER(new PartOrItemGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerRenamer(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiRenamer(player.inventory, inv);
        }
    }),
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

    FLUID_LEVEL_EMITTER(new PartGuiFactory<>(PartFluidLevelEmitter.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, PartFluidLevelEmitter inv) {
            return new ContainerFluidLevelEmitter(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, PartFluidLevelEmitter inv) {
            return new GuiFluidLevelEmitter(player.inventory, inv);
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

    DUAL_INTERFACE(new TileOrPartGuiFactory<>(IInterfaceHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, IInterfaceHost inv) {
            return new ContainerDualInterface(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IInterfaceHost inv) {
            return new GuiDualInterface(player.inventory, inv);
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

    PRIORITY(new TileOrPartGuiFactory<>(IPriorityHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, IPriorityHost inv) {
            return new ContainerPriority(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IPriorityHost inv) {
            return new GuiFCPriority(player.inventory, inv);
        }
    }),

    FLUID_BUS_IO(new PartGuiFactory<>(FCSharedFluidBus.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, FCSharedFluidBus inv) {
            return new ContainerFluidIO(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, FCSharedFluidBus inv) {
            return new GuiFluidIO(player.inventory, inv);
        }
    }),

    FLUID_STORAGE_BUS(new PartGuiFactory<>(PartFluidStorageBus.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, PartFluidStorageBus inv) {
            return new ContainerFluidStorageBus(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, PartFluidStorageBus inv) {
            return new GuiFluidStorageBus(player.inventory, inv);
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

    CRAFTING_STATUS(new PartOrItemGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerCraftingStatus(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiCraftingStatus(player.inventory, inv);
        }
    }),

    FLUID_TERMINAL(new TileOrPartGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerFluidMonitor(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiFluidTerminal(player.inventory, inv);
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

    PORTABLE_FLUID_CELL(new ItemGuiFactory<>(IFluidPortableCell.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, IFluidPortableCell inv) {
            return new ContainerFluidPortableCell(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IFluidPortableCell inv) {
            return new GuiFluidPortableCell(player.inventory, inv);
        }
    }),

    WIRELESS_FLUID_TERMINAL(new ItemGuiFactory<>(IWirelessTerminal.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, IWirelessTerminal inv) {
            return new ContainerFluidPortableCell(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IWirelessTerminal inv) {
            return new GuiFluidPortableCell(player.inventory, inv);
        }
    }),

    WIRELESS_ESSENTIA_TERMINAL(new ItemGuiFactory<>(IWirelessTerminal.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, IWirelessTerminal inv) {
            return new ContainerEssentiaMonitor(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IWirelessTerminal inv) {
            return new GuiEssentiaTerminal(player.inventory, inv);
        }
    }),

    WIRELESS_FLUID_PATTERN_TERMINAL(new ItemGuiFactory<>(IWirelessTerminal.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, IWirelessTerminal inv) {
            return new ContainerFluidPatternWireless(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IWirelessTerminal inv) {
            return new GuiFluidPatternWireless(player.inventory, inv);
        }
    }),
    WIRELESS_FLUID_PATTERN_TERMINAL_EX(new ItemGuiFactory<>(IWirelessTerminal.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, IWirelessTerminal inv) {
            return new ContainerFluidPatternExWireless(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IWirelessTerminal inv) {
            return new GuiFluidPatternExWireless(player.inventory, inv);
        }
    }),
    WIRELESS_INTERFACE_TERMINAL(new ItemGuiFactory<>(IWirelessTerminal.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, IWirelessTerminal inv) {
            return new ContainerInterfaceWireless(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IWirelessTerminal inv) {
            return new GuiInterfaceWireless(player.inventory, inv);
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

    WIRELESS_CRAFTING_TERMINAL(new ItemGuiFactory<>(IWirelessTerminal.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, IWirelessTerminal inv) {
            return new ContainerCraftingWireless(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IWirelessTerminal inv) {
            return new GuiFluidCraftingWireless(player.inventory, inv);
        }
    }),

    FLUID_PATTERN_TERMINAL(new PartGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerFluidPatternTerminal(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiFluidPatternTerminal(player.inventory, inv);
        }
    }),

    FLUID_PATTERN_TERMINAL_EX(new PartGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerFluidPatternTerminalEx(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiFluidPatternTerminalEx(player.inventory, inv);
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

    FLUID_CRAFTING_CONFIRM(new PartGuiFactory<>(FCPart.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, FCPart inv) {
            return new ContainerFluidCraftConfirm(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, FCPart inv) {
            return new GuiFluidCraftConfirm(player.inventory, inv);
        }
    }),

    FLUID_CRAFTING_CONFIRM_ITEM(new ItemGuiFactory<>(IWirelessTerminal.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, IWirelessTerminal inv) {
            return new ContainerFluidCraftConfirm(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IWirelessTerminal inv) {
            return new GuiFluidCraftConfirm(player.inventory, inv);
        }
    }),

    FLUID_OPTIMIZE_PATTERNS(new PartGuiFactory<>(FCPart.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, FCPart inv) {
            return new ContainerFluidOptimizePatterns(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, FCPart inv) {
            return new GuiFluidOptimizePatterns(player.inventory, inv);
        }
    }),

    FLUID_OPTIMIZE_PATTERNS_ITEM(new ItemGuiFactory<>(IWirelessTerminal.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, IWirelessTerminal inv) {
            return new ContainerFluidOptimizePatterns(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IWirelessTerminal inv) {
            return new GuiFluidOptimizePatterns(player.inventory, inv);
        }
    }),

    FLUID_CRAFTING_AMOUNT(new PartOrItemGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerCraftAmount(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiFluidCraftAmount(player.inventory, inv);
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

    PATTERN_VALUE_SET(new PartOrItemGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerPatternValueAmount(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiPatternValueAmount(player.inventory, inv);
        }
    }),

    GUI_PATTERN_MULTI(new PartOrItemGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerPatternMulti(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiPatternMulti(player.inventory, inv);
        }
    }),

    GUI_PATTERN_ITEM_RENAMER(new PartOrItemGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerPatternItemRenamer(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiPatternItemRenamer(player.inventory, inv);
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
