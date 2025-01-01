package processor3d;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class STLReader {

    public List<Triangle> readSTLFile(final String filePath) throws IOException {
        final File file = new File(filePath);
        try (final InputStream is = new FileInputStream(file)) {
            return readBinary(is);
        }
    }

    private List<Triangle> readBinary(final InputStream is) throws IOException {
        final List<Triangle> triangles = new ArrayList<>();
        final DataInputStream dis = new DataInputStream(is);

        final byte[] header = new byte[80];
        dis.readFully(header);

        final int triangleCount = Integer.reverseBytes(dis.readInt());
        for (int i = 0; i < triangleCount; i++) {
            final Vertex normal = readVertex(dis);
            final Vertex v1 = readVertex(dis);
            final Vertex v2 = readVertex(dis);
            final Vertex v3 = readVertex(dis);
            dis.readUnsignedShort(); // Attribute byte count
            triangles.add(new Triangle(normal, v1, v2, v3));
        }

        return triangles;
    }

    private Vertex readVertex(final DataInputStream dis) throws IOException {
        return new Vertex(
                (int) Float.intBitsToFloat(Integer.reverseBytes(dis.readInt())),
                (int) Float.intBitsToFloat(Integer.reverseBytes(dis.readInt())),
                (int) Float.intBitsToFloat(Integer.reverseBytes(dis.readInt()))
        );
    }

    public static class Vertex {
        public int x, y, z;

        public Vertex(final int x, final int y, final int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static class Triangle {
        public Vertex normal;
        public Vertex v1, v2, v3;

        public Triangle(final Vertex normal, final Vertex v1, final Vertex v2, final Vertex v3) {
            this.normal = normal;
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
        }
    }
}
