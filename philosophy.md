# Philosophy of Mercurial 2

Mercurial 2 was designed to replace the command based paradigm for robot 
control. I wanted to move away from commands as I felt they had several 
major design flaws that made it hard to write robot programs well.

Commands make it hard to write complex behaviour where commands hold and share
data correctly.

Requirements are amorphus and hard to reason about for students. I would often
find that using requirements would lead to shooting myself in the foot, but I
also felt there was no good alternative to get dynamic control flow. 
Additionally, sometimes a small change in how we wanted a subsystem to 
behave would cause all the commands related to that subsystem to change, as 
the behaviour contract was caused by the union of all the commands, and the 
way requirements would interact, rather than just that command.

Many of the issues were "N + 1" issues, where you would solve the immediate 
problem with some tool command based offers, and in doing so push the issue 
down the line, and have some new problem to solve, caused by the solution to 
the previous problem.

I felt that these issues made it both annoying for me to think about how to
approach a problem, and annoying to teach, as I was teaching how to work around
the deficiencies in commands, rather than how to actually think about and model
a problem, and I had to direct students more than I wished, as it was hard 
to explain why we had to write the commands I knew we needed to model the 
robot behaviour the way we agreed.

All of these gripes lead me to work on Mercurial 2 with a few goals:
1. Improve state management and control flow in commands to make them a more 
   complete sub-language, with variables and functions, etc.
2. Improve process management so that processes could more easily be started, 
   stopped and interacted with without needing to construct a new program 
   every time.
3. Change the way robot behaviours were modelled from using requirements to 
   more concrete subsystem processes.

And I feel that I've generally achieved this.

In Mercurial 2 there are no requirements or mutexes, although they could be 
added in the future.

Each subsystem is modelled as a state machine, not a finite state machine, 
but a mealy machine, which is a finite state machine with messages. This 
puts each subsystem in charge of itself. It decides when to switch modes, 
with the help of messages coming in over channels.

I think this approach makes it much easier to model complex behaviours, and 
makes it much easier to write maintainable and reusable code and programs 
for robots, and hopefully you will too.
