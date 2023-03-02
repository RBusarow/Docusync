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

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream

const val TRIPLE_QUOTES = "\"\"\""

class NamedSamplesTest {

  @Test
  fun `can be serialized as java serializable`() {

    val namedSamples = NamedSamples(DocusyncPsiFileFactory())

    shouldNotThrowAny {
      ObjectOutputStream(ByteArrayOutputStream()).writeObject(namedSamples)
    }
  }

  @Test
  fun `string template property within a class and bodyOnly = true`() {
    val result = parse(
      fqName = "com.test.MyClass.groovy",
      bodyOnly = true,
      """
      package com.test

      class MyClass {
        val groovy = $TRIPLE_QUOTES
          plugins {
            id 'com.squareup.anvil' version '2.4.4'
          }

          android {
            foo()
          }

          dependencies {
            compileOnly 'javax:inject:1'
          }
        $TRIPLE_QUOTES.trimIndent()
      }
      """
    )

    result.trimIndent() shouldBe """
      plugins {
        id 'com.squareup.anvil' version '2.4.4'
      }

      android {
        foo()
      }

      dependencies {
        compileOnly 'javax:inject:1'
      }
    """.trimIndent()
  }

  @Test
  fun `string template property within a class and bodyOnly = false`() {
    val result = parse(
      fqName = "com.test.MyClass.groovy",
      bodyOnly = false,
      """
      package com.test

      class MyClass {
        val groovy = $TRIPLE_QUOTES
          plugins {
            id 'com.squareup.anvil' version '2.4.4'
          }

          android {
            foo()
          }

          dependencies {
            compileOnly 'javax:inject:1'
          }
        $TRIPLE_QUOTES.trimIndent()
      }
      """
    )

    result.trimIndent() shouldBe """
      val groovy = $TRIPLE_QUOTES
          plugins {
            id 'com.squareup.anvil' version '2.4.4'
          }

          android {
            foo()
          }

          dependencies {
            compileOnly 'javax:inject:1'
          }
        $TRIPLE_QUOTES.trimIndent()
    """.trimIndent()
  }

  @Test
  fun `top level string template property and bodyOnly = true`() {
    val result = parse(
      fqName = "com.test.groovy",
      bodyOnly = true,
      """
      package com.test

      val groovy = $TRIPLE_QUOTES
        plugins {
          id 'com.squareup.anvil' version '2.4.4'
        }

        android {
          foo()
        }

        dependencies {
          compileOnly 'javax:inject:1'
        }
      $TRIPLE_QUOTES
      """
    )

    result.trimIndent() shouldBe """
      plugins {
        id 'com.squareup.anvil' version '2.4.4'
      }

      android {
        foo()
      }

      dependencies {
        compileOnly 'javax:inject:1'
      }
    """.trimIndent()
  }

  @Test
  fun `top level string template property with trimIndent and bodyOnly = true`() {
    val result = parse(
      fqName = "com.test.groovy",
      bodyOnly = true,
      """
      package com.test

      val groovy = $TRIPLE_QUOTES
        plugins {
          id 'com.squareup.anvil' version '2.4.4'
        }

        android {
          foo()
        }

        dependencies {
          compileOnly 'javax:inject:1'
        }
      $TRIPLE_QUOTES.trimIndent()
      """
    )

    result.trimIndent() shouldBe """
      plugins {
        id 'com.squareup.anvil' version '2.4.4'
      }

      android {
        foo()
      }

      dependencies {
        compileOnly 'javax:inject:1'
      }
    """.trimIndent()
  }

  @Test
  fun `top level string template property and bodyOnly = false`() {
    val result = parse(
      fqName = "com.test.groovy",
      bodyOnly = false,
      """
      package com.test

      val groovy = $TRIPLE_QUOTES
        plugins {
          id 'com.squareup.anvil' version '2.4.4'
        }

        android {
          foo()
        }

        dependencies {
          compileOnly 'javax:inject:1'
        }
      $TRIPLE_QUOTES.trimIndent()
      """
    )

    result.trimIndent() shouldBe """
      val groovy = $TRIPLE_QUOTES
        plugins {
          id 'com.squareup.anvil' version '2.4.4'
        }

        android {
          foo()
        }

        dependencies {
          compileOnly 'javax:inject:1'
        }
      $TRIPLE_QUOTES.trimIndent()
    """.trimIndent()
  }

  fun parse(
    fqName: String,
    bodyOnly: Boolean,
    @Language("kotlin") vararg content: String
  ): String {
    val files = content.mapIndexed { index, code ->
      DocusyncPsiFileFactory().createKotlin("Source_$index.kt", code.trimIndent())
    }

    return NamedSamples(DocusyncPsiFileFactory())
      .findAll(
        ktFiles = files,
        requests = listOf(SampleRequest(fqName, bodyOnly))
      )
      .single()
      .content
  }
}
