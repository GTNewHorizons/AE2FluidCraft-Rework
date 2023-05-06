package com.glodblock.github.inventory.gui;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;

import com.glodblock.github.client.gui.*;
import com.glodblock.github.client.gui.container.*;
import com.glodblock.github.common.parts.PartFluidLevelEmitter;
import com.glodblock.github.common.parts.PartFluidStorageBus;
import com.glodblock.github.common.parts.base.FCPart;
import com.glodblock.github.common.parts.base.FCSharedFluidBus;
import com.glodblock.github.common.tile.*;
import com.glodblock.github.inventory.IDualHost;
import com.glodblock.github.inventory.item.IFluidPortableCell;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.google.common.collect.ImmutableList;

import appeng.api.storage.ITerminalHost;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.container.implementations.ContainerCraftingStatus;
import appeng.container.implementations.ContainerPriority;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;

public enum GuiType {

    RENAMER(new PartOrItemGuiFactory<ITerminalHost>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerRenamer(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiRenamer(player.inventory, inv);
        }
    }),
    FLUID_AUTO_FILLER(new TileGuiFactory<TileFluidAutoFiller>(TileFluidAutoFiller.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, TileFluidAutoFiller inv) {
            return new ContainerFluidAutoFiller(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, TileFluidAutoFiller inv) {
            return new GuiFluidAutoFiller(player.inventory, inv);
        }
    }),

    FLUID_LEVEL_EMITTER(new PartGuiFactory<PartFluidLevelEmitter>(PartFluidLevelEmitter.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, PartFluidLevelEmitter inv) {
            return new ContainerFluidLevelEmitter(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, PartFluidLevelEmitter inv) {
            return new GuiFluidLevelEmitter(player.inventory, inv);
        }
    }),

    LEVEL_MAINTAINER(new TileGuiFactory<TileLevelMaintainer>(TileLevelMaintainer.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, TileLevelMaintainer inv) {
            return new ContainerLevelMaintainer(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, TileLevelMaintainer inv) {
            return new GuiLevelMaintainer(player.inventory, inv);
        }
    }),

    OC_PATTERN_EDITOR(new TileGuiFactory<TileOCPatternEditor>(TileOCPatternEditor.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, TileOCPatternEditor inv) {
            return new ContainerOCPatternEditor(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, TileOCPatternEditor inv) {
            return new GuiOCPatternEditor(player.inventory, inv);
        }
    }),

    DUAL_INTERFACE(new TileOrPartGuiFactory<IInterfaceHost>(IInterfaceHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, IInterfaceHost inv) {
            return new ContainerDualInterface(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IInterfaceHost inv) {
            return new GuiDualInterface(player.inventory, inv);
        }
    }),

    DUAL_INTERFACE_FLUID(new TileOrPartGuiFactory<IDualHost>(IDualHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, IDualHost inv) {
            return new ContainerFluidInterface(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IDualHost inv) {
            return new GuiFluidInterface(player.inventory, inv);
        }
    }),

    PRIORITY(new TileOrPartGuiFactory<IPriorityHost>(IPriorityHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, IPriorityHost inv) {
            return new ContainerPriority(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IPriorityHost inv) {
            return new GuiFCPriority(player.inventory, inv);
        }
    }),

    FLUID_BUS_IO(new PartGuiFactory<FCSharedFluidBus>(FCSharedFluidBus.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, FCSharedFluidBus inv) {
            return new ContainerFluidIO(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, FCSharedFluidBus inv) {
            return new GuiFluidIO(player.inventory, inv);
        }
    }),

    FLUID_STORAGE_BUS(new PartGuiFactory<PartFluidStorageBus>(PartFluidStorageBus.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, PartFluidStorageBus inv) {
            return new ContainerFluidStorageBus(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, PartFluidStorageBus inv) {
            return new GuiFluidStorageBus(player.inventory, inv);
        }
    }),

    INGREDIENT_BUFFER(new TileGuiFactory<TileIngredientBuffer>(TileIngredientBuffer.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, TileIngredientBuffer inv) {
            return new ContainerIngredientBuffer(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, TileIngredientBuffer inv) {
            return new GuiIngredientBuffer(player.inventory, inv);
        }
    }),

    LARGE_INGREDIENT_BUFFER(new TileGuiFactory<TileLargeIngredientBuffer>(TileLargeIngredientBuffer.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, TileLargeIngredientBuffer inv) {
            return new ContainerLargeIngredientBuffer(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, TileLargeIngredientBuffer inv) {
            return new GuiLargeIngredientBuffer(player.inventory, inv);
        }
    }),

    CRAFTING_STATUS(new PartOrItemGuiFactory<ITerminalHost>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerCraftingStatus(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiFluidPatternTerminalCraftingStatus(player.inventory, inv);
        }
    }),

    FLUID_TERMINAL(new TileOrPartGuiFactory<ITerminalHost>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerFluidMonitor(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiFluidTerminal(player.inventory, inv);
        }
    }),

    PORTABLE_FLUID_CELL(new ItemGuiFactory<IFluidPortableCell>(IFluidPortableCell.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, IFluidPortableCell inv) {
            return new ContainerFluidPortableCell(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IFluidPortableCell inv) {
            return new GuiFluidPortableCell(player.inventory, inv);
        }
    }),

    WIRELESS_FLUID_TERMINAL(new ItemGuiFactory<IWirelessTerminal>(IWirelessTerminal.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, IWirelessTerminal inv) {
            return new ContainerFluidPortableCell(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IWirelessTerminal inv) {
            return new GuiFluidPortableCell(player.inventory, inv);
        }
    }),

    WIRELESS_ESSENTIA_TERMINAL(new ItemGuiFactory<IWirelessTerminal>(IWirelessTerminal.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, IWirelessTerminal inv) {
            return new ContainerEssentiaMonitor(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IWirelessTerminal inv) {
            return new GuiEssentiaTerminal(player.inventory, inv);
        }
    }),

    WIRELESS_FLUID_PATTERN_TERMINAL(new ItemGuiFactory<IWirelessTerminal>(IWirelessTerminal.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, IWirelessTerminal inv) {
            return new ContainerFluidPatternWireless(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IWirelessTerminal inv) {
            return new GuiFluidPatternWireless(player.inventory, inv);
        }
    }),
    WIRELESS_FLUID_PATTERN_TERMINAL_EX(new ItemGuiFactory<IWirelessTerminal>(IWirelessTerminal.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, IWirelessTerminal inv) {
            return new ContainerFluidPatternExWireless(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IWirelessTerminal inv) {
            return new GuiFluidPatternExWireless(player.inventory, inv);
        }
    }),
    WIRELESS_INTERFACE_TERMINAL(new ItemGuiFactory<IWirelessTerminal>(IWirelessTerminal.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, IWirelessTerminal inv) {
            return new ContainerInterfaceWireless(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IWirelessTerminal inv) {
            return new GuiInterfaceTerminalWireless(player.inventory, inv);
        }
    }),

    WIRELESS_CRAFTING_TERMINAL(new ItemGuiFactory<IWirelessTerminal>(IWirelessTerminal.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, IWirelessTerminal inv) {
            return new ContainerCraftingWireless(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IWirelessTerminal inv) {
            return new GuiFluidCraftingWireless(player.inventory, inv);
        }
    }),

    FLUID_PATTERN_TERMINAL(new PartGuiFactory<ITerminalHost>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerFluidPatternTerminal(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiFluidPatternTerminal(player.inventory, inv);
        }
    }),

    FLUID_PATTERN_TERMINAL_EX(new PartGuiFactory<ITerminalHost>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerFluidPatternTerminalEx(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiFluidPatternTerminalEx(player.inventory, inv);
        }
    }),

    FLUID_PATTERN_ENCODER(new TileGuiFactory<TileFluidPatternEncoder>(TileFluidPatternEncoder.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, TileFluidPatternEncoder inv) {
            return new ContainerFluidPatternEncoder(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, TileFluidPatternEncoder inv) {
            return new GuiFluidPatternEncoder(player.inventory, inv);
        }
    }),

    FLUID_CRAFTING_CONFIRM(new PartGuiFactory<FCPart>(FCPart.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, FCPart inv) {
            return new ContainerFluidCraftConfirm(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, FCPart inv) {
            return new GuiFluidCraftConfirm(player.inventory, inv);
        }
    }),

    FLUID_CRAFTING_CONFIRM_ITEM(new ItemGuiFactory<IWirelessTerminal>(IWirelessTerminal.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, IWirelessTerminal inv) {
            return new ContainerFluidCraftConfirm(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IWirelessTerminal inv) {
            return new GuiFluidCraftConfirm(player.inventory, inv);
        }
    }),

    FLUID_CRAFTING_AMOUNT(new PartOrItemGuiFactory<ITerminalHost>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerCraftAmount(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiFluidCraftAmount(player.inventory, inv);
        }
    }),

    FLUID_PACKET_DECODER(new TileGuiFactory<TileFluidPacketDecoder>(TileFluidPacketDecoder.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, TileFluidPacketDecoder inv) {
            return new ContainerFluidPacketDecoder(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, TileFluidPacketDecoder inv) {
            return new GuiFluidPacketDecoder(player.inventory, inv);
        }
    }),

    PATTERN_VALUE_SET(new PartOrItemGuiFactory<ITerminalHost>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerPatternValueAmount(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiPatternValueAmount(player.inventory, inv);
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
