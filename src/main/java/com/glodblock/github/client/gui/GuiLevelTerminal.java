package com.glodblock.github.client.gui;

import static com.glodblock.github.common.item.ItemWirelessUltraTerminal.hasInfinityBoosterCard;
import static com.glodblock.github.network.SPacketLevelTerminalUpdate.CLEAR_ALL_BIT;
import static com.glodblock.github.network.SPacketLevelTerminalUpdate.DISCONNECT_BIT;
import static com.glodblock.github.network.SPacketLevelTerminalUpdate.PacketAdd;
import static com.glodblock.github.network.SPacketLevelTerminalUpdate.PacketEntry;
import static com.glodblock.github.network.SPacketLevelTerminalUpdate.PacketOverwrite;
import static com.glodblock.github.network.SPacketLevelTerminalUpdate.PacketRemove;
import static com.glodblock.github.network.SPacketLevelTerminalUpdate.PacketRename;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.base.FCBaseMEGui;
import com.glodblock.github.client.gui.container.ContainerLevelTerminal;
import com.glodblock.github.common.tile.TileLevelMaintainer.State;
import com.glodblock.github.common.tile.TileLevelMaintainer.TLMTags;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.network.CPacketInventoryAction;
import com.glodblock.github.network.CPacketLevelTerminalCommands;
import com.glodblock.github.network.CPacketLevelTerminalCommands.Action;
import com.glodblock.github.network.CPacketRenamer;
import com.glodblock.github.util.FCGuiColors;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.Util;

import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.api.config.YesNo;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.WorldCoord;
import appeng.client.gui.IGuiTooltipHandler;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.gui.widgets.IDropToFillTextField;
import appeng.client.gui.widgets.MEGuiTextField;
import appeng.client.render.BlockPosHighlighter;
import appeng.container.AEBaseContainer;
import appeng.container.slot.AppEngSlot;
import appeng.core.AEConfig;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiColors;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.InventoryAction;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.ReadableNumberConverter;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.Loader;

public class GuiLevelTerminal extends FCBaseMEGui implements IDropToFillTextField, IGuiTooltipHandler {

    public static final int HEADER_HEIGHT = 52;
    public static final int INV_HEIGHT = 98;
    public static final int VIEW_WIDTH = 174;
    public static final int VIEW_LEFT = 8;
    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/level_terminal.png");
    protected int offsetY;
    private static final int offsetX = 21;
    protected static String searchFieldOutputsText = "";
    protected static String searchFieldNamesText = "";
    protected static String currentMode = "OFF";
    private final MEGuiTextField searchFieldOutputs;
    private final MEGuiTextField searchFieldNames;
    private GuiImgButton searchStringSave;
    private GuiImgButton terminalStyleBox;

    private ItemStack tooltipStack;
    private boolean online;
    private final boolean neiPresent;
    private int viewHeight;

    protected GuiTabButton craftingStatusBtn;
    // TODO: @Laiff: Implement command `enable_all`, `disable_all` main consern:
    // TODO: Scheduling of all activated maintainers should be spreaded across ticks to not request all items on one
    // tick
    // private GuiFCImgButton modeSwitchView;
    // private GuiFCImgButton modeSwitchEdit;
    private final LevelTerminalList masterList = new LevelTerminalList();
    private final List<String> extraOptionsText = new ArrayList<>(2);
    private static final float ITEM_STACK_Z = 100.0f;
    private static final float SLOT_Z = 0.5f;
    private static final float ITEM_STACK_OVERLAY_Z = 200.0f;
    private static final float SLOT_HOVER_Z = 310.0f;
    private static final float TOOLTIP_Z = 410.0f;
    private static final float STEP_Z = 10.0f;
    private static final float MAGIC_RENDER_ITEM_Z = 50.0f;

    public GuiLevelTerminal(InventoryPlayer inventoryPlayer, Container container) {
        super(inventoryPlayer, container);
        setScrollBar(new GuiScrollbar());
        xSize = 208;
        ySize = 255;
        neiPresent = Loader.isModLoaded("NotEnoughItems");

        searchFieldOutputs = new MEGuiTextField(86, 12, ButtonToolTips.SearchFieldOutputs.getLocal()) {

            @Override
            public void onTextChange(final String oldText) {
                masterList.markDirty();
            }
        };

        searchFieldNames = new MEGuiTextField(71, 12, ButtonToolTips.SearchFieldNames.getLocal()) {

            @Override
            public void onTextChange(final String oldText) {
                masterList.markDirty();
            }
        };
        searchFieldNames.setFocused(true);
        extraOptionsText.add(ButtonToolTips.HighlightInterface.getLocal());
    }

    public GuiLevelTerminal(final InventoryPlayer inventoryPlayer, final ITerminalHost te) {
        this(inventoryPlayer, new ContainerLevelTerminal(inventoryPlayer, te));
    }

    @Override
    public int getOffsetY() {
        return offsetY;
    }

    @Override
    public void setOffsetY(int y) {
        offsetY = y;
    }

    private void setScrollBar() {
        int maxScroll = masterList.getHeight() - viewHeight - 1;
        if (maxScroll <= 0) {
            getScrollBar().setTop(52).setLeft(189).setHeight(viewHeight).setRange(0, 0, 1);
        } else {
            getScrollBar().setTop(52).setLeft(189).setHeight(viewHeight).setRange(0, maxScroll, 12);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initGui() {
        super.initGui();
        viewHeight = calculateViewHeight();
        ySize = HEADER_HEIGHT + INV_HEIGHT + viewHeight;
        final int unusedSpace = height - ySize;
        guiTop = (int) Math.floor(unusedSpace / (unusedSpace < 0 ? 3.8f : 2.0f));

        // modeSwitchView = new GuiFCImgButton(guiLeft + xSize - 40, guiTop + 1, "SWITCH", "OFF", false);
        // modeSwitchEdit = new GuiFCImgButton(guiLeft + xSize - 40, guiTop + 1, "SWITCH", "ON", false);
        offsetY = guiTop + 8;
        terminalStyleBox = new GuiImgButton(
                guiLeft - 18,
                offsetY,
                Settings.TERMINAL_STYLE,
                AEConfig.instance.settings.getSetting(Settings.TERMINAL_STYLE));
        offsetY += 20;
        searchStringSave = new GuiImgButton(
                guiLeft - 18,
                offsetY,
                Settings.SAVE_SEARCH,
                AEConfig.instance.settings.getSetting(Settings.SAVE_SEARCH));
        offsetY += 20;

        searchFieldOutputs.x = guiLeft + Math.max(32, offsetX);
        searchFieldOutputs.y = guiTop + 38;

        searchFieldNames.x = guiLeft + Math.max(32, offsetX) + 99;
        searchFieldNames.y = guiTop + 38;

        terminalStyleBox.xPosition = guiLeft - 18;
        terminalStyleBox.yPosition = guiTop + 8;
        craftingStatusBtn = new GuiTabButton(
                guiLeft + xSize - 24,
                guiTop - 4,
                2 + 11 * 16,
                GuiText.CraftingStatus.getLocal(),
                itemRender);
        craftingStatusBtn.setHideEdge(13);

        if (ModAndClassUtil.isSearchBar && (AEConfig.instance.preserveSearchBar || isSubGui())) {
            setSearchString();
        }
        buttonList.add(terminalStyleBox);
        buttonList.add(searchStringSave);
        buttonList.add(craftingStatusBtn);

        setScrollBar();
        repositionSlots();
        initGuiDone();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        searchFieldOutputsText = searchFieldOutputs.getText();
        searchFieldNamesText = searchFieldNames.getText();
    }

    public void setSearchString() {
        searchFieldOutputs.setText(searchFieldOutputsText);
        searchFieldNames.setText(searchFieldNamesText);
    }

    protected void repositionSlots() {
        for (final Object obj : inventorySlots.inventorySlots) {
            if (obj instanceof final AppEngSlot slot) {
                slot.yDisplayPosition = slot.getY() + ySize - 78 - 4;
            }
        }
    }

    private int getMaxViewHeight() {
        return AEConfig.instance.getConfigManager().getSetting(Settings.TERMINAL_STYLE) == TerminalStyle.SMALL
                ? AEConfig.instance.InterfaceTerminalSmallSize * 18
                : Integer.MAX_VALUE;
    }

    protected int calculateViewHeight() {
        final int maxViewHeight = getMaxViewHeight();
        final boolean hasNEI = IntegrationRegistry.INSTANCE.isEnabled(IntegrationType.NEI);
        final int NEIPadding = hasNEI ? 22 /* input */ + 18 /* top panel */ : 0;
        final int availableSpace = height - HEADER_HEIGHT - INV_HEIGHT - NEIPadding;

        // screen should use 95% of the space it can, 5% margins
        return Math.min((int) (availableSpace * 0.95), maxViewHeight);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        fontRendererObj.drawString(
                StatCollector.translateToLocal(NameConst.GUI_LEVEL_TERMINAL),
                8,
                6,
                GuiColors.InterfaceTerminalTitle.getColor());
        fontRendererObj.drawString(
                GuiText.inventory.getLocal(),
                GuiLevelTerminal.offsetX + 2,
                ySize - 96 + 3,
                GuiColors.InterfaceTerminalInventory.getColor());

        if (!neiPresent && tooltipStack != null) {
            renderToolTip(tooltipStack, mouseX, mouseY);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float btn) {
        terminalStyleBox.set(AEConfig.instance.settings.getSetting(Settings.TERMINAL_STYLE));

        buttonList.clear();
        buttonList.add(terminalStyleBox);
        buttonList.add(searchStringSave);
        buttonList.add(craftingStatusBtn);
        // buttonList.add(Objects.equals(currentMode, "OFF") ? modeSwitchView : modeSwitchEdit);
        addSwitchGuiBtns();

        super.drawScreen(mouseX, mouseY, btn);

        handleTooltip(mouseX, mouseY, searchFieldOutputs);
        handleTooltip(mouseX, mouseY, searchFieldNames);
    }

    @Override
    protected void mouseClicked(final int xCoord, final int yCoord, final int btn) {
        boolean focusOut = searchFieldOutputs.isFocused();
        boolean focusName = searchFieldNames.isFocused();

        searchFieldOutputs.mouseClicked(xCoord, yCoord, btn);
        searchFieldNames.mouseClicked(xCoord, yCoord, btn);

        if (focusOut && !searchFieldOutputs.isFocused()) {
            searchFieldOutputsText = searchFieldOutputs.getText();
        } else if (focusName && !searchFieldNames.isFocused()) {
            searchFieldNamesText = searchFieldNames.getText();
        }

        if (masterList.mouseClicked(xCoord - guiLeft - VIEW_LEFT, yCoord - guiTop - HEADER_HEIGHT, btn)) {
            return;
        }

        super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
//        if (btn == modeSwitchView) {
//            currentMode = "ON";
//        } else if (btn == modeSwitchEdit) {
//            currentMode = "OFF";
//        } else
            if (ModAndClassUtil.isSaveText && btn == searchStringSave) {

            final boolean backwards = Mouse.isButtonDown(1);
            final GuiImgButton iBtn = (GuiImgButton) btn;
            final Enum<?> cv = iBtn.getCurrentValue();
            final Enum<?> next = Platform.rotateEnum(cv, backwards, iBtn.getSetting().getPossibleValues());
            AEConfig.instance.preserveSearchBar = next == YesNo.YES;
            AEConfig.instance.settings.putSetting(Settings.SAVE_SEARCH, next);
            searchStringSave.set(next);

        } else if (btn == craftingStatusBtn) {
            InventoryHandler.switchGui(GuiType.CRAFTING_STATUS);
        } else if (btn instanceof final GuiImgButton iBtn) {
            if (iBtn.getSetting() != Settings.ACTIONS) {
                final Enum<?> cv = iBtn.getCurrentValue();
                final boolean backwards = Mouse.isButtonDown(1);
                final Enum<?> next = Platform.rotateEnum(cv, backwards, iBtn.getSetting().getPossibleValues());

                if (btn == terminalStyleBox) {
                    AEConfig.instance.settings.putSetting(iBtn.getSetting(), next);

                    reinitialize();
                }

                iBtn.set(next);
            }
        }
        super.actionPerformed(btn);
    }

    private void reinitialize() {
        buttonList.clear();
        initGui();
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        mc.getTextureManager().bindTexture(TEX_BG);
        /* Draws the top part. */
        drawTexturedModalRect(offsetX, offsetY, 0, 0, xSize, HEADER_HEIGHT);
        /* Draws the middle part. */
        Tessellator.instance.startDrawingQuads();
        addTexturedRectToTesselator(
                offsetX,
                offsetY + HEADER_HEIGHT,
                offsetX + xSize,
                offsetY + HEADER_HEIGHT + viewHeight + 1,
                0.0f,
                0.0f,
                (HEADER_HEIGHT + LevelTerminalSection.TITLE_HEIGHT + 1.0f) / 256.0f,
                xSize / 256.0f,
                (HEADER_HEIGHT + 106.0f) / 256.0f);
        Tessellator.instance.draw();
        /* Draw the bottom part */
        drawTexturedModalRect(offsetX, offsetY + HEADER_HEIGHT + viewHeight, 0, 158, xSize, INV_HEIGHT);
        if (online) {
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            /* (0,0) => viewPort's (0,0) */
            GL11.glPushMatrix();
            GL11.glTranslatef(offsetX + VIEW_LEFT, offsetY + HEADER_HEIGHT, 0);
            tooltipStack = null;
            masterList.hoveredEntry = null;
            drawViewport(mouseX - offsetX - VIEW_LEFT, mouseY - offsetY - HEADER_HEIGHT - 1);
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
        searchFieldOutputs.drawTextBox();
        searchFieldNames.drawTextBox();
    }

    private void drawViewport(int relMouseX, int relMouseY) {
        /* Viewport Magic */
        final int scroll = getScrollBar().getCurrentScroll();
        int viewY = -scroll; // current y in viewport coordinates
        int entryIdx = 0;
        List<LevelTerminalSection> visibleSections = masterList.getVisibleSections();

        final float guiScaleX = (float) mc.displayWidth / width;
        final float guiScaleY = (float) mc.displayHeight / height;
        GL11.glScissor(
                (int) ((guiLeft + VIEW_LEFT) * guiScaleX),
                (int) ((height - (guiTop + HEADER_HEIGHT + viewHeight)) * guiScaleY),
                (int) (VIEW_WIDTH * guiScaleX),
                (int) (viewHeight * guiScaleY));
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        /*
         * Render each section
         */
        while (viewY < viewHeight && entryIdx < visibleSections.size()) {
            LevelTerminalSection section = visibleSections.get(entryIdx);
            int sectionHeight = section.getHeight();

            /* Is it viewable/in the viewport at all? */
            if (viewY + sectionHeight < 0) {
                entryIdx++;
                viewY += sectionHeight;
                section.visible = false;
                continue;
            }

            section.visible = true;
            int advanceY = drawSection(section, viewY, relMouseX, relMouseY);
            viewY += advanceY;
            entryIdx++;
        }
    }

    private int drawSection(LevelTerminalSection section, int viewY, int relMouseX, int relMouseY) {
        int title;
        int renderY = 0;
        final int sectionBottom = viewY + section.getHeight() - 1;
        final int fontColor = GuiColors.InterfaceTerminalInventory.getColor();
        /*
         * Render title
         */
        mc.getTextureManager().bindTexture(TEX_BG);
        if (sectionBottom > 0 && sectionBottom < LevelTerminalSection.TITLE_HEIGHT) {
            title = sectionBottom;
        } else if (viewY < 0) {
            title = 0;
        } else {
            title = 0;
        }
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        Iterator<LevelTerminalEntry> visible = section.getVisible();
        while (visible.hasNext()) {
            if (viewY < viewHeight) {
                renderY += drawEntry(
                        visible.next(),
                        viewY + LevelTerminalSection.TITLE_HEIGHT + renderY,
                        title,
                        relMouseX,
                        relMouseY);
            } else {
                LevelTerminalEntry entry = visible.next();
                entry.dispY = -9999;
                entry.highlightButton.yPosition = -1;
                entry.renameButton.yPosition = -1;
                entry.configButton.yPosition = -1;
            }
        }
        /*
         * Render title
         */
        mc.getTextureManager().bindTexture(TEX_BG);
        GL11.glTranslatef(0.0f, 0.0f, ITEM_STACK_OVERLAY_Z + ITEM_STACK_Z + STEP_Z);
        if (sectionBottom > 0 && sectionBottom < LevelTerminalSection.TITLE_HEIGHT) {
            /* Transition draw */
            drawTexturedModalRect(
                    0,
                    0,
                    VIEW_LEFT,
                    HEADER_HEIGHT + LevelTerminalSection.TITLE_HEIGHT - sectionBottom,
                    VIEW_WIDTH,
                    sectionBottom);
            fontRendererObj
                    .drawString(section.name, 2, sectionBottom - LevelTerminalSection.TITLE_HEIGHT + 2, fontColor);
        } else if (viewY < 0) {
            /* Hidden title draw */
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glTranslatef(0.0f, 0.0f, 100f);
            drawTexturedModalRect(0, 0, VIEW_LEFT, HEADER_HEIGHT, VIEW_WIDTH, LevelTerminalSection.TITLE_HEIGHT);
            fontRendererObj.drawString(section.name, 2, 2, fontColor);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        } else {
            /* Normal title draw */
            drawTexturedModalRect(0, viewY, VIEW_LEFT, HEADER_HEIGHT, VIEW_WIDTH, LevelTerminalSection.TITLE_HEIGHT);
            fontRendererObj.drawString(section.name, 2, viewY + 2, fontColor);
        }
        GL11.glTranslatef(0.0f, 0.0f, -(ITEM_STACK_OVERLAY_Z + ITEM_STACK_Z + STEP_Z));

        return LevelTerminalSection.TITLE_HEIGHT + renderY;
    }

    private int drawEntry(LevelTerminalEntry entry, int viewY, int titleBottom, int relMouseX, int relMouseY) {
        final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        mc.getTextureManager().bindTexture(TEX_BG);
        Tessellator.instance.startDrawingQuads();
        int relY = 0;
        final int slotLeftMargin = (VIEW_WIDTH - entry.rowSize * 18);
        float lastZLevel = aeRenderItem.zLevel;

        entry.dispY = viewY;
        /* PASS 1: BG */
        for (int row = 0; row < entry.rows; ++row) {
            final int rowYTop = row * 18;
            final int rowYBot = rowYTop + 18;

            relY += 18;
            /* Is the slot row in view? */
            if (viewY + rowYBot <= titleBottom) {
                continue;
            }
            for (int col = 0; col < entry.rowSize; ++col) {
                addTexturedRectToTesselator(
                        col * 18 + slotLeftMargin,
                        viewY + rowYTop,
                        18 * col + 18 + slotLeftMargin,
                        viewY + rowYBot,
                        0,
                        21 / 256f,
                        173 / 256f,
                        (21 + 18) / 256f,
                        (173 + 18) / 256f);
            }
        }
        Tessellator.instance.draw();
        /* Draw button */
        if (viewY + entry.highlightButton.height > 0 && viewY < viewHeight) {
            entry.highlightButton.yPosition = viewY + 1;
            entry.renameButton.yPosition = viewY + 1;
            entry.configButton.yPosition = viewY + 1;
            GuiFCImgButton toRender;
            if (isCtrlKeyDown() && isShiftKeyDown() && hasInfinityBoosterCard(player)) {
                toRender = entry.configButton;
            } else if (isShiftKeyDown()) {
                toRender = entry.renameButton;
            } else {
                toRender = entry.highlightButton;
            }
            toRender.drawButton(mc, relMouseX, relMouseY);
            if (toRender.getMouseIn()
                    && relMouseY >= Math.max(LevelTerminalSection.TITLE_HEIGHT, entry.highlightButton.yPosition)) {
                // draw a tooltip
                GL11.glTranslatef(0f, 0f, TOOLTIP_Z);
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
                drawHoveringText(Arrays.asList(toRender.getMessage()), relMouseX, relMouseY);
                GL11.glTranslatef(0f, 0f, -TOOLTIP_Z);
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
            }
        } else {
            entry.highlightButton.yPosition = -1;
            entry.renameButton.yPosition = -1;
        }
        /* PASS 2: Items */
        for (int row = 0; row < entry.rows; ++row) {
            final int rowYTop = row * 18;
            final int rowYBot = rowYTop + 18;
            /* Is the slot row in view? */
            if (viewY + rowYBot <= titleBottom) {
                continue;
            }
            AppEngInternalInventory inv = entry.getInventory();

            for (int col = 0; col < entry.rowSize; ++col) {
                final int colLeft = col * 18 + slotLeftMargin + 1;
                final int colRight = colLeft + 18 + 1;
                final int slotIdx = row * entry.rowSize + col;
                ItemStack stack = inv.getStackInSlot(slotIdx);

                boolean tooltip = relMouseX > colLeft - 1 && relMouseX < colRight - 1
                        && relMouseY >= Math.max(viewY + rowYTop, LevelTerminalSection.TITLE_HEIGHT)
                        && relMouseY < Math.min(viewY + rowYBot, viewHeight);
                if (stack != null) {

                    AEItemStack aeItemStack = AEItemStack.create(stack);
                    NBTTagCompound data = stack.getTagCompound();
                    ItemStack itemStack = data.hasKey(TLMTags.Stack.tagName)
                            ? ItemStack.loadItemStackFromNBT(data.getCompoundTag(TLMTags.Stack.tagName))
                            : stack.copy();
                    long quantity = data.getLong(TLMTags.Quantity.tagName);
                    long batch = data.getLong(TLMTags.Batch.tagName);
                    State state = State.values()[data.getInteger(TLMTags.State.tagName)];
                    NBTTagCompound linkTag = data.getCompoundTag(TLMTags.Link.tagName);

                    GL11.glPushMatrix();
                    GL11.glTranslatef(colLeft, viewY + rowYTop + 1, ITEM_STACK_Z);
                    GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                    RenderHelper.enableGUIStandardItemLighting();
                    itemStack.stackSize = 1;
                    aeItemStack.setStackSize(quantity);
                    aeRenderItem.setAeStack(aeItemStack);
                    aeRenderItem.zLevel = ITEM_STACK_Z - MAGIC_RENDER_ITEM_Z;
                    aeRenderItem.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), itemStack, 0, 0);
                    aeRenderItem.renderItemOverlayIntoGUI(fontRendererObj, mc.getTextureManager(), itemStack, 0, 0);
                    GL11.glTranslatef(0.0f, 0.0f, ITEM_STACK_OVERLAY_Z - ITEM_STACK_Z);
                    int color = switch (state) {
                        case Idle -> FCGuiColors.StateIdle.getColor();
                        case Craft -> FCGuiColors.StateCraft.getColor();
                        case Export -> FCGuiColors.StateExport.getColor();
                        case Error -> FCGuiColors.StateError.getColor();
                        case None -> FCGuiColors.StateNone.getColor();
                    };
                    int offset = 0;
                    int size = 4;
                    drawRect(offset, offset, offset + size, offset + size, color);
                    GL11.glTranslatef(0.0f, 0.0f, ITEM_STACK_OVERLAY_Z - ITEM_STACK_Z);
                    this.drawReadableAmount(fontRendererObj, quantity, 16.0f, 12f, 16777215);
                    if (batch > 0) {
                        this.drawReadableAmount(fontRendererObj, batch, 16.0f, 1f, 16777215);
                    }
                    RenderHelper.disableStandardItemLighting();

                    /*
                     * Mouse overlay. such large z value because items are rendered at zLevel=100.0f, whatever that is
                     */
                    if (!tooltip) {
                        if (entry.filteredRecipes[slotIdx]) {
                            GL11.glTranslatef(0.0f, 0.0f, ITEM_STACK_OVERLAY_Z);
                            drawRect(0, 0, 16, 16, GuiColors.ItemSlotOverlayUnpowered.getColor());
                        }
                    } else {
                        tooltipStack = stack;
                    }
                    GL11.glPopMatrix();
                } else if (entry.filteredRecipes[slotIdx]) {
                    GL11.glPushMatrix();
                    GL11.glTranslatef(colLeft, viewY + rowYTop + 1, ITEM_STACK_OVERLAY_Z);
                    drawRect(0, 0, 16, 16, GuiColors.ItemSlotOverlayUnpowered.getColor());
                    GL11.glPopMatrix();
                }
                if (tooltip) {
                    // overlay highlight
                    GL11.glDisable(GL11.GL_LIGHTING);
                    GL11.glTranslatef(0.0f, 0.0f, SLOT_HOVER_Z);
                    drawRect(colLeft, viewY + 1 + rowYTop, -2 + colRight, viewY - 1 + rowYBot, 0x77FFFFFF);
                    GL11.glTranslatef(0.0f, 0.0f, -SLOT_HOVER_Z);
                    masterList.hoveredEntry = entry;
                    entry.hoveredSlotIdx = slotIdx;
                }
                GL11.glDisable(GL11.GL_LIGHTING);
            }
        }
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        aeRenderItem.zLevel = lastZLevel;
        return relY + 1;
    }

    public void drawReadableAmount(final FontRenderer fontRenderer, long amount, float x, float y, int color) {

        final float scaleFactor = AEConfig.instance.useTerminalUseLargeFont() ? 0.85f : 0.5f;
        final float inverseScaleFactor = 1.0f / scaleFactor;
        final int offset = AEConfig.instance.useTerminalUseLargeFont() ? 0 : -1;
        final boolean unicodeFlag = fontRenderer.getUnicodeFlag();
        final String stackSize = this.getToBeRenderedStackSize(amount);

        fontRenderer.setUnicodeFlag(false);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glPushMatrix();
        GL11.glScaled(scaleFactor, scaleFactor, scaleFactor);

        final int X = (int) (((float) x + offset - fontRenderer.getStringWidth(stackSize) * scaleFactor)
                * inverseScaleFactor);
        final int Y = (int) (((float) y + offset * scaleFactor) * inverseScaleFactor);

        fontRenderer.drawString(stackSize, X + 1, Y, 0);
        fontRenderer.drawString(stackSize, X - 1, Y, 0);
        fontRenderer.drawString(stackSize, X, Y + 1, 0);
        fontRenderer.drawString(stackSize, X, Y - 1, 0);

        fontRenderer.drawString(stackSize, X, Y, color);

        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_LIGHTING);

        fontRenderer.setUnicodeFlag(unicodeFlag);
    }

    private String getToBeRenderedStackSize(final long originalSize) {
        if (AEConfig.instance.useTerminalUseLargeFont()) {
            return ReadableNumberConverter.INSTANCE.toSlimReadableForm(originalSize);
        } else {
            return ReadableNumberConverter.INSTANCE.toWideReadableForm(originalSize);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void drawHoveringText(List textLines, int x, int y, FontRenderer font) {
        if (!textLines.isEmpty()) {
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.disableStandardItemLighting();
            int maxStrWidth = 0;

            // is this more efficient than doing 1 pass, then doing a translate before drawing the text?
            for (String s : (List<String>) textLines) {
                int width = font.getStringWidth(s);

                if (width > maxStrWidth) {
                    maxStrWidth = width;
                }
            }

            // top left corner
            int curX = x + 12;
            int curY = y - 12;
            int totalHeight = 8;

            if (textLines.size() > 1) {
                totalHeight += 2 + (textLines.size() - 1) * 10;
            }

            /* String is too long? Display on the left side */
            if (curX + maxStrWidth > width) {
                curX -= 28 + maxStrWidth;
            }

            /* String is too tall? move it up */
            if (curY + totalHeight + 6 > height) {
                curY = height - totalHeight - 6;
            }

            int borderColor = -267386864;
            // drawing the border...
            drawGradientRect(curX - 3, curY - 4, curX + maxStrWidth + 3, curY - 3, borderColor, borderColor);
            drawGradientRect(
                    curX - 3,
                    curY + totalHeight + 3,
                    curX + maxStrWidth + 3,
                    curY + totalHeight + 4,
                    borderColor,
                    borderColor);
            drawGradientRect(
                    curX - 3,
                    curY - 3,
                    curX + maxStrWidth + 3,
                    curY + totalHeight + 3,
                    borderColor,
                    borderColor);
            drawGradientRect(curX - 4, curY - 3, curX - 3, curY + totalHeight + 3, borderColor, borderColor);
            drawGradientRect(
                    curX + maxStrWidth + 3,
                    curY - 3,
                    curX + maxStrWidth + 4,
                    curY + totalHeight + 3,
                    borderColor,
                    borderColor);
            int color1 = 1347420415;
            int color2 = (color1 & 16711422) >> 1 | color1 & -16777216;
            drawGradientRect(curX - 3, curY - 3 + 1, curX - 3 + 1, curY + totalHeight + 3 - 1, color1, color2);
            drawGradientRect(
                    curX + maxStrWidth + 2,
                    curY - 3 + 1,
                    curX + maxStrWidth + 3,
                    curY + totalHeight + 3 - 1,
                    color1,
                    color2);
            drawGradientRect(curX - 3, curY - 3, curX + maxStrWidth + 3, curY - 3 + 1, color1, color1);
            drawGradientRect(
                    curX - 3,
                    curY + totalHeight + 2,
                    curX + maxStrWidth + 3,
                    curY + totalHeight + 3,
                    color2,
                    color2);

            for (int i = 0; i < textLines.size(); ++i) {
                String line = (String) textLines.get(i);
                font.drawStringWithShadow(line, curX, curY, -1);

                if (i == 0) {
                    // gap between name and lore text
                    curY += 2;
                }

                curY += 10;
            }

            RenderHelper.enableGUIStandardItemLighting();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        }
    }

    @Override
    protected void keyTyped(final char character, final int key) {
        if (!checkHotbarKeys(key)) {
            if (character == ' ') {
                if ((searchFieldOutputs.getText().isEmpty() && searchFieldOutputs.isFocused())
                        || (searchFieldNames.getText().isEmpty() && searchFieldNames.isFocused()))
                    return;
            } else if (character == '\t') {
                if (handleTab()) return;
            }
            if (searchFieldOutputs.textboxKeyTyped(character, key)
                    || searchFieldNames.textboxKeyTyped(character, key)) {
                masterList.markDirty();
            } else {
                super.keyTyped(character, key);
            }
        }
    }

    private boolean handleTab() {
        if (searchFieldOutputs.isFocused()) {
            searchFieldOutputs.setFocused(false);
            searchFieldNames.setFocused(true);
            return true;
        } else if (searchFieldNames.isFocused()) {
            searchFieldNames.setFocused(false);
            searchFieldOutputs.setFocused(true);
            return true;
        }
        return false;
    }

    @Override
    protected boolean mouseWheelEvent(int mouseX, int mouseY, int wheel) {
        boolean isMouseInViewport = isMouseInViewport(mouseX, mouseY);
        GuiScrollbar scrollbar = getScrollBar();
        if (isMouseInViewport && isCtrlKeyDown()) {
            if (wheel < 0) {
                scrollbar.setCurrentScroll(masterList.getHeight());
            } else {
                getScrollBar().setCurrentScroll(0);
            }
            return true;
        } else if (isMouseInViewport && isShiftKeyDown()) {
            // advance to the next section
            return masterList.scrollNextSection(wheel > 0);
        } else {
            return super.mouseWheelEvent(mouseX, mouseY, wheel);
        }
    }

    private boolean isMouseInViewport(int mouseX, int mouseY) {
        return mouseX > guiLeft + VIEW_LEFT && mouseX < guiLeft + VIEW_LEFT + VIEW_WIDTH
                && mouseY > guiTop + HEADER_HEIGHT
                && mouseY < guiTop + HEADER_HEIGHT + viewHeight;
    }

    public void postUpdate(List<PacketEntry> updates, int statusFlags) {
        if ((statusFlags & CLEAR_ALL_BIT) == CLEAR_ALL_BIT) {
            /* Should clear all client entries. */
            masterList.list.clear();
        }
        /* Should indicate disconnected, so the terminal turns dark. */
        online = (statusFlags & DISCONNECT_BIT) != DISCONNECT_BIT;

        for (PacketEntry cmd : updates) {
            parsePacketCmd(cmd);
        }
        masterList.markDirty();
    }

    private void parsePacketCmd(PacketEntry cmd) {
        long id = cmd.entryId;
        if (cmd instanceof PacketAdd addCmd) {
            LevelTerminalEntry entry = new LevelTerminalEntry(
                    id,
                    addCmd.name,
                    addCmd.rows,
                    addCmd.rowSize,
                    addCmd.online).setLocation(addCmd.x, addCmd.y, addCmd.z, addCmd.dim, addCmd.side)
                            .setIcons(addCmd.selfItemStack, addCmd.displayItemStack).setItems(addCmd.items);
            masterList.addEntry(entry);
        } else if (cmd instanceof PacketRemove) {
            masterList.removeEntry(id);
        } else if (cmd instanceof PacketOverwrite owCmd) {
            LevelTerminalEntry entry = masterList.list.get(id);

            if (entry == null) {
                return;
            }

            if (owCmd.onlineValid) {
                entry.online = owCmd.online;
            }

            if (owCmd.itemsValid) {
                if (owCmd.allItemUpdate) {
                    entry.fullItemUpdate(owCmd.items, owCmd.validIndices.length);
                } else {
                    entry.partialItemUpdate(owCmd.items, owCmd.validIndices);
                }
            }
            masterList.isDirty = true;
        } else if (cmd instanceof PacketRename renameCmd) {
            LevelTerminalEntry entry = masterList.list.get(id);

            if (entry != null) {
                if (StatCollector.canTranslate(renameCmd.newName)) {
                    entry.customName = StatCollector.translateToLocal(renameCmd.newName);
                } else {
                    entry.customName = StatCollector.translateToFallback(renameCmd.newName);
                }
            }
            masterList.isDirty = true;
        }
    }

    private boolean itemStackMatchesSearchTerm(final ItemStack itemStack, final String searchTerm) {
        if (itemStack == null) {
            return false;
        }

        final String displayName = Platform.getItemDisplayName(itemStack).toLowerCase();

        if (displayName.contains(searchTerm)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isOverTextField(final int mouseX, final int mouseY) {
        return searchFieldOutputs.isMouseIn(mouseX, mouseY) || searchFieldNames.isMouseIn(mouseX, mouseY);
    }

    @Override
    public void setTextFieldValue(final String displayName, final int mousex, final int mousey, final ItemStack stack) {
        if (searchFieldOutputs.isMouseIn(mousex, mousey)) {
            searchFieldOutputs.setText(displayName);
        } else if (searchFieldNames.isMouseIn(mousex, mousey)) {
            searchFieldNames.setText(displayName);
        }
    }

    @Override
    public List<String> handleItemTooltip(ItemStack stack, int mouseX, int mouseY, List<String> currentToolTip) {
        return currentToolTip;
    }

    @Override
    public ItemStack getHoveredStack() {
        return tooltipStack;
    }

    /**
     * Tracks the list of entries.
     */
    private class LevelTerminalList {

        private final Map<Long, LevelTerminalEntry> list = new HashMap<>();
        private final Map<String, LevelTerminalSection> sections = new TreeMap<>();
        private final List<LevelTerminalSection> visibleSections = new ArrayList<>();
        private boolean isDirty;
        private int height;
        private LevelTerminalEntry hoveredEntry;

        LevelTerminalList() {
            isDirty = true;
        }

        /**
         * Performs a full update.
         */
        private void update() {
            height = 0;
            visibleSections.clear();

            for (LevelTerminalSection section : sections.values()) {
                String query = GuiLevelTerminal.this.searchFieldNames.getText();
                if (!query.isEmpty() && !section.name.toLowerCase().contains(query.toLowerCase())) {
                    continue;
                }

                section.isDirty = true;
                if (section.getVisible().hasNext()) {
                    height += section.getHeight();
                    visibleSections.add(section);
                }
            }
            isDirty = false;
        }

        public void markDirty() {
            isDirty = true;
            setScrollBar();
        }

        public int getHeight() {
            if (isDirty) {
                update();
            }
            return height;
        }

        /**
         * Jump between sections.
         */
        private boolean scrollNextSection(boolean up) {
            GuiScrollbar scrollbar = getScrollBar();
            int viewY = scrollbar.getCurrentScroll();
            var sections = getVisibleSections();
            boolean result = false;

            if (up) {
                int y = masterList.getHeight();
                int i = sections.size() - 1;

                while (y > 0 && i >= 0) {
                    y -= sections.get(i).getHeight();
                    i -= 1;
                    if (y < viewY) {
                        result = true;
                        scrollbar.setCurrentScroll(y);
                        break;
                    }
                }
            } else {
                int y = 0;

                for (LevelTerminalSection section : sections) {
                    if (y > viewY) {
                        result = true;
                        scrollbar.setCurrentScroll(y);
                        break;
                    }
                    y += section.getHeight();
                }
            }
            return result;
        }

        public void addEntry(LevelTerminalEntry entry) {
            LevelTerminalSection section = sections.get(entry.customName);

            if (section == null) {
                section = new LevelTerminalSection(entry.customName);
                sections.put(entry.customName, section);
            }
            section.addEntry(entry);
            list.put(entry.id, entry);
            isDirty = true;
        }

        public void removeEntry(long id) {
            LevelTerminalEntry entry = list.remove(id);

            if (entry != null) {
                entry.section.removeEntry(entry);
            }
        }

        public List<LevelTerminalSection> getVisibleSections() {
            if (isDirty) {
                update();
            }
            return visibleSections;
        }

        /**
         * Mouse button click.
         *
         * @param relMouseX viewport coords mouse X
         * @param relMouseY viewport coords mouse Y
         * @param btn       button code
         */
        public boolean mouseClicked(int relMouseX, int relMouseY, int btn) {
            if (relMouseX < 0 || relMouseX >= VIEW_WIDTH || relMouseY < 0 || relMouseY >= viewHeight) {
                return false;
            }
            for (LevelTerminalSection section : getVisibleSections()) {
                if (section.mouseClicked(relMouseX, relMouseY, btn)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * A section holds all the interface entries with the same name.
     */
    private class LevelTerminalSection {

        public static final int TITLE_HEIGHT = 12;

        String name;
        List<LevelTerminalEntry> entries = new ArrayList<>();
        Set<LevelTerminalEntry> visibleEntries = new TreeSet<>(Comparator.comparing(e -> {
            if (e.displayItemStack != null) {
                return e.displayItemStack.getDisplayName() + e.id;
            } else {
                return String.valueOf(e.id);
            }
        }));
        int height;
        private boolean isDirty = true;
        boolean visible = false;

        LevelTerminalSection(String name) {
            this.name = name;
        }

        /**
         * Gets the height. Includes title.
         */
        public int getHeight() {
            if (isDirty) {
                update();
            }
            return height;
        }

        private void update() {
            refreshVisible();
            if (visibleEntries.isEmpty()) {
                height = 0;
            } else {
                height = TITLE_HEIGHT;
                for (LevelTerminalEntry entry : visibleEntries) {
                    height += entry.guiHeight;
                }
            }
            isDirty = false;
        }

        public void refreshVisible() {
            visibleEntries.clear();
            String output = GuiLevelTerminal.this.searchFieldOutputs.getText().toLowerCase();

            for (LevelTerminalEntry entry : entries) {
                entry.dispY = -9999;
                // Find search terms
                if (!output.isEmpty()) {
                    AppEngInternalInventory inv = entry.inv;
                    boolean shouldAdd = false;

                    for (int i = 0; i < inv.getSizeInventory(); ++i) {
                        ItemStack stack = inv.getStackInSlot(i);
                        if (itemStackMatchesSearchTerm(stack, output)) {
                            shouldAdd = true;
                            entry.filteredRecipes[i] = false;
                        } else {
                            entry.filteredRecipes[i] = true;
                        }
                    }
                    if (!shouldAdd) {
                        continue;
                    }
                } else {
                    Arrays.fill(entry.filteredRecipes, false);
                }
                visibleEntries.add(entry);
            }
        }

        public void addEntry(LevelTerminalEntry entry) {
            entries.add(entry);
            entry.section = this;
            isDirty = true;
        }

        public void removeEntry(LevelTerminalEntry entry) {
            entries.remove(entry);
            entry.section = null;
            isDirty = true;
        }

        public Iterator<LevelTerminalEntry> getVisible() {
            if (isDirty) {
                update();
            }
            return visibleEntries.iterator();
        }

        public boolean mouseClicked(int relMouseX, int relMouseY, int btn) {
            Iterator<LevelTerminalEntry> it = getVisible();
            boolean ret = false;

            while (it.hasNext() && !ret) {
                ret = it.next().mouseClicked(relMouseX, relMouseY, btn);
            }

            return ret;
        }
    }

    /**
     * This class keeps track of an entry and its widgets.
     */
    private class LevelTerminalEntry {

        String customName;
        AppEngInternalInventory inv;
        GuiFCImgButton highlightButton;
        GuiFCImgButton renameButton;
        GuiFCImgButton configButton;
        /** Nullable - icon that represents the interface */
        ItemStack selfItemStack;
        /** Nullable - icon that represents the interface's "target" */
        ItemStack displayItemStack;
        LevelTerminalSection section;
        long id;
        int x, y, z, dim, side;
        int rows, rowSize;
        int guiHeight;
        int dispY = -9999;
        boolean online;
        boolean[] filteredRecipes;
        int numItems = 0;
        private int hoveredSlotIdx = -1;

        LevelTerminalEntry(long id, String name, int rows, int rowSize, boolean online) {
            this.id = id;
            this.rows = rows;
            this.rowSize = rowSize;
            this.online = online;
            if (StatCollector.canTranslate(name)) {
                customName = StatCollector.translateToLocal(name);
            } else {
                String fallback = name + ".name"; // its whatever. save some bytes on network but looks ugly
                if (StatCollector.canTranslate(fallback)) {
                    customName = StatCollector.translateToLocal(fallback);
                } else {
                    customName = StatCollector.translateToFallback(name);
                }
            }
            inv = new AppEngInternalInventory(null, rows * rowSize, 1);
            highlightButton = new GuiFCImgButton(1, 0, "HIGHLIGHT", "YES");
            renameButton = new GuiFCImgButton(1, 0, "EDIT", "YES");
            configButton = new GuiFCImgButton(1, 0, "CONFIG", "YES");
            guiHeight = 18 * rows + 1;
            filteredRecipes = new boolean[rows * rowSize];
        }

        LevelTerminalEntry setLocation(int x, int y, int z, int dim, int side) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.dim = dim;
            this.side = side;

            return this;
        }

        LevelTerminalEntry setIcons(ItemStack selfItemStack, ItemStack displayItemStack) {
            this.selfItemStack = selfItemStack;
            this.displayItemStack = displayItemStack;

            return this;
        }

        public void fullItemUpdate(NBTTagList items, int newSize) {
            inv = new AppEngInternalInventory(null, newSize);
            rows = newSize / rowSize;
            numItems = 0;

            for (int i = 0; i < inv.getSizeInventory(); ++i) {
                setItemInSlot(ItemStack.loadItemStackFromNBT(items.getCompoundTagAt(i)), i);
            }
            guiHeight = 18 * rows + 4;
        }

        LevelTerminalEntry setItems(NBTTagList items) {
            assert items.tagCount() == inv.getSizeInventory();

            for (int i = 0; i < items.tagCount(); ++i) {
                setItemInSlot(ItemStack.loadItemStackFromNBT(items.getCompoundTagAt(i)), i);
            }
            return this;
        }

        public void partialItemUpdate(NBTTagList items, int[] validIndices) {
            for (int i = 0; i < validIndices.length; ++i) {
                setItemInSlot(ItemStack.loadItemStackFromNBT(items.getCompoundTagAt(i)), validIndices[i]);
            }
        }

        private void setItemInSlot(ItemStack stack, int idx) {
            final int oldHasItem = inv.getStackInSlot(idx) != null ? 1 : 0;
            final int newHasItem = stack != null ? 1 : 0;

            inv.setInventorySlotContents(idx, stack);

            // Update item count
            numItems += newHasItem - oldHasItem;
            assert numItems >= 0;
        }

        public AppEngInternalInventory getInventory() {
            return inv;
        }

        public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
            final EntityPlayer player = Minecraft.getMinecraft().thePlayer;

            if (!section.visible || mouseButton < 0 || mouseButton > 2) {
                return false;
            }
            if (mouseX >= highlightButton.xPosition && mouseX < 2 + highlightButton.width
                    && mouseY > Math.max(highlightButton.yPosition, LevelTerminalSection.TITLE_HEIGHT)
                    && mouseY <= Math.min(highlightButton.yPosition + highlightButton.height, viewHeight)) {
                highlightButton.func_146113_a(mc.getSoundHandler());
                Util.DimensionalCoordSide blockPos = new Util.DimensionalCoordSide(
                        x,
                        y,
                        z,
                        dim,
                        ForgeDirection.getOrientation(side),
                        "");
                if (isCtrlKeyDown() && isShiftKeyDown() && hasInfinityBoosterCard(player)) {
                    FluidCraft.proxy.netHandler.sendToServer(
                            new CPacketLevelTerminalCommands(
                                    Action.EDIT,
                                    blockPos.x,
                                    blockPos.y,
                                    blockPos.z,
                                    blockPos.getDimension(),
                                    blockPos.getSide()));
                } else if (isShiftKeyDown()) {
                    FluidCraft.proxy.netHandler.sendToServer(
                            new CPacketRenamer(
                                    blockPos.x,
                                    blockPos.y,
                                    blockPos.z,
                                    blockPos.getDimension(),
                                    blockPos.getSide()));
                } else {
                    /* View in world */
                    WorldCoord blockPos2 = new WorldCoord((int) player.posX, (int) player.posY, (int) player.posZ);
                    if (mc.theWorld.provider.dimensionId != dim) {
                        player.addChatMessage(
                                new ChatComponentTranslation(PlayerMessages.InterfaceInOtherDim.getName(), dim));
                    } else {
                        BlockPosHighlighter.highlightBlock(
                                blockPos,
                                System.currentTimeMillis() + 500 * WorldCoord.getTaxicabDistance(blockPos, blockPos2));
                        player.addChatMessage(
                                new ChatComponentTranslation(
                                        PlayerMessages.InterfaceHighlighted.getName(),
                                        blockPos.x,
                                        blockPos.y,
                                        blockPos.z));
                    }
                    player.closeScreen();
                }
                return true;
            }

            int offsetY = mouseY - dispY;
            int offsetX = mouseX - (VIEW_WIDTH - rowSize * 18) - 1;
            if (offsetX >= 0 && offsetX < (rowSize * 18)
                    && mouseY > Math.max(dispY, LevelTerminalSection.TITLE_HEIGHT)
                    && offsetY < Math.min(viewHeight - dispY, guiHeight)) {
                final int col = offsetX / 18;
                final int row = offsetY / 18;
                final int slotIdx = row * rowSize + col;

                // send packet to server, request an update
                InventoryAction action = switch (mouseButton) {
                    case 0 -> {
                        // pickup / set-down.
                        yield null;
                    }
                    case 1 -> {
                        yield null;
                    }
                    case 2 -> {
                        // creative dupe:
                        if (player.capabilities.isCreativeMode) {
                            yield InventoryAction.CREATIVE_DUPLICATE;
                        } else {
                            yield InventoryAction.AUTO_CRAFT;
                        }
                    }
                    default -> {
                        // drop item:
                        yield null;
                    }
                };

                if (action != null) {
                    ItemStack itemStack = getInventory().getStackInSlot(slotIdx);
                    if (itemStack != null && itemStack.hasTagCompound()
                            && itemStack.getTagCompound().hasKey(TLMTags.Stack.tagName)) {
                        long batch = itemStack.getTagCompound().getLong(TLMTags.Batch.tagName);
                        NBTTagCompound stackData = itemStack.getTagCompound().getCompoundTag(TLMTags.Stack.tagName);
                        ItemStack is = ItemStack.loadItemStackFromNBT(stackData);
                        IAEItemStack stack = AEItemStack.create(is);

                        if (stack == null) return true;
                        stack.setStackSize(batch);
                        ((AEBaseContainer) inventorySlots).setTargetStack(stack);
                        FluidCraft.proxy.netHandler.sendToServer(new CPacketInventoryAction(action, slotIdx, 0, stack));
                    }
                }

                return true;
            }

            return false;
        }
    }
}
