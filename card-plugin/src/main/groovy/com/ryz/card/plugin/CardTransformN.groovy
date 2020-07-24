package com.ryz.card.plugin

import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

class CardTransformN extends Transform{

    Project project


    CardTransformN(Project project){
        this.project=project;
    }
    //该Transform的名称，自定义即可，只是一个标识
    @Override
    String getName() {
        return "CardTransform"
    }
//该Transform支持扫描的文件类型，分为class文件和资源文件，我们这里只处理class文件的扫描
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }
    //Transfrom的扫描范围，我这里扫描整个工程，包括当前module以及其他jar包、aar文件等所有的class
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }
    //是否增量扫描
    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        println ("--------------start transform ------------>>>>>>>>>---")

        def appCardRootClassList = []
        //inputs就是所有扫描到的class文件或者是jar包，一共2种类型
        inputs.each { TransformInput input ->

            input.directoryInputs.each { DirectoryInput directoryInput ->
                //1.遍历所有的class文件目录
                if (directoryInput.file.isDirectory()) {
                    directoryInput.file.eachFileRecurse {File file ->
                        //形如 cardManager$$Root$$ 的类，是我们要找的目标class
                        if (ScanUtil.isTargetProxyClass(file)) {
                            //如果是我们自己生产的代理类，保存该类的类名
                            println("file --------name="+file.name);
                            appCardRootClassList.add(file.name)
                        }
                    }
                }
                //Transform扫描的class文件是输入文件(input)，有输入必然会有输出(output)，处理完成后需要将输入文件拷贝到一个输出目录下去，
                //后面打包将class文件转换成dex文件时，直接采用的就是输出目录下的class文件了。
                //必须这样获取输出路径的目录名称
                def dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
            //2.遍历查找所有的jar包
            input.jarInputs.each { JarInput jarInput ->
                println "\njarInput = ${jarInput}"
                //与处理class文件一样，处理jar包也是一样，最后要将inputs转换为outputs
                def jarName = jarInput.name
                def md5 = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                //获取输出路径下的jar包名称，必须这样获取，得到的输出路径名不能重复，否则会被覆
                def dest = outputProvider.getContentLocation(jarName + md5, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                if (jarInput.file.getAbsolutePath().endsWith(".jar")) {
                    //处理jar包里的代码
                    File src = jarInput.file
                    //先简单过滤掉 support-v4 之类的jar包，只处理有我们业务逻辑的jar包
                    if (ScanUtil.shouldProcessPreDexJar(src.absolutePath)) {
                        //扫描jar包的核心代码在这里，主要做2件事情：
                        //1.扫描该jar包里有没有实现IAppLike接口的代理类;
                        //2.扫描AppLifeCycleManager这个类在哪个jar包里，并记录下来，后面需要在该类里动态注入字节码；

                        List<String> list = ScanUtil.scanJar(src, dest)
                   for (String str :list){
                       println("jar --------name="+src)
                   }
                        if (list != null) {
                            appCardRootClassList.addAll(list)
                        }
                    }
                }
                //将输入文件拷贝到输出目录下
                FileUtils.copyFile(jarInput.file, dest)
            }
        }

        println ""
        appCardRootClassList.forEach({fileName ->
            println "file name = " + fileName
        })
        println "\n包含CardManager类的jar文件"
        println ScanUtil.FILE_CONTAINS_INIT_CLASS.getAbsolutePath()
        println "开始自动注册"

        new AppCardCodeInjector(appCardRootClassList).execute()



        println ("--------------end transform ------------<<<<<<<<---")
    }
}