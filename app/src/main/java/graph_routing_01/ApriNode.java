package graph_routing_01;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;

public class ApriNode {

    public final String id;
    public Coordinate coord;
    public String govNodeId = null; // goverment Node ID
    public String transitID = null; // id for each transit
    public String name = "";
    public String type = "RD";
    public List<ApriEdge> startEdges = new ArrayList<>();
    public List<ApriEdge> endEdges = new ArrayList<>(); 
    public ApriNode(String id, Coordinate coord){
        this.id= id;
        this.coord = coord;
    } 
    public ApriNode(String id, Coordinate coord, String name, String type,String govNodeId,String transitID) {
        this.id = id;
        this.coord = coord;
        this.name = name;
        this.type =type;
        this.govNodeId = govNodeId;
        this.transitID = transitID;
    }
    public void addStartEdge(ApriEdge edge){
        startEdges.add(edge);
    }
    public void deleteStartEdge(ApriEdge edge){
       startEdges.remove(edge); 
    }
    public void addEndEdge(ApriEdge edge){
        endEdges.add(edge);
    }
    public void deleteEndEdge(ApriEdge edge){
        endEdges.remove(edge);
    }
    
}
