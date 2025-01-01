package ev3.rubikscube.ui;

import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.log4j.Log4j2;
import processor3d.*;

@Log4j2
public class STLViewer extends Application {

    private static final double SPACING = 21; // Adjust SPACING as needed

    private final Group root = new Group();
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final Group sceneGroup = new Group(); // Group to hold the 3D content
    private double mouseX, mouseY; // Variables to track mouse position
    private double rotateX, rotateY, rotateZ; // Variables to store current rotation angles
    private RotateTransition rotateTransition;

    public static void main(final String[] args) {
        launch(args);
    }

    private static Color getColor(final int sideIndex) {
        return switch (sideIndex) {
            case 0 -> Color.BLUE;
            case 1 -> Color.WHITE;
            case 2 -> Color.GREEN;
            case 3 -> Color.RED;
            case 4 -> Color.YELLOW;
            case 5 -> Color.ORANGE;
            default -> throw new IllegalStateException();
        };
    }

    @Override
    public void start(final Stage stage) {
        final Group front = createGrid(sceneGroup);
        sceneGroup.getChildren().add(front);
        root.getChildren().addAll(sceneGroup);

        {
            // Set up a RotateTransition
            rotateTransition = new RotateTransition(Duration.seconds(2), front);
            rotateTransition.setByAngle(90);
            rotateTransition.setCycleCount(1);
            rotateTransition.setAxis(Rotate.Z_AXIS);
            rotateTransition.setAutoReverse(false);
        }

        // Setup scene
        final Scene scene = new Scene(root, 800, 600, true);
        scene.setFill(Color.LIGHTGRAY);
        scene.setCamera(camera);

        // Setup camera and interactions
        setupCamera(scene);
        setupMouseControls(scene);

        stage.setScene(scene);
        stage.setTitle("Enhanced STL Viewer");
        stage.show();
    }

    private Group createCubeMeshView() {
        final STLCube stlCube = new STLCube();
        final Group group = new Group();

        int sumX = 0;
        int sumY = 0;
        int sumZ = 0;
        int counter = 0;
        int sideIndex = 0;
        for (final STLCubeSide side : stlCube.getCubeSides()) {
            final TriangleMesh mesh = new TriangleMesh();
            mesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);
            // vertices
            for (final STLTriangle triangle : side.getTriangles()) {
                for (final STLVertex vertex : triangle.getVertices()) {
                    mesh.getPoints().addAll(vertex.getX(), vertex.getY(), vertex.getZ());
                    sumX += vertex.getX();
                    sumY += vertex.getY();
                    sumZ += vertex.getZ();
                    counter++;
                }
            }

            // normals
            final STLVertex normal = side.getNormal().getNormal();
            mesh.getNormals().addAll(normal.getX(), normal.getY(), normal.getZ());
            // texture
            mesh.getTexCoords().addAll(
                    0, 0
            );
            // faces
            for (int i = 0; i < side.getTriangles().size(); i++) {
                final int offset = i * 3;
                mesh.getFaces().addAll(
                        offset, 0, 0, // Point 0 / Normal 0 / TexCoord 0
                        offset + 1, 0, 0,
                        offset + 2, 0, 0
                );
            }

            final MeshView meshView = new MeshView(mesh);
            final PhongMaterial material =
                    new PhongMaterial(getColor(sideIndex).desaturate().darker());
            meshView.setMaterial(material);
            sideIndex++;
            group.getChildren().add(meshView);
        }
        // center the cube
        group.setTranslateX(group.getTranslateX() - ((double) sumX / counter) - 1.5 * SPACING);
        group.setTranslateY(group.getTranslateY() - ((double) sumY / counter) - 1.5 * SPACING);
        group.setTranslateZ(group.getTranslateZ() - ((double) sumZ / counter) - 1.5 * SPACING);

        return group;
    }


    private Group createGrid(final Group sceneGroup) {
        // Calculate the offset to center the grid
        final double offsetX = (0) * SPACING;  // Since the grid is 3x3x3, the center is at (1,1,1)
        final double offsetY = (0) * SPACING;
        final double offsetZ = (0) * SPACING;

        final Group front = new Group();
        // Create a 3x3x3 grid of meshes
        for (int i = 0; i < 3; i++) {       // X-axis
            for (int j = 0; j < 3; j++) {   // Y-axis
                for (int k = 0; k < 3; k++) { // Z-axis
                    // Clone the original mesh
                    final Group clone = createCubeMeshView();

                    // Apply translation to center the grid
                    clone.setTranslateX(clone.getTranslateX() + i * SPACING + offsetX);
                    clone.setTranslateY(clone.getTranslateY() + j * SPACING + offsetY);
                    clone.setTranslateZ(clone.getTranslateZ() + k * SPACING + offsetZ);

                    // Add the cloned mesh to the scene group
                    sceneGroup.getChildren().add(clone);

                    if (k == 2) {
                        front.getChildren().add(clone);
                    }
                }
            }
        }
        return front;
    }

    private void setupCamera(final Scene scene) {
        camera.setTranslateZ(-500);
        camera.setNearClip(0.1);
        camera.setFarClip(2000);

        // Zoom with mouse scroll
        scene.addEventHandler(ScrollEvent.SCROLL, event -> {
            final double zoomFactor = event.getDeltaY() > 0 ? 0.9 : 1.1;
            camera.setTranslateZ(camera.getTranslateZ() * zoomFactor);
        });
    }

    private void setupMouseControls(final Scene scene) {
        final Rotate rotateXTransform = new Rotate(0, Rotate.X_AXIS);
        final Rotate rotateYTransform = new Rotate(0, Rotate.Y_AXIS);
        final Rotate rotateZTransform = new Rotate(0, Rotate.Z_AXIS);

        // Add all transformations to the scene group
        sceneGroup.getTransforms().addAll(rotateXTransform, rotateYTransform, rotateZTransform);

        scene.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() || event.isSecondaryButtonDown()) {
                // Track mouse position when either button is pressed
                mouseX = event.getSceneX();
                mouseY = event.getSceneY();
                log.info("start rotation");
                rotateTransition.play();
            }
        });

        scene.setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown()) {
                // Left mouse button: Rotate X and Y axes
                final double deltaX = event.getSceneX() - mouseX;
                final double deltaY = event.getSceneY() - mouseY;

                rotateX += deltaY * 0.2;
                rotateY += deltaX * 0.2;

                rotateXTransform.setAngle(rotateX);
                rotateYTransform.setAngle(rotateY);

                mouseX = event.getSceneX();
                mouseY = event.getSceneY();
            } else if (event.isSecondaryButtonDown()) {
                // Right mouse button: Rotate Z axis
                final double deltaX = event.getSceneX() - mouseX;

                rotateZ += deltaX * 0.2; // Rotate based on horizontal movement
                rotateZTransform.setAngle(rotateZ);

                mouseX = event.getSceneX(); // Update mouse position
            }
        });
    }
}