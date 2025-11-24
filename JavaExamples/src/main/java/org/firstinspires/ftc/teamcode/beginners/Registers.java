package org.firstinspires.ftc.teamcode.beginners;

import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.ifHuh;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.parallel;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.scope;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.waitSeconds;

import dev.frozenmilk.dairy.mercurial.continuations.Closure;
import dev.frozenmilk.dairy.mercurial.continuations.registers.ValRegister;
import dev.frozenmilk.dairy.mercurial.continuations.registers.VarRegister;

public class Registers {
    static int globalCount = 0;
    static {
        // so far, our Continuations have been pretty boring

        // we can get way more interesting with `scope`
        // NOTE: scope is much nicer in kotlin, I recommend you check it out!
        Closure countTo20 = scope(env -> {
            VarRegister<Integer> count = env.variable(() -> 0);
            return loop(
                    () -> count.get() < 20,
                    exec(() -> count.map(x -> x + 1))
            );
        });
        // `count` is data that is now available only in the scope of the scope block

        // every time that we run countTo20, the count is reset
        sequence(
                countTo20,
                countTo20,
                countTo20
        );

        // and if we run it in parallel, each separate process has its own count
        parallel(
                countTo20,
                countTo20,
                countTo20
        );

        // in comparison, if we used a regular variable:
        Closure countTo20Again = scope(env -> {
            VarRegister<Integer> count = env.variable(() -> 0);
            return loop(
                    () -> count.get() < 20,
                    exec(() -> count.map(x -> x + 1))
            );
        });

        // this would only count to 20 once
        sequence(
                countTo20Again,
                countTo20Again,
                countTo20Again
        );

        // and here, all three copies would be using the same value together
        // so they'd count to 20 really fast, but all at once
        parallel(
                countTo20Again,
                countTo20Again,
                countTo20Again
        );

        // registers are really powerful, and are a pretty easy to use tool to make
        // sure that we can safely use data across a lexical scope

        // theres nothing stopping you from using global scope when you need to
        // and when its appropriate
        // but mastering registers will make it really easy to write complex Mercurial code

        // we can use registers to branch
        scope(env -> {
            VarRegister<Integer> count = env.variable(() -> 0);
            return loop(
                    () -> count.get() < 20,
                    ifHuh(
                            () -> count.get() < 10,
                            exec(() -> count.map(x -> x + 2))
                    ).elseHuh(
                            exec(() -> count.map(x -> x + 1))
                    )
            );
        });

        scope(env -> {
            // we can also have value registers
            // they're a little more niche
            ValRegister<Integer> count = env.value(() -> globalCount);
            VarRegister<Integer> countPlus1 = env.variable(() -> count.get() + 1);

            // registers are shared across parallel processes as well
            return parallel(
                    exec(() -> globalCount++),
                    sequence(
                            waitSeconds(1.0),
                            exec(() -> globalCount++)
                    ),
                    sequence(
                            waitSeconds(1.0),
                            exec(() -> {
                                System.out.println(count);
                                System.out.println(countPlus1.get());
                                countPlus1.map(x -> x + 1);
                            })
                    )
            );
        });

        // theres plenty more you can do with registers, but these are the basics
        // you can also construct registers, then bind to them:

        scope(env -> {
            VarRegister<Integer> countRegister = env.variable(() -> 0);

            // see the function below
            Closure increaseCount = increaseCount(countRegister);

            return sequence(
                    increaseCount,
                    increaseCount,
                    increaseCount
            );
        });

        // value registers all implement Supplier
        // and variable registers also implement Consumer
        // which means they can easily be passed as functions to things as well

        // now, onto MyFirstMercurialTeleOp
    }

    // note that we didn't use scope here
    static Closure increaseCount(VarRegister<Integer> count) {
        return exec(() -> count.map(x -> x + 1));
    }
}
