package com.bangbang93.kdraft.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import com.bangbang93.kdraft.annotations.GenerateBuilder

class BuilderProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation(GenerateBuilder::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        val unableToProcess = symbols.filterNot { it.validate() }.toList()

        symbols
            .filter { it.validate() }
            .forEach { classDeclaration ->
                generateBuilder(classDeclaration)
            }

        return unableToProcess
    }

    private fun generateBuilder(classDeclaration: KSClassDeclaration) {
        val packageName = classDeclaration.packageName.asString()
        val className = classDeclaration.simpleName.asString()
        val builderClassName = "${className}Builder"

        val classType = classDeclaration.toClassName()
        val properties = classDeclaration.getAllProperties()
            .filter { it.isMutable || it.setter != null }
            .toList()

        // Create builder class
        val builderClass = TypeSpec.classBuilder(builderClassName)
            .apply {
                // Add properties for each field
                properties.forEach { property ->
                    val propertyName = property.simpleName.asString()
                    val propertyType = property.type.resolve().toClassName()
                    addProperty(
                        PropertySpec.builder(propertyName, propertyType.copy(nullable = true))
                            .mutable()
                            .initializer("null")
                            .build()
                    )
                }

                // Add setter methods
                properties.forEach { property ->
                    val propertyName = property.simpleName.asString()
                    val propertyType = property.type.resolve().toClassName()
                    addFunction(
                        FunSpec.builder(propertyName)
                            .addParameter(propertyName, propertyType)
                            .returns(ClassName(packageName, builderClassName))
                            .addStatement("this.%N = %N", propertyName, propertyName)
                            .addStatement("return this")
                            .build()
                    )
                }

                // Add build method
                addFunction(
                    FunSpec.builder("build")
                        .returns(classType)
                        .addCode(buildString {
                            append("return %T().apply {\n")
                            properties.forEach { property ->
                                val propertyName = property.simpleName.asString()
                                append("    this@apply.%N = this@%N.%N ?: error(\"Property %N is required\")\n")
                            }
                            append("}")
                        }, classType, *properties.flatMap { p ->
                            val name = p.simpleName.asString()
                            listOf(name, builderClassName, name, name)
                        }.toTypedArray())
                        .build()
                )
            }
            .build()

        // Generate the file
        val fileSpec = FileSpec.builder(packageName, builderClassName)
            .addType(builderClass)
            .build()

        fileSpec.writeTo(environment.codeGenerator, Dependencies(true, classDeclaration.containingFile!!))

        environment.logger.info("Generated builder for $className")
    }
}
