package com.shawn.add_log_for_method_plugin

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class AddLogMethodVisitor (methodVisitor : MethodVisitor) : MethodVisitor(Opcodes.ASM7,methodVisitor) {
    override fun visitCode() {
        super.visitCode()
        //方法前插入
    }

    override fun visitInsn(opcode: Int) {
        //方法后插入

        super.visitInsn(opcode)
    }
}