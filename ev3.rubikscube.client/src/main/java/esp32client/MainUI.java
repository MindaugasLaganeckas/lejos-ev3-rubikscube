package esp32client;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.greenrobot.eventbus.EventBus;
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

import static esp32client.Constants.*;

@Slf4j
public class MainUI extends JFrame {

    private final JLabel[] displayLabels = {
            new JLabel("Waiting for stream...", SwingConstants.CENTER),
            new JLabel("Waiting for stream...", SwingConstants.CENTER),
            new JLabel("Waiting for stream...", SwingConstants.CENTER),
    };
    private final int numberOfViews = this.displayLabels.length;

    private final List<Closeable> clients = new ArrayList<>();

    public MainUI() {
        setTitle("OV3660 Stream Monitor");
        this.setLayout(new GridLayout(1, this.numberOfViews));

        this.add(this.displayLabels[LEFT_CAMERA_INDEX]);
        this.add(this.displayLabels[MAIN_CAMERA_INDEX]); // main camera
        this.add(this.displayLabels[RIGHT_CAMERA_INDEX]); // right hand side camera

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

        setVisible(true);
        final EventBus bus = EventBus.getDefault();
        bus.register(this);

        final OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS) // Give it time to handshaking
                .readTimeout(5, TimeUnit.SECONDS) // Give the sensor time to capture
                .retryOnConnectionFailure(true) // Let OkHttp handle minor hiccups
                .build();
        final RubiksColorDetector rubiksColorDetector = new RubiksColorDetector();
        this.clients.add(new RobustLogitechC920Client(new InputStreamProcessor(rubiksColorDetector, LEFT_CAMERA_INDEX, true)));
        this.clients.add(new RobustESP32Client("http://192.168.1.88:80/capture", new InputStreamProcessor(rubiksColorDetector, RIGHT_CAMERA_INDEX, true), client));
        this.clients.add(new RobustESP32Client("http://192.168.1.89:80/capture", new InputStreamProcessor(rubiksColorDetector, MAIN_CAMERA_INDEX, false), client));

    }

    public static void main(final String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        SwingUtilities.invokeLater(MainUI::new);
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void processFrame(final Frame frame) {
        if (frame.cameraIndex() >= 0 && frame.cameraIndex() < this.numberOfViews) {
            final JLabel label = this.displayLabels[frame.cameraIndex()];
            final ImageIcon icon = new ImageIcon(frame.bufferedImage());
            label.setIcon(icon);
            label.setText("");
        }
    }

    private void shutdown() throws IOException {
        log.info("Shutting down stream clients...");
        // 1. Stop the polling loops
        for (final Closeable client : this.clients) {
            client.close();
        }
        // 2. Unregister from EventBus to prevent memory leaks
        EventBus.getDefault().unregister(this);
        // 3. Optional: If you want the app to exit completely
        System.exit(0);
    }
}
