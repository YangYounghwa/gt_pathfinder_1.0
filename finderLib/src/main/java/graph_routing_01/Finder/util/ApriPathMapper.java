package graph_routing_01.Finder.util;


import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import graph_routing_01.Finder.model.ApriPath;
import graph_routing_01.Finder.model.ApriPathDTO;

/**
 * A utility class to map between ApriPath domain objects and ApriPathDTOs.
 * This class isolates the conversion logic.
 */
public final class ApriPathMapper {

    // Make the constructor private so it cannot be instantiated.
    private ApriPathMapper() {}

    /**
     * Converts an ApriPath domain object into its DTO representation.
     */
    public static ApriPathDTO toDTO(ApriPath path) {
        if (path == null) {
            return null;
        }

        ApriPathDTO dto = new ApriPathDTO();
        dto.setTotalTime(path.totalTime);
        dto.setRoadNameList(new ArrayList<>(path.roadName));
        dto.setRoadTypeList(new ArrayList<>(path.roadType));
        
        List<List<List<Double>>> lineSegments = new ArrayList<>();
        for (LineString line : path.lineSeg) {
            List<List<Double>> singleSegment = new ArrayList<>();
            for (Coordinate coord : line.getCoordinates()) {
                singleSegment.add(List.of(coord.getX(), coord.getY()));
            }
            lineSegments.add(singleSegment);
        }
        dto.setLineSegList(lineSegments);
        
        return dto;
    }

    /**
     * Converts an ApriPathDTO back into an ApriPath domain object.
     * This is useful when receiving data from an API.
     * Note: Creating LineString objects requires a GeometryFactory.
     */
    public static ApriPath fromDTO(ApriPathDTO dto, GeometryFactory geometryFactory) {
        if (dto == null) {
            return null;
        }

        ApriPath path = new ApriPath(dto.getTotalTime());
        
        // Assuming the lists in the DTO are not null and have the same size.
        // You might want to add error handling here.
        List<List<List<Double>>> lineSegments = dto.getLineSegList();
        for (int i = 0; i < lineSegments.size(); i++) {
            List<List<Double>> singleSegment = lineSegments.get(i);
            
            Coordinate[] coords = new Coordinate[singleSegment.size()];
            for (int j = 0; j < singleSegment.size(); j++) {
                List<Double> point = singleSegment.get(j);
                coords[j] = new Coordinate(point.get(0), point.get(1));
            }
            
            LineString lineString = geometryFactory.createLineString(coords);
            
            path.addEdge(
                lineString,
                dto.getRoadNameList().get(i),
                dto.getRoadTypeList().get(i)
            );
        }
        
        return path;
    }
}