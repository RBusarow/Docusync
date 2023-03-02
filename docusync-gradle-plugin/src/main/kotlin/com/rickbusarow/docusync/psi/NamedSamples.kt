/*
 * Copyright (C) 2023 Rick Busarow
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rickbusarow.docusync.psi

import com.rickbusarow.docusync.internal.joinToStringConcat
import com.rickbusarow.docusync.internal.requireNotNull
import com.rickbusarow.docusync.psi.LazyMap.Companion.toLazyMap
import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDeclarationContainer
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import java.io.File

@Serializable
data class SampleRequest(
  val fqName: String,
  val bodyOnly: Boolean
) : java.io.Serializable

@Serializable
data class SampleResult(
  val request: SampleRequest,
  val content: String
) : java.io.Serializable

class NamedSamples(
  private val psiFactory: DocusyncPsiFileFactory
) : java.io.Serializable {

  fun findAll(files: List<File>, requests: List<SampleRequest>): List<SampleResult> {
    return findAll(files.map { psiFactory.createKotlin(it.name, it.readText()) }, requests)
  }

  @JvmName("findAllInKotlin")
  fun findAll(ktFiles: List<KtFile>, requests: List<SampleRequest>): List<SampleResult> {

    val cache = createDeclarationCache(ktFiles, requests)

    return requests.map { request ->

      val content =
        when (val namedDeclaration = cache[FqName(request.fqName)]) {
          is KtClassOrObject -> if (request.bodyOnly) {
            namedDeclaration.body!!.textInScope()
          } else {
            namedDeclaration.text
          }

          is KtProperty -> if (request.bodyOnly) {
            namedDeclaration.initializer
              .requireNotNull {
                "${namedDeclaration.containingKtFile} > " +
                  "A property must have an initializer when using 'bodyOnly = true'."
              }
              .let { (it as? KtStringTemplateExpression) ?: it.getChildOfType() }
              .requireNotNull {
                "${namedDeclaration.containingKtFile} > " +
                  "A property initializer must be a string template."
              }
              .textInScope()
          } else {
            namedDeclaration.text
          }

          is KtNamedFunction -> if (request.bodyOnly) {
            namedDeclaration.bodyBlockExpression!!.textInScope()
          } else {
            namedDeclaration.text
          }

          null -> error("could not find a psi element with the name of ${request.fqName}")

          else -> error("Unsupported psi element -- ${namedDeclaration.text}")
        }

      SampleResult(request, content)
    }
  }

  private fun createDeclarationCache(
    ktFiles: List<KtFile>,
    requests: List<SampleRequest>
  ): LazyMap<FqName?, KtNamedDeclaration?> {
    val namesAndParentNames = requests
      .map { FqName(it.fqName) }
      .flatMap { requestedName ->
        generateSequence(requestedName) { if (it.isRoot) null else it.parent() }
      }
      .toSet()

    return ktFiles.asSequence()
      .flatMap<KtFile, KtNamedDeclaration> { ktFile ->

        ktFile.getChildrenOfTypeRecursive { element ->

          when (element) {
            is KtNamedDeclaration -> element.fqName in namesAndParentNames
            else -> element is KtDeclarationContainer
          }
        }
      }
      .distinct()
      .map { it.fqName to it }
      .toLazyMap()
  }

  private fun KtElement.textInScope() = children
    .drop(1)
    .dropLast(1)
    .joinToStringConcat { it.text }
}
