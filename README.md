# 本文简介
使用GradlePlugin、Transform和ASM实现字节码插桩，GradlePlugin相关代码全部由Kotlin编写，所以不熟悉groovy的小伙伴也可以无障碍阅读。
# 什么是字节码插桩
> 要了解字节码插桩，首先要了解AOP(Aspect Oriented Programming)思想，对比来说，OOP(面向对象编程)主要针对的是业务处理过程的实体极其属性和行为进行抽象封装，为的是有更清晰高效的**逻辑单元**划分。AOP(面向切面编程)则是针对业务处理过程中的切面进行提取，它所面对的是处理过程中的某个步骤或阶段，以获得逻辑过程中各部分之间低耦合的隔离效果。

## 字节码插入位置

Android开发者应该会熟悉这个图片(来自于[Android Developer](https://developer.android.com/studio/build#detailed-build))：

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/a138ce182dc141daab15f597fe6963bb~tplv-k3u1fbpfcp-watermark.image)


在Compilers中具体为：

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/688c536dd01c4f4da6d00d4823ca13e7~tplv-k3u1fbpfcp-watermark.image)


字节码插桩就是在.class文件转为.dex之前，修改.class文件从而达到修改代码的目的。

## 业务场景

字节码插桩可以用于日志记录、性能统计、异常处理、修改三方库代码等。

## 需要掌握的知识

在Android项目中使用字节码插桩，你要掌握
- **GradlePlugin相关知识**
- **了解Transform API**
- **了解字ASM，Javassist或者其他相关插桩框架**
# 创建你的GradlePlugin

> GradlePlugin相关知识也可以直接参考[官网文档](https://docs.gradle.org/current/userguide/custom_plugins.html)

## 创建的三种方式

- **直接在相应model的build.gradle里面写相应的plugin代码(方式直接，但是作用范围只有单个model)**

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/a4bc1dfaacab4f2b82f954239c24b110~tplv-k3u1fbpfcp-watermark.image)

- **在buildSrc里面写plugin代码(作用范围是整个工程)**

![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/06da3c86d6cb407fb3496c9d55736e94~tplv-k3u1fbpfcp-watermark.image)


![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/0afc4d5732ff4fdda7dd125337565c28~tplv-k3u1fbpfcp-watermark.image)

- **创建plugin模块，写资源文件xxx.properties文件确定plugin名字以及文件对应地址(相比前两个实现方式较为复杂，但是可以上传到公共仓库多项目复用)**


![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/994f4abae41d4001b7b77d2d47af4788~tplv-k3u1fbpfcp-watermark.image)

在properties文件中标出模块文件位置：

**properties文件**
![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/57942baa67284290ad233613ef5f2a22~tplv-k3u1fbpfcp-watermark.image)

引入模块的名字：properties文件的文件名(例子中为com.shawn.addlogformethodplugin)

**build.gradle**
![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/3f7da74da49940cf830e9ae3ac5f547d~tplv-k3u1fbpfcp-watermark.image)

引入maven库方便本地调试。

## 使用plugin

- **在第一种方式中plugin为默认包名，可以直接引用**

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ec185fb87a6b473e809d439f3af4dc9c~tplv-k3u1fbpfcp-watermark.image)

- **第二种方式中根据自己定义的包名引入**

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/49003720013c4fa6b7088480efb701af~tplv-k3u1fbpfcp-watermark.image)

- **第三种方式首先在项目根目录下引入仓库，再引用**

**根目录下build.gradle**
![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/c4407c88afb741e4ace4e5c84e9dff30~tplv-k3u1fbpfcp-watermark.image)

**model里面build.gradle**
![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/900a02aa16a1400c8b9267866c596cae~tplv-k3u1fbpfcp-watermark.image)

# 了解Transform

## 什么是Transform
从1.5.0-beta1开始，Gradle插件包含一个Transform API，允许第三方插件在将已编译的类文件转换为dex文件之前对其进行操作。
（[该API已存在于1.4.0-beta2中，但已在1.5.0-beta1中进行了彻底的改进](http://tools.android.com/tech-docs/new-build-system/transform-api)）

## Transform的工作原理
类似于OkHttp的拦截器，采用责任链模式。每一个Transform都是一个Task，编译中间产物在多个Transform链上传递。用户自定一的Transform会在链头第一个执行，由于每一个Transform的处理源都是由前一个Transform提供的，所以如果用户自定义的Transform没有把相应的文件搬运到指定输出位置，那么下一个Transform就无法正常工作。

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/42fe550e4746421c8002b3768675cb37~tplv-k3u1fbpfcp-watermark.image)

## Transform相关方法

### getName()

```java
    /**
     * Returns the unique name of the transform.
     *
     * <p>This is associated with the type of work that the transform does. It does not have to be
     * unique per variant.
     */
    @NonNull
    public abstract String getName();
```

表示当前Transform名称，这个名称会被用来创建目录，它会出现在app/build/intermediates/transforms目录下。

### getInputTypes()

```java
    /**
     * Returns the type(s) of data that is consumed by the Transform. This may be more than
     * one type.
     *
     * <strong>This must be of type {@link QualifiedContent.DefaultContentType}</strong>
     */
    @NonNull
    public abstract Set<ContentType> getInputTypes();
```

需要处理的数据类型，用于确定我们需要对哪些类型的结果进行修改，比如class，资源文件等。


![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e580c678d139417aaa80ce38afbfb32b~tplv-k3u1fbpfcp-watermark.image)

### getScopes()

```java
    /**
     * Returns the scope(s) of the Transform. This indicates which scopes the transform consumes.
     */
    @NonNull
    public abstract Set<? super Scope> getScopes();
```

表示Transform要操作的内容范围

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/b42224f041c94207978a6d84166ab3b8~tplv-k3u1fbpfcp-watermark.image)

这些Scope可以自由组合使用(比如SCOPE_FULL_PROJECT包含了Scope.PROJECT,Scope.SUB_PROJECTS,Scope.EXTERNAL_LIBRARIES)

### isIncremental()

```java
    /**
     * Returns whether the Transform can perform incremental work.
     *
     * <p>If it does, then the TransformInput may contain a list of changed/removed/added files, unless
     * something else triggers a non incremental run.
     */
    public abstract boolean isIncremental();
```

是否支持增量更新：如果返回true，则TransformInput会包含一份修改的文件列表，如果返回false，则进行全量编译，删除上一次输出的内容。


相关状态说明：

- **NOTCHANGED：** 不需要做任何处理。


- **REMOVED：** 需要移除文件。


- **ADDED、CHANGED：** 需要正常修改文件。

### transform()
```java
    /**
     * Executes the Transform.
     *
     * <p>The inputs are packaged as an instance of {@link TransformInvocation}
     * <ul>
     *     <li>The <var>inputs</var> collection of {@link TransformInput}. These are the inputs
     *     that are consumed by this Transform. A transformed version of these inputs must
     *     be written into the output. What is received is controlled through
     *     {@link #getInputTypes()}, and {@link #getScopes()}.</li>
     *     <li>The <var>referencedInputs</var> collection of {@link TransformInput}. This is
     *     for reference only and should be not be transformed. What is received is controlled
     *     through {@link #getReferencedScopes()}.</li>
     * </ul>
     *
     * A transform that does not want to consume anything but instead just wants to see the content
     * of some inputs should return an empty set in {@link #getScopes()}, and what it wants to
     * see in {@link #getReferencedScopes()}.
     *
     * <p>Even though a transform's {@link Transform#isIncremental()} returns true, this method may
     * be receive <code>false</code> in <var>isIncremental</var>. This can be due to
     * <ul>
     *     <li>a change in secondary files ({@link #getSecondaryFiles()},
     *     {@link #getSecondaryFileOutputs()}, {@link #getSecondaryDirectoryOutputs()})</li>
     *     <li>a change to a non file input ({@link #getParameterInputs()})</li>
     *     <li>an unexpected change to the output files/directories. This should not happen unless
     *     tasks are improperly configured and clobber each other's output.</li>
     *     <li>a file deletion that the transform mechanism could not match to a previous input.
     *     This should not happen in most case, except in some cases where dependencies have
     *     changed.</li>
     * </ul>
     * In such an event, when <var>isIncremental</var> is false, the inputs will not have any
     * incremental change information:
     * <ul>
     *     <li>{@link JarInput#getStatus()} will return {@link Status#NOTCHANGED} even though
     *     the file may be added/changed.</li>
     *     <li>{@link DirectoryInput#getChangedFiles()} will return an empty map even though
     *     some files may be added/changed.</li>
     * </ul>
     *
     * @param transformInvocation the invocation object containing the transform inputs.
     * @throws IOException if an IO error occurs.
     * @throws InterruptedException
     * @throws TransformException Generic exception encapsulating the cause.
     */
    public void transform(@NonNull TransformInvocation transformInvocation)
            throws TransformException, InterruptedException, IOException {
        // Just delegate to old method, for code that uses the old API.
        //noinspection deprecation
        transform(transformInvocation.getContext(), transformInvocation.getInputs(),
                transformInvocation.getReferencedInputs(),
                transformInvocation.getOutputProvider(),
                transformInvocation.isIncremental());
    }

```

在这里可以获取到getInputs()，如果消费了getInputs()的话，则transform后必须输出给下一级，不然下一级读取不到编译中间件。是否增量编译要以isIncremental为准。

```kotlin
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
```

# ASM实践方案
[字节码相关知识](https://juejin.cn/post/6844903588716609543)请自行查看，本文不做过多讲解。

## ASM核心API

> ASM Core API可以类比解析XML文件中的SAX方式，不需要把这个类的整个结构读取进来，就可以用流式的方法来处理字节码文件。好处是非常节约内存，但是编程难度较大。然而出于性能考虑，一般情况下编程都使用Core API。在Core API中有以下几个关键类：
> - ClassReader：用于读取已经编译好的.class文件。
> - ClassWriter：用于重新构建编译后的类，生成新的类的字节码文件。
> - 各种Visitor类：CoreAPI根据字节码从上到下依次处理，对于字节码文件中不同的区域有不同的Visitor，比如用于访问方法的MethodVisitor、用于访问类变量的FieldVisitor、用于访问注解的AnnotationVisitor等。为了实现AOP，重点要使用的是MethodVisitor。
> 

## 情景目标

**要给MainActivity.kt中所有的方法添加一个自定义方法的调用：**

- **首先在transform()方法中读取想要修改的class文件。创建ClassVisitor类并且传入ClassWriter用于字节码定位修改操作，把ClassVisitor传入ClassReader用于重新构建新的字节码文件。**

```kotlin
    val classPath = file.absolutePath
    val cr = ClassReader(file.readBytes())
    val cw = ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
    val visitor = AddLogClassVisitor(cw)
    cr.accept(visitor, ClassReader.EXPAND_FRAMES)

    val byte = cw.toByteArray()
    val fos = FileOutputStream(classPath)
    fos.write(byte)
    fos.close()
```

- **编写自定义Visitor类继承于ClassVisitor，实现方法定位。**
1. visit()方法可以拿到类的相关信息：

```java
 public void visit(
      final int version,
      final int access,
      final String name,
      final String signature,
      final String superName,
      final String[] interfaces) {
    if (cv != null) {
      cv.visit(version, access, name, signature, superName, interfaces);
    }
  }
```

2. visitMethod()方法可以遍历所有方法信息，用于定位需要修改的方法。
```java
  public MethodVisitor visitMethod(
      final int access,
      final String name,
      final String descriptor,
      final String signature,
      final String[] exceptions) {
    if (cv != null) {
      return cv.visitMethod(access, name, descriptor, signature, exceptions);
    }
    return null;
  }
```
****

在这里找到通过名字判断定位目标方法：

**AddLogClassVisitor.kt**
```kotlin
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
```

- **编写自定义Visitor类继承于MethodVisitor，进行字节码修改。**

1. 在visitCode()方法中的字节码会插入到方法执行之前。

```java
  /** Starts the visit of the method's code, if any (i.e. non abstract method). */
  public void visitCode() {
    if (mv != null) {
      mv.visitCode();
    }
  }
```

2. 在visitInsn(opcode: Int)方法中当opcode == ARETURN || opcode == RETURN的时候，插入的字节码会插入到方法后面。

```java
  /**
   * Visits a zero operand instruction.
   *
   * @param opcode the opcode of the instruction to be visited. This opcode is either NOP,
   *     ACONST_NULL, ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5,
   *     LCONST_0, LCONST_1, FCONST_0, FCONST_1, FCONST_2, DCONST_0, DCONST_1, IALOAD, LALOAD,
   *     FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD, IASTORE, LASTORE, FASTORE, DASTORE,
   *     AASTORE, BASTORE, CASTORE, SASTORE, POP, POP2, DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2,
   *     SWAP, IADD, LADD, FADD, DADD, ISUB, LSUB, FSUB, DSUB, IMUL, LMUL, FMUL, DMUL, IDIV, LDIV,
   *     FDIV, DDIV, IREM, LREM, FREM, DREM, INEG, LNEG, FNEG, DNEG, ISHL, LSHL, ISHR, LSHR, IUSHR,
   *     LUSHR, IAND, LAND, IOR, LOR, IXOR, LXOR, I2L, I2F, I2D, L2I, L2F, L2D, F2I, F2L, F2D, D2I,
   *     D2L, D2F, I2B, I2C, I2S, LCMP, FCMPL, FCMPG, DCMPL, DCMPG, IRETURN, LRETURN, FRETURN,
   *     DRETURN, ARETURN, RETURN, ARRAYLENGTH, ATHROW, MONITORENTER, or MONITOREXIT.
   */
  public void visitInsn(final int opcode) {
    if (mv != null) {
      mv.visitInsn(opcode);
    }
  }
```
我们在这里插入需要的字节码：

**AddLogMethodVisitor.kt**
```kotlin
class AddLogMethodVisitor(methodVisitor: MethodVisitor) :
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
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/Exception")
        mv.visitInsn(Opcodes.DUP)
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Exception", "<init>", "()V", false)
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/Exception",
            "getStackTrace",
            "()[Ljava/lang/StackTraceElement;",
            false
        )
        mv.visitInsn(Opcodes.ICONST_0)
        mv.visitInsn(Opcodes.AALOAD)
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/StackTraceElement",
            "getMethodName",
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
```

- **把plugin编译出来，然后引入相应model中，便可以实现需求。**

**查看class文件，可以看到插入字节码成功**
![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/906bd0a96e5a4e048b1889c3d496319d~tplv-k3u1fbpfcp-watermark.image)

**log输出**
![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/49c2ca3943e2420688583371cd0c3b85~tplv-k3u1fbpfcp-watermark.image)

## 轻松的生成插入代码

在很多网络文章中都会推荐使用ASM Bytecode Outline插件来查看字节码，但是这个插件目前只可以安装在Intellij IDEA，Android studio无法安装，这边找到ASM Bytecode Viewer这个插件也可以达到同样的效果，可以让我们更加方便的插入字节码。

- 首先编写Test.java类，编译称.class，然后用ASM Bytecode Viewer插件来查看相关ASM代码。

**Test.java**
```java
package com.shawn.krouter;

import android.util.Log;

import com.shawn.krouter.uitl.UtilsKt;

public class Test {
    String name = "testFun";
    public void TestFun(){
        Log.d("suihw","lalala");
    }
}
```

通过插件查看ASM相关代码：
```java
package asm.com.shawn.krouter;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;

public class TestDump implements Opcodes {

    public static byte[] dump() throws Exception {

        ClassWriter classWriter = new ClassWriter(0);
        FieldVisitor fieldVisitor;
        MethodVisitor methodVisitor;
        AnnotationVisitor annotationVisitor0;

        classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER, "com/shawn/krouter/Test", null, "java/lang/Object", null);

        classWriter.visitSource("Test.java", null);

        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(5, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            methodVisitor.visitInsn(RETURN);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("this", "Lcom/shawn/krouter/Test;", null, label0, label1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "TestFun", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(7, label0);
            methodVisitor.visitLdcInsn("suihw");
            methodVisitor.visitLdcInsn("lalala");
            methodVisitor.visitMethodInsn(INVOKESTATIC, "android/util/Log", "d", "(Ljava/lang/String;Ljava/lang/String;)I", false);
            methodVisitor.visitInsn(POP);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(8, label1);
            methodVisitor.visitInsn(RETURN);
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLocalVariable("this", "Lcom/shawn/krouter/Test;", null, label0, label2, 0);
            methodVisitor.visitMaxs(2, 1);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }
}
```

- 修改Test.java类，把想要插入的相关java代码写好，然后在编译成.class文件：

**修改后的Test.java**
```java
package com.shawn.krouter;

import android.util.Log;

import com.shawn.krouter.uitl.UtilsKt;

public class Test {
    String name = "testFun";
    public void TestFun(){
        UtilsKt.MyLogD("suihw","into " + new Exception().getStackTrace()[0].getMethodName());
        Log.d("suihw","lalala");
    }
}
```

通过插件查看ASM相关代码：

```java
package asm.com.shawn.krouter;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;

public class TestDump implements Opcodes {

    public static byte[] dump() throws Exception {

        ClassWriter classWriter = new ClassWriter(0);
        FieldVisitor fieldVisitor;
        MethodVisitor methodVisitor;
        AnnotationVisitor annotationVisitor0;

        classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER, "com/shawn/krouter/Test", null, "java/lang/Object", null);

        classWriter.visitSource("Test.java", null);

        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(7, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            methodVisitor.visitInsn(RETURN);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("this", "Lcom/shawn/krouter/Test;", null, label0, label1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "TestFun", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(9, label0);
            methodVisitor.visitLdcInsn("suihw");
            methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            methodVisitor.visitLdcInsn("into ");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "com/shawn/krouter/uitl/UtilsKt", "MyLogD", "(Ljava/lang/String;Ljava/lang/String;)V", false);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(10, label1);
            methodVisitor.visitLdcInsn("suihw");
            methodVisitor.visitLdcInsn("lalala");
            methodVisitor.visitMethodInsn(INVOKESTATIC, "android/util/Log", "d", "(Ljava/lang/String;Ljava/lang/String;)I", false);
            methodVisitor.visitInsn(POP);
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLineNumber(11, label2);
            methodVisitor.visitInsn(RETURN);
            Label label3 = new Label();
            methodVisitor.visitLabel(label3);
            methodVisitor.visitLocalVariable("this", "Lcom/shawn/krouter/Test;", null, label0, label3, 0);
            methodVisitor.visitMaxs(3, 1);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }
}

```

- 找到异同处，便是我们需要的相关字节码，复制插入，便可以实现。


![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/b4190fc1418c438e99fc9d9fe785b4e3~tplv-k3u1fbpfcp-watermark.image)

# 彩蛋

**buildSrc统一版本控制**

> [官方介绍](https://docs.gradle.org/current/userguide/organizing_gradle_projects.html#sec:build_sources)

buildSrc模块会在整个项目编译最前执行，比项目根目录下build.gradle执行还要早，所以可以用来做统一版本控制，还会有代码提示，十分方便实用。

**项目结构**

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/260a78b7b289456b8d300cabb2aded90~tplv-k3u1fbpfcp-watermark.image)


**build.gradle.kts**

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/211fd0b89490464b8ec59db0ef0843b8~tplv-k3u1fbpfcp-watermark.image)

**Dependencies.kt**

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/7567b83447a64e0eb7faeedd30538ac1~tplv-k3u1fbpfcp-watermark.image)

在相关模块中引入：

![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/0ec4c85d841144a9b7082860ef1a1dc4~tplv-k3u1fbpfcp-watermark.image)

