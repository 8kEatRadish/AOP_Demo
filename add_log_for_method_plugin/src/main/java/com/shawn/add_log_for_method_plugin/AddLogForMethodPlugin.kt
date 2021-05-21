package com.shawn.add_log_for_method_plugin

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.FileOutputStream

class AddLogForMethodPlugin : Transform(), Plugin<Project> {

    private val CLASS_NAMES = "classNames"

    override fun apply(target: Project) {
        println("begin addLogForMethodPlugin")
        target.extensions.create(CLASS_NAMES,ClassNameExtension::class.java)
        val appExtension = target.extensions.getByType(AppExtension::class.java)
        appExtension.registerTransform(this)
        println("end addLogForMethodPlugin")
    }

    override fun getName(): String {
        return "AddLogForMethodPlugin"
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun isIncremental(): Boolean {
        return false
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun transform(transformInvocation: TransformInvocation?) {
        val inputs = transformInvocation?.inputs
        val out = transformInvocation?.outputProvider

        inputs?.forEach { transformInput ->

            //项目目录
            transformInput.directoryInputs.forEach { directoryInput ->
                if (directoryInput.file.isDirectory) {
                    FileUtils.getAllFiles(directoryInput.file).forEach {
                        val file = it
                        val name = file.name
                        if (name.endsWith(".class") && name != "R.class" && !name.startsWith("R\$") && name != "BuildConfig.class") {
                            val classPath = file.absolutePath
                            val cr = ClassReader(file.readBytes())
                            val cw = ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
                            val visitor = AddLogClassVisitor(cw)
                            cr.accept(visitor, ClassReader.EXPAND_FRAMES)

                            val byte = cw.toByteArray();
                            val fos = FileOutputStream(classPath)
                            fos.write(byte)
                            fos.close()
                        }
                    }
                }

                val dest = out?.getContentLocation(
                    directoryInput.name,
                    directoryInput.contentTypes,
                    directoryInput.scopes,
                    Format.DIRECTORY
                )

                FileUtils.copyDirectoryToDirectory(directoryInput.file, dest)

            }


            //jar包
            transformInput.jarInputs.forEach {
                val dest = out?.getContentLocation(
                    it.name,
                    it.contentTypes,
                    it.scopes,
                    Format.JAR
                )

                FileUtils.copyFile(it.file, dest)
            }

        }

    }
}