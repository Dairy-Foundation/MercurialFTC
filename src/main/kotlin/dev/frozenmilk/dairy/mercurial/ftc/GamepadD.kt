package dev.frozenmilk.dairy.mercurial.ftc

import com.qualcomm.robotcore.hardware.Gamepad
import dev.frozenmilk.dairy.mercurial.ftc.GamepadD.Delta
import dev.frozenmilk.dairy.mercurial.processes.EventManager
import org.jetbrains.annotations.Contract

class GamepadD : EventManager<Delta>() {
    enum class Id {
        One, Two
    }
    data class Data(
        @get:JvmName("leftStickX")
        val leftStickX: Double,
        @get:JvmName("leftStickY")
        val leftStickY: Double,
        @get:JvmName("rightStickX")
        val rightStickX: Double,
        @get:JvmName("rightStickY")
        val rightStickY: Double,
        @get:JvmName("dpadUp")
        val dpadUp: Boolean,
        @get:JvmName("dpadDown")
        val dpadDown: Boolean,
        @get:JvmName("dpadLeft")
        val dpadLeft: Boolean,
        @get:JvmName("dpadRight")
        val dpadRight: Boolean,
        @get:JvmName("a")
        val a: Boolean,
        @get:JvmName("b")
        val b: Boolean,
        @get:JvmName("x")
        val x: Boolean,
        @get:JvmName("y")
        val y: Boolean,
        @get:JvmName("guide")
        val guide: Boolean,
        @get:JvmName("start")
        val start: Boolean,
        @get:JvmName("back")
        val back: Boolean,
        @get:JvmName("leftBumper")
        val leftBumper: Boolean,
        @get:JvmName("rightBumper")
        val rightBumper: Boolean,
        @get:JvmName("leftStickButton")
        val leftStickButton: Boolean,
        @get:JvmName("rightStickButton")
        val rightStickButton: Boolean,
        @get:JvmName("leftTrigger")
        val leftTrigger: Double,
        @get:JvmName("rightTrigger")
        val rightTrigger: Double,
        @get:JvmName("touchpad")
        val touchpad: Boolean,
        @get:JvmName("touchpadFinger1")
        val touchpadFinger1: Boolean,
        @get:JvmName("touchpadFinger1X")
        val touchpadFinger1X: Double,
        @get:JvmName("touchpadFinger1Y")
        val touchpadFinger1Y: Double,
        @get:JvmName("touchpadFinger2")
        val touchpadFinger2: Boolean,
        @get:JvmName("touchpadFinger2X")
        val touchpadFinger2X: Double,
        @get:JvmName("touchpadFinger2Y")
        val touchpadFinger2Y: Double,
        @get:JvmName("ps")
        val ps: Boolean,
    ) {
        @get:JvmName("circle")
        val circle = b

        @get:JvmName("cross")
        val cross = a

        @get:JvmName("triangle")
        val triangle = y

        @get:JvmName("square")
        val square = x

        @get:JvmName("share")
        val share = back

        @get:JvmName("options")
        val options = start

        constructor(gamepad: Gamepad) : this(
            gamepad.left_stick_x.toDouble(),
            gamepad.left_stick_y.toDouble(),
            gamepad.right_stick_y.toDouble(),
            gamepad.right_stick_x.toDouble(),
            gamepad.dpad_up,
            gamepad.dpad_down,
            gamepad.dpad_left,
            gamepad.dpad_right,
            gamepad.a,
            gamepad.b,
            gamepad.x,
            gamepad.y,
            gamepad.guide,
            gamepad.start,
            gamepad.back,
            gamepad.left_bumper,
            gamepad.right_bumper,
            gamepad.left_stick_button,
            gamepad.right_stick_button,
            gamepad.left_trigger.toDouble(),
            gamepad.right_trigger.toDouble(),
            gamepad.touchpad,
            gamepad.touchpad_finger_1,
            gamepad.touchpad_finger_1_x.toDouble(),
            gamepad.touchpad_finger_1_y.toDouble(),
            gamepad.touchpad_finger_2,
            gamepad.touchpad_finger_2_x.toDouble(),
            gamepad.touchpad_finger_2_y.toDouble(),
            gamepad.ps,
        )

        constructor() : this(
            0.0,
            0.0,
            0.0,
            0.0,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            0.0,
            0.0,
            false,
            false,
            0.0,
            0.0,
            false,
            0.0,
            0.0,
            false,
        )
    }

    data class Delta(
        @get:JvmName("id")
        val id: Id,
        @get:JvmName("type")
        val type: Gamepad.Type,
        @get:JvmName("previousData")
        val previousData: Data,
        @get:JvmName("currentData")
        val currentData: Data,
    ) {
        @get:JvmName("leftStickX")
        val leftStickX by currentData::leftStickX

        @get:JvmName("leftStickY")
        val leftStickY by currentData::leftStickY

        @get:JvmName("rightStickX")
        val rightStickX by currentData::rightStickX

        @get:JvmName("rightStickY")
        val rightStickY by currentData::rightStickY

        @get:JvmName("dpadUp")
        val dpadUp by currentData::dpadUp

        @get:JvmName("dpadUpWasPressed")
        val dpadUpWasPressed = dpadUp && !previousData.dpadUp

        @get:JvmName("dpadDown")
        val dpadDown by currentData::dpadDown

        @get:JvmName("dpadDownWasPressed")
        val dpadDownWasPressed = dpadDown && !previousData.dpadDown

        @get:JvmName("dpadDownWasReleased")
        val dpadDownWasReleased = !dpadDown && previousData.dpadDown

        @get:JvmName("dpadLeft")
        val dpadLeft by currentData::dpadLeft

        @get:JvmName("dpadLeftWasPressed")
        val dpadLeftWasPressed = dpadLeft && !previousData.dpadLeft

        @get:JvmName("dpadLeftWasReleased")
        val dpadLeftWasReleased = !dpadLeft && previousData.dpadLeft

        @get:JvmName("dpadRight")
        val dpadRight by currentData::dpadRight

        @get:JvmName("dpadRightWasPressed")
        val dpadRightWasPressed = dpadRight && !previousData.dpadRight

        @get:JvmName("dpadRightWasReleased")
        val dpadRightWasReleased = !dpadRight && previousData.dpadRight

        @get:JvmName("a")
        val a by currentData::a

        @get:JvmName("aWasPressed")
        val aWasPressed = a && !previousData.a

        @get:JvmName("aWasReleased")
        val aWasReleased = !a && previousData.a

        @get:JvmName("b")
        val b by currentData::b

        @get:JvmName("bWasPressed")
        val bWasPressed = b && !previousData.b

        @get:JvmName("bWasReleased")
        val bWasReleased = !b && previousData.b

        @get:JvmName("x")
        val x by currentData::x

        @get:JvmName("xWasPressed")
        val xWasPressed = x && !previousData.x

        @get:JvmName("xWasReleased")
        val xWasReleased = !x && previousData.x

        @get:JvmName("y")
        val y by currentData::y

        @get:JvmName("yWasPressed")
        val yWasPressed = y && !previousData.y

        @get:JvmName("yWasReleased")
        val yWasReleased = !y && previousData.y

        @get:JvmName("guide")
        val guide by currentData::guide

        @get:JvmName("guideWasPressed")
        val guideWasPressed = guide && !previousData.guide

        @get:JvmName("guideWasReleased")
        val guideWasReleased = !guide && previousData.guide

        @get:JvmName("start")
        val start by currentData::start

        @get:JvmName("startWasPressed")
        val startWasPressed = start && !previousData.start

        @get:JvmName("startWasReleased")
        val startWasReleased = !start && previousData.start

        @get:JvmName("back")
        val back by currentData::back

        @get:JvmName("backWasPressed")
        val backWasPressed = back && !previousData.back

        @get:JvmName("backWasReleased")
        val backWasReleased = !back && previousData.back

        @get:JvmName("leftBumper")
        val leftBumper by currentData::leftBumper

        @get:JvmName("leftBumperWasPressed")
        val leftBumperWasPressed = leftBumper && !previousData.leftBumper

        @get:JvmName("leftBumperWasReleased")
        val leftBumperWasReleased = !leftBumper && previousData.leftBumper

        @get:JvmName("rightBumper")
        val rightBumper by currentData::rightBumper

        @get:JvmName("rightBumperWasPressed")
        val rightBumperWasPressed = rightBumper && !previousData.rightBumper

        @get:JvmName("rightBumperWasReleased")
        val rightBumperWasReleased = !rightBumper && previousData.rightBumper

        @get:JvmName("leftStickButton")
        val leftStickButton by currentData::leftStickButton

        @get:JvmName("leftStickButtonWasPressed")
        val leftStickButtonWasPressed = leftStickButton && !previousData.leftStickButton

        @get:JvmName("leftStickButtonWasReleased")
        val leftStickButtonWasReleased = !leftStickButton && previousData.leftStickButton

        @get:JvmName("rightStickButton")
        val rightStickButton by currentData::rightStickButton

        @get:JvmName("rightStickButtonWasPressed")
        val rightStickButtonWasPressed = rightStickButton && !previousData.rightStickButton

        @get:JvmName("rightStickButtonWasReleased")
        val rightStickButtonWasReleased = !rightStickButton && previousData.rightStickButton

        @get:JvmName("leftTrigger")
        val leftTrigger by currentData::leftTrigger

        fun leftTriggerOver(threshold: Double) = leftTrigger > threshold
        fun leftTriggerUnder(threshold: Double) = leftTrigger < threshold
        fun leftTriggerPassedOver(threshold: Double) =
            leftTrigger > threshold && !(previousData.leftTrigger > threshold)

        fun leftTriggerPassedUnder(threshold: Double) =
            leftTrigger < threshold && !(previousData.leftTrigger < threshold)

        @get:JvmName("rightTrigger")
        val rightTrigger by currentData::rightTrigger

        fun rightTriggerOver(threshold: Double) = rightTrigger > threshold
        fun rightTriggerUnder(threshold: Double) = rightTrigger < threshold
        fun rightTriggerPassedOver(threshold: Double) =
            rightTrigger > threshold && !(previousData.rightTrigger > threshold)

        fun rightTriggerPassedUnder(threshold: Double) =
            rightTrigger < threshold && !(previousData.rightTrigger < threshold)

        @get:JvmName("touchpad")
        val touchpad by currentData::touchpad

        @get:JvmName("touchpadWasPressed")
        val touchpadWasPressed = touchpad && !previousData.touchpad

        @get:JvmName("touchpadWasReleased")
        val touchpadWasReleased = !touchpad && previousData.touchpad

        @get:JvmName("touchpadFinger1X")
        val touchpadFinger1X by currentData::touchpadFinger1X

        @get:JvmName("touchpadFinger1Y")
        val touchpadFinger1Y by currentData::touchpadFinger1Y

        @get:JvmName("touchpadFinger2X")
        val touchpadFinger2X by currentData::touchpadFinger2X

        @get:JvmName("touchpadFinger2Y")
        val touchpadFinger2Y by currentData::touchpadFinger2Y

        @get:JvmName("ps")
        val ps by currentData::ps

        @get:JvmName("psWasPressed")
        val psWasPressed = ps && !previousData.ps

        @get:JvmName("psWasReleased")
        val psWasReleased = !ps && previousData.ps

        @get:JvmName("circle")
        val circle by currentData::circle

        @get:JvmName("circleWasPressed")
        val circleWasPressed = circle && !previousData.circle

        @get:JvmName("circleWasReleased")
        val circleWasReleased = !circle && previousData.circle

        @get:JvmName("cross")
        val cross by currentData::cross

        @get:JvmName("crossWasPressed")
        val crossWasPressed = cross && !previousData.cross

        @get:JvmName("crossWasReleased")
        val crossWasReleased = !cross && previousData.cross

        @get:JvmName("triangle")
        val triangle by currentData::triangle

        @get:JvmName("triangleWasPressed")
        val triangleWasPressed = triangle && !previousData.triangle

        @get:JvmName("triangleWasReleased")
        val triangleWasReleased = !triangle && previousData.triangle

        @get:JvmName("square")
        val square by currentData::square

        @get:JvmName("squareWasPressed")
        val squareWasPressed = square && !previousData.square

        @get:JvmName("squareWasReleased")
        val squareWasReleased = !square && previousData.square

        @get:JvmName("share")
        val share by currentData::share

        @get:JvmName("shareWasPressed")
        val shareWasPressed = share && !previousData.share

        @get:JvmName("shareWasReleased")
        val shareWasReleased = !share && previousData.share

        @get:JvmName("options")
        val options by currentData::options

        @get:JvmName("optionsWasPressed")
        val optionsWasPressed = options && !previousData.options

        @get:JvmName("optionsWasReleased")
        val optionsWasReleased = !options && previousData.options
    }

    private class FilteredHandler(
        val id: Id,
        val handler: Handler<Delta>,
    ) : Handler<Delta> {
        override fun handleEvent(event: Delta) = if (event.id == id) handler.handleEvent(event)
        else EventHandled.Ok

        override fun remove() = handler.remove()
    }

    companion object {
        @JvmStatic
        @Contract(pure = true)
        fun gamepadHandler(
            id: Id,
            handler: Handler<Delta>,
        ): Handler<Delta> = FilteredHandler(
            id,
            handler,
        )

        @JvmStatic
        @Contract(pure = true)
        fun  gamepad1Handler(
            handler: Handler<Delta>,
        ) = gamepadHandler(Id.One, handler)

        @JvmStatic
        @Contract(pure = true)
        fun  gamepad2Handler(
            handler: Handler<Delta>,
        ) = gamepadHandler(Id.Two, handler)
    }
}
