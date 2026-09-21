Thanks for trying Mercurial 2.

Mercurial is a concurrency and control flow library for FTC, and an alternative
to command based concurrency and control flow. Concurrency and control flow
libraries aim to make it easy to run separate parts of your robot code at the
same time (concurrency), and to have the separate parts influence each other,
allowing you to control the robot in a dynamic manner (control flow). Libraries
like Mercurial make it easy to write and maintain complex teleop and autonomous
programs.

If you have worked with command based before, you might want to start with
reading [the design philosophy](philosophy.md). It explores the issues I have
with command based, and how Mercurial was designed to work around them. Once you
have read it, continue on to the rest of the documentation.

If you have not used command based I recommend not reading [the design
philosophy](philosophy.md), and instead you continue on to sequential
programming in
[kotlin](KotlinExamples/src/main/kotlin/org/firstinspires/ftc/teamcode/SequentialProgramming.kt) or
[java](JavaExamples/src/main/java/org/firstinspires/ftc/teamcode/SequentialProgramming.java),
which will introduce you to how to use Mercurial in a more natural order.
