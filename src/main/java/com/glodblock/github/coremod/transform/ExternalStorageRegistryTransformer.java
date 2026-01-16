package com.glodblock.github.coremod.transform;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.glodblock.github.coremod.FCClassTransformer;

public class ExternalStorageRegistryTransformer extends FCClassTransformer.ClassMapper {

    public static final ExternalStorageRegistryTransformer INSTANCE = new ExternalStorageRegistryTransformer();

    private ExternalStorageRegistryTransformer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TransformExternalStorageRegistry(Opcodes.ASM5, downstream);
    }

    @Override
    protected int getWriteFlags() {
        return ClassWriter.COMPUTE_FRAMES;
    }

    private static class TransformExternalStorageRegistry extends ClassVisitor {

        public TransformExternalStorageRegistry(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if ("getHandler".equals(name)) {
                return new TransformGetHandler(api, super.visitMethod(access, name, desc, signature, exceptions));
            } else {
                return super.visitMethod(access, name, desc, signature, exceptions);
            }
        }
    }

    private static class TransformGetHandler extends MethodVisitor {

        private int cnt_return = 0;
        private int cnt_if = 0;
        private Label L6 = null;
        private final Label L9 = new Label();

        TransformGetHandler(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            if (opcode == Opcodes.IFEQ) {
                cnt_if++;
            }
            if (cnt_if == 3) {
                L6 = label;
                cnt_if++;
                super.visitJumpInsn(opcode, L9);
                return;
            }
            super.visitJumpInsn(opcode, label);
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == Opcodes.ARETURN) {
                cnt_return++;
            }
            super.visitInsn(opcode);
            if (cnt_return == 2) {
                super.visitLabel(L9);
                super.visitFieldInsn(
                        Opcodes.GETSTATIC,
                        "com/glodblock/github/inventory/external/AEFluidTankHandler",
                        "INSTANCE",
                        "Lcom/glodblock/github/inventory/external/AEFluidTankHandler;");
                super.visitVarInsn(Opcodes.ALOAD, 1);
                super.visitVarInsn(Opcodes.ALOAD, 2);
                super.visitVarInsn(Opcodes.ALOAD, 3);
                super.visitVarInsn(Opcodes.ALOAD, 4);
                super.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "com/glodblock/github/inventory/external/AEFluidTankHandler",
                        "canHandle",
                        "(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraftforge/common/util/ForgeDirection;Lappeng/api/storage/StorageChannel;Lappeng/api/networking/security/BaseActionSource;)Z",
                        false);
                super.visitJumpInsn(Opcodes.IFEQ, L6);
                super.visitFieldInsn(
                        Opcodes.GETSTATIC,
                        "com/glodblock/github/inventory/external/AEFluidTankHandler",
                        "INSTANCE",
                        "Lcom/glodblock/github/inventory/external/AEFluidTankHandler;");
                super.visitInsn(Opcodes.ARETURN);
                cnt_return++;
            }
        }
    }
}
