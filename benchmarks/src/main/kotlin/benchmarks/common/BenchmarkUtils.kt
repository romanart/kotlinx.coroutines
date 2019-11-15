/*
* Copyright 2016-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
*/

package benchmarks.common

import java.nio.file.*
import java.util.concurrent.ThreadLocalRandom

fun doGeomDistrWork(work: Int) {
    // We use geometric distribution here
    val p = 1.0 / work
    val r = ThreadLocalRandom.current()
    while (true) {
        if (r.nextDouble() < p) break
    }
}

// Runs the specified process, waits until it finishes, and returns its exit code.
fun runProcess(classNameToRun : String, jvmOptions : List<String>, args : Array<String>) : Int {
    val runJavaProcessCommand = ArrayList<String>()
    runJavaProcessCommand.run {
        this.add(JAVA_PATH)
        this.addAll(jvmOptions)
        this.add("-cp")
        this.add(CLASS_PATH)
        this.add(classNameToRun)
        this.addAll(args)
    }
    val processBuilder = ProcessBuilder(runJavaProcessCommand)
    val process = processBuilder.inheritIO().redirectError(ProcessBuilder.Redirect.INHERIT).start()
    return process.waitFor()
}

private val CLASS_PATH = System.getProperty("java.class.path")
private val JAVA_PATH = Paths.get(System.getProperty("java.home")).resolve("bin").resolve("java").toString()