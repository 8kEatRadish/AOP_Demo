package com.shawn.add_log_for_method_plugin

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes


class AddLogClassVisitor(classVisitor: ClassVisitor) : ClassVisitor(Opcodes.ASM7, classVisitor) {
    private var className: String? = null
    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        className = name
        println("className = $className")
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        println("method = $name")

        if (className.equals("com/shawn/krouter/MainActivity") && name != "<init>") {
            return if (name == "onCreate") {
                AddLogInitMethodVisitor(methodVisitor)
            } else {
                AddLogMethodVisitor(methodVisitor)
            }
        }

        return methodVisitor
    }
}