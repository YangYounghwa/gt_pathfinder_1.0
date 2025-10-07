package graph_routing_01.Finder.model;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;


/**
 * A Data Transfer Object for ApriPath.
 * This class has no external dependencies beyond the standard Java library.
 * It represents the path data in a simple, serializable format
 * (lists, strings, and primitives) that any JSON library can easily handle.
 */
public class ApriPathDTO {

    // The structure is a List of LineStrings, where each LineString is a List of Points,
    // and each Point is a List of coordinates [x, y].
    // e.g., [[[x1, y1], [x2, y2]], [[x3, y3], [x4, y4]]]
    private List<List<List<Double>>> lineSegList;
    private List<String> roadNameList;
    private List<String> roadTypeList;
    private double totalTime; // in seconds

    /**
     * No-argument constructor is often required by JSON deserialization libraries like Jackson or Gson.
     */
    public ApriPathDTO() {
    }

    // --- Getters ---

    public List<List<List<Double>>> getLineSegList() {
        return lineSegList;
    }

    public List<String> getRoadNameList() {
        return roadNameList;
    }

    public List<String> getRoadTypeList() {
        return roadTypeList;
    }

    public double getTotalTime() {
        return totalTime;
    }

    // --- Setters (useful for building the object, e.g., during deserialization) ---

    public void setLineSegList(List<List<List<Double>>> lineSegList) {
        this.lineSegList = lineSegList;
    }

    public void setRoadNameList(List<String> roadNameList) {
        this.roadNameList = roadNameList;
    }

    public void setRoadTypeList(List<String> roadTypeList) {
        this.roadTypeList = roadTypeList;
    }

    public void setTotalTime(double totalTime) {
        this.totalTime = totalTime;
    }
}