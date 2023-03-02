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
  id("module")
}

module {
  shadow()
  published(
    artifactId = "docusync-cli",
    pomDescription = "the Docusync command line interface"
  )

  autoService()
  serialization()
}

dependencies {

  api(libs.clikt)
  api(libs.ec4j.core)
  api(libs.kaml)

  implementation(libs.java.diff.utils)
  implementation(libs.jetbrains.markdown)
  implementation(libs.kotlin.compiler)
  implementation(libs.kotlinx.coroutines.core)

  testImplementation(libs.junit.engine)
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.junit.jupiter.api)
  testImplementation(libs.junit.params)
  testImplementation(libs.kotest.assertions.api)
  testImplementation(libs.kotest.assertions.core.jvm)
  testImplementation(libs.kotest.assertions.shared)
  testImplementation(libs.kotest.common)
  testImplementation(libs.kotest.extensions)
  testImplementation(libs.kotest.property.jvm)
}
