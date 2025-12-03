# Getting Started

Thanks for trying Mercurial 2.0!

This 'beginners' package aims to introduce:

- Control flow, regardless of if you've used Command Based
- Mercurial's basic concepts
- Mercurial's alternative to OpModes, known as Programs
- Simple TeleOp and Auto

## Control Flow

Control flow refers to the ability for our program to run, or not run certain
bits of our code, and the order that we want them to be run in.

In Java, we have a few common control flow operators that have emerged from
procedural programming:

- `if`/`else` - branching
- `switch` - branching
- `for`/`while`/`do` - looping
- `throw` and `try`/`catch`/`finally` (although these are little special)
- call/return (as in, calling functions, and returning from them)

Mainly we rely on branching, and looped branching operators to do the majority
of our control flow within function bodies. Branching operators are how our
program makes choices at runtime.

In robotics, we often want a complex control flow, and so in Java we need to
take advantage of the limited tools we have in order to build more complex
systems, in FTC we often want to do a few things:

1. Run code in parallel in lots of little tasks
2. Be able to interrupt currently running tasks
3. Be able to spawn new tasks when certain events occur
4. Be able to pass data between the tasks
5. Have a nice way to build and compose tasks together

## Solutions:

You can skip [Command Based](#command-based) and [Actions](#actions) if
you're not interested in weaknesses of other systems.

### Continuations, Fibers and Registers
This is the model that is used by Mercurial 2.0. A Continuation is a 
function that takes in no arguments, and returns the next Continuation to run:

```java
@FunctionalInterface
interface Continuation {
    Continuation apply();
}
```

In Mercurial 2.0, we add a bit more information on top of the simple function 
definition that makes them a little harder to construct, but the core idea 
is the same.

A `Fiber` is a structure that holds a Continuation, and will run the `apply` 
function, and store the next output. This allows us to easily run 
Continuations in parallel, or cancel a Continuation that is being run by 
something else.

Additionally, Mercurial 2.0 uses a data structure known as a `Register` 
which stores data with the currently running stack of `Fiber`s. A `Register` 
is basically a handle to some `Continiuation`-local data, solving a lot of 
issues that normal JVM variables or state presents with `Continuation`s.

Unfortunately, `Continuation`s are annoying to construct, as we always need 
to build the last step first (which is a bit back to front!).

In Mercurial 2.0 we solve this by using a `Closure` which is a 
`Continuation` that doesn't know what will happen next! Once we've decided 
all the steps that need to happen in our `Continuation` we `close` our 
`Closure` and produce the final `Continuation` for the program to run.

This might all seem a little confusing at the moment, so we'll take a look 
at some examples shortly in order to get a grasp of the basics.

### Command Based

Typically, teams have used the Command Based paradigm, and a library that
handles a lot of the heavy lifting in this department.

Command Based provides:

- A 'task' type: `Command`
- The Command can run over and over again, waiting to finish
- The Command can require certain parts of the robot, and so be interrupted
  if something new wants to have that part
- React to being interrupted, in order to shut down smoothly

This all sounds great, but commands have some pretty big flaws, with a lot
of 'bandaid' fixes on top of them in order to hide the flaws most of the time.

Ultimately, a lot of teams end up using Commands in order to manage the
complex reactive control flow that reacts to human input, but use
switch-statement Finite State Machines in their subsystems in order to
manage subsystem behaviour.

This is because Commands struggle to do non-local control flow, just like
Java does. Commands have one strong tool for doing non-local control flow,
which is the requirements system. The requirements system allows us to
interrupt running Commands, but is hard to manipulate, and does not allow us
a lot of control over how the command responds.

Other weaknesses of commands:

1. running the same command in two places at the same time is often
   undefined behaviour, so we make lots of little copies of it (not the end
   of the world, but can present issues at times)
2. Its hard to 'subschedule' commands (run a command within another command)
   as commands are complex structures, this means that it pretty much must
   be done by the library only.
3. Commands typically rely on either global state, or command-local state.
   Command-local state is prone to causing bugs due to needing to be reset
   by the command before the command can be run again. Global state makes
   command based code bases complex and hard to read, and doesn't allow
   users to safely add their own command-local state to pre-existing commands.

### Actions

TODO

## Example Files
Recommended reading order:
1. ReinventingMercurialFromFirstPrinciples
2. Closures
3. MyFirstMercurialOpMode
4. Registers
5. MyFirstMercurialTeleOp
6. Closures2
7. MyFirstMercurialAuto
