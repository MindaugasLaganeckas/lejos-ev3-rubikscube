package ev3.rubikscube.asyncmessaging;

import org.greenrobot.eventbus.EventBus;

import javax.swing.*;
import java.util.concurrent.*;

public class RubiksCubeAppWithLeasedUI {

    public static void main(final String[] args) {
        final int width = 320, height = 240;
        final int poolSize = 3, workerCount = 4;

        final ExecutorService eventExec = Executors.newVirtualThreadPerTaskExecutor();
        final EventBus bus = EventBus.builder()
                .executorService(eventExec)
                .installDefaultEventBus();

        final FrameBufferPool pool = new FrameBufferPool(poolSize, width, height);
        final BlockingQueue<FrameBuffer> queue = new ArrayBlockingQueue<>(8);

        // UI setup
        final JLabel label = new JLabel();
        final JFrame frame = new JFrame("Rubik's Cube (Leased UI + Structured)");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(label);
        frame.setSize(width + 20, height + 40);
        frame.setVisible(true);

        // EventBus subscribers
        bus.register(new FrameDispatcher(queue));
        bus.register(new UIRenderer(label, pool));
        bus.register(new CubeSolver());
        bus.register(new RobotController());

        final CameraReader camera = new CameraReader(pool, bus);

        // Shared shutdown signal
        final var stopSignal = new CompletableFuture<Void>();
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(final java.awt.event.WindowEvent e) {
                stopSignal.complete(null);
            }
        });

        try (final var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            scope.fork(() -> {
                camera.run();
                return null;
            });
            for (int i = 0; i < workerCount; i++)
                scope.fork(() -> {
                    new FrameWorker(queue, pool, bus).run();
                    return null;
                });

            // Wait for either stop signal or any failure
            stopSignal.join(); // waits until window closes

            System.out.println("User requested stop...");
            camera.stop(); // signal camera loop to exit
            scope.shutdown(); // ✅ legal now (same thread as owner)
            scope.join();     // wait for all forked tasks to finish
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            eventExec.shutdownNow();
            System.out.println("Application exited cleanly.");
        }
    }

}