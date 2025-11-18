package org.firstinspires.ftc.teamcode.beginners

import dev.frozenmilk.dairy.mercurial.continuations.Closure
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.ifHuh
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.parallel
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.scope
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.wait
import dev.frozenmilk.dairy.mercurial.continuations.registers.VarRegister

class Registers {
    init {
        // so far, our Continuations have been pretty boring

        // we can get way more interesting with `scope`
        val countTo20 = scope {
            var count by variable { 0 }
            loop(
                { count < 20 },
                exec {
                    count++
                },
            )
        }
        // `count` is data that is now available only in the scope of the scope block
        // the `by` keyword in kotlin is for delegation
        // this basically means that when we get or set count,
        // it runs a couple of functions behind the scenes to do so

        // every time that we run countTo20, the count is reset
        sequence(
            countTo20,
            countTo20,
            countTo20,
        )

        // and if we run it in parallel, each separate process has its own count
        parallel(
            countTo20,
            countTo20,
            countTo20,
        )

        // in comparison, if we used a regular variable:
        val countTo20Again = scope {
            var count = 0
            loop(
                { count < 20 },
                exec {
                    count++
                },
            )
        }

        // this would only count to 20 once
        sequence(
            countTo20Again,
            countTo20Again,
            countTo20Again,
        )

        // and here, all three copies would be using the same value together
        // so they'd count to 20 really fast, but all at once
        parallel(
            countTo20Again,
            countTo20Again,
            countTo20Again,
        )

        // registers are really powerful, and are a pretty easy to use tool to make
        // sure that we can safely use data across a lexical scope

        // theres nothing stopping you from using global scope when you need to
        // and when its appropriate
        // but mastering registers will make it really easy to write complex Mercurial code

        // we can use registers to branch
        scope {
            var count by variable { 0 }
            loop(
                { count < 20 },
                ifHuh(
                    { count < 10 },
                    exec { count += 2 },
                ).elseHuh(
                    exec { count++ },
                ),
            )
        }

        var globalCount = 0
        scope {
            // we can also have value registers
            // they're a little more niche
            val count by value { globalCount }
            var countPlus1 by variable { count + 1 }

            // registers are shared across parallel processes as well
            parallel(
                exec { globalCount++ },
                sequence(
                    wait(1.0),
                    exec { globalCount++ },
                ),
                sequence(
                    wait(1.0),
                    exec {
                        println(count)
                        println(countPlus1++)
                    },
                ),
            )
        }

        // theres plenty more you can do with registers, but these are the basics
        // you can also construct registers, then bind to them:

        scope {
            val countRegister = variable { 0 }

            // see the function below
            val increaseCount = increaseCount(countRegister)

            sequence(
                increaseCount,
                increaseCount,
                increaseCount,
            )
        }

        // value registers all implement Supplier
        // and variable registers also implement Consumer
        // which means they can easily be passed as functions to things as well

        // now, onto MyFirstMercurialTeleOp
    }

    // note that we didn't use scope here
    fun increaseCount(countRegister: VarRegister<Int>): Closure {
        var count by countRegister
        return exec { count++ }
    }
}