package esp32client;

import esp32client.enums.RobotStatus;
import esp32client.enums.SolutionStatus;
import esp32client.events.EventBusWrapper;
import esp32client.events.RobotStatusChanged;
import esp32client.events.SolutionStatusChanged;
import lombok.RequiredArgsConstructor;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

@RequiredArgsConstructor
public class MainController {

    private final EventBusWrapper eventBus;

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void processFrame(final SolutionStatusChanged statusChanged) {
        if (statusChanged.status() == SolutionStatus.STARTED) {
            // this will trigger color read
            this.eventBus.post(new RobotStatusChanged(RobotStatus.IDLE));
        }
    }
}
