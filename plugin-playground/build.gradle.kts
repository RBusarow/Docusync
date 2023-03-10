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

plugins {
  base
  id("com.rickbusarow.docusync")
}

docusync {

  docSet("main") {

    docs(
      fileTree(projectDir) {
        include(
          "**/*.md",
          "**/*.mdx"
        )
      }
    )

    rule("cats-to-dogs") {
      regex = "cat"
      replacement = "dog"
    }

    rule("dogs-to-cats") {
      regex = "dog"
      replacement = "cat"
    }
  }
}

val createDocs by tasks.registering {

  val root = project.file("mds")

  doLast {

    root.mkdirs()

    repeat(100) {

      val file = root.resolve("markdown_$it.md")

      file.writeText(
        """
        ## My File $it

        <!---docusync cats-to-dogs,cats-to-dogs-->
        cat

        cats

        category

        dog

        <!---/docusync-->
        """.trimIndent()
      )
    }
  }
}
