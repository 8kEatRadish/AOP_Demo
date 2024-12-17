package com.shawn.add_log_for_method_plugin

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes


class AddLogMethodVisitor(methodVisitor: MethodVisitor) :
    MethodVisitor(Opcodes.ASM7, methodVisitor) {
    override fun visitCode() {
        super.visitCode()
        //方法执行前插入
        mv.visitLdcInsn("AddLogMethodPlugin")
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
        mv.visitInsn(Opcodes.DUP)
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
        mv.visitLdcInsn("into ")
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/Exception")
        mv.visitInsn(Opcodes.DUP)
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Exception", "<init>", "()V", false)
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Exception", "getStackTrace", "()[Ljava/lang/StackTraceElement;", false)
        mv.visitInsn(Opcodes.ICONST_0)
        mv.visitInsn(Opcodes.AALOAD)
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StackTraceElement", "getMethodName", "()Ljava/lang/String;", false)
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/shawn/krouter/uitl/UtilsKt", "MyLogD", "(Ljava/lang/String;Ljava/lang/String;)V", false)
    }


    override fun visitInsn(opcode: Int) {
        //方法后插入

        super.visitInsn(opcode)
    }
}