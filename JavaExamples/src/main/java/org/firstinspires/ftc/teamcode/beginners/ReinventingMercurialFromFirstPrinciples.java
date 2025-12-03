package org.firstinspires.ftc.teamcode.beginners;

@SuppressWarnings("unused")
public class ReinventingMercurialFromFirstPrinciples {
	// This file will aim to walk you through reinventing the main concepts in Mercurial
	// We will re-use the names that Mercurial uses for each of these concepts
	// Mercurial adds some extra nice stuff under the hood.
	
	// Continuations:
	// Continuations are functional interfaces with one method
	// that runs the step of the continuation, and returns the next step
	@FunctionalInterface
	interface Continuation {
		Continuation apply();
		// halt is the last continuation, it always returns itself, and is a marker that we are done.
		Continuation halt = () -> Continuation.halt;
	}
	
	// there are several ways we could write Continuations, in order to recreate
	// the features of switch-statement finite state machines
	static Continuation c = () -> {
		// do something
		return Continuation.halt;
	};
	
	static Continuation b = () -> {
		// do something
		return c;
	};
	
	static Continuation a = () -> {
		// do something
		return b;
	};
	
	static Continuation continuation = a;
	
	// the logic in loop1 is pretty much the same, but the continuations are much nicer to work with and compose
	static int state = 0;
	static void loop1() {
		continuation = continuation.apply();
		
		switch (state) {
			case 0:
				// do something
				state++;
				break;
			case 1:
				// do something
				state++;
				break;
			case 2:
				// do something
				state++;
				break;
			default:
				break;
		}
	}
	
	// we could also have Continuations that return themselves for a bit,
	// and have associated state (fields)
	
	static Continuation wait3loops = new Continuation() {
		private int count = 0;
		@Override
		public Continuation apply() {
			if (count >= 3) {
				// reset
				count = 0;
				return Continuation.halt;
			}
			else {
				count++;
				// a simple loop!
				return this;
			}
		}
	};
	
	// by constructing an anonymous class, we can write some more complex states
	
	// however, there are some issues:
	// 1. when we declare our continuations, we need to declare them in reverse order
	// 2. there isn't a good easy way to reuse and combine them
	
	// this is because continuations can only be built when they know the whole future that they need to run
	// we can work around this weakness by writing a builder stage, that leaves a 'hole' in the continuation,
	// to be filled later with the 'future' or next continuation to run.
	
	@FunctionalInterface
	interface Closure {
		Continuation build(Continuation k);
		// by default, we can build it with halt
		default Continuation build() {
			return build(Continuation.halt);
		}
		
		Closure identity = k -> k;
	}
	
	// now we can write wait3 that can be joined together!
	
	static Closure wait3 = k -> new Continuation() {
			private int count = 0;
			@Override
			public Continuation apply() {
				if (count >= 3) {
					// reset
					count = 0;
					// instead of returning Continuation.halt, we return k
					return k;
				}
				else {
					count++;
					// a simple loop!
					return this;
				}
			}
		};
	
	// we can also write some useful combiners, like sequence!
	static Closure sequence(Closure... closures) {
		// we get no closures, then we can return the identity closure
		if (closures.length == 0) return Closure.identity;
		// if there is only one closure, we can just return it
		else if (closures.length == 1) return closures[0];
		// this goes through the list in reverse, and provides each closure the current final result as
		// the next closure's future.
		return k -> {
			// store the result
			Continuation res = k;
			for (int i = closures.length; i > 0; i--) {
				res = closures[i-1].build(res);
			}
			return res;
		};
	}
	
	// its easy to see that we can apply optimisations to our code with Closures,
	// as it gives us a chance to analyse the code before running it
	
	// and we can also reimplement exec
	static Closure exec(Runnable f) {
		return k -> () -> {
			f.run();
			return k;
		};
	}
	
	// these are two simple Closure's that are implemented in Mercurial already
	// but its easy to see how Closures make it easy to combine and compose Continuations
	// and each other
	
	// this is essentially how Continuations and Closures work in Mercurial, but Mercurial also
	// has the ability to run Continuations in parallel, which we haven't explored here.
}
