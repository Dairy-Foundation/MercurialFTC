package dev.frozenmilk.dairy.mercurial.pedro

import com.pedropathing.localization.Localizer
import com.pedropathing.localization.MotionState
import com.pedropathing.math.Pose
import com.pedropathing.math.Twist
import com.pedropathing.math.Velocity
import dev.frozenmilk.dairy.mercurial.processes.EventManager

class MercurialLocalizer(
    @get:JvmName("localizer")
    val localizer: Localizer,
) : EventManager<MotionState>(), Localizer by localizer {
    override fun eventSource() {
        localizer.update()
        notify(localizer.state())
    }

    override fun setX(x: Double) {
        localizer.setX(x)
    }

    override fun setY(y: Double) {
        localizer.setY(y)
    }

    override fun setHeading(heading: Double) {
        localizer.setHeading(heading)
    }

    override fun pose(): Pose {
        return localizer.pose()
    }

    override fun twist(): Twist {
        return localizer.twist()
    }

    override fun velocity(): Velocity {
        return localizer.velocity()
    }

    override fun debug(): Map<String?, Any?> {
        return localizer.debug()
    }
}
