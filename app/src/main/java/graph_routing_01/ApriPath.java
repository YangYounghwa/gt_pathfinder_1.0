package graph_routing_01;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.LineString;

// What data should be stored in Path... 
// 
// Minimal data is preferred. 
// MultiLineString? 
// MultilineStirng with its information 
// should have a method to export it into json file.
// do we actually need information on nodes? 
// to show on the map, list of linestrings and its attributes are enough? isn't it? 
// I think it is good to add one at a time. 

public class ApriPath {
    
    public List<LineString> lineSeg = new ArrayList<>();
    public List<String> roadName = new ArrayList<>();
    public List<String> roadType = new ArrayList<>();
    public final double totalTime;

    public ApriPath(double totalTime){
        this.totalTime=totalTime;
    }
    public void addEdge(LineString lSeg, String rName,String rType){
        lineSeg.add(lSeg);
        roadName.add(rName);
        roadType.add(rType);
    }

}
