package esp32client;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A robust MJPEG-like stream client that fetches snapshots sequentially.
 */
@Slf4j
public class RobustESP32Client implements Closeable {
    private final String url;
    private final InputStreamProcessor inputStreamProcessor;
    private final OkHttpClient client;

    // Each instance gets its own executor to manage its specific loop
    private final ScheduledExecutorService executor;
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);
    private volatile boolean isRunning = true;

    public RobustESP32Client(final String url, final InputStreamProcessor inputStreamProcessor, final OkHttpClient sharedClient) {
        this.url = url;
        this.inputStreamProcessor = inputStreamProcessor;
        this.client = sharedClient;

        // Single thread ensures we aren't decoding two frames from the SAME camera at once
        this.executor = Executors.newSingleThreadScheduledExecutor();
        // Kick off the loop
        startStreaming();
    }

    private void startStreaming() {
        if (this.isRunning) {
            fetchSnapshot();
        }
    }

    private void fetchSnapshot() {
        // Safety gate: prevent overlapping requests for this specific camera
        if (!this.isRunning || !this.isProcessing.compareAndSet(false, true)) {
            return;
        }

        final Request request = new Request.Builder()
                .url(this.url)
                .header("Connection", "close")
                .build();

        this.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull final Call call, @NotNull final IOException e) {
                RobustESP32Client.this.isProcessing.set(false);
                if (RobustESP32Client.this.isRunning) {
                    // Network hiccup? Wait 1 second and try again to avoid spamming
                    RobustESP32Client.this.executor.schedule(() -> fetchSnapshot(), 1000, TimeUnit.MILLISECONDS);
                }
            }

            @Override
            public void onResponse(@NotNull final Call call, @NotNull final Response response) {
                try (final ResponseBody body = response.body()) {
                    if (response.isSuccessful() && body != null) {
                        RobustESP32Client.this.inputStreamProcessor.processStream(body.byteStream());
                    }
                } finally {
                    RobustESP32Client.this.isProcessing.set(false);
                    if (RobustESP32Client.this.isRunning) {
                        // Immediately schedule the next frame
                        RobustESP32Client.this.executor.execute(() -> fetchSnapshot());
                    }
                }
            }
        });
    }

    @Override
    public void close() throws IOException {
        this.isRunning = false;
        this.executor.shutdownNow();
    }
}