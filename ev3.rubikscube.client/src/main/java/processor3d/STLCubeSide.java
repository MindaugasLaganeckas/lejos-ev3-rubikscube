package processor3d;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import java.util.*;

@Getter
@ToString
@Log4j2
public class STLCubeSide {
    private final List<STLTriangle> triangles;
    private final STLNormal normal;

    STLCubeSide(final STLNormal normal, final STLTriangle t1, final STLTriangle t2) {
        this.normal = normal;
        this.triangles = Collections.unmodifiableList(Arrays.asList(t1, t2));
    }
}
