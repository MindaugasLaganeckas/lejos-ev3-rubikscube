package esp32client;

import esp32client.enums.CameraId;
import esp32client.enums.SolutionStatus;
import esp32client.events.EventBusWrapper;
import esp32client.events.FrameCreated;
import esp32client.events.SolutionStatusChanged;
import esp32client.physical.MindstormRubiksCubeClient;
import esp32client.physical.RobustESP32Client;
import esp32client.physical.RobustLogitechC920Client;
import esp32client.util.ColorAnalyzer;
import esp32client.util.InputStreamProcessor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.opencv.core.Core;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import java.awt.*;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MainUI extends JFrame {

    // event bus
    private final EventBusWrapper eventBus = new EventBusWrapper();

    private final JLabel[] displayLabels = {
            new JLabel("Waiting for stream...", SwingConstants.CENTER),
            new JLabel("Waiting for stream...", SwingConstants.CENTER),
            new JLabel("Waiting for stream...", SwingConstants.CENTER),
    };
    private final List<Closeable> clients = new ArrayList<>();

    public MainUI() {
        setTitle("OV3660 Stream Monitor");
        this.setLayout(new GridLayout(2, this.displayLabels.length));

        this.add(this.displayLabels[CameraId.LEFT.ordinal()]);
        this.add(this.displayLabels[CameraId.MAIN.ordinal()]); // main camera
        this.add(this.displayLabels[CameraId.RIGHT.ordinal()]);
        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        final JButton startStopButton = new JButton("Start");
        startStopButton.setPreferredSize(new Dimension(80, 30));
        buttonPanel.add(startStopButton);
        this.add(new JPanel());
        this.add(buttonPanel);
        this.add(new JPanel());

        setSize(1000, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Add the cleanup hook
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                try {
                    shutdown();
                } catch (final IOException ex) {
                    log.error("Failed to close resources {}", e);
                }
            }
        });

        this.eventBus.register(this);

        final OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS) // Give it time to handshaking
                .readTimeout(5, TimeUnit.SECONDS) // Give the sensor time to capture
                .retryOnConnectionFailure(true) // Let OkHttp handle minor hiccups
                .build();
        final RubiksColorDetector rubiksColorDetector = new RubiksColorDetector();
        final ColorAnalyzer colorAnalyzer = new ColorAnalyzer();
        final InputStreamProcessor leftProcessor = new InputStreamProcessor(colorAnalyzer, this.eventBus, rubiksColorDetector, CameraId.LEFT);
        this.clients.add(new RobustLogitechC920Client(leftProcessor));
        final InputStreamProcessor rightProcessor = new InputStreamProcessor(colorAnalyzer, this.eventBus, rubiksColorDetector, CameraId.RIGHT);
        this.clients.add(new RobustESP32Client("http://192.168.1.88:80/capture", rightProcessor, client));
        final InputStreamProcessor mainProcessor = new InputStreamProcessor(colorAnalyzer, this.eventBus, rubiksColorDetector, CameraId.MAIN);
        this.clients.add(new RobustESP32Client("http://192.168.1.89:80/capture", mainProcessor, client));

        this.eventBus.register(leftProcessor);
        this.eventBus.register(rightProcessor);
        this.eventBus.register(mainProcessor);

        final MindstormRubiksCubeClient robotClient = new MindstormRubiksCubeClient(this.eventBus, "192.168.1.130", 3333);
        this.eventBus.register(robotClient);
        final MainController mainController = new MainController(this.eventBus);
        this.eventBus.register(mainController);

        // Add a listener for the start/stop button
        startStopButton.addActionListener(e -> toggleCameraStreams());

        setVisible(true);
    }

    public static void main(final String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        SwingUtilities.invokeLater(MainUI::new);
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void processFrame(final FrameCreated frame) {
        final JLabel label = this.displayLabels[frame.cameraId().ordinal()];
        final ImageIcon icon = new ImageIcon(frame.bufferedImage());
        label.setIcon(icon);
        label.setText("");
    }

    private void shutdown() throws IOException {
        log.info("Shutting down stream clients...");
        // 1. Stop the polling loops
        for (final Closeable client : this.clients) {
            client.close();
        }
        // 2. Unregister from EventBus to prevent memory leaks
        this.eventBus.unsubscribeAll();
        // 3. Optional: If you want the app to exit completely
        System.exit(0);
    }

    private void toggleCameraStreams() {
        this.eventBus.post(new SolutionStatusChanged(SolutionStatus.STARTED));
    }
}
