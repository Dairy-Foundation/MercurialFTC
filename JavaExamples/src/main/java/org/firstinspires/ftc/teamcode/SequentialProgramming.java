package org.firstinspires.ftc.teamcode;

import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.*;

import com.qualcomm.robotcore.hardware.DcMotor;

import java.util.List;

import dev.frozenmilk.dairy.mercurial.continuations.Cases;
import dev.frozenmilk.dairy.mercurial.continuations.Command;
import dev.frozenmilk.dairy.mercurial.continuations.Continuation;
import dev.frozenmilk.dairy.mercurial.continuations.Continuation.Builder;
import dev.frozenmilk.dairy.mercurial.continuations.Continuations;
import dev.frozenmilk.dairy.mercurial.continuations.Expression;
import dev.frozenmilk.dairy.mercurial.continuations.IfThen;
import dev.frozenmilk.dairy.mercurial.continuations.Parameter;
import dev.frozenmilk.dairy.mercurial.continuations.Return;
import dev.frozenmilk.dairy.mercurial.environments.Reference;

@SuppressWarnings("unused")
public class SequentialProgramming {
    // Builders and Continuations
    {
        // program is a Builder (noop comes with Mercurial)
        Builder<?> program = noop;
        // Builders are programs that are under construction
        // Builders can be composed with other builders in order to assemble a larger program

        // This document will teach you how to build large sequential programs using Builders
        // A sequential program is one that runs in order and does the steps in the order you wrote them
        // This like regular programming in Java or Kotlin

        // Once you are done building up your program, you can compile it to a Continuation:
        Continuation compiledProgram = program.compile();
        // Mercurial only knows how to run Continuations, not Builders.
        // However, you normally won't compile programs by hand

        // The compilation phase allows Mercurial to optimise and link your programs together.
        // By the end of this document you will be able to see why its important that
        // Mercurial has a separate compilation phase.
    }

    // noop, exec and sequences
    {
        // First lets look at how to write a builder that runs some code:
        Builder<?> program = exec(() -> {
            // write some code here!
        });
        // exec runs some code in one step.
        // in general, each exec should be ~1-10 lines of code.
        // and should not contain anything blocking, like Thread.sleep(),
        // as this would hold up the rest of the processes from running.

        // if we want to join multiple programs together we can use sequence:
        Builder<?> program2 = sequence(
            exec(() -> println("a")),
            exec(() -> println("b")),
            exec(() -> println("c"))
        );

        // exec and sequence are what we call meta functions.
        // meta functions are used when you are building your program,
        // and work with builders, composing them, or making them for you.
        // you should be careful not to run any meta functions inside of your built programs:
        Builder<?> _a = exec(() -> exec(() -> {
        }));
        // as meta functions can be expensive, and the program built inside will not run.
        // Mercurial has ways write much more complex programs
        // that eliminate the need for you to do this.
        // Android Studio should give you a warning
        // if you have built a program that will not be run (as shown above)
        // this system is not perfect,
        // and so you will need to consider if the code you have written is correct

        // sequence actually just runs the .then function under the hood:
        Builder<?> program3 =
            exec(() -> println("a"))
                .then(exec(() -> println("b")))
                // .thenExec is a special version of .then, that makes an exec for you
                .thenExec(() -> println("c"));

        // program2 is equivalent to program3,
        // and compiling either of them would produce the same output continuation

        // finally, noop is a builtin builder that does nothing
        Builder<?> program4 = noop.thenExec(() -> println("a"));
        // when you compile your program the noop is removed,
        // so you never need to worry about it

        // sometimes it is useful to specify that you want to do nothing, which we will see later

        // that is all for running code directly
        // from now on, we will call exec a statement
        // this is because exec doesn't return anything
        // it can only edit variables, or set motor powers
    }

    // values
    {
        // So far all of our Builders have been statements
        // value.* is a group of builders that are expressions
        // they return a value
        Builder<String> s = value.o(() -> "Hello, World");
        // Builder is generic
        // here, s is Builder<String>, because it returns a String

        // Mercurial has special support for 4 primitive types:
        // value.o is generic, and can return any object type
        Builder<Object> _a = value.o(() -> new Object());
        // value.d returns a double
        Builder<Double> _b = value.d(() -> 0.0);
        // value.i returns an integer
        Builder<Integer> _c = value.i(() -> 0);
        // value.b returns a boolean
        Builder<Boolean> _d = value.b(() -> true);
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
        Builder<Double> program2 = exec(() -> println("a"))
            .then(value.d(() -> 0.0));

        // this makes .then more useful than sequence when working with return values
        // as it can get the return value types correct
        // which sequence cannot do

        // you can use map, mapD, mapI, or mapB to map the previously returned value
        Builder<String> program3 = value.o(() -> "Hello, ") //
            .map(str -> str + "World");

        // mapD, mapI, and mabB are each optimised to return a double, int, or boolean primitive
        // however, all 4 map functions will convert the previously returned value to an object first

        // this is because functions that took the
        // primitive would not be sound, and could cause runtime crashes

        // values will come in use more in the following sections
    }

    // if then else
    {
        // we often need to make decisions to run different bits of code at runtime
        // this is broadly known as branching

        // Mercurial 2 supports if/else as a method of declaring branching

        // you can declare a complete if / else check using the ifThen function
        Builder<String> program = ifThen(
            () -> true,
            value.o(() -> "True"),
            value.o(() -> "False")
        );

        // you can provide a Builder<Boolean> instead of a
        // boolean supplier for the condition
        // the supplier is converted to a value.b under the hood
        Builder<String> program2 = ifThen(
            exec(() -> println("a")).then(value.b(() -> true)),
            value.o(() -> "True"),
            value.o(() -> "False")
        );

        // you can also declare a partial if / else using the ifThen function
        IfThen<String> program3 = ifThen(
            () -> true,
            value.o(() -> "True")
        );

        // partial if / else s can chain several if else calls together
        Builder<String> program4 = program3
            .elseIfThen(() -> false, value.o(() -> "Something else"))
            .elseThen(value.o(() -> "False"));

        // you do not need to complete the chain
        // an incomplete if / else chain is a statement
        // while a complete if / else chain is an expression, as it will always return a value
        Builder<?> program5 = ifThen(() -> true, value.b(() -> true));

        // hopefully its becoming clear that Mercurial is flexible and easy to work with
        // and good at optimising primitives when needed
        // notice that we can use a mix of value.o and value.b!
        Builder<Boolean> program6 = ifThen(
            ifThen(() -> true, value.b(() -> true), value.o(() -> false)),
            value.o(() -> true),
            value.b(() -> false)
        );

        // lets move on to another way to branch
    }

    // match with cases
    {
        // match with cases is a more complex pattern matching construct
        // it provides a different way to branch than if / else
        Builder<?> program1 = match(
            () -> DcMotor.RunMode.RUN_TO_POSITION,
            cases()
                .constCase(DcMotor.RunMode.RUN_TO_POSITION, value.o(() -> "RTP"))
        );

        // just like in if / else, if our cases are exhaustive,
        // then match with cases is an expression
        // otherwise its a statement
        Builder<String> program2 = match(
            () -> DcMotor.RunMode.RUN_TO_POSITION,
            // note that you need to specify the type that is being matched
            // and the type that will be returned
            Continuations.<DcMotor.RunMode, String>cases()
                .constCase(DcMotor.RunMode.RUN_TO_POSITION, value.o(() -> "RTP"))
                // you can use default to make the cases exhaustive
                .defaultCase(value.o(() -> "UNKNOWN"))
        );

        // its possible to build the cases, then later match them with a value:
        Cases.Exhaustive<DcMotor.RunMode, String> cases1 =
            Continuations.<DcMotor.RunMode, String>cases()
                .constCase(DcMotor.RunMode.RUN_TO_POSITION, value.o(() -> "RTP"))
                // assert exhaustive will cause a crash at runtime
                // if you haven't handled the case
                .assertExhaustive();

        // this program will fail, as I haven't handled this case
        Builder<String> program3 =
            value.o(() -> DcMotor.RunMode.RESET_ENCODERS).match(cases1);

        // program 4 is equivalent to program 3
        Builder<String> program4 = match(
            value.o(() -> DcMotor.RunMode.RESET_ENCODERS),
            cases1
        );

        Builder<String> program5 = match(
            // just like if / else, you can use a builder
            // or a supplier function for the value
            ifThen(
                () -> true,
                value.o(() -> DcMotor.RunMode.RESET_ENCODERS),
                value.o(() -> DcMotor.RunMode.STOP_AND_RESET_ENCODER)
            ),
            cases1
        );

        // we will check state in the following cases
        String state = "";

        // so far we've only matched constantss and defaults
        // lets look at all the ways cases can match
        Cases<Object, String> cases2 = Continuations.<Object, String>cases()
            // you can add a guard to match constants
            .constCase(null, n -> state == null, value.o(() -> "Null"))
            // the guards will be checked in the same order you give them
            .constCase(null, n -> state != null, value.o(() -> "Null"))
            // once you add an unguarded case,
            // we consider it matched exhaustively
            // so you can't add anymore matches for 'null'
            .constCase(null, value.o(() -> "Null"))
            // you can also get the value in the guard,
            // allowing to write more reusable guard functions
            // but this is likely not that useful for constants
            .constCase("?", str -> str.equals("?"), value.o(() -> "?"))
            // lets move on to more complex way to match
            // type will match against a class specifically
            .typeCase(Object.class, value.o(() -> "Any"))
            // just like with constCase we can use a guard
            .typeCase(String.class, str -> str.equals("?"), value.o(() -> "?"))
            // typeCase is specific, and doesn't respect inheritance,
            // so you should never match on an abstract class or interface
            // if you need to respect inheritance use default with guards
            .defaultCase(anything -> anything instanceof List<?>, value.o(() -> "list"))
            // you can use the default guards to check anything,
            // it is a lot like if / else
            .defaultCase(anything -> anything.equals(true), value.o(() -> "true"))
            // guards are a bit more limited than if / else,
            // you cannot run an arbitrary builder as a guard

            // once you specify a final default value
            // your cases are complete and exhaustive
            .defaultCase(value.o(() -> "no idea!"));

        // we will come back to cases once we have looked at variables and functions
        // as there are alternate forms for matching where we pass the matched value to a function
        // however, you need to learn some more about functions first
    }

    // panic
    {
        // we will quickly look at some slightly special meta functions

        // each of these programs will crash
        Builder<?> program1 = panic("Something went wrong!");
        Builder<?> program2 = panic(() -> "A more dynamic error message!");
        // unreachable is a common version of panic
        Builder<?> program3 = unreachable;

        // you can use unreachable or panics to throw an exception
        // case's .assertExhaustive uses unreachable to say
        // that all other cases should be unreachable
    }

    // expression
    {
        // so far all our programs have been very simple.
        // in order to write more complex programs,
        // we need variables

        // expression is like the body of a function,
        // we can create variables, read them, and write to them
        // and those variables are removed after the expression finishes running

        Builder<Double> program1 = expression((scope, _self) -> {
            // we can introduce variables in an expression, using the scope
            // the value in the supplier is the initial value of the variable
            Reference.O<String> _a = scope.o(() -> "");
            Reference.D _b = scope.d(() -> 0.0);
            Reference.I _c = scope.i(() -> 0);
            Reference.B _d = scope.b(() -> true);
            // Reference.[O,D,I,B] are like value.*
            // the primitive References are optimised
            // a Reference is like the address of the variable at runtime
            // each time we run program1, it reads and writes to a different place
            // this way, even if 500 copies of program1 are running at the same time
            // each one sees its own independent copy of the variables

            Reference.D x = scope.d(() -> 5.0);
            Reference.D y = scope.d(() -> 3.0);

            // you can also run builders in a scope without storing it in a result
            // scope.run just adds it as a step to run
            scope.run(exec(() -> println("Hello, World")));

            // the final builder in an expression is returned
            return value.d(() ->
                // we can call .get on x and y to read the data contained within
                x.get() + y.get()
            );
        });

        Builder<Double> program2 = expression((scope, _self) -> {
            // you can also pass a builder to make a reference
            // this stores the result of running that program
            // as the initial value of the reference
            Reference.D x = scope.d(
                ifThen(
                    () -> true,
                    value.d(() -> 5.0),
                    value.d(() -> 0.0)
                )
            );
            Reference.D y = scope.d(() -> 3.0);

            // we set variables by calling .set on them
            scope.run(exec(() -> y.set(30.0)));
            // scope.runExec is just like thenExec, a helpful shorthand
            scope.runExec(() -> x.set(14.0));

            return value.d(() -> x.get() + y.get());
        });

        // expression also gets itself as a parameter
        // you can use it to write recursive functions
        // this program prints out "Hello, World" forever
        Builder<?> program4 = expression((scope, self) -> {
            Reference.O<String> x = scope.o(() -> "Hello, ");

            // you can use references as suppliers
            // which makes them work well with meta functions
            // set y to x
            Reference.O<String> y = scope.o(x);

            // set y to "World"
            scope.runExec(() -> y.set("World"));
            // print out "Hello, World"
            scope.runExec(() -> println(x.get() + y.get()));

            // call ourself again
            return self;
        });

        // A quick note on tail call optimisation:
        // Mercurial implements tail call optimisation
        // and uses a very cheap call stack regardless
        // this means that program4 won't use all the memory on the computer
        // and will run in an efficient loop
        // recursion is the only way to loop in Mercurial
        // (there are loop meta functions, but they use recursion under the hood)

        Builder<Double> program5 = expression((scope, _self) -> {
            Reference.D x = scope.d(() -> 0.0);

            // expressions constructed inside of other expressions capture variables
            // in Java this can become ugly,
            // so you might prefer to use functions to de-nest inner expression
            // its very important that you only use the most inner scope
            // never the outer one
            Builder<?> inner = expression((innerScope, __self) -> {
                // set y to the square of x
                Reference.D y = innerScope.d(() -> x.get() * x.get());
                // set x to y
                return exec(() -> x.set(y.get()));
            });

            // this makes it easy to construct and re-use complex programs

            // run inner 4 times
            scope.run(inner);
            scope.run(inner);
            scope.run(inner);
            scope.run(inner);

            // return x
            return value.d(x);
        });

        // now that you've seen expression,
        // lets look at its more powerful sibling, the function
    }

    // functions
    {
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
        // additionally, function (with nothing after the dot) is bound to be
        // function.O, function.OO, and function.OOO, as a nicer shorthand
        Parameter.O<String, Parameter.O<String, Return<Object>>> f1 = function((scope, _self, a, b) ->
            exec(() -> println(a.get() + ", " + b.get()))
        );
        // notice the complicated type of f1
        // this is the type of functions under the hood

        // in order to run f1, you need to bind parameters, one by one
        Builder<?> program1 = f1.bind(() -> "Hello").bind(() -> "World");
        // you can bind either a supplier, or a builder to the parameters

        Parameter.D<Parameter.D<Return<Double>>> f2 = function.DD((scope, _self, x, y) -> {
            // function bodies work the same as expression!
            scope.runExec(() -> println("multiplying " + x.get() + " by " + y.get()));

            // you can run statements,
            // make new variables,
            // construct more functions that capture the current function,
            // and so on and so forth

            return value.d(() -> x.get() * y.get());
        });

        Builder<Double> _a = expression((scope, _self) -> {
            // you can bind either suppliers or builders
            // and use functions expressively in other builders
            Reference.D z = scope.d(f2.bind(() -> 10.0).bind(value.d(() -> 1000.0)));
            return value.d(z);
        });

        // heres a recursive implementation of the Fibonacci function
        Parameter.I<Parameter.I<Parameter.I<Return<Integer>>>> f3 = function.III((scope, self, a, b, n) ->
            ifThen(
                () -> n.get() <= 0,
                value.i(b),
                self.bind(b).bind(() -> a.get() + b.get()).bind(() -> n.get() - 1)
            ));
        // this will return the 5th Fibonacci number
        Builder<Integer> fibn5 = f3.bind(() -> 0).bind(() -> 1).bind(() -> 5);
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

        Cases.Inexhaustive<Object, Object> cases = Continuations.cases()
            // for type and default, you can pass a function instead of a normal builder
            // the value that is matched against will be passed to the function
            .typeCase(String.class, function((scope, _self, str) ->
                exec(() -> println(str.get()))
            ))
            // you can also pass the function body in place
            // unfortunately, this confuses java,
            // and you need to specify the types for the lambda
            .typeCase(
                String.class,
                (Expression<?> scope, Reference.O<String> str) -> exec(() -> println(str.get()))
            );
        // this can also be done with guards of course
        // and can make match with cases much more powerful
    }

    // extras
    {
        // you've seen all the basic building blocks
        // but Mercurial comes with some other stuff already built for you

        // loops

        // loop can take a boolean supplier
        Builder<Object> program1 = loop(
            () -> true,
            exec(() -> {
            })
        );
        // or a Builder<Boolean>
        Builder<Object> program2 = loop(
            exec(() -> {
            })
                .then(value.b(() -> false)),
            exec(() -> {
            })
        );
        // or nothing
        Builder<?> program3 = loop(exec(() -> {
        }));

        // the first two forms are like a while loop
        // while the third one loops forever, and never ends

        // repeat repeats something several times at compile time
        // you might not use it that much
        // you must repeat at least once, so that repeat can return the last result
        Builder<?> program4 = repeat(3, exec(() -> {
        }));

        // waiting

        // wait until can take a boolean supplier
        Builder<?> program5 = waitUntil(() -> true);
        // or a Builder<Boolean>
        Builder<?> program6 = waitUntil(
            waitUntil(() -> true)
                .then(value.b(() -> false))
        );
        // it is useful for waiting for something to happen

        // sometimes you want to wait for an amount of time
        // which is a little annoying to do with wait until

        // wait for waits for an amount of time
        // wait for is a function!
        // so you need to bind an argument to it
        // Mercurial gives you some helper functions to make it easy to
        // specify different amounts of time
        // in kotlin, we recommend using the duration function, and specifying a duration
        Builder<?> program7 = waitFor.bind(seconds(() -> 1.0));
        // if the time is fixed, you should prefer the constant api
        Builder<?> program8 = waitFor.bind(seconds(1));

        // these helper functions make it easy to return durations from complex programs
        // and you will use them with other time related apis in Mercurial
        Builder<?> program9 = waitFor.bind(
            waitUntil(() -> false).then(value.o(seconds(() -> 0.1)))
        );

        // side note, the type waitFor expects is actually a TimeMark
        // seconds will take a time mark from that far in the future
        // when it is evaluated

        // command provides a command builder,
        // like Mercurial 1's LambdaCommand, or Ivy' Command.build()
        // if you have not used command based
        // I recommend not bothering with learning about this
        Command cmd = command
            .init((waitFor.bind(seconds(10)).thenExec(() -> println("Hello, World"))))
            // each part of the command can be a runnable or complete builder
            .exec(() -> println("i guess bro"))
            // finished can be a boolean supplier or a boolean builder
            .finished(() -> true)
            // if you specify a part again,
            // it overwrites the previous one
            .finished(value.b(() -> false))
            // the parts of a command are init, exec, finished, end
            .end(() -> println("67"));

        // there is no support for requirements, or other more complex command tooling
        // the produced command is still a builder,
        // so you can use it alongside everything else you have learnt

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

    // ignore:
    private static void println(String str) {
        System.out.println(str);
    }
}
