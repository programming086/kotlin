/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.codegen

import org.jetbrains.kotlin.cli.AbstractCliTest
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.load.kotlin.ModuleMapping
import org.jetbrains.kotlin.serialization.deserialization.DeserializationConfiguration
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.testFramework.KtUsefulTestCase
import java.io.File

class JvmPackageTableTest : KtUsefulTestCase() {
    private fun doTest(relativeDirectory: String) {
        val directory = KotlinTestUtils.getTestDataPathBase() + relativeDirectory
        val tmpdir = KotlinTestUtils.tmpDir(this::class.simpleName)

        val moduleName = "main"
        val (output, exitCode) = AbstractCliTest.executeCompilerGrabOutput(K2JVMCompiler(), listOf(
                directory,
                "-d", tmpdir.path,
                "-module-name", moduleName
        ))
        System.err.println(output) // normally output is empty
        assertEquals("Compilation should complete successfully", ExitCode.OK, exitCode)

        val mapping = ModuleMapping.create(
                File(tmpdir, "META-INF/$moduleName.${ModuleMapping.MAPPING_FILE_EXT}").readBytes(), "test",
                DeserializationConfiguration.Default
        )
        val result = buildString {
            for ((fqName, packageParts) in mapping.packageFqName2Parts) {
                appendln(fqName)
                for (part in packageParts.parts) {
                    append("  ")
                    append(part)
                    val facadeName = packageParts.getMultifileFacadeName(part)
                    if (facadeName != null) {
                        append(" (")
                        append(facadeName)
                        append(")")
                    }
                    appendln()
                }
            }
        }

        KotlinTestUtils.assertEqualsToFile(File(directory, "jvm-package-table.txt"), result)
    }

    fun testSimple() {
        doTest("/jvmPackageTable/simple")
    }
}
