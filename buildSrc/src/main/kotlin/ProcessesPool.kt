/*
 * ART
 *
 * Copyright 2020 ART
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.zeroturnaround.exec.StartedProcess
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.HOURS

private val processes: MutableList<StartedProcess> = mutableListOf()
private val scheduler = ForkJoinPool()

fun waitProcesses(timeout: Long = 1, unit: TimeUnit = HOURS) = scheduler.awaitTermination(timeout, unit)

fun forEachProcess(action: (process: StartedProcess) -> Unit) = processes.forEach(action)

fun killProcesses() = forEachProcess { process ->
    //executionService.kill(process)
    process.future.cancel(true)
}

fun StartedProcess.registerProcess() = scheduler.execute { future.get() }
