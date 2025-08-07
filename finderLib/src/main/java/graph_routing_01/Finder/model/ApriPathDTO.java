package graph_routing_01.Finder.model;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

public class ApriPathDTO {

    private List<List<List<Double>>> lineSeg; // [[ [x1, y1], [x2, y2], ... ], ...]
    private List<String> roadName;
    private List<String> roadType;
    private double totalTime;

    public ApriPathDTO(ApriPath path) {
        this.totalTime = path.totalTime;
        this.roadName = path.roadName;
        this.roadType = path.roadType;
        this.lineSeg = new ArrayList<>();

        for (LineString line : path.lineSeg) {
            List<List<Double>> segment = new ArrayList<>();
            for (Coordinate coord : line.getCoordinates()) {
                segment.add(List.of(coord.x, coord.y));
            }
            this.lineSeg.add(segment);
        }
    }

    // getters if needed
}