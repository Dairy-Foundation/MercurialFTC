package org.firstinspires.ftc.teamcode.beginners

@Suppress("unused")
object ReinventingMercurialFromFirstPrinciples {
    // This file will aim to walk you through reinventing the main concepts in Mercurial
    // We will re-use the names that Mercurial uses for each of these concepts
    // Mercurial adds some extra nice stuff under the hood.

    // Continuations:
    // Continuations are functional interfaces with one method
    // that runs the step of the continuation, and returns the next step
    fun interface Continuation {
        fun apply(): Continuation

        companion object {
            // halt is the last continuation, it always returns itself, and is a marker that we are done.
            val halt: Continuation = Continuation { halt }
        }
    }

    // there are several ways we could write Continuations, in order to recreate
    // the features of switch-statement finite state machines
    var c: Continuation = Continuation { Continuation.Companion.halt }

    var b: Continuation = Continuation { c }

    var a: Continuation = Continuation { b }

    var continuation: Continuation = a

    // the logic in loop1 is pretty much the same, but the continuations are much nicer to work with and compose
    var state: Int = 0
    fun loop1() {
        continuation = continuation.apply()

        when (state) {
            0 ->                // do something
                state++

            1 ->                // do something
                state++

            2 ->                // do something
                state++

            else -> {}
        }
    }

    // we could also have Continuations that return themselves for a bit,
    // and have associated state (fields)
    var wait3loops: Continuation = object : Continuation {
        private var count = 0
        override fun apply(): Continuation {
            if (count >= 3) {
                // reset
                count = 0
                return Continuation.Companion.halt
            } else {
                count++
                // a simple loop!
                return this
            }
        }
    }

    // by constructing an anonymous class, we can write some more complex states

    // however, there are some issues:
    // 1. when we declare our continuations, we need to declare them in reverse order
    // 2. there isn't a good easy way to reuse and combine them

    // this is because continuations can only be built when they know the whole future that they need to run
    // we can work around this weakness by writing a builder stage, that leaves a 'hole' in the continuation,
    // to be filled later with the 'future' or next continuation to run.

    fun interface Closure {
        fun build(k: Continuation): Continuation

        // by default, we can build it with halt
        fun build() = build(Continuation.halt)

        companion object {
            val identity = Closure { k -> k }
        }
    }

    // now we can write wait3 that can be joined together!

    val wait3 = Closure { k ->
        object : Continuation {
            private var count = 0
            override fun apply(): Continuation {
                if (count >= 3) {
                    // reset
                    count = 0
                    // instead of returning Continuation.halt, we return k
                    return k
                } else {
                    count++
                    // a simple loop!
                    return this
                }
            }
        }
    }

    // we can also write some useful combiners, like sequence!
    fun sequence(vararg closures: Closure): Closure {
        // we get no closures, then we can return the identity closure
        if (closures.isEmpty()) return Closure.Companion.identity
        // if there is only one closure, we can just return it
        else if (closures.size == 1) return closures[0]
        // this goes through the list in reverse, and provides each closure the current final result as
        // the next closure's future.
        return Closure { k ->
            // store the result
            var res = k
            for (i in closures.size downTo 1) {
                res = closures[i - 1].build(res)
            }
            res
        }
    }

    // its easy to see that we can apply optimisations to our code with Closures,
    // as it gives us a chance to analyse the code before running it

    // and we can also reimplement exec
    fun exec(f: Runnable): Closure {
        return Closure { k ->
            Continuation {
                f.run()
                k
            }
        }
    }

    // these are two simple Closure's that are implemented in Mercurial already
    // but its easy to see how Closures make it easy to combine and compose Continuations
    // and each other

    // this is essentially how Continuations and Closures work in Mercurial, but Mercurial also
    // has the ability to run Continuations in parallel, which we haven't explored here.
}