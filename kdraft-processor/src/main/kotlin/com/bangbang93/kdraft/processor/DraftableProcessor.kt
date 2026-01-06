package com.bangbang93.kdraft.processor

import com.bangbang93.kdraft.Draftable
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

class DraftableProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation(Draftable::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        val unableToProcess = symbols.filterNot { it.validate() }.toList()

        symbols
            .filter { it.validate() }
            .forEach { classDeclaration ->
                generateDraft(classDeclaration)
            }

        return unableToProcess
    }

    private fun generateDraft(classDeclaration: KSClassDeclaration) {
        val packageName = classDeclaration.packageName.asString()
        val className = classDeclaration.simpleName.asString()
        val draftClassName = "${className}Draft"
        val draftFunctionName = className.replaceFirstChar { it.lowercase() } + "Draft"

        val classType = classDeclaration.toClassName()
        val draftType = ClassName(packageName, draftClassName)

        // Get primary constructor parameters
        val primaryConstructor = classDeclaration.primaryConstructor
        if (primaryConstructor == null) {
            environment.logger.error(
                "@Draftable requires a primary constructor",
                classDeclaration
            )
            return
        }
        val parameters = primaryConstructor.parameters

        // Create draft class
        val draftClass = TypeSpec.classBuilder(draftClassName)
            .apply {
                // Add properties for each constructor parameter
                parameters.forEach { parameter ->
                    val propertyName = parameter.name?.asString() ?: return@forEach
                    val propertyType = parameter.type.resolve()
                    val typeName = propertyType.toTypeName()
                    val defaultValue = getDefaultValueForType(typeName.toString(), propertyType.isMarkedNullable)

                    addProperty(
                        PropertySpec.builder(propertyName, typeName)
                            .mutable()
                            .initializer(defaultValue)
                            .build()
                    )
                }

                // Add set(propertyName: String, value: Any?) method
                addFunction(buildSetFunction(parameters))

                // Add build() method
                addFunction(buildBuildFunction(classType, parameters))
            }
            .build()

        // Create DSL function
        val draftFunction = FunSpec.builder(draftFunctionName)
            .addParameter(
                ParameterSpec.builder(
                    "block",
                    LambdaTypeName.get(receiver = draftType, returnType = UNIT)
                ).build()
            )
            .returns(classType)
            .addStatement("return %T().apply(block).build()", draftType)
            .build()

        // Generate the file
        val fileSpec = FileSpec.builder(packageName, draftClassName)
            .addType(draftClass)
            .addFunction(draftFunction)
            .build()

        fileSpec.writeTo(environment.codeGenerator, Dependencies(true, classDeclaration.containingFile!!))

        environment.logger.info("Generated draft for $className")
    }

    private fun buildSetFunction(
        parameters: List<com.google.devtools.ksp.symbol.KSValueParameter>
    ): FunSpec {
        val setFunctionBuilder = FunSpec.builder("set")
            .addParameter("propertyName", String::class)
            .addParameter("value", ANY.copy(nullable = true))

        val whenCodeBuilder = CodeBlock.builder()
            .beginControlFlow("when (propertyName)")

        parameters.forEach { parameter ->
            val propertyName = parameter.name?.asString() ?: return@forEach
            val propertyType = parameter.type.resolve()
            val typeName = propertyType.toTypeName()
            val isNullable = typeName.isNullable
            val nonNullableTypeName = typeName.copy(nullable = false)

            if (isNullable) {
                whenCodeBuilder.addStatement(
                    "%S -> if (value == null) { %N = null } else if (value is %T) { %N = value } else { throw IllegalArgumentException(%P) }",
                    propertyName,
                    propertyName,
                    nonNullableTypeName,
                    propertyName,
                    "Invalid type for property '$propertyName'. Expected ${typeName} but got \${value::class.qualifiedName}"
                )
            } else {
                whenCodeBuilder.addStatement(
                    "%S -> if (value is %T) { %N = value } else { throw IllegalArgumentException(%P) }",
                    propertyName,
                    nonNullableTypeName,
                    propertyName,
                    "Invalid type for property '$propertyName'. Expected ${typeName} but got \${value?.let { it::class.qualifiedName } ?: \"null\"}"
                )
            }
        }

        whenCodeBuilder.addStatement("else -> throw IllegalArgumentException(%P)", "Unknown property: \$propertyName")
        whenCodeBuilder.endControlFlow()

        setFunctionBuilder.addCode(whenCodeBuilder.build())

        return setFunctionBuilder.build()
    }

    private fun buildBuildFunction(
        classType: ClassName,
        parameters: List<com.google.devtools.ksp.symbol.KSValueParameter>
    ): FunSpec {
        val parameterNames = parameters.mapNotNull { it.name?.asString() }

        val format = buildString {
            append("return %T(")
            append(parameterNames.joinToString(", ") { "%N" })
            append(")")
        }

        val args = mutableListOf<Any>(classType)
        args.addAll(parameterNames)

        return FunSpec.builder("build")
            .returns(classType)
            .addStatement(format, *args.toTypedArray())
            .build()
    }

    private fun getDefaultValueForType(typeName: String, isNullable: Boolean): String {
        if (isNullable) {
            return "null"
        }

        return when {
            typeName == "kotlin.Int" || typeName == "Int" -> "0"
            typeName == "kotlin.Long" || typeName == "Long" -> "0L"
            typeName == "kotlin.Short" || typeName == "Short" -> "0"
            typeName == "kotlin.Byte" || typeName == "Byte" -> "0"
            typeName == "kotlin.Float" || typeName == "Float" -> "0.0f"
            typeName == "kotlin.Double" || typeName == "Double" -> "0.0"
            typeName == "kotlin.Boolean" || typeName == "Boolean" -> "false"
            typeName == "kotlin.Char" || typeName == "Char" -> "'\\u0000'"
            typeName == "kotlin.String" || typeName == "String" -> "\"\""
            // Mutable collections must come before immutable ones
            typeName.startsWith("kotlin.collections.MutableList") || typeName.startsWith("MutableList") -> "mutableListOf()"
            typeName.startsWith("kotlin.collections.MutableSet") || typeName.startsWith("MutableSet") -> "mutableSetOf()"
            typeName.startsWith("kotlin.collections.MutableMap") || typeName.startsWith("MutableMap") -> "mutableMapOf()"
            typeName.startsWith("kotlin.collections.List") || typeName.startsWith("List") -> "emptyList()"
            typeName.startsWith("kotlin.collections.Set") || typeName.startsWith("Set") -> "emptySet()"
            typeName.startsWith("kotlin.collections.Map") || typeName.startsWith("Map") -> "emptyMap()"
            else -> throw IllegalArgumentException(
                "No default value can be generated for non-nullable type '$typeName'. " +
                    "Make the property nullable or provide explicit handling in the processor."
            )
        }
    }
}
