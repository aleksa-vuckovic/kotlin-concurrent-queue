package com.aleksa.select

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic

class SpinlockConcurrentQueue<T>: ConcurrentQueue<T> {

    private class Node<T>(val data: T) {
        val next: AtomicRef<Node<T>?>
        init {
            next = atomic(null)
        }
    }

    private val tail: AtomicRef<Node<T>?> = atomic(null)
    private var head: Node<T>? = null
    private val producerLock = atomic(false)
    private val CONSUMED = Node<Any?>(null) as Node<T> //Special value for the last consumed node

    override fun get(): T? {
        if (tail.value == null)
            return null
        val consumed = tail.value!!
        tail.value = null
        if (!consumed.next.compareAndSet(null, CONSUMED)) {
            val a = consumed.next.value
            tail.value = a
        }
        return consumed.data
    }

    override fun put(value: T) {
        val newNode = Node(value)
        while (!producerLock.compareAndSet(false, true))
            Thread.yield()

        if (head == null || !head!!.next.compareAndSet(null, newNode))
            tail.value = newNode
        head = newNode

        producerLock.value = false
    }
}