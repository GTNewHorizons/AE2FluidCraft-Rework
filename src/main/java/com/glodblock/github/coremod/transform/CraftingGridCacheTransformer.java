package com.glodblock.github.coremod.transform;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.glodblock.github.coremod.FCClassTransformer;

public class CraftingGridCacheTransformer extends FCClassTransformer.ClassMapper {

    public static final CraftingGridCacheTransformer INSTANCE = new CraftingGridCacheTransformer();

    private CraftingGridCacheTransformer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new CraftingGridCacheTransformer.TransformCraftingGridCache(Opcodes.ASM5, downstream);
    }

    private static class TransformCraftingGridCache extends ClassVisitor {

        TransformCraftingGridCache(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("getCellArray")) {
                return new CraftingGridCacheTransformer.ReplaceGetCellArray(
                        api,
                        super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }

    private static class ReplaceGetCellArray extends MethodVisitor {

        private final MethodVisitor target;

        ReplaceGetCellArray(int api, MethodVisitor mv) {
            // Original method is replaced
            super(api, null);
            target = mv;
        }

        @Override
        public void visitCode() {
            target.visitCode();
            // Equivalent to
            // return CoreModHooks::craftingGridCacheGetCellArray(this, channel);
            target.visitVarInsn(Opcodes.ALOAD, 0);
            target.visitVarInsn(Opcodes.ALOAD, 1);
            target.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "com/glodblock/github/coremod/hooker/CoreModHooks",
                    "craftingGridCacheGetCellArray",
                    "(Lappeng/me/cache/CraftingGridCache;Lappeng/api/storage/StorageChannel;)Ljava/util/List;",
                    false);
            target.visitInsn(Opcodes.ARETURN);
            target.visitMaxs(2, 3);
            target.visitEnd();
        }
    }
}
