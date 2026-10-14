package org.firstinspires.ftc.teamcode.examplerobot

import com.pedropathing.drivetrain.DrivePowers
import com.pedropathing.follower.ManualDrive
import com.pedropathing.localization.MotionState
import com.pedropathing.math.Pose
import com.pedropathing.paths.Path
import com.pedropathing.paths.PathTracker
import dev.frozenmilk.dairy.mercurial.continuations.Continuation.Builder
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.expression
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.receive
import dev.frozenmilk.dairy.mercurial.ftc.Context
import dev.frozenmilk.dairy.mercurial.processes.Channel
import dev.frozenmilk.dairy.mercurial.processes.SwitchedStateMachine
import org.firstinspires.ftc.teamcode.examplerobot.pedro.Constants
import kotlin.time.TimeMark

class Drivetrain(ctx: Context) : SwitchedStateMachine() {
    // create the follower helper, which has a pedro drivetrain and algorithm
    private val follower = Constants.createFollower(ctx.hardwareMap)

    // localization updates
    private val motionStates = linkedSingleChannel<MotionState>()

    // driver inputs
    private val drivePowers = linkedSingleChannel<DrivePowers>()

    // Mercurial does not use telemetry the same way the SDK does
    // instead, you retain the entry, and update the value over time
    private val stateTelemetry = ctx.telemetry.addData("Drivetrain/State", "unknown")

    // constant modes

    // the idle mode just stops the robot when you enter it
    private val idle: Mode = entranceMode { _ ->
        stateTelemetry.setValue("idle")
        follower.idle()
    }

    // field centric uses the most recent motion state data to drive the robot
    private val fieldCentric: Mode = mode { self ->
        stateTelemetry.setValue("field centric")
        // consume the next inputs from the driver
        val input = drivePowers.poll()
        // we only query motion states, which doesn't remove the data
        val motionState = motionStates.query()

        if (input != null && motionState != null) {
            // calculate the field centric inputs
            val fieldCentricInput = ManualDrive.fieldCentric(
                input,
                motionState.pose().heading()
            )
            // drive!
            follower.manual(fieldCentricInput)
        }
        self
    }

    // parametric modes

    private fun holdPoseMode(timeMark: TimeMark?, pose: Pose): Mode = mode { self ->
        stateTelemetry.setValue("holding pose")

        // we poll the motion state,
        // so that we only update when a new motion state arrives
        val motionState = motionStates.poll()

        // if there is a new motion state
        if (motionState != null) {
            // we calculate an update
            val nextTimeMark = follower.hold(
                pose,
                true,
                motionState,
                timeMark
            )
            return@mode holdPoseMode(nextTimeMark, pose)
        }
        self
    }

    private fun holdPoseMode(pose: Pose): Mode = holdPoseMode(null, pose)

    private fun followPathMode(
        timeMark: TimeMark?,
        holdEnd: Boolean,
        tracker: PathTracker,
        resultChannel: Channel<FollowResult>?
    ): Mode = egressMode(
        // eval
        { self ->
            stateTelemetry.setValue("following path")

            // we poll the motion state,
            // so that we only update when a new motion state arrives
            val motionState = motionStates.poll()
            if (motionState != null) {
                val nextTimeMark = follower.follow(
                    tracker,
                    motionState,
                    timeMark
                )
                // follower.follow will return null when its finished
                if (nextTimeMark == null) {
                    // tell the awaiting process to move on!
                    resultChannel?.sendIfEmpty(FollowResult.Ok)
                    // if hold end, move to holding the end
                    if (holdEnd) holdPoseMode(tracker.endPose())
                    else idle
                } else followPathMode(
                    nextTimeMark,
                    holdEnd,
                    tracker,
                    resultChannel
                )
            } else self
        },
        // exit
        {
            // this function gets run when we exit following due to a switch
            resultChannel?.sendIfEmpty(FollowResult.Cancelled)
            // make sure to release the tracker
            tracker.release()
        },
    )

    private fun followPathMode(
        holdEnd: Boolean,
        path: Path,
        resultChannel: Channel<FollowResult>?
    ) = followPathMode(null, holdEnd, PathTracker(path), resultChannel)

    // you might want to add more results, like timedout
    enum class FollowResult {
        Ok,
        Cancelled,
    }

    // init into idle
    override fun init(): Mode {
        return idle
    }

    // public api
    fun sendPowers(powers: DrivePowers) {
        drivePowers.send(powers)
    }

    fun sendPowers(forward: Double, strafe: Double, turn: Double) {
        drivePowers.send(DrivePowers(forward, strafe, turn))
    }

    fun sendMotionState(motionState: MotionState) {
        motionStates.send(motionState)
    }

    fun fieldCentric() {
        switchTo(fieldCentric)
    }

    fun idle() {
        switchTo(idle)
    }

    fun holdPose(pose: Pose) {
        switchTo(holdPoseMode(pose))
    }

    fun followPathAsync(
        path: Path,
        holdEnd: Boolean,
        resultChannel: Channel<FollowResult>? = null,
    ) {
        switchTo(followPathMode(holdEnd, path, resultChannel))
    }

    fun followPathSync(path: Path, holdEnd: Boolean): Builder<FollowResult?> {
        return expression { _ ->
            // local channel variable
            val channel = o { Channel.single<FollowResult>() }
            // run async
            !exec { followPathAsync(path, holdEnd, channel()) }

            receive<FollowResult>().receive(channel)
        }
    }
}
