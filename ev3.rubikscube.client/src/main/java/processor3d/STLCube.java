package processor3d;

import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class STLCube {

    private final List<STLCubeSide> cubeSides = new ArrayList<>();

    @SneakyThrows
    public STLCube() {
        final STLReader reader = new STLReader();
        final List<STLTriangle> triangles = reader.readSTLFile("src/main/resources/ev3/rubikscube/ui/cube.stl");

        for (int i = 0; i < triangles.size(); i += 2) {
            final STLTriangle t1 = triangles.get(i);
            final STLTriangle t2 = triangles.get(i + 1);
            if (t1.getNormal().equals(t2.getNormal())) {
                cubeSides.add(new STLCubeSide(t1.getNormal(), t1, t2));
            } else {
                throw new IllegalStateException();
            }

        }
    }

    public List<STLCubeSide> getCubeSides() {
        return Collections.unmodifiableList(cubeSides);
    }

}
