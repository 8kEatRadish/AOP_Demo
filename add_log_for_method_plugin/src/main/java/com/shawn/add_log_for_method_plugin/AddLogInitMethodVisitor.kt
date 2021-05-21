package com.shawn.add_log_for_method_plugin

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes


class AddLogInitMethodVisitor(methodVisitor: MethodVisitor) :
    MethodVisitor(Opcodes.ASM7, methodVisitor) {
    override fun visitCode() {
        super.visitCode()
        //方法执行前插入
        mv.visitLdcInsn("AddLogMethodPlugin")
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
        mv.visitInsn(Opcodes.DUP)
        mv.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "java/lang/StringBuilder",
            "<init>",
            "()V",
            false
        )
        mv.visitLdcInsn("into ")
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "append",
            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
            false
        )
        mv.visitVarInsn(Opcodes.ALOAD, 0)
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/Object",
            "getClass",
            "()Ljava/lang/Class;",
            false
        )
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/Class",
            "getSimpleName",
            "()Ljava/lang/String;",
            false
        )
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "append",
            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
            false
        )
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "toString",
            "()Ljava/lang/String;",
            false
        )
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "com/shawn/krouter/uitl/UtilsKt",
            "MyLogD",
            "(Ljava/lang/String;Ljava/lang/String;)V",
            false
        )
    }


    override fun visitInsn(opcode: Int) {
        //方法后插入

        super.visitInsn(opcode)
    }
}