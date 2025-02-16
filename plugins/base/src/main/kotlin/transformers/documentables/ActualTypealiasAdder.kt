package org.jetbrains.dokka.base.transformers.documentables

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.*
import org.jetbrains.dokka.model.properties.WithExtraProperties
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.transformers.documentation.DocumentableTransformer

class ActualTypealiasAdder : DocumentableTransformer {

    override fun invoke(original: DModule, context: DokkaContext) = original.generateTypealiasesMap().let { aliases ->
        original.copy(packages = original.packages.map { it.copy(classlikes = addActualTypeAliasToClasslikes(it.classlikes, aliases)) })
    }

    private fun DModule.generateTypealiasesMap(): Map<DRI, DTypeAlias> =
        packages.flatMap { pkg ->
            pkg.typealiases.map { typeAlias ->
                typeAlias.dri to typeAlias
            }
        }.toMap()


    private fun addActualTypeAliasToClasslikes(
        elements: Iterable<DClasslike>,
        typealiases: Map<DRI, DTypeAlias>
    ): List<DClasslike> = elements.flatMap {
        when (it) {
            is DClass -> addActualTypeAlias(
                it.copy(
                    classlikes = addActualTypeAliasToClasslikes(it.classlikes, typealiases)
                ).let(::listOf),
                typealiases
            )
            is DEnum -> addActualTypeAlias(
                it.copy(
                    classlikes = addActualTypeAliasToClasslikes(it.classlikes, typealiases)
                ).let(::listOf),
                typealiases
            )
            is DInterface -> addActualTypeAlias(
                it.copy(
                    classlikes = addActualTypeAliasToClasslikes(it.classlikes, typealiases)
                ).let(::listOf),
                typealiases
            )
            is DObject -> addActualTypeAlias(
                it.copy(
                    classlikes = addActualTypeAliasToClasslikes(it.classlikes, typealiases)
                ).let(::listOf),
                typealiases
            )
            is DAnnotation -> addActualTypeAlias(
                it.copy(
                    classlikes = addActualTypeAliasToClasslikes(it.classlikes, typealiases)
                ).let(::listOf),
                typealiases
            )
            else -> throw IllegalStateException("${it::class.qualifiedName} ${it.name} cannot have extra added")
        }
    }

    private fun <T> addActualTypeAlias(
        elements: Iterable<T>,
        typealiases: Map<DRI, DTypeAlias>
    ): List<T> where T : DClasslike, T : WithExtraProperties<T>, T : WithSources =
        elements.map { element ->
            if (element.expectPresentInSet != null) {
                typealiases[element.dri]?.let { ta ->
                    val merged = element.withNewExtras(element.extra + ActualTypealias(ta.underlyingType)).let {
                        when (it) {
                            is DClass -> it.copy(
                                documentation = element.documentation + ta.documentation,
                                sourceSets = element.sourceSets + ta.sourceSets
                            )

                            is DEnum -> it.copy(
                                documentation = element.documentation + ta.documentation,
                                sourceSets = element.sourceSets + ta.sourceSets
                            )

                            is DInterface -> it.copy(
                                documentation = element.documentation + ta.documentation,
                                sourceSets = element.sourceSets + ta.sourceSets
                            )

                            is DObject -> it.copy(
                                documentation = element.documentation + ta.documentation,
                                sourceSets = element.sourceSets + ta.sourceSets
                            )

                            is DAnnotation -> it.copy(
                                documentation = element.documentation + ta.documentation,
                                sourceSets = element.sourceSets + ta.sourceSets
                            )

                            else -> throw IllegalStateException("${it::class.qualifiedName} ${it.name} cannot have copy its sourceSets")
                        }
                    }
                    @Suppress("UNCHECKED_CAST")
                    merged as T
                } ?: element
            } else {
                element
            }
        }
}
