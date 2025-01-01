package ev3.rubikscube.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import processor3d.STLReader;

import java.io.IOException;
import java.util.List;

public class STLViewer extends Application {
    private final Group root = new Group();
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final Group sceneGroup = new Group(); // Group to hold the 3D content
    private double mouseX, mouseY; // Variables to track mouse position
    private double rotateX, rotateY, rotateZ; // Variables to store current rotation angles

    private static MeshView createCube() {
        // Create a custom mesh for the cube
        final TriangleMesh mesh = new TriangleMesh();

        // Define the 8 vertices of the cube
        final float halfSize = 100;
        final float[] points = {
                -halfSize, -halfSize, -halfSize, // 0
                halfSize, -halfSize, -halfSize, // 1
                halfSize, halfSize, -halfSize, // 2
                -halfSize, halfSize, -halfSize, // 3
                -halfSize, -halfSize, halfSize, // 4
                halfSize, -halfSize, halfSize, // 5
                halfSize, halfSize, halfSize, // 6
                -halfSize, halfSize, halfSize  // 7
        };
        mesh.getPoints().addAll(points);

        // Define the texture coordinates (dummy in this case, as we are using materials)
        final float[] texCoords = {
                0, 0
        };
        mesh.getTexCoords().addAll(texCoords);

        // Define the faces of the cube (12 triangles, 6 faces)
        final int[] faces = {
                // Front face
                0, 0, 1, 0, 2, 0,
                0, 0, 2, 0, 3, 0,
                // Back face
                4, 0, 6, 0, 5, 0,
                4, 0, 7, 0, 6, 0,
                // Left face
                0, 0, 3, 0, 7, 0,
                0, 0, 7, 0, 4, 0,
                // Right face
                1, 0, 5, 0, 6, 0,
                1, 0, 6, 0, 2, 0,
                // Top face
                0, 0, 4, 0, 5, 0,
                0, 0, 5, 0, 1, 0,
                // Bottom face
                3, 0, 2, 0, 6, 0,
                3, 0, 6, 0, 7, 0
        };
        mesh.getFaces().addAll(faces);

        // Create a MeshView
        final MeshView cube = new MeshView(mesh);

        // Apply different materials for each face
        final PhongMaterial[] materials = {
                new PhongMaterial(Color.RED),    // Front
                new PhongMaterial(Color.GREEN),  // Back
                new PhongMaterial(Color.BLUE),   // Left
                new PhongMaterial(Color.YELLOW), // Right
                new PhongMaterial(Color.CYAN),   // Top
                new PhongMaterial(Color.MAGENTA) // Bottom
        };

        // Assign materials based on face indices
        cube.setMaterial(materials[0]); // Default material for now

        // Rotate for better visibility
        cube.getTransforms().add(new Rotate(45, Rotate.X_AXIS));
        cube.getTransforms().add(new Rotate(45, Rotate.Y_AXIS));
        return cube;
    }

    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage stage) {
        displayBinarySTL("src/main/resources/ev3/rubikscube/ui/cube.stl");

        // Setup scene
        final Scene scene = new Scene(root, 800, 600, true);
        scene.setFill(Color.LIGHTGRAY);
        scene.setCamera(camera);

        // Setup camera and interactions
        setupCamera(scene);
        setupMouseControls(scene);
        setupKeyControls(scene);

        stage.setScene(scene);
        stage.setTitle("Enhanced STL Viewer");
        stage.show();
    }

    private MeshView createMeshView(final List<STLReader.Triangle> triangles) {
        final TriangleMesh mesh = new TriangleMesh();

        // Add vertices to the mesh
        for (final STLReader.Triangle triangle : triangles) {
            final STLReader.Vertex[] vertices = {triangle.v1, triangle.v2, triangle.v3};
            for (final STLReader.Vertex vertex : vertices) {
                mesh.getPoints().addAll(vertex.x, vertex.y, vertex.z);
            }
        }

        // Add faces (each face is three indices into the points list)
        for (int i = 0; i < triangles.size(); i++) {
            final int offset = i * 3;
            mesh.getFaces().addAll(offset, 0, offset + 1, 0, offset + 2, 0);
        }

        // Add dummy texture coordinates (required by JavaFX)
        mesh.getTexCoords().addAll(0, 0);

        final MeshView meshView = new MeshView(mesh);
        final PhongMaterial material = new PhongMaterial(Color.CORNFLOWERBLUE);
        meshView.setMaterial(material);

        return meshView;
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

    private void createGrid(final Group sceneGroup, final MeshView originalMesh, final double spacing) {
        // Calculate the offset to center the grid
        final double offsetX = (0) * spacing;  // Since the grid is 3x3x3, the center is at (1,1,1)
        final double offsetY = (0) * spacing;
        final double offsetZ = (0) * spacing;

        // Create a 3x3x3 grid of meshes
        for (int i = 0; i < 3; i++) {       // X-axis
            for (int j = 0; j < 3; j++) {   // Y-axis
                for (int k = 0; k < 3; k++) { // Z-axis
                    // Clone the original mesh
                    final MeshView clone = cloneMeshView(originalMesh);

                    // Apply translation to center the grid
                    clone.setTranslateX(i * spacing - offsetX);
                    clone.setTranslateY(j * spacing - offsetY);
                    clone.setTranslateZ(k * spacing - offsetZ);

                    // Add the cloned mesh to the scene group
                    sceneGroup.getChildren().add(clone);
                }
            }
        }
    }

    private MeshView cloneMeshView(final MeshView original) {
        final TriangleMesh originalMesh = (TriangleMesh) original.getMesh();
        final TriangleMesh clonedMesh = new TriangleMesh();

        // Copy points, texCoords, and faces
        clonedMesh.getPoints().addAll(originalMesh.getPoints());
        clonedMesh.getTexCoords().addAll(originalMesh.getTexCoords());
        clonedMesh.getFaces().addAll(originalMesh.getFaces());

        // Create a new MeshView with the cloned mesh
        final MeshView clonedView = new MeshView(clonedMesh);

        // Copy material from the original MeshView
        clonedView.setMaterial(original.getMaterial());

        return clonedView;
    }

    private void displayBinarySTL(final String filePath) {
        final STLReader reader = new STLReader();
        final List<STLReader.Triangle> triangles;

        try {
            triangles = reader.readSTLFile(filePath);
        } catch (final IOException e) {
            e.printStackTrace();
            return;
        }

        // Create the original mesh
        final MeshView originalMesh = createMeshView(triangles);

        // Add a 3x3x3 grid of the mesh to the scene
        final double spacing = 21; // Adjust spacing as needed
        createGrid(sceneGroup, originalMesh, spacing);

        // Add the scene group to the root
        root.getChildren().add(sceneGroup);
    }

    private void rotateMeshesByX(final Group sceneGroup, final int targetX, final double angle) {
        final double spacing = 1.1;
        // Calculate the center of the grid (the midpoint)
        final double centerX = 1 * spacing;  // Since we are using a 3x3 grid centered around the middle point (1, 1, 1)
        final double centerY = 1 * spacing;
        final double centerZ = 1 * spacing;

        // Loop through the sceneGroup and find all meshes with the same X-coordinate
        for (final Node node : sceneGroup.getChildren()) {
            if (node instanceof final MeshView meshView) {
                // Get the current position of the mesh
                final double currentX = meshView.getTranslateX() / spacing; // Convert to grid index
                final double currentY = meshView.getTranslateY() / spacing;
                final double currentZ = meshView.getTranslateZ() / spacing;

                // Check if this mesh's x-coordinate matches the targetX
                if (Double.compare(currentX, targetX) == 0) {
                    // Calculate the rotation angle in the 3D space for the YZ plane rotation
                    rotateAroundAxis(meshView, centerX, centerY, centerZ, angle);
                }
            }
        }
    }

    private void rotateAroundAxis(final MeshView meshView, final double centerX, final double centerY, final double centerZ, final double angle) {
        // Calculate the mesh's current position
        final double x = meshView.getTranslateX();
        final double y = meshView.getTranslateY();
        final double z = meshView.getTranslateZ();

        // Translate mesh to origin, apply rotation, and then translate back
        final double dx = x - centerX;
        final double dy = y - centerY;
        final double dz = z - centerZ;

        // Rotation matrix for rotation around the YZ-plane (perpendicular to the X-axis)
        final double rad = Math.toRadians(angle); // Convert angle to radians
        final double cos = Math.cos(rad);
        final double sin = Math.sin(rad);

        // Apply rotation around the X-axis (rotation matrix for YZ-plane)
        final double newY = cos * dy - sin * dz;
        final double newZ = sin * dy + cos * dz;

        // Update the position of the mesh
        meshView.setTranslateX(centerX + dx);
        meshView.setTranslateY(centerY + newY);
        meshView.setTranslateZ(centerZ + newZ);

        // Rotate the mesh itself by updating its local transformation (so the mesh orientation remains consistent)
        final Rotate rotation = new Rotate(angle, Rotate.X_AXIS); // Rotate around the X-axis
        meshView.getTransforms().add(rotation);
    }

    private void setupKeyControls(final Scene scene) {
        scene.setOnKeyPressed(event -> {
            if ("r".equalsIgnoreCase(event.getText())) {
                // Rotate meshes sharing the same x = 1
                rotateMeshesByX(sceneGroup, 0, 5.0); // Rotate by 5 degrees
            }
        });
    }
}