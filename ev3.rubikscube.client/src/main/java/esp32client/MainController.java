package esp32client;

import esp32client.enums.*;
import esp32client.events.ColorReadCompleted;
import esp32client.events.EventBusWrapper;
import esp32client.events.RobotStatusChanged;
import esp32client.events.SolutionStatusChanged;
import esp32client.physical.MindstormRubiksCubeClient;
import lombok.RequiredArgsConstructor;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class MainController {

    private final EventBusWrapper eventBus;
    private final MindstormRubiksCubeClient robotClient;
    private final CubeSides[] orderOfSidesToRead = {CubeSides.FRONT, CubeSides.UP, CubeSides.BACK, CubeSides.DOWN};
    private int currentSideIndex = 0;
    private Map<CubeSides, List<ColorReadCompleted>> colorsUnprocessed = new HashMap<>();

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void solutionStatusChanged(final SolutionStatusChanged statusChanged) {
        if (statusChanged.status() == SolutionStatus.STARTED) {
            this.colorsUnprocessed = new HashMap<>();
            for (final CubeSides cubeSides : this.orderOfSidesToRead) {
                this.colorsUnprocessed.put(cubeSides, new ArrayList<>());
            }
            this.currentSideIndex = 0;
            // this will trigger color read
            this.eventBus.post(new RobotStatusChanged(RobotStatus.IDLE));
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void colorReadCompleted(final ColorReadCompleted colorReadCompleted) throws IOException {
        final List<ColorReadCompleted> colorReadCompletedList = this.colorsUnprocessed.get(this.orderOfSidesToRead[this.currentSideIndex]);
        colorReadCompletedList.add(colorReadCompleted);
        // have we received readings from all cameras?
        if (colorReadCompletedList.size() == CameraId.values().length) {
            this.currentSideIndex++;
            this.robotClient.makeXTurn();
            // have we finished reading of all sides
            if (this.currentSideIndex == this.orderOfSidesToRead.length) {

            } else {
                this.eventBus.post(new RobotStatusChanged(RobotStatus.IDLE));
            }
        }
    }
}
