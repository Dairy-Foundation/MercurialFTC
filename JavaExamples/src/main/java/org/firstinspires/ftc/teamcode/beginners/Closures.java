package org.firstinspires.ftc.teamcode.beginners;

import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.deadline;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.ifHuh;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.match;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.noop;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.parallel;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.race;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence;

import dev.frozenmilk.dairy.mercurial.continuations.Closure;
import dev.frozenmilk.dairy.mercurial.continuations.Continuations;

public class Closures {
    {
        // Almost all pre-built Closures come in the `Continuations` namespace in Mercurial
        // This means you can call them all like this:
        Closure a = Continuations.exec(() -> {
        });

        // or you can import the individual functions directly instead:
        Closure b = exec(() -> {
        });

        // Its recommended to import them directly,
        // but you might need the namespace to avoid clashes sometimes

        // exec is like an `InstantCommand` in Command Based, it runs the function given to it
        // in Mercurial, exec is a fundamental tool for writing code,
        // as its where you'll put a lot of runtime logic
        exec(() -> {
            // motor.setPower(1)
            // ,,,
            // val power = pid.calculate(target, current)
            // ,,,
        });

        // we'll get into practical examples in the other files

        // we can join several steps together in a sequence:
        sequence(
                exec(() -> {}),
                exec(() -> {}),
                exec(() -> {}),
                exec(() -> {})
        );

        // or in parallel:
        parallel(
                exec(() -> {}),
                exec(() -> {}),
                exec(() -> {}),
                exec(() -> {})
        );

        // more complex parallel runners are also possible:

        // race cancels everything else once any of the processes finish
        race(
                exec(() -> {}),
                exec(() -> {}),
                exec(() -> {}),
                exec(() -> {})
        );

        // and deadline cancels everything else once the deadline process finishes:
        deadline(
                // deadline
                exec(() -> {}),
                // others:
                exec(() -> {}),
                exec(() -> {}),
                exec(() -> {}),
                exec(() -> {})
        );

        // its also possible to branch:
        ifHuh(
                // condition
                () -> true,
                // body
                exec(() -> {})
        );

        // the name 'ifHuh' comes from lisp
        // in lisp, you can use any characters for your variable names
        // and lots of boolean or branching related functions
        // are named with a question mark at the end
        // like: `if?` and `eq?`
        // this question mark is pronounced `huh?`
        // so its like you're asking a question of the program

        ifHuh(
                // condition
                () -> false,
                // body
                exec(() -> {})
        ).elseIfHuh(
                // condition
                () -> false,
                // body
                exec(() -> {})
        ).elseHuh(
                // body
                exec(() -> {})
        );

        // we also have match (switch):
        match(() -> 0)
                // 0
                .branch(0, exec(() -> {}))
                // 10
                .branch(10, exec(() -> {}))
                // default
                .defaultBranch(exec(() -> {}));

        match(() -> 0)
                // 0
                .branch (0, exec(() -> {}))
                // 10
                .branch (10, exec(() -> {}))
                // we think we've matched all the potential cases
                .assertExhaustive();

        // after you've added a default case,
        // or asserted that you've matched all the cases, you can't add any more
        // but you don't have to do either of those things
        // without them, match will just ignore the value, and skip the whole match statement

        // every function from `Continuations` will return a `Closure`,
        // or something that builds into a `Closure`

        // looping:
        loop(
                () -> true,
                exec(() -> {})
        );

        // no condition will loop forever, in a slightly more efficient way
        loop(exec(() -> {}));

        // sometimes we need to fill in a hole, but we don't want to add any special code
        // like to a ifHuh-else chain,
        // where we want to skip checking the rest if an early case returns true

        // noop is completely free

        Closure noop = noop();

        // In fact, an advantage of the closures system we haven't investigated is
        // that it can automatically optimise our code

        // an empty sequence becomes a noop()
        sequence();

        // so does an empty parallel
        parallel();

        // even cooler:
        parallel(exec(() -> {}));
        // is equal to:
        exec(() -> {});
        // and this optimisation is applied before our code is run!

        // Mercurial will take any opportunity it can to optimise your code,
        // as long as its safe to do so

        // This concludes the first introduction to pre-fabricated closures that come in Mercurial
        // Next, look at `MyFirstMercurialOpMode`
    }
}
