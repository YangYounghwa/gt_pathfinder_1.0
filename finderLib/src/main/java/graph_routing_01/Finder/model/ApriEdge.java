package graph_routing_01.Finder.model;

import org.locationtech.jts.geom.LineString;

public class ApriEdge {
    public final String id;
    public final ApriNode source;
    public final ApriNode target;
    public final LineString geometry;
    public final String edgeType;
    public final double walkTLength;
    public final String govRoadId;
    public final String edgeTrackName;

    public ApriEdge(String id, ApriNode source, ApriNode target,
    LineString geometry, String edgeType, String edgeTrackName,String govRoadId){
        this.id = new String(id);
        this.govRoadId =new String(govRoadId);
        this.source = source;
        this.target = target;
        this.geometry = geometry;
        this.edgeType = new String(edgeType);
        this.edgeTrackName = new String(edgeTrackName);
        this.walkTLength = geometry.getLength(); // length divided by 1m/s
    }


        public ApriEdge(String id, ApriNode source, ApriNode target,
    LineString geometry, String edgeType, String edgeTrackName,String govRoadId,double walkTLength){
        this.id = id;
        this.govRoadId = govRoadId;
        this.source = source;
        this.target = target;
        this.geometry = geometry;
        this.edgeType = edgeType;
        this.edgeTrackName = edgeTrackName;
        this.walkTLength = walkTLength; // length divided by 1m/s
    }


}
