package com.glodblock.github.coremod;

import net.minecraft.launchwrapper.IClassTransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import com.glodblock.github.coremod.transform.CraftingCpuTransformer;
import com.glodblock.github.coremod.transform.CraftingGridCacheTransformer;
import com.glodblock.github.coremod.transform.CraftingTreeNodeTransformer;
import com.glodblock.github.coremod.transform.DualityInterfaceTransformer;
import com.glodblock.github.coremod.transform.ExternalStorageRegistryTransformer;
import com.glodblock.github.coremod.transform.GuiCraftingTransformer;
import com.glodblock.github.coremod.transform.NEITransformer;
import com.gtnewhorizon.gtnhlib.asm.ASMUtil;

public class FCClassTransformer implements IClassTransformer {

    private static final Logger logger = LogManager.getLogger("ASM AE2FC");

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        ClassMapper tform;
        switch (transformedName) {
            case "appeng.crafting.CraftingTreeNode" -> tform = CraftingTreeNodeTransformer.INSTANCE;
            case "appeng.me.cache.CraftingGridCache" -> tform = CraftingGridCacheTransformer.INSTANCE;
            case "appeng.me.cluster.implementations.CraftingCPUCluster" -> tform = CraftingCpuTransformer.INSTANCE;
            case "appeng.helpers.DualityInterface" -> tform = DualityInterfaceTransformer.INSTANCE;
            case "appeng.client.gui.implementations.GuiCraftingCPU", "appeng.client.gui.implementations.GuiCraftConfirm", "net.p455w0rd.wirelesscraftingterminal.client.gui.GuiCraftConfirm", "appeng.client.gui.widgets.GuiCraftingTree" -> tform = GuiCraftingTransformer.INSTANCE;
            case "appeng.integration.modules.NEI" -> tform = NEITransformer.INSTANCE;
            case "appeng.core.features.registries.ExternalStorageRegistry" -> tform = ExternalStorageRegistryTransformer.INSTANCE;
            default -> {
                return basicClass;
            }
        }
        logger.debug("Transforming class: " + transformedName);
        final byte[] bytes = tform.transformClass(basicClass);
        if (FluidCraftCore.DUMP_CLASSES()) {
            ASMUtil.saveAsRawClassFile(basicClass, transformedName + "_PRE", this);
            ASMUtil.saveAsRawClassFile(bytes, transformedName + "_POST", this);
        }
        return bytes;
    }

    public abstract static class ClassMapper {

        public final byte[] transformClass(byte[] code) {
            ClassReader reader = new ClassReader(code);
            ClassWriter writer = new ClassWriter(reader, getWriteFlags());
            reader.accept(getClassMapper(writer), ClassReader.EXPAND_FRAMES);
            return writer.toByteArray();
        }

        protected int getWriteFlags() {
            return 0;
        }

        protected abstract ClassVisitor getClassMapper(ClassVisitor downstream);
    }
}
