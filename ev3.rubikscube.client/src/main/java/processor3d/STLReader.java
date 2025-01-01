package processor3d;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class STLReader {

    public List<STLTriangle> readSTLFile(final String filePath) throws IOException {
        final File file = new File(filePath);
        try (final InputStream is = new FileInputStream(file)) {
            return readBinary(is);
        }
    }

    private List<STLTriangle> readBinary(final InputStream is) throws IOException {
        final List<STLTriangle> STLTriangles = new ArrayList<>();
        final DataInputStream dis = new DataInputStream(is);

        final byte[] header = new byte[80];
        dis.readFully(header);

        final int STLTriangleCount = Integer.reverseBytes(dis.readInt());
        for (int i = 0; i < STLTriangleCount; i++) {
            final STLVertex normal = readSTLVertex(dis);
            final STLVertex v1 = readSTLVertex(dis);
            final STLVertex v2 = readSTLVertex(dis);
            final STLVertex v3 = readSTLVertex(dis);
            dis.readUnsignedShort(); // Attribute byte count
            STLTriangles.add(new STLTriangle(v1, v2, v3, new STLNormal(normal)));
        }

        return STLTriangles;
    }

    private STLVertex readSTLVertex(final DataInputStream dis) throws IOException {
        return new STLVertex(
                (int) Float.intBitsToFloat(Integer.reverseBytes(dis.readInt())),
                (int) Float.intBitsToFloat(Integer.reverseBytes(dis.readInt())),
                (int) Float.intBitsToFloat(Integer.reverseBytes(dis.readInt()))
        );
    }
}
