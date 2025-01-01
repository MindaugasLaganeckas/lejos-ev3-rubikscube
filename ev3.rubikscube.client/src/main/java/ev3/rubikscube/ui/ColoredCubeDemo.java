package ev3.rubikscube.ui;

import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public class ColoredCubeDemo extends Application {

    @Override
    public void start(Stage stage) {
        MeshView cube = createCube();

        // Set up a PerspectiveCamera
        Camera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-600);

        // Set up the Scene
        Group root = new Group(cube);
        Scene scene = new Scene(root, 800, 600, true);
        scene.setFill(Color.LIGHTGRAY);
        scene.setCamera(camera);

        // Display the stage
        stage.setTitle("Colored Cube");
        stage.setScene(scene);
        stage.show();
    }

    private static MeshView createCube() {
        // Create a custom mesh for the cube
        TriangleMesh mesh = new TriangleMesh();

        // Define the 8 vertices of the cube
        float halfSize = 100;
        float[] points = {
                -halfSize, -halfSize, -halfSize, // 0
                halfSize, -halfSize, -halfSize, // 1
                halfSize,  halfSize, -halfSize, // 2
                -halfSize,  halfSize, -halfSize, // 3
                -halfSize, -halfSize,  halfSize, // 4
                halfSize, -halfSize,  halfSize, // 5
                halfSize,  halfSize,  halfSize, // 6
                -halfSize,  halfSize,  halfSize  // 7
        };
        mesh.getPoints().addAll(points);

        // Define the texture coordinates (dummy in this case, as we are using materials)
        float[] texCoords = {
                0, 0
        };
        mesh.getTexCoords().addAll(texCoords);

        // Define the faces of the cube (12 triangles, 6 faces)
        int[] faces = {
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
        MeshView cube = new MeshView(mesh);

        // Apply different materials for each face
        PhongMaterial[] materials = {
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

    public static void main(String[] args) {
        launch();
    }
}

