package com.cx

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.GradleException
import org.gradle.api.Project

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

class OptimizerUtil {

    def static final PNG = 'png'
    def static final JPG = 'jpg'
    def static final JPEG = 'jpeg'
    def static final PNG9 = '.9.png'

    def static getTool(Project project, String name) {
        String toolName
        if (Os.isFamily(Os.FAMILY_MAC)) {
            toolName = "${name}_darwin"
        } else if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            toolName = "${name}_exe"
        } else {
            toolName = "${name}_linux"
        }

        def tool = "${project.buildDir}/tools/$toolName"

        def file = new File(tool)

        if (!file.exists()) {
            file.parentFile.mkdir()

            new FileOutputStream(file).withStream {
                def is = OptimizerUtil.class.getResourceAsStream("/$name/$toolName")
                it.write(is.bytes)
                is.close()

            }
        }

        if (file.exists() && file.setExecutable(true)) {
            return file.absolutePath
        }
        throw new GradleException('无法执行或者不存在')
    }
    /**
     * 判断是否存在透明通道
     * @param file
     * @return
     */
    def static isTransparent(File file) {
        BufferedImage image = ImageIO.read(file)
        return image.colorModel.hasAlpha()
    }

    def static isImageFolder(File file) {
        return file.name.startsWith("drawable") || file.name.startsWith("mipmap")
    }

    def static isPreOptimizePng(File file) {
        return (file.name.endsWith(PNG) || file.name.endsWith(PNG.toUpperCase())) &&
                !file.name.endsWith(PNG9) && !file.name.endsWith(PNG9.toUpperCase())
    }

    def static isPreOptimizeJpg(File file) {
        return file.name.endsWith(JPG) || file.name.endsWith(JPG.toUpperCase()) ||
                file.name.endsWith(JPEG) || file.name.startsWith(JPEG.toUpperCase())
    }
}
