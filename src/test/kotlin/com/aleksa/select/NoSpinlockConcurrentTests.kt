package com.aleksa.select

import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.annotations.Param
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import kotlin.test.Test

@Param(name = "elem", gen = IntGen::class, conf = "1:100")
class NoSpinlockConcurrentTests {

    val queue = NoSpinlockConcurrentQueue<Int>()

    @Operation
    fun insert(@Param(name = "elem") elem: Int) = queue.put(elem)

    @Operation(nonParallelGroup = "remove")
    fun remove(): Int? = queue.get()

    @Test
    fun modelTest() {
        ModelCheckingOptions().check(this::class)
    }

    @Test
    fun stressTest() {
        StressOptions().check(this::class)
    }
}