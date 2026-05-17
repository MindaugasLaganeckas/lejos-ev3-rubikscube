package esp32client.physical;

import esp32client.commands.MakeTurnCommand;
import esp32client.enums.RobotStatus;
import esp32client.events.EventBusWrapper;
import esp32client.events.RobotStatusChanged;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.*;
import java.net.Socket;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class MindstormRubiksCubeClient implements Closeable {

    private static final String COMMAND_FINISH = "FINISH";

    private static final Map<String, Integer> communicationCodes = new LinkedHashMap<>() {
        @Serial
        private static final long serialVersionUID = 1L;

        {
            put("B", 1);
            put("B2", 2);
            put("B'", 3);

            put("D", 4);
            put("D2", 5);
            put("D'", 6);

            put("F", 7);
            put("F2", 8);
            put("F'", 9);

            put("L", 10);
            put("L2", 11);
            put("L'", 12);

            put("R", 13);
            put("R2", 14);
            put("R'", 15);

            put("U", 16);
            put("U2", 17);
            put("U'", 18);

            put("UP", 19);
            put("DOWN", 20);

            put("COMPLETED", 21);

            put(COMMAND_FINISH, 100);
        }
    };
    private final EventBusWrapper eventBus;
    private final String address;
    private final int port;
    private final boolean connected = true;
    private Socket socket;
    private DataInputStream din;
    private DataOutputStream dout;
    private boolean debugModeEnabled;

    public static void main(final String[] args) throws Exception {
        try (final MindstormRubiksCubeClient client = new MindstormRubiksCubeClient(new EventBusWrapper(), "192.168.1.130", 3333)) {
            final List<String> list = new LinkedList<>(communicationCodes.keySet());
            final Random rand = new Random();
            int counter = 0;
            while (counter < 20) {
                final String code = list.get(rand.nextInt(list.size() - 1));
                client.sendCommand(code);
                counter++;
            }
        }
    }

    public void connect(final String address, final int port) throws IOException {
        this.socket = new Socket(address, port);
        this.din = new DataInputStream(this.socket.getInputStream());
        this.dout = new DataOutputStream(this.socket.getOutputStream());
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void process(final MakeTurnCommand command) {
        this.eventBus.post(new RobotStatusChanged(RobotStatus.IN_MOTION));
        makeXTurn();
        this.eventBus.post(new RobotStatusChanged(RobotStatus.IDLE));
    }

    public void enableDebugMode(final boolean enableDebugMode) {
        this.debugModeEnabled = enableDebugMode;
    }

    public void makeXTurn() {
        log.info("makeXTurn(). Status {}", this.connected);
    }

    private void sendCommand(final String command) throws IOException {
        if (!this.connected) {
            return;
        }
        System.out.println("Sending " + command);
        if (!communicationCodes.containsKey(command)) {
            System.out.println("Unknown command '" + command + "'");
            return;
        }

        final int code = communicationCodes.get(command);
        this.dout.write(code);
        this.dout.flush();
        final int returnCode = this.din.read();
        if (returnCode != 0) {
            throw new RuntimeException("Server returned " + returnCode);
        }

        if (this.debugModeEnabled) {
            System.in.read();
        }
    }

    @Override
    public void close() throws IOException {
        sendCommand(COMMAND_FINISH);
        if (this.socket != null) {
            this.socket.close();
        }
        if (this.din != null) {
            this.din.close();
        }
        if (this.dout != null) {
            this.dout.close();
        }
    }

    public void turn() {
    }
}
