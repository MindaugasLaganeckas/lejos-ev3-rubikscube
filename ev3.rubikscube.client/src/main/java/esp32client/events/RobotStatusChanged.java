package esp32client.events;

import esp32client.enums.RobotStatus;

public record RobotStatusChanged(RobotStatus status) {
}
