package org.firstinspires.ftc.teamcode.examplerobot

import com.pedropathing.drivetrain.DrivePowers
import com.pedropathing.localization.MotionState
import com.pedropathing.math.Pose
import com.pedropathing.paths.Path
import com.pedropathing.paths.PathTracker
import dev.frozenmilk.dairy.mercurial.continuations.Continuation.Builder
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.expression
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.receive
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.value
import dev.frozenmilk.dairy.mercurial.pedro.MercurialFollower
import dev.frozenmilk.dairy.mercurial.processes.Channel
import dev.frozenmilk.dairy.mercurial.processes.Channel.Companion.tryPoll
import dev.frozenmilk.dairy.mercurial.processes.EventManager
import dev.frozenmilk.dairy.mercurial.processes.StateMachine
import java.util.function.BooleanSupplier
import java.util.function.Supplier
import kotlin.time.TimeMark

class Drivetrain(val pedro: MercurialFollower) : StateMachine() {
    // we need some channels for communicating with the drivetrain
    // drive powers are manual drive inputs
    private val drivePowers = Channel.single<DrivePowers>()

    // and motion states are updates from the localiser
    private val motionStates = Channel.single<MotionState>()

    // we add our own mode switching system, which will come in over this channel
    // we will just send the mode directly to the drivetrain when we want it to switch
    private val switches = Channel.single<Mode>()

    // lets set up some modes, this tutorial will just add the 4 pedro modes
    private val manual: Mode = object : Mode {
        override fun eval(): Mode {
            // if there are drive powers, we use the pedro helper to drive the robot
            drivePowers.tryPoll { powers ->
                pedro.manual(powers)
            }

            // either switch to a new mode, or continue doing this
            return switches.poll() ?: this
        }
    }

    private val idle: Mode = object : Mode {
        // when we enter idle, we want to stop the drivetrain
        // this helper will do so for us
        override fun enter(previousMode: Mode) = pedro.idle()
        override fun eval(): Mode {
            // except for switching modes
            return switches.poll() ?: this
        }
    }

    // unlike manual and idle, hold and follow have some extra data
    // so we will make them inner classes instead

    private inner class Hold(
        // we need the pose to hold at
        val pose: Pose,
        // if to use scaling
        val useScaling: Boolean,
        // the timeMark is to tell us how long has passed since last update
        // you don't need to worry about it much
        val timeMark: TimeMark? = null,
    ) : Mode {
        override fun eval(): Mode {
            // but hold uses localisation updates
            motionStates.tryPoll { motionStates ->
                // the helper for holding is a bit more complex
                // it returns the next timeMark for use to use
                val nextTimeMark = pedro.hold(
                    pose,
                    useScaling,
                    motionStates,
                    timeMark,
                )
                // so we need to go into a new version of hold
                return switches.poll() ?: Hold(
                    pose,
                    useScaling,
                    nextTimeMark,
                )
            }
            // we use the same switching logic
            return switches.poll() ?: this
        }
    }

    enum class FollowResult {
        Finished,
        Cancelled,
    }

    // follow is the most complex
    private inner class Follow(
        // when a different bit of code asks us to follow a path
        // it may want to know when we are finished
        // so it can give us a response channel
        // this is optional
        val respondTo: Channel<FollowResult>?,
        // the path tracker is part of pedro
        val pathTracker: PathTracker,
        // if to hold the end position afterward
        val holdEnd: Boolean,
        // the same time mark as hold
        val timeMark: TimeMark? = null,
    ) : Mode {
        // this constructor converts paths to path trackers
        // you will use it more
        constructor(
            respondTo: Channel<FollowResult>?,
            path: Path,
            holdEnd: Boolean,
        ) : this(
            respondTo,
            PathTracker(path),
            holdEnd,
        )

        override fun eval(): Mode {
            // the follow helper is the most complex
            motionStates.tryPoll { motionState ->
                val nextTimeMark = pedro.follow(
                    pathTracker,
                    motionState,
                    timeMark,
                )
                // the helper will return null if the tracker is finished
                return if (nextTimeMark === null) {
                    // let the caller know that we have finished successfully
                    respondTo?.send(FollowResult.Finished)

                    switches.poll() ?:
                    // if hold end is true, we need to switch to hold
                    if (holdEnd) Hold(pathTracker.endPose(), true)
                    // otherwise we idle
                    else idle
                }
                // if the tracker didn't finish, then we keep going
                else {
                    val switch = switches.poll()
                    return if (switch !== null) {
                        // release the path tracker
                        pathTracker.release()
                        // let the caller know that we didn't complete following the path
                        respondTo?.send(FollowResult.Cancelled)
                        // return the new mode
                        switch
                    }
                    else Follow(respondTo, pathTracker, holdEnd, nextTimeMark)
                }
            }
            val switch = switches.poll()
            return if (switch !== null) {
                // release the path tracker
                pathTracker.release()
                // let the caller know that we didn't complete following the path
                respondTo?.send(FollowResult.Cancelled)
                // return the new mode
                switch
            } else this
        }
    }

    // this event handler will forward motion state events to the drivetrain
    private val motionStateForwarder = object : EventManager.Handler<MotionState> {
        override fun handleEvent(event: MotionState): EventManager.EventHandled<MotionState> {
            motionStates.send(event)
            // if the drivetrain finishes, remove this handler
            return if (fiber.status.alive) EventManager.EventHandled.Ok
            else EventManager.EventHandled.Remove
        }
    }

    init {
        // we tell the localiser to send events to us
        pedro.localizationEventManager.addHandler(motionStateForwarder)
    }

    override fun init(): Mode {
        // we init into the idle mode
        return idle
    }

    // NOTE:
    // everything so far is a suggestion
    // you might want to make it that following or holding can be cancelled by incoming driver powers
    // or add another mode to use a pid controller for aiming the robot while manually driving
    // or add slow mode instructions over another channel
    // there is no "correct" way to do this in mercurial, you can take this and adjust it to your needs

    // public api:
    fun manual() {
        switches.send(manual)
    }

    fun idle() {
        switches.send(idle)
    }

    fun hold(pose: Pose, useScaling: Boolean) {
        switches.send(Hold(pose, useScaling))
    }

    fun followAsync(path: Path, holdEnd: Boolean) {
        switches.send(Follow(null, path, holdEnd))
    }

    fun followAsync(respondTo: Channel<FollowResult>, path: Path, holdEnd: Boolean) {
        switches.send(Follow(respondTo, path, holdEnd))
    }

    fun followSync(path: Supplier<Path>, holdEnd: BooleanSupplier): Builder<FollowResult> =
        expression {
            val channel = o { Channel.single<FollowResult>() }
            !exec { switches.send(Follow(channel(), path.get(), holdEnd.asBoolean)) }
            receive<FollowResult>()
                .receive(channel) { result -> value.o(result) }
        }

    fun followSync(path: Path, holdEnd: BooleanSupplier) = followSync({ path }, holdEnd)
    fun followSync(path: Supplier<Path>, holdEnd: Boolean) = followSync(path) { holdEnd }
    fun followSync(path: Path, holdEnd: Boolean) = followSync({ path }, { holdEnd })

    fun sendPowers(drivePowers: DrivePowers) {
        this.drivePowers.send(drivePowers)
    }
}
