package com.cx

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import groovy.xml.Namespace

class OptimizerTask extends DefaultTask {

    def webpTool
    def pngTool
    def jpgTool

    @Input
    def manifestFile
    @Input
    def minSdk

    @Input
    def res

    String icon
    String roundIcon

    OptimizerTask() {
        group = 'Optimizer'
        webpTool = OptimizerUtil.getTool(project, 'cwebp')
        pngTool = OptimizerUtil.getTool(project, 'pngcrush')
        jpgTool = OptimizerUtil.getTool(project, 'guetzli')

    }

    @TaskAction
    void run() {
        println "==============Optimizer================="
        println "webp tool : $webpTool"
        println "png tool : $pngTool"
        println "jpg tool : $jpgTool"

        //解析manifest文件
        def ns = new Namespace("http://schemas.android.com/apk/res/android", "android")
        def node = new XmlParser().parse(manifestFile)


        Node applicationNode = node.application[0]

        icon = applicationNode.attributes()[ns.icon]
        roundIcon = applicationNode.attributes()[ns.roundIcon]

        icon = icon.substring(icon.indexOf('/') + 1, icon.length())
        roundIcon = roundIcon.substring(roundIcon.indexOf('/') + 1, roundIcon.length())

        println "parse manifest launcher : $icon"
        println "parse manifest launcher : $roundIcon"

        //不能将原来的图片删掉，要拿准备打包的资源，都在build目录下
        def pngs = []
        def jpgs = []
        res.eachDir {
            if (OptimizerUtil.isImageFolder(it)) {
                it.eachFile {
                    if (OptimizerUtil.isPreOptimizeJpg(it)) {
                        jpgs << it
                    } else if (OptimizerUtil.isPreOptimizePng(it) && isNoLauncher(it)) {
                        pngs << it
                    }
                }
            }
        }
        println("pngs.size==============>${pngs.size()}")
        println("jpgs.size===============>${jpgs.size()}")
        pngs.each {
            println "pngs======>${it}"
        }

        jpgs.each {
            println "jpgs=======>${it}"
        }

        println "current minSdk : $minSdk"

        if (minSdk > 14 && minSdk < 18) {
            println("minSdk 14----------->18")
            def compress = []
            //处理png 如果带透明度则加入压缩集合
            pngs.each {
                if (OptimizerUtil.isTransparent(it)) {
                    compress << it
                } else {
                    convertWebp(it)
                }
            }
            compress.each {
                compressPng(it)
            }
            jpgs.each {
                convertWebp(it)
            }
        } else if (minSdk >= 18) {
            println("minSdk >= 18")
            pngs.each {
                convertWebp(it)
            }

            jpgs.each {
                convertWebp(it)
            }
        } else {
            pngs.each {
                compressPng(it)
            }
            jpgs.each {
                compressJpg(it)
            }
        }

    }
    /**
     * 如果不是launcher图标，则为true,否则为false
     * @param file
     * @return
     */
    def isNoLauncher(File file) {
        String name = file.name
        println("isNoLauncher----------->${name}")
        println("${icon}.png")
        println("${roundIcon}.png")
        return name != "${icon}.png" && name != "${roundIcon}.png"
    }

    /**
     * 转换为webp
     * @param file
     * @return
     */
    def convertWebp(File file) {
        String name = file.name
        name = name.substring(0, name.lastIndexOf('.'))
        def result = "$webpTool -q 75 ${file.absolutePath} -o ${file.parent}/${name}.webp".execute()
        result.waitForProcessOutput()
        if (result.exitValue() == 0) {
            file.delete()
            println "convert webp ${file.absolutePath} success"
        } else {
            println "convert webtp ${file.absolutePath} error"
        }
    }

    def compressPng(File file) {
        def out = new File(file.parent, "temp-preoptimizer-${file.name}")
        def result =  "${pngTool} -brute -rem alla - reduce -q ${file.absolutePath} ${out.absolutePath}".execute()
        result.waitForProcessOutput()
        if (result.exitValue() == 0) {
            file.delete()
            out.renameTo(file)
            println "compress png ${file.absolutePath} success"
        } else {
            println "compress png ${file.absolutePath} error"
        }
    }

    def compressJpg(File file) {
        def out = new File(file.parent, "temp-preoptimizer-${file.name}")
        def result = "${jpgTool} --quality 84 ${file.absolutePath} ${out.absolutePath}".execute()
        result.waitForProcessOutput()
        if (result.exitValue() == 0) {
            file.delete()
            out.renameTo(file)
            println "compress png ${file.absolutePath} success"
        } else {
            println "compress png ${file.absolutePath} error"
        }
    }
}

