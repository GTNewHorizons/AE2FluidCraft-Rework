package com.glodblock.github.crossmod.opencomputers;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.common.tile.TileOCPatternEditor;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.NameConst;

import appeng.api.AEApi;
import li.cil.oc.api.driver.EnvironmentProvider;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import li.cil.oc.integration.ManagedTileEntityEnvironment;
import li.cil.oc.integration.appeng.internal.PatternEnvironment;
import li.cil.oc.integration.appeng.internal.PatternEnvironment$class;

public class DriverOCPatternEditor extends DriverSidedTileEntity {

    public DriverOCPatternEditor() {}

    @Override
    public Class<?> getTileEntityClass() {
        return TileOCPatternEditor.class;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, int x, int y, int z, ForgeDirection forgeDirection) {
        return new DriverOCPatternEditor.Environment((TileOCPatternEditor) world.getTileEntity(x, y, z));
    }

    public static class Environment extends ManagedTileEntityEnvironment<TileOCPatternEditor>
            implements NamedBlock, PatternEnvironment {

        public Environment(TileOCPatternEditor tileEntity) {
            super(tileEntity, NameConst.BLOCK_OC_PATTERN_EDITOR);
        }

        @Override
        public IInventory getPatternInventory(Context context, Arguments args) {
            return tileEntity.getInternalInventory();
        }

        @Override
        public int offset() {
            return PatternEnvironment$class.offset(this);
        }

        @Override
        public Object[] setPatternSlot(Context context, Arguments args, String tag) {
            return PatternEnvironment$class.setPatternSlot(this, context, args, tag);
        }

        @Override
        public Object[] storeInterfacePattern(Context context, Arguments args, String tag) {
            return PatternEnvironment$class.storeInterfacePattern(this, context, args, tag);
        }

        private int optSlot(Arguments args, IInventory inv, int index, int def) {
            if (index >= 0 && index < args.count()) {
                int slot = args.checkInteger(index) - 1;
                if (slot < 0 || slot >= inv.getSizeInventory()) {
                    throw new IllegalArgumentException("invalid slot");
                }
                return slot;
            } else {
                return def;
            }
        }

        private int checkSlot(Arguments args, IInventory inv, int index) {
            int slot = args.checkInteger(index) - 1;
            if (slot < 0 || slot >= inv.getSizeInventory()) {
                throw new IllegalArgumentException("invalid slot");
            }
            return slot;
        }

        private void replaceBlankPattern(Arguments args) {
            IInventory inv = tileEntity.getInternalInventory();
            int slot = checkSlot(args, inv, 0);
            if (AEApi.instance().definitions().materials().blankPattern().isSameAs(inv.getStackInSlot(slot))) {
                ItemStack pattern = AEApi.instance().definitions().items().encodedUltimatePattern().maybeStack(1).get();
                pattern.setTagCompound(new NBTTagCompound());
                inv.setInventorySlotContents(slot, pattern);
            }
        }

        @Callback(doc = "function([slot:number]):table -- Get the given pattern in the interface.")
        public Object[] getInterfacePattern(Context context, Arguments args) {
            IInventory inv = tileEntity.getInternalInventory();
            int slot = optSlot(args, inv, 0, 0);
            ItemStack stack = inv.getStackInSlot(slot);
            return new Object[] { stack };
        }

        @Callback(
                doc = "function(slot:number, index:number[, database:address, entry:number, size:number]):boolean OR function(slot:number, index:number[, detail:table, type:string]):boolean -- Set the pattern input at the given index. A blank pattern becomes an ultimate pattern.")
        public Object[] setInterfacePatternInput(Context context, Arguments args) {
            replaceBlankPattern(args);
            return setPatternSlot(context, args, "in");
        }

        @Callback(
                doc = "function(slot:number, index:number[, database:address, entry:number, size:number]):boolean OR function(slot:number, index:number[, detail:table, type:string]):boolean -- Set the pattern output at the given index. A blank pattern becomes an ultimate pattern.")
        public Object[] setInterfacePatternOutput(Context context, Arguments args) {
            replaceBlankPattern(args);
            return setPatternSlot(context, args, "out");
        }

        @Callback(
                doc = "function(slot:number, index:number, database:address, entry:number):boolean -- Store pattern input at the given index to the database entry.")
        public Object[] storeInterfacePatternInput(Context context, Arguments args) {
            return storeInterfacePattern(context, args, "in");
        }

        @Callback(
                doc = "function(slot:number, index:number, database:address, entry:number):boolean -- Store pattern output at the given index to the database entry.")
        public Object[] storeInterfacePatternOutput(Context context, Arguments args) {
            return storeInterfacePattern(context, args, "out");
        }

        @Callback(doc = "function(slot:number, index:number):boolean -- Clear pattern input at the given index.")
        public Object[] clearInterfacePatternInput(Context context, Arguments args) {
            return setPatternSlot(context, args, "in");
        }

        @Callback(doc = "function(slot:number, index:number):boolean -- Clear pattern output at the given index.")
        public Object[] clearInterfacePatternOutput(Context context, Arguments args) {
            return setPatternSlot(context, args, "out");
        }

        @Override
        public String preferredName() {
            return NameConst.BLOCK_OC_PATTERN_EDITOR;
        }

        @Override
        public int priority() {
            return 5;
        }
    }

    public static class Provider implements EnvironmentProvider {

        Provider() {}

        @Override
        public Class<?> getEnvironment(ItemStack itemStack) {
            if (itemStack != null && itemStack.isItemEqual(ItemAndBlockHolder.OC_EDITOR.stack())) {
                return DriverOCPatternEditor.Environment.class;
            }
            return null;
        }
    }
}
