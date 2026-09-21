"Fiber" is the term we use for a running program, like the ones we wrote in
SequentialProgramming.

The name is used in many programming languages to refer to a light-weight
Thread or Process, which is very much the case here.

Mercurial has a lot of tools to start and manage Fibers and their relation to
each other. Some of them might be familiar to you.

You can easily spawn individual and collections of Fibers to run lots of little
programs independently, then wait for those Fibers to finish, and get their
return values. This is a lot like the async/await approach popular in a lot of
programming languages like Javascript and Rust.

However, Mercurial's Fibers are heavily inspired by a programming language
called 'Erlang'. It is a somewhat strange language, so you do not need to go
read up on Erlang at the moment, nor will you need to in order to use Mercurial.
There is nothing stopping you from doing everything using Mercurial's
async/await apis, but I would recommend you learn about Mercurial's other
approaches first.

This document will talk about the process model of Erlang and Mercurial, and
how it is well suited to modelling your robot, in fairly abstract /
theoretical terms.

Once you understand how to approach programming a robot in Mercurial, we
will look at the specific apis.

NOTE: just because the process model is different to async/await, doesn't mean
you can only use one or the other. Both work well together, it is more about
having both tools available to you, and picking the best one for the job.

In Mercurial, you can create any number of channels to send messages to, and
specify the types of messages you allow in them.

Each Fiber can send messages to any other channel, or can check / wait for a
message to come in over any of its channels.

We call checking / waiting for a message 'receiving' a message.

Using sending / receiving over channels, we can write complex, long-running
processes that send and receive messages back and forth to each other,
influencing each other.

In Mercurial, this is easy to do, and the library comes with some helpers
for building common types of complex, long-running Fibers.

We recommend that you program your robot as a lot of long-running processes,
like one for each subsystem, one for each sensor, and one for the whole robot!

This is opposite to how command based works, where requirements are used to
dynamically contest control over a subsystem. In Mercurial, you make each
subsystem in charge of itself, and in control of how it reacts to other
events in the system.

Now that you understand how Mercurial is designed to handle complex robot logic,
go check out concurrent programming in
[kotlin](KotlinExamples/src/main/kotlin/org/firstinspires/ftc/teamcode/ConcurrentProgramming.kt)
or
[java](JavaExamples/src/main/java/org/firstinspires/ftc/teamcode/ConcurrentProgramming.java).
