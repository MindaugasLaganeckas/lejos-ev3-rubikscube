package esp32client.util;

import esp32client.enums.CubeColor;

import java.util.*;

public class ColorAnalyzer {

    public CubeColor[][] mostFrequentPerCell(final List<CubeColor[][]> colorReads) {
        if (colorReads.isEmpty()) {
            return new CubeColor[0][0];
        }

        final int rows = colorReads.get(0).length;
        final int cols = colorReads.get(0)[0].length;

        final CubeColor[][] result = new CubeColor[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {

                final Map<CubeColor, Integer> counts = new EnumMap<>(CubeColor.class);

                for (final CubeColor[][] read : colorReads) {
                    final CubeColor color = read[i][j];
                    counts.merge(color, 1, Integer::sum);
                }

                CubeColor best = null;
                int max = -1;

                for (final Map.Entry<CubeColor, Integer> e : counts.entrySet()) {
                    if (e.getValue() > max) {
                        max = e.getValue();
                        best = e.getKey();
                    }
                }
                result[i][j] = best;
            }
        }
        return result;
    }
}