package com.glodblock.github.nei;

import static net.minecraft.util.EnumChatFormatting.GRAY;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.storage.FluidCellInventoryHandler;
import com.glodblock.github.util.ModAndClassUtil;
import com.mitchej123.hodgepodge.textures.IPatchedTextureAtlasSprite;

import appeng.api.AEApi;
import appeng.api.config.TerminalFontSize;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.client.render.StackSizeRenderer;
import appeng.core.localization.GuiText;
import appeng.util.Platform;
import appeng.util.item.FluidList;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IRecipeOverlayRenderer;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.IUsageHandler;

public class NEICellViewHandler implements IUsageHandler {

    private static class ViewItemStack {

        public PositionedStack stack;
        public long amount;
        public IIcon icon;
        public int color;

        public ViewItemStack(PositionedStack stack, long amount, IIcon icon, int color) {
            this.stack = stack;
            this.amount = amount;
            this.icon = icon;
            this.color = color;
        }
    }

    private static final ResourceLocation SLOT_TEXTURE_LOCATION = new ResourceLocation("nei", "textures/slot.png");
    private static final int OFFSET_X = 2;
    private static final int INFO_OFFSET_Y = 4;
    private static final int ITEMS_OFFSET_Y = INFO_OFFSET_Y + Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT * 2 + 6;
    private static final int ROW_ITEM_NUM = 9;

    private final List<ViewItemStack> stacks = new ArrayList<>();
    private FluidCellInventoryHandler cellHandler;

    @Override
    public IUsageHandler getUsageHandler(String inputId, Object... ingredients) {
        if (ingredients.length > 0 && ingredients[0] instanceof ItemStack ingredient && AEApi.instance().registries().cell()
            .getCellInventory(ingredient, null, StorageChannel.FLUIDS) instanceof final FluidCellInventoryHandler handler && handler.getTotalBytes() > 0) {
            this.cellHandler = handler;

            FluidList list = new FluidList();
            handler.getAvailableItems(list, appeng.util.IterationCounter.fetchNewId());

            List<IAEFluidStack> sortedStacks = new ArrayList<>();
            list.iterator().forEachRemaining(sortedStacks::add);
            sortedStacks.sort(Comparator.comparing(IAEStack::getStackSize, Comparator.reverseOrder()));


            stacks.clear();
            int count = 0;
            for (IAEFluidStack aeFluid : sortedStacks) {
                FluidStack fluid = aeFluid.getFluidStack();
                ItemStack fluidItemStack = ItemFluidDrop.newStack(fluid);
                if (fluidItemStack != null) {
                    fluidItemStack.stackSize = 1;
                    PositionedStack positionedStack = new PositionedStack(
                        fluidItemStack,
                        OFFSET_X + count % ROW_ITEM_NUM * 18 + 1,
                        ITEMS_OFFSET_Y + count / ROW_ITEM_NUM * 18 + 1);
                    stacks.add(new ViewItemStack(positionedStack, aeFluid.getStackSize(), fluid.getFluid().getIcon(), fluid.getFluid().getColor()));
                count++;
                }
            }

            return this;
        }
        return null;
    }

    @Override
    public String getRecipeName() {
        return GuiText.CellView.getLocal();
    }

    @Override
    public int numRecipes() {
        return 1;
    }

    @Override
    public void drawBackground(int recipe) {}

    @Override
    public void drawForeground(int recipe) {
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        final String usedBytes = Platform.formatByteDouble(cellHandler.getUsedBytes());
        final String totalBytes = Platform.formatByteDouble(cellHandler.getTotalBytes());
        fontRenderer.drawString(
                usedBytes + " " + GuiText.Of.getLocal() + ' ' + totalBytes + ' ' + GuiText.BytesUsed.getLocal(),
                OFFSET_X,
                INFO_OFFSET_Y,
                0);
        fontRenderer.drawString(
                NumberFormat.getInstance().format(cellHandler.getUsedTypes()) + " "
                        + GuiText.Of.getLocal()
                        + ' '
                        + NumberFormat.getInstance().format(cellHandler.getTotalTypes())
                        + ' '
                        + GuiText.Types.getLocal(),
                OFFSET_X,
                INFO_OFFSET_Y + fontRenderer.FONT_HEIGHT + 2,
                0);

        final Tessellator tessellator = Tessellator.instance;
        GL11.glColor3f(1F, 1F, 1F);

        // Draw slots
        Minecraft.getMinecraft().getTextureManager().bindTexture(SLOT_TEXTURE_LOCATION);
        tessellator.startDrawingQuads();
        for (int i = 0; i < this.cellHandler.getTotalTypes(); i++) {
            final int line = i % ROW_ITEM_NUM;
            final int row = i / ROW_ITEM_NUM;
            tessellator.addVertexWithUV(OFFSET_X + 18 * (line + 1), ITEMS_OFFSET_Y + 18 * row, 0, 1, 0);
            tessellator.addVertexWithUV(OFFSET_X + 18 * line, ITEMS_OFFSET_Y + 18 * row, 0, 0, 0);
            tessellator.addVertexWithUV(OFFSET_X + 18 * line, ITEMS_OFFSET_Y + 18 * (row + 1), 0, 0, 1);
            tessellator.addVertexWithUV(OFFSET_X + 18 * (line + 1), ITEMS_OFFSET_Y + 18 * (row + 1), 0, 1, 1);
        }
        tessellator.draw();

        // Draw fluids
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        tessellator.startDrawingQuads();
        for (int i = 0; i < this.stacks.size(); i++) {
            final IIcon icon = this.stacks.get(i).icon;
            final int color = this.stacks.get(i).color;
            final int line = i % ROW_ITEM_NUM;
            final int row = i / ROW_ITEM_NUM;

            if (ModAndClassUtil.HODGEPODGE && icon instanceof IPatchedTextureAtlasSprite sprite) {
                sprite.markNeedsAnimationUpdate();
            }

            tessellator.setColorRGBA(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, 0xFF);
            tessellator.addVertexWithUV(
                    OFFSET_X + 18 * (line + 1) - 1,
                    ITEMS_OFFSET_Y + 18 * row + 1,
                    0,
                    icon.getMaxU(),
                    icon.getMinV());
            tessellator.addVertexWithUV(
                    OFFSET_X + 18 * line + 1,
                    ITEMS_OFFSET_Y + 18 * row + 1,
                    0,
                    icon.getMinU(),
                    icon.getMinV());
            tessellator.addVertexWithUV(
                    OFFSET_X + 18 * line + 1,
                    ITEMS_OFFSET_Y + 18 * (row + 1) - 1,
                    0,
                    icon.getMinU(),
                    icon.getMaxV());
            tessellator.addVertexWithUV(
                    OFFSET_X + 18 * (line + 1) - 1,
                    ITEMS_OFFSET_Y + 18 * (row + 1) - 1,
                    0,
                    icon.getMaxU(),
                    icon.getMaxV());
        }
        tessellator.draw();
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);

        // Draw amount
        for (ViewItemStack viewStack : this.stacks) {
            StackSizeRenderer.drawStackSize(
                    viewStack.stack.relx,
                    viewStack.stack.rely,
                    viewStack.amount,
                    fontRenderer,
                    TerminalFontSize.SMALL);
        }
    }

    @Override
    public List<PositionedStack> getIngredientStacks(int recipe) {
        return new ArrayList<>();
    }

    @Override
    public List<PositionedStack> getOtherStacks(int recipe) {
        return this.stacks.stream().map(stack -> stack.stack).collect(Collectors.toList());
    }

    @Override
    public PositionedStack getResultStack(int recipe) {
        return null;
    }

    @Override
    public void onUpdate() {}

    @Override
    public boolean hasOverlay(GuiContainer gui, Container container, int recipe) {
        return false;
    }

    @Override
    public IRecipeOverlayRenderer getOverlayRenderer(GuiContainer gui, int recipe) {
        return null;
    }

    @Override
    public IOverlayHandler getOverlayHandler(GuiContainer gui, int recipe) {
        return null;
    }

    @Override
    public int recipiesPerPage() {
        return 1;
    }

    @Override
    public List<String> handleTooltip(GuiRecipe<?> gui, List<String> currenttip, int recipe) {
        return currenttip;
    }

    @Override
    public List<String> handleItemTooltip(GuiRecipe<?> gui, ItemStack stack, List<String> currenttip, int recipe) {
        if (stack == null) return currenttip;

        this.stacks.stream().filter(viewStack -> viewStack.stack.item.equals(stack)).findFirst().ifPresent(
                viewItemStack -> currenttip.set(
                        1,
                        GRAY + GuiText.Stored.getLocal()
                                + ": "
                                + NumberFormat.getNumberInstance().format(viewItemStack.amount)));
        return currenttip;
    }

    @Override
    public boolean keyTyped(GuiRecipe<?> gui, char keyChar, int keyCode, int recipe) {
        return false;
    }

    @Override
    public boolean mouseClicked(GuiRecipe<?> gui, int button, int recipe) {
        return false;
    }
}
