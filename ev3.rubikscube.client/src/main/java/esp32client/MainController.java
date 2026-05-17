package esp32client;

import esp32client.commands.MakeTurnCommand;
import esp32client.commands.ReadColorsCommand;
import esp32client.enums.*;
import esp32client.events.ColorReadCompleted;
import esp32client.events.EventBusWrapper;
import esp32client.events.RobotStatusChanged;
import esp32client.events.SolutionStatusChanged;
import lombok.RequiredArgsConstructor;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.*;

@RequiredArgsConstructor
public class MainController {

    public static final String X_TURN = "XTurn";
    public static final String SOLUTION = "solution";
    private final EventBusWrapper eventBus;
    private final CubeSides[] orderOfSidesToRead = {CubeSides.FRONT, CubeSides.UP, CubeSides.BACK, CubeSides.DOWN};
    private final Object lock = new Object();
    private int currentSideIndex = 0;
    private boolean readColorsInProgress = false;
    private Map<CubeSides, Map<CameraId, CubeColor[][]>> colorsUnprocessed = new EnumMap<>(CubeSides.class);

    private static boolean allCamerasFinishedReading(final Map<CameraId, CubeColor[][]> colorReadCompletedList) {
        return colorReadCompletedList.size() == CameraId.values().length;
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void solutionStatusChanged(final SolutionStatusChanged statusChanged) {
        Object commandToPost = null;
        synchronized (this.lock) {
            if (statusChanged.status() == SolutionStatus.STARTED) {
                reset();
                this.readColorsInProgress = true;
                commandToPost = new ReadColorsCommand();
            }
        }
        this.eventBus.post(commandToPost);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void robotStateChanged(final RobotStatusChanged event) {
        Object commandToPost = null;
        synchronized (this.lock) {
            if (!this.readColorsInProgress) {
                return;
            }
            if (event.status() == RobotStatus.IDLE) {
                commandToPost = new ReadColorsCommand();
            }
        }
        this.eventBus.post(commandToPost);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void colorReadCompleted(final ColorReadCompleted colorReadCompleted) {
        Object commandToPost = null;
        synchronized (this.lock) {
            if (!this.readColorsInProgress) {
                return;
            }
            final Map<CameraId, CubeColor[][]> colorReadCompletedList = this.colorsUnprocessed.get(this.orderOfSidesToRead[this.currentSideIndex]);
            colorReadCompletedList.put(colorReadCompleted.cameraId(), colorReadCompleted.cubeColors());
            if (allCamerasFinishedReading(colorReadCompletedList)) {
                this.currentSideIndex++;
                commandToPost = new MakeTurnCommand(X_TURN);
                if (allSidesReadCompleted()) {
                    commandToPost = new MakeTurnCommand(SOLUTION);
                    this.readColorsInProgress = false;
                }
            }
        }
        this.eventBus.post(commandToPost);
    }

    private boolean allSidesReadCompleted() {
        return this.currentSideIndex == this.orderOfSidesToRead.length;
    }

    private void reset() {
        this.colorsUnprocessed = new EnumMap<>(CubeSides.class);
        for (final CubeSides cubeSides : this.orderOfSidesToRead) {
            this.colorsUnprocessed.put(cubeSides, new EnumMap<>(CameraId.class));
        }
        this.currentSideIndex = 0;
    }
}
