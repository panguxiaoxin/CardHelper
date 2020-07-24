package com.ryz.card.plugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.AppExtension
class CardPlugin implements Plugin<Project>{


    @Override
    void apply(Project project) {
    println("wo shi ceshi . start.....>>>>>>>>")
    def android = project.extensions.getByType(AppExtension)
     android.registerTransform(new CardTransformN(project))
        println("wo shi end........<<<<<<....")
    }
}