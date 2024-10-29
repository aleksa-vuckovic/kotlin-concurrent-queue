package com.aleksa.select

interface ConcurrentQueue<T> {
    fun get(): T?
    fun put(value: T)
}