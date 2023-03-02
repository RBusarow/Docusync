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

import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.lang.java.JavaLanguage
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.openapi.util.text.StringUtilRt
import org.jetbrains.kotlin.com.intellij.psi.PsiFile
import org.jetbrains.kotlin.com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.com.intellij.psi.PsiJavaFile
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import java.io.File
import java.io.FileNotFoundException

class DocusyncPsiFileFactory() : java.io.Serializable {

  @delegate:Transient
  private val configuration: CompilerConfiguration by lazy {
    CompilerConfiguration().apply {
      put(
        CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
        PrintingMessageCollector(
          System.err,
          MessageRenderer.PLAIN_FULL_PATHS,
          false
        )
      )
    }
  }

  @delegate:Transient
  val coreEnvironment by lazy {
    KotlinCoreEnvironment.createForProduction(
      parentDisposable = Disposer.newDisposable(),
      configuration = configuration,
      configFiles = EnvironmentConfigFiles.JVM_CONFIG_FILES
    )
  }

  @delegate:Transient
  private val psiProject by lazy { coreEnvironment.project }

  @delegate:Transient
  private val ktFileFactory by lazy { KtPsiFactory(psiProject, markGenerated = false) }

  @delegate:Transient
  private val javaFileFactory by lazy { PsiFileFactory.getInstance(psiProject) }

  fun create(file: File): PsiFile {
    if (!file.exists()) throw FileNotFoundException("could not find file $file")

    return when (file.extension) {
      "java" -> javaFileFactory.createFileFromText(
        file.name,
        JavaLanguage.INSTANCE,
        file.readText()
      ) as PsiJavaFile

      "kt", "kts" -> ktFileFactory.createPhysicalFile(
        file.name,
        StringUtilRt.convertLineSeparators(file.readText().trimIndent())
      )

      else -> throw IllegalArgumentException(
        "file extension must be one of [java, kt, kts], but it was `${file.extension}`."
      )
    }
  }

  /**
   * @return a "virtual" Psi `KtFile` with the given [name] and [content]. This file does not exist in
   *     a Java file system.
   * @see createKotlin
   */
  fun createKotlin(
    name: String,
    @Language("kotlin")
    content: String
  ): KtFile {
    return ktFileFactory
      .createFile(name, content)
  }

  /**
   * @return a "virtual" Psi `PsiJavaFile` with the given [name] and [content]. This file does not
   *     exist in a Java file system.
   * @see createJava
   */
  fun createJava(
    name: String,
    @Language("java")
    content: String
  ): PsiJavaFile {

    return javaFileFactory
      .createFileFromText(
        name,
        JavaLanguage.INSTANCE,
        content.trimIndent()
      ) as PsiJavaFile
  }
}
