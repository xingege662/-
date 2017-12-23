package com.cx

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class OptimizePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if(!project.plugins.hasPlugin(AppPlugin)){
          throw new GradleException('只能够在 android application中使用')
        }
        project.afterEvaluate {
            project.android.applicationVariants.all{
                BaseVariant variant ->
                   def task =  project.tasks.create("optimize${variant.name.capitalize()}",OptimizerTask){
                        //设置manifest文件
//                      manifestFile = variant.outputs.first().processManifest.manifestOutputDirectory.absoluteFile
                        manifestFile = variant.outputs.first().processManifest.manifestOutputFile
                        minSdk = variant.mergeResources.minSdk
                        res = variant.mergeResources.outputDir
                    }

                    variant.outputs.first().processResources.dependsOn task

                    task.dependsOn variant.outputs.first().processManifest
            }
        }
    }
}


