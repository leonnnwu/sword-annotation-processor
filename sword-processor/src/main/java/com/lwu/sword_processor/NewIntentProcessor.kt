package com.lwu.sword_processor

import com.lwu.sword_annotation.NewIntent
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import java.io.IOException
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

/**
 * Created by lwu on 1/31/18.
 */
class NewIntentProcessor : AbstractProcessor() {
    private lateinit var activitiesWithPackage: MutableMap<String, String>


    override fun init(p0: ProcessingEnvironment?) {
        super.init(p0)

        activitiesWithPackage = HashMap()
    }

    override fun process(typeElementSet: MutableSet<out TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
        try {
            // 1. find all annotated element
            roundEnvironment.getElementsAnnotatedWith(NewIntent::class.java).forEach {
                if (it.kind != ElementKind.CLASS) {
                    processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Can be applied to class.")
                    return true
                }

                activitiesWithPackage.put(
                        it.simpleName.toString(),
                        processingEnv.elementUtils.getPackageOf(it).qualifiedName.toString()
                )
            }

            // 2. create class and methods
            val navigatorClass = TypeSpec
                    .classBuilder("Navigator")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)

            activitiesWithPackage.entries.forEach {
                val activityName = it.key
                val packageName = it.value
                val activityClass = ClassName.get(packageName, activityName)
                val intentMethod = MethodSpec
                        .methodBuilder("$METHOD_PREFIX$activityName")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(classIntent)
                        .addParameter(classContext, "context")
                        .addStatement("return new \$T(\$L, \$L)", classIntent, "context", "$activityClass.class")
                        .build()
                navigatorClass.addMethod(intentMethod)
            }

            JavaFile.builder("com.lwu", navigatorClass.build()).build().writeTo(processingEnv.filer)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return true
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(NewIntent::class.java.canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    companion object {
        private const val METHOD_PREFIX = "start"
        private val classIntent = ClassName.get("android.content", "Intent")
        private val classContext = ClassName.get("android.content", "Context")
    }
}