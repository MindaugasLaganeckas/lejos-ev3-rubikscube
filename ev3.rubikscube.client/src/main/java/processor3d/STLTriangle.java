package processor3d;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
@Getter
@ToString
public class STLTriangle {
    private STLVertex v1, v2, v3;
    private STLNormal normal;

    public List<STLVertex> getVertices() {
        return Collections.unmodifiableList(Arrays.asList(v1, v2, v3));
    }
}
