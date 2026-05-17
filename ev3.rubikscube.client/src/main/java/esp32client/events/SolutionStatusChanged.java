package esp32client.events;

import esp32client.enums.SolutionStatus;

public record SolutionStatusChanged(SolutionStatus status) {
}
