package esp32client.util;

import esp32client.enums.CameraId;
import esp32client.enums.CubeColor;
import esp32client.enums.CubeSides;

import java.awt.*;
import java.util.EnumMap;
import java.util.Map;

public class ScrambledCube {

    private static final Point[] points = {
            new Point(0, 0),
            new Point(0, 1),
            new Point(0, 2),
            new Point(1, 0),
            new Point(1, 1),
            new Point(1, 2),
            new Point(2, 0),
            new Point(2, 1),
            new Point(2, 2),
    };

    private static void mapSideColors(final Map<CubeSides, Map<CameraId, CubeColor[][]>> colorsUnprocessed, final Map<CubeSides, int[]> mapping, final CameraId cameraId, final CubeColor[][] sideRead) {
        for (final CubeSides side : mapping.keySet()) {
            final Map<CameraId, CubeColor[][]> sideColors = colorsUnprocessed.get(side);
            final CubeColor[][] left = sideColors.get(cameraId);
            int counter = 0;
            for (final int id : mapping.get(side)) {
                final Point p = points[id];
                sideRead[p.x][p.y] = left[1][counter];
                counter++;
            }
        }
    }

    /**
     * Prepare scrambledCube as
     * <p>
     * |************|
     * |*U1**U2**U3*|
     * |************|
     * |*U4**U5**U6*|
     * |************|
     * |*U7**U8**U9*|
     * |************|
     * ************|************|************|************|
     * *L1**L2**L3*|*F1**F2**F3*|*R1**R2**R3*|*B1**B2**B3*|
     * ************|************|************|************|
     * *L4**L5**L6*|*F4**F5**F6*|*R4**R5**R6*|*B4**B5**B6*|
     * ************|************|************|************|
     * *L7**L8**L9*|*F7**F8**F9*|*R7**R8**R9*|*B7**B8**B9*|
     * ************|************|************|************|
     * |************|
     * |*D1**D2**D3*|
     * |************|
     * |*D4**D5**D6*|
     * |************|
     * |*D7**D8**D9*|
     * |************|
     * <p>
     * -> U1 U2 ... U9 R1 ... R9 F1 ... F9 D1 ... D9 L1 ... L9 B1 ... B9
     */
    public String getScrambledCube(final Map<CubeSides, Map<CameraId, CubeColor[][]>> colorsUnprocessed) {

        return getSide(colorsUnprocessed.get(CubeSides.UP).get(CameraId.MAIN)) +
                getRightSide(colorsUnprocessed) +
                getSide(colorsUnprocessed.get(CubeSides.FRONT).get(CameraId.MAIN)) +
                getSide(colorsUnprocessed.get(CubeSides.DOWN).get(CameraId.MAIN)) +
                getLeftSide(colorsUnprocessed) +
                getBackSide(colorsUnprocessed.get(CubeSides.BACK).get(CameraId.MAIN));
    }

    private String getRightSide(final Map<CubeSides, Map<CameraId, CubeColor[][]>> colorsUnprocessed) {
        final CubeColor[][] sideRead = new CubeColor[3][3];
        sideRead[1][1] = CubeColor.YELLOW;

        final Map<CubeSides, int[]> mapping = new EnumMap<>(CubeSides.class) {{
            put(CubeSides.FRONT, new int[]{1, 4, 7});
            put(CubeSides.UP, new int[]{3, 2, 1});
            put(CubeSides.BACK, new int[]{9, 6, 3});
            put(CubeSides.DOWN, new int[]{7, 8, 9});
        }};
        mapSideColors(colorsUnprocessed, mapping, CameraId.RIGHT, sideRead);
        return getSide(sideRead);
    }

    private String getLeftSide(final Map<CubeSides, Map<CameraId, CubeColor[][]>> colorsUnprocessed) {
        final CubeColor[][] sideRead = new CubeColor[3][3];
        sideRead[1][1] = CubeColor.WHITE;

        final Map<CubeSides, int[]> mapping = new EnumMap<>(CubeSides.class) {{
            put(CubeSides.FRONT, new int[]{3, 6, 9});
            put(CubeSides.UP, new int[]{1, 2, 3});
            put(CubeSides.BACK, new int[]{7, 4, 1});
            put(CubeSides.DOWN, new int[]{9, 8, 7});
        }};
        mapSideColors(colorsUnprocessed, mapping, CameraId.LEFT, sideRead);
        return getSide(sideRead);
    }

    private String getBackSide(final CubeColor[][] sideRead) {
        final StringBuilder builder = new StringBuilder();
        for (int i = sideRead.length - 1; i >= 0; i--) {
            for (int j = sideRead[i].length - 1; j >= 0; j--) {
                builder.append(sideRead[i][j]);
            }
        }
        return builder.toString();
    }

    private String getSide(final CubeColor[][] sideRead) {
        final StringBuilder builder = new StringBuilder();
        for (final CubeColor[] row : sideRead) {
            for (final CubeColor column : row) {
                builder.append(column.name());
            }
        }
        return builder.toString();
    }

}
