package com.aleksa.select
import kotlinx.atomicfu.*

/**
 * This is an implementation that uses no semaphores, no spin locks, and no loops.
 * The downside - get() can return null even after an insert is executed, if a
 * previous insert has not finished yet. This is because inserts are executed in 2 steps,
 * and the insert might, on rare occasions, be interrupted between them.
 * The underlying data structure remains consistent, and after the initial insert completes,
 * the consumer will get all the remaining values.
 *
 * The other solution I have, which guarantees that all inserts will happen atomically,
 * requires a spinlock, and you can find it in the other file. I think this
 * solution here is better because producers never wait on each other, and the consumer
 * waits as much as or less than when using locks (it's really up to the underlying thread scheduler).
 * However, it doesn't pass the lincheck test since the results are not always linearizable.
 *
 * EXAMPLE:
 * Say the queue has some values and put(?) is invoked and finishes step 1,
 * but its thread is then suspended by the scheduler.
 * Then even before step 2 is finished, many other insert invocations
 * can successfully finish and add their values. Once all previously present values (before the
 * initial aforementioned put) are consumed, get() will return null until the initial put
 * completes step 2. And yet new values are already there.
 */
class NoSpinlockConcurrentQueue<T>: ConcurrentQueue<T> {

    private class Node<T>(val data: T) {
        val next: AtomicRef<Node<T>?>
        init {
            next = atomic(null)
        }
    }

    private val tail: AtomicRef<Node<T>?> = atomic(null)
    private val head: AtomicRef<Node<T>?> = atomic(null)
    private val CONSUMED = Node<Any?>(null) as Node<T> //I just need a special value for the 'next' reference

    override fun get(): T? {
        if (tail.value == null)
            return null
        val consumed = tail.value!!
        tail.value = null
        if (!consumed.next.compareAndSet(null, CONSUMED)) {
            //This was the last node and we consumed it
            val a = consumed.next.value
            tail.value = a
        }
        return consumed.data
    }

    override fun put(value: T) {
        //STEP 1: Allocate node and move the head pointer
        val newNode = Node(value)
        val prevNode = head.getAndSet(newNode)

        //STEP 2: Link prevNode to newNode
        if (prevNode == null || !prevNode.next.compareAndSet(null, newNode))
            tail.value = newNode
    }
}