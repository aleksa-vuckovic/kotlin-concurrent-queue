package com.aleksa.select

import kotlin.test.Test
import kotlin.test.assertEquals


class SequentialTests {
    @Test
    fun `NoSpinlock sanity check`() {
        val queue: ConcurrentQueue<Int> = NoSpinlockConcurrentQueue()
        queue.put(1)
        queue.put(2)
        queue.put(3)
        assertEquals(1, queue.get())
        assertEquals(2, queue.get())
        assertEquals(3, queue.get())
    }

    @Test
    fun `Spinlock sanity check`() {
        val queue: ConcurrentQueue<Int> = SpinlockConcurrentQueue()
        queue.put(1)
        queue.put(2)
        queue.put(3)
        assertEquals(1, queue.get())
        assertEquals(2, queue.get())
        assertEquals(3, queue.get())
    }
}