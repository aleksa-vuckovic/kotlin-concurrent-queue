## Single consumer concurrent queue using atomicFU

I offer two solutions, [one using a spinlock](./src/main/kotlin/com/aleksa/select/SpinlockConcurrentQueue.kt) which passes the lincheck test, and [another with no spinlock](./src/main/kotlin/com/aleksa/select/NoSpinlockConcurrentQueue.kt), no semaphores and no loops, that that does not pass the lincheck test, but I think is the better solution.

One thing shared by both solutions is that nodes themselves have the 'next' pointer as an atomic field. This helps with synchronizing the producer and consumer, when the last node is consumed and the queue is left empty. The 'next' reference is then set to a special value.