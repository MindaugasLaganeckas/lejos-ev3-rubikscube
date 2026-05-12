package esp32client;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RobustESP32ClientUI extends JFrame {

    final JLabel[] displayLabels = {new JLabel("Waiting for stream...", SwingConstants.CENTER),
            new JLabel("Waiting for stream...", SwingConstants.CENTER)};
    private final List<RobustESP32Client> clients = new ArrayList<>();

    public RobustESP32ClientUI() {
        setTitle("OV3660 Stream Monitor");
        this.setLayout(new GridLayout(1, 2));
        this.add(this.displayLabels[0]);
        this.add(this.displayLabels[1]);
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Add the cleanup hook
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                shutdown();
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

        this.clients.add(new RobustESP32Client("http://192.168.1.88:80/capture", 0, bus, client));
        this.clients.add(new RobustESP32Client("http://192.168.1.89:80/capture", 1, bus, client));
    }

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(RobustESP32ClientUI::new);
    }

    private void shutdown() {
        log.info("Shutting down stream clients...");
        // 1. Stop the polling loops
        for (final RobustESP32Client client : this.clients) {
            client.stop();
        }
        // 2. Unregister from EventBus to prevent memory leaks
        EventBus.getDefault().unregister(this);
        // 3. Optional: If you want the app to exit completely
        System.exit(0);
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void processFrame(final Frame frame) {
        if (frame.cameraIndex() >= 0 && frame.cameraIndex() < this.displayLabels.length) {
            final JLabel label = this.displayLabels[frame.cameraIndex()];
            label.setIcon(new ImageIcon(frame.bufferedImage()));
            label.setText("");
        }
    }
}
