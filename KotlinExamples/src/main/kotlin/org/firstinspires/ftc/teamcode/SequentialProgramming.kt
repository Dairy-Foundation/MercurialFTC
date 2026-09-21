package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import dev.frozenmilk.dairy.mercurial.Mercurial
import dev.frozenmilk.dairy.mercurial.continuations.Cases
import dev.frozenmilk.dairy.mercurial.continuations.Command
import dev.frozenmilk.dairy.mercurial.continuations.Continuation
import dev.frozenmilk.dairy.mercurial.continuations.Continuation.Builder
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.cases
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.command
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.duration
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.expression
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.function
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.ifThen
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.match
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.noop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.panic
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.repeat
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.seconds
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.unreachable
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.value
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.waitFor
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.waitUntil
import dev.frozenmilk.dairy.mercurial.continuations.Coroutines
import dev.frozenmilk.dairy.mercurial.continuations.IfThen
import dev.frozenmilk.dairy.mercurial.continuations.Parameter
import dev.frozenmilk.dairy.mercurial.continuations.Return
import dev.frozenmilk.dairy.mercurial.environments.Reference
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class SequentialProgramming {
    // Builders and Continuations
    init {
        // program is a Builder (noop comes with Mercurial)
        val program: Builder<*> = noop
        // Builders are programs that are under construction
        // Builders can be composed with other builders in order to assemble a larger program

        // This document will teach you how to build large sequential programs using Builders
        // A sequential program is one that runs in order and does the steps in the order you wrote them
        // This like regular programming in Java or Kotlin

        // Once you are done building up your program, you can compile it to a Continuation:
        val compiledProgram: Continuation = program.compile()
        // Mercurial only knows how to run Continuations, not Builders.
        // However, you normally won't compile programs by hand

        // The compilation phase allows Mercurial to optimise and link your programs together.
        // By the end of this document you will be able to see why its important that
        // Mercurial has a separate compilation phase.
    }

    // noop, exec and sequences
    init {
        // First lets look at how to write a builder that runs some code:
        val program: Builder<*> = exec {
            // write some code here!
        }
        // exec runs some code in one step.
        // in general, each exec should be ~1-10 lines of code.
        // and should not contain anything blocking, like Thread.sleep(),
        // as this would hold up the rest of the processes from running.

        // if we want to join multiple programs together we can use sequence:
        val program2: Builder<*> = sequence(
            exec { println("a") },
            exec { println("b") },
            exec { println("c") },
        )

        // exec and sequence are what we call meta functions.
        // meta functions are used when you are building your program,
        // and work with builders, composing them, or making them for you.
        // you should be careful not to run any meta functions inside of your built programs:
        val _ = exec { exec { } }
        // as meta functions can be expensive, and the program built inside will not run.
        // Mercurial has ways write much more complex programs
        // that eliminate the need for you to do this.
        // Android Studio should give you a warning
        // if you have built a program that will not be run (as shown above)
        // this system is not perfect,
        // and so you will need to consider if the code you have written is correct

        // sequence actually just runs the .then function under the hood:
        val program3: Builder<*> =
            exec { println("a") }
                .then(exec { println("b") })
                // .thenExec is a special version of .then, that makes an exec for you
                .thenExec { println("c") }

        // program2 is equivalent to program3,
        // and compiling either of them would produce the same output continuation

        // finally, noop is a builtin builder that does nothing
        val program4 = noop.thenExec { println("a") }
        // when you compile your program the noop is removed,
        // so you never need to worry about it

        // sometimes it is useful to specify that you want to do nothing, which we will see later

        // that is all for running code directly
        // from now on, we will call exec a statement
        // this is because exec doesn't return anything
        // it can only edit variables, or set motor powers
    }

    // values
    init {
        // So far all of our Builders have been statements
        // value.* is a group of builders that are expressions
        // they return a value
        val s: Builder<String> = value.o { "Hello, World" }
        // Builder is generic
        // here, s is Builder<String>, because it returns a String

        // Mercurial has special support for 4 primitive types:
        // value.o is generic, and can return any object type
        val _: Builder<Any> = value.o { Any() }
        // value.d returns a double
        val _: Builder<Double> = value.d { 0.0 }
        // value.i returns an int
        val _: Builder<Int> = value.i { 0 }
        // value.b returns a boolean
        val _: Builder<Boolean> = value.b { true }
        // You will see this pattern (o, d, i, b) come up quite a bit in Mercurial.

        // The primitive versions of value are important, as they are more optimised
        // In Java / Kotlin, there is an object version of each primitive type, and a primitive
        // value.[d,i,b] will avoid converting back and forth
        // between the object types and the primitive types

        // We advise that you prefer them over value.o when appropriate
        // If you do end up needing the object version or visa-versa,
        // Mercurial will handle the conversion for you automatically

        // As you can see, value.d and value.o are both Builders,
        // there is no special type for a builder that returns a primitive value.

        // .then will discard the return value of the first builder,
        // and instead replace it with the return value of the second builder
        val program2: Builder<Double> = exec { println("a") }
            .then(value.d { 0.0 })

        // this makes .then more useful than sequence when working with return values
        // as it can get the return value types correct
        // which sequence cannot do

        // you can use map, mapD, mapI, or mapB to map the previously returned value
        val program3: Builder<String> = value.o { "Hello, " }.map { str ->
            str + "World"
        }

        // mapD, mapI, and mabB are each optimised to return a double, int, or boolean primitive
        // however, all 4 map functions will convert the previously returned value to an object first

        // this is because functions that took the
        // primitive would not be sound, and could cause runtime crashes

        // values will come in use more in the following sections
    }

    // if then else
    init {
        // we often need to make decisions to run different bits of code at runtime
        // this is broadly known as branching

        // Mercurial 2 supports if/else as a method of declaring branching

        // you can declare a complete if / else check using the ifThen function
        val program: Builder<String> = ifThen(
            { true },
            value.o { "True" },
            value.o { "False" },
        )

        // you can provide a Builder<Boolean> instead of a
        // boolean supplier for the condition
        // the supplier is converted to a value.b under the hood
        val program2 = ifThen(
            exec { println("a") }.then(value.b { true }),
            value.o { "True" },
            value.o { "False" },
        )

        // you can also declare a partial if / else using the ifThen function
        val program3: IfThen<String> = ifThen(
            { true },
            value.o { "True" },
        )

        // partial if / else s can chain several if else calls together
        val program4 = program3
            .elseIfThen({ false }, value.o { "Something else" })
            .elseThen(
                value.o { "False" },
            )

        // you do not need to complete the chain
        // an incomplete if / else chain is a statement
        // while a complete if / else chain is an expression, as it will always return a value
        val program5: Builder<*> = ifThen({ true }, value.b { true })

        // hopefully its becoming clear that Mercurial is flexible and easy to work with
        // and good at optimising primitives when needed
        // notice that we can use a mix of value.o and value.b!
        val program6 = ifThen(
            ifThen({ true }, value.b { true }, value.o { false }),
            value.o { true },
            value.b { false },
        )

        // lets move on to another way to branch
    }

    // match with cases
    init {
        // match with cases is a more complex pattern matching construct
        // it provides a different way to branch than if / else
        val program1: Builder<Any?> = match(
            { DcMotor.RunMode.RUN_TO_POSITION },
            // note that you need to specify the type that is being matched
            // and the type that will be returned
            cases<DcMotor.RunMode, String>()
                .constCase(DcMotor.RunMode.RUN_TO_POSITION, value.o { "RTP" })
        )

        // just like in if / else, if our cases are exhaustive,
        // then match with cases is an expression
        // otherwise its a statement
        val program2: Builder<String> = match(
            { DcMotor.RunMode.RUN_TO_POSITION },
            cases<DcMotor.RunMode, String>()
                .constCase(DcMotor.RunMode.RUN_TO_POSITION, value.o { "RTP" })
                // you can use default to make the cases exhaustive
                .defaultCase(value.o { "UNKNOWN" })
        )

        // its possible to build the cases, then later match them with a value:
        val cases1 = cases<DcMotor.RunMode, String>()
            .constCase(DcMotor.RunMode.RUN_TO_POSITION, value.o { "RTP" })
            // assert exhaustive will cause a crash at runtime
            // if you haven't handled the case
            .assertExhaustive()

        // this program will fail, as I haven't handled this case
        val program3: Builder<String> =
            value.o { DcMotor.RunMode.RESET_ENCODERS }.match(cases1)

        // program 4 is equivalent to program 3
        val program4: Builder<String> = match(
            value.o { DcMotor.RunMode.RESET_ENCODERS },
            cases1,
        )

        val program5 = match(
            // just like if / else, you can use a builder
            // or a supplier function for the value
            ifThen(
                { true },
                value.o { DcMotor.RunMode.RESET_ENCODERS },
                value.o { DcMotor.RunMode.STOP_AND_RESET_ENCODER },
            ),
            cases1,
        )

        // we will check state in the following cases
        var state: String? = ""

        // so far we've only matched consts and defaults
        // lets look at all the ways cases can match
        val cases2 = cases<Any?, String>()
            // you can add a guard to match consts
            .constCase(null, { state === null }, value.o { "Null" })
            // the guards will be checked in the same order you give them
            .constCase(null, { state !== null }, value.o { "Null" })
            // once you add an unguarded case,
            // we consider it matched exhaustively
            // so you can't add anymore matches for 'null'
            .constCase(null, value.o { "Null" })
            // you can also get the value in the guard,
            // allowing to write more reusable guard functions
            // but this is likely not that useful for consts
            .constCase("?", { str -> str == "?" }, value.o { "?" })
            // lets move on to more complex way to match
            // type will match against a class specifically
            .typeCase<Any>(value.o { "Any" })
            // just like with constCase we can use a guard
            .typeCase<String>({ str -> str == "?" }, value.o { "?" })
            // these two cases are equivalent to the above case
            // if you prefer to specify the class like this, that is ok too
            .typeCase(String::class, { str -> str == "?" }, value.o { "?" })
            .typeCase(String::class.java, { str -> str == "?" }, value.o { "?" })
            // type is specific, and doesn't respect inheritance,
            // so you should never match on an abstract class or interface
            // if you need to respect inheritance use default with guards
            .defaultCase({ anything -> anything is List<*> }, value.o { "list" })
            // you can use the default guards to check anything,
            // it is a lot like if / else
            .defaultCase({ anything -> anything == true }, value.o { "true" })
            // guards are a bit more limited than if / else,
            // you cannot run an arbitrary builder as a guard

            // once you specify a final default value
            // your cases are complete and exhaustive
            .defaultCase(value.o { "no idea!" })

        // we will come back to cases once we have looked at variables and functions
        // as there are alternate forms for matching where we pass the matched value to a function
        // however, you need to learn some more about functions first
    }

    // panic
    init {
        // we will quickly look at some slightly special meta functions

        // each of these programs will crash
        val program1: Builder<Nothing> = panic("Something went wrong!")
        val program2: Builder<Nothing> = panic { "A more dynamic error message!" }
        // unreachable is a common version of panic
        val program3: Builder<Nothing> = unreachable

        // the Nothing type is part of kotlin,
        // and tells you that each of these won't return a value

        // you can use unreachable or panics to throw an exception
        // case's .assertExhaustive uses unreachable to say
        // that all other cases should be unreachable
    }

    // expression
    init {
        // so far all our programs have been very simple.
        // in order to write more complex programs,
        // we need variables

        // expression is like the body of a function,
        // we can create variables, read them, and write to them
        // and those variables are removed after the expression finishes running

        val program1: Builder<Double> = expression {
            // we can introduce variables in an expression
            // the value in the supplier is the initial value of the variable
            val _: Reference.O<String> = o { "" }
            val _: Reference.D = d { 0.0 }
            val _: Reference.I = i { 0 }
            val _: Reference.B = b { true }
            // Reference.[O,D,I,B] are like value.*
            // the primitive References are optimised
            // a Reference is like the address of the variable at runtime
            // each time we run program1, it reads and writes to a different place
            // this way, even if 500 copies of program1 are running at the same time
            // each one sees its own independent copy of the variables

            val x = d { 5.0 }
            val y = d { 3.0 }
            // the final builder in an expression is returned
            value.d {
                // we can invoke x and y to read the data contained within
                // like a function
                x() + y()
            }
        }

        val program2: Builder<Double> = expression {
            // in kotlin you can use delegation instead
            // here, x and y look like normal variables
            // and kotlin will automatically run the function
            // when you need the value
            var x by d { 5.0 }
            // y is a val, so we can't change it later
            val y by d { 3.0 }

            // we can run statements in the body of our expression like this:
            // x is a var, so we can reassign it
            !exec { x = 20.0 }
            // see the `!` at the front, it tells the expression to run it

            // so we could make a reusable builder in the body
            // this line doesn't actually run result, just makes a builder for it
            val result = value.d { x + y }
            // and then run it multiple times
            !result
            result
        }

        val program3: Builder<Double> = expression {
            // you can also pass a builder to make a reference
            // this stores the result of running that program
            // as the initial value of the reference
            val x = d(
                ifThen(
                    { true },
                    value.d { 5.0 },
                    value.d { 0.0 },
                )
            )
            val y = d { 3.0 }

            // note that because we aren't using delegation,
            // we set y by invoking it, like a function
            !exec { y(30.0) }

            value.d { x() + y() }
        }

        // expression also gets itself as a parameter
        // you can use it to write recursive functions
        // this program prints out "Hello, World" forever
        // so we need to specify its return type
        // nothing is a good choice, as it will never return
        val program4: Builder<Nothing> = expression { self ->
            val x = o { "Hello, " }

            // one advantage of not using delegation is that references are suppliers
            // so you can use them directly very easily

            // set y to x
            val y = o(x)

            // set y to "World"
            !exec { y("World") }
            // print out "Hello, World"
            !exec { println(x() + y()) }

            // call ourself again
            self
        }

        // A quick note on tail call optimisation:
        // Mercurial implements tail call optimisation
        // and uses a very cheap call stack regardless
        // this means that program4 won't use all the memory on the computer
        // and will run in an efficient loop
        // recursion is the only way to loop in Mercurial
        // (there are loop meta functions, but they use recursion under the hood)

        val program5 = expression {
            val x = d { 0.0 }

            // expressions constructed inside of other expressions capture variables
            val inner = expression {
                // set y to the square of x
                val y = d { x() * x() }
                // set x to y
                exec { x(y()) }
            }

            // this makes it easy to construct and re-use complex programs

            // run inner 4 times
            !inner
            !inner
            !inner
            !inner

            // return x
            value.d(x)
        }

        // now that you've seen expression,
        // lets look at its more powerful sibling, the function
    }

    // functions
    init {
        // Mercurial can construct functions with any number of arguments
        // functions are available under Continuations.function.*
        // the naming scheme is very simple
        // function.O has a single object parameter
        // function.D has a single double parameter
        // function.I has a single int parameter
        // function.B has a single boolean parameter
        // function.ODO has an object parameter, then a double parameter, then an object parameter
        // Mercurial comes with functions up to length 3 preconstructed

        // just like expressions, functions get themselves as a parameter for recursion
        // you can discard it if you're not using it
        // additionally, function (with nothing after the dot) is bound to be
        // function.O, function.OO, and function.OOO, as a nicer shorthand
        val f1: Parameter.O<String, Parameter.O<String, Return<Any?>>> =
            function { _, a, b ->
                exec {
                    println(a() + ", " + b())
                }
            }
        // notice the complicated type of f1
        // this is the type of functions under the hood
        // in general, you probably don't want to write this all out
        // in kotlin, you can use typealias,
        // and generally avoid writing types to help you out here

        // in order to run f1, you need to bind parameters, using the * operator
        val program1: Builder<*> = f1 * { "Hello" } * { "World" }
        // you can bind either a supplier, or a builder to the parameters

        val f2 = function.DD { _, x, y ->
            // function bodies work the same as expression!
            !exec { println("multiplying ${x()} by ${y()}") }

            // you can run statements,
            // make new variables,
            // construct more functions that capture the current function,
            // and so on and so forth

            value.d { x() * y() }
        }

        val _ = expression {
            // you can bind either suppliers or builders
            // and use functions expressively in other builders
            val z = d(f2 * { 10.0 } * value.d { 1000.0 })
            value.d(z)
        }

        val f3 = function.DD { _, x, y ->
            // if you want to use delegation,
            // you will need to write something like this:
            val x by x
            val y by y

            // which can be a bit annoying,
            // but there is no way around it

            value.d { x * y }
        }

        // heres a recursive implementation of the Fibonacci function
        val f4 = function.III { self, a, b, n ->
            ifThen(
                { n() <= 0 },
                value.i(b),
                self * { b() } * { a() + b() } * { n() - 1 },
            )
        }
        // this will return the 5th Fibonacci number
        val fibn5: Builder<Int> = f4 * { 0 } * { 1 } * { 5 }
        // you do not need to use recursion to write complex maths like this
        // but it is a good demonstration of recursion

        // important notes:
        // we said Mercurial allows arbitrary parameter functions
        // but we will not look at how you make your own longer functions right now
        // the syntax is complex and ugly, and in general you won't need it

        // functions don't really support partial application
        // the arguments are evaluated right before the function is called
        // which means if you do partial application,
        // you may pass arguments you weren't expecting,
        // because you bound them in one state, but they weren't evaluated until later

        // at the moment, there is no support for returning functions from other functions
        // however it is very much possible to add this feature

        // function recursion is very much supported,
        // including co-recursive functions
        // so feel free to go bananas with them

        // we will quickly revisit match with cases

        val cases = cases<Any?, Any?>()
            // for type and default, you can pass a function instead of a normal builder
            // the value that is matched against will be passed to the function
            .typeCase<String>(function { _, str ->
                exec { println(str()) }
            })
            // you can also pass the function body in place
            .typeCase<String> { str ->
                exec { println(str()) }
            }
        // this can also be done with guards of course
        // and can make match with cases much more powerful
    }

    // extras
    init {
        // you've seen all the basic building blocks
        // but Mercurial comes with some other stuff already built for you

        // loops

        // loop can take a boolean supplier
        val program1 = loop(
            { true },
            exec {  },
        )
        // or a Builder<Boolean>
        val program2 = loop(
            exec { }.then(value.b { false }),
            exec {  },
        )
        // or nothing
        val program3 = loop(exec {})

        // the first two forms are like a while loop
        // while the third one loops forever, and never ends

        // repeat repeats something several times at compile time
        // you might not use it that much
        // you must repeat at least once, so that repeat can return the last result
        val program4 = repeat(3, exec {})

        // waiting

        // wait until can take a boolean supplier
        val program5 = waitUntil { true }
        // or a Builder<Boolean>
        val program6 = waitUntil(
            waitUntil { true }
                .then(value.b { false })
        )
        // it is useful for waiting for something to happen

        // sometimes you want to wait for an amount of time
        // which is a little annoying to do with wait until

        // wait for waits for an amount of time
        // wait for is a function!
        // so we use the * syntax
        // Mercurial gives you some helper functions to make it easy to
        // specify different amounts of time
        // in kotlin, we recommend using the duration function, and specifying a duration
        val program7 = waitFor * duration { 1.seconds }
        // if the time is fixed, you should prefer the constant api
        val program8 = waitFor * duration(1.seconds)

        // if you don't want to use kotlin's duration apis,
        // you can use the seconds function instead, which do the same, but only handle seconds
        val program9 = waitFor * seconds { 1.0 }
        val program10 = waitFor * seconds(1.0)

        // these helper functions make it easy to return durations from complex programs
        // and you will use them with other time related apis in Mercurial
        val program11 = waitFor *
                waitUntil { false }
                    .then(value.o(duration { 100.milliseconds }))

        // side note, the type waitFor expects is actually a TimeMark
        // duration and seconds will take a time mark from that far in the future
        // when they're evaluated

        // command provides a command builder,
        // like Mercurial 1's LambdaCommand, or Ivy' Command.build()
        // if you have not used command based
        // I recommend not bothering with learning about this
        val cmd: Command = command
            .init((waitFor * duration(10.seconds)).thenExec { println("Hello, World") })
            // each part of the command can be a runnable or complete builder
            .exec { println("i guess bro") }
            // finished can be a boolean supplier or a boolean builder
            .finished { true }
            // if you specify a part again,
            // it overwrites the previous one
            .finished(value.b { false })
            // the parts of a command are init, exec, finished, end
            .end { println("67") }

        // there is no support for requirements, or other more complex command tooling
        // the produced command is still a builder,
        // so you can use it alongside everything else you have learnt

        // kotlin also supports coroutines
        // you can use the yield function to pause the coroutine
        // at the moment, nothing else is supported for coroutines
        val program12: Builder<String> = Coroutines.builder {
            val inTheFuture = Mercurial.timeSource.markNow() + 10.seconds
            while (inTheFuture.hasNotPassedNow()) yield()
            "Done!"
        }

        // lastly,
        // something you can't see is that Mercurial does re-tracing of stack traces
        // when your program crashes Mercurial will stitch the stack trace back to where you made
        // the program, making it clear where it actually went wrong

        // I am sure re-tracing isn't perfect, and I am looking for feedback on the system,
        // especially around dealing with Mercurial's own callstack
    }

    // now you know all about sequential programming with Mercurial
    // hopefully it was fairly easy to wrap your head around
    // it is time to learn about Mercurial's concept of a running program, the fiber
    // head on over to fibers.md, in the root of the repository
}
