package com.glodblock.github.coremod.transform;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.glodblock.github.coremod.FCClassTransformer;

public class NEITransfermer extends FCClassTransformer.ClassMapper {

    public static final NEITransfermer INSTANCE = new NEITransfermer();

    private NEITransfermer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TransformNEI(Opcodes.ASM5, downstream);
    }

    private static class TransformNEI extends ClassVisitor {

        TransformNEI(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return switch (name) {
                case "getStackUnderMouse" -> new TransformStackUnderMouse(
                        api,
                        super.visitMethod(access, name, desc, signature, exceptions));
                case "shouldShowTooltip" -> new TransformShowTooltip(
                        api,
                        super.visitMethod(access, name, desc, signature, exceptions));
                default -> super.visitMethod(access, name, desc, signature, exceptions);
            };
        }
    }

    private static class TransformStackUnderMouse extends MethodVisitor {

        TransformStackUnderMouse(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == Opcodes.ACONST_NULL) {
                super.visitVarInsn(Opcodes.ALOAD, 1);
                super.visitVarInsn(Opcodes.ILOAD, 2);
                super.visitVarInsn(Opcodes.ILOAD, 3);
                super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "com/glodblock/github/coremod/hooker/CoreModHooks",
                        "getStackUnderMouse",
                        "(Lnet/minecraft/client/gui/inventory/GuiContainer;II)Lnet/minecraft/item/ItemStack;",
                        false);
                return;
            }
            super.visitInsn(opcode);
        }
    }

    private static class TransformShowTooltip extends MethodVisitor {

        int const_1 = 0;

        TransformShowTooltip(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == Opcodes.ICONST_1 && const_1 == 2) {
                super.visitVarInsn(Opcodes.ALOAD, 1);
                super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "com/glodblock/github/coremod/hooker/CoreModHooks",
                        "shouldShowTooltip",
                        "(Lnet/minecraft/client/gui/inventory/GuiContainer;)Z",
                        false);
                const_1++;
                return;
            }
            if (opcode == Opcodes.ICONST_1) {
                const_1++;
            }
            super.visitInsn(opcode);
        }
    }
}
