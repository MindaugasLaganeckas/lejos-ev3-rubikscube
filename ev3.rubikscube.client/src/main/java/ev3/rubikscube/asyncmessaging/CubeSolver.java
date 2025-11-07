package ev3.rubikscube.asyncmessaging;

import ev3.rubikscube.asyncmessaging.events.ColorsRecognizedEvent;
import ev3.rubikscube.asyncmessaging.events.SolutionReadyEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Map;

public final class CubeSolver {
    
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onColors(final ColorsRecognizedEvent event) {
        final String[] moves = computeSolution(event.colors());
        EventBus.getDefault().post(new SolutionReadyEvent(moves));
    }

    private String[] computeSolution(final Map<String, String> colors) {
        return new String[]{"R", "U", "R'", "U'"};
    }
}