package ev3.rubikscube.asyncmessaging;

import ev3.rubikscube.asyncmessaging.events.SolutionReadyEvent;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public final class RobotController {
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onSolution(final SolutionReadyEvent event) {
        System.out.println("Robot executing: " + String.join(" ", event.moves()));
    }
}