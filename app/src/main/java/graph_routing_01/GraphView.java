package graph_routing_01;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.linearref.LengthIndexedLine;

import graph_routing_01.exceptions.ApriException;
import graph_routing_01.model.ApriEdge;
import graph_routing_01.model.ApriGraph;
import graph_routing_01.model.ApriNode;

public class GraphView {
    private final ApriGraph baseGraph;
    private final Map<String, ApriNode> virtualNodes = new HashMap<>();
    private final Map<String, ApriEdge> virtualEdges = new HashMap<>();

    private ApriEdge endBP;
    private ApriEdge endAP;
    private ApriEdge endPE;
    private ApriNode endNodeP;
    private ApriNode endNodeE;
    private ApriNode endNodeA;
    private ApriNode endNodeB;

    public GraphView(ApriGraph baseGraph) {
        this.baseGraph = baseGraph;
    }

    public ApriNode getNodeById(String id) {
        if (virtualNodes.containsKey(id))
            return virtualNodes.get(id);
        return this.baseGraph.getNodeById(id);
    }

    public ApriEdge getEdgeById(String id) {
        if (virtualEdges.containsKey(id))
            return virtualEdges.get(id);
        return this.baseGraph.getEdgeById(id);
    }

    public Collection<ApriNode> getNodes() {
        List<ApriNode> combined = new ArrayList<>(this.baseGraph.getNodes());
        combined.addAll(virtualNodes.values());
        return combined;
    }

    public Collection<ApriEdge> getEdges() {
        List<ApriEdge> combined = new ArrayList<>(baseGraph.getEdges());
        combined.addAll(virtualEdges.values());
        return combined;
    }

    private void addVirtualNode(ApriNode node) {
        virtualNodes.put(node.id, node);
    }

    private void addVirtualEdge(ApriEdge edge) {
        virtualEdges.put(edge.id, edge);
    }

    public void clearVirtuals() {
        virtualNodes.clear();
        virtualEdges.clear();
    }

    public ApriEdge findNearestEdge(Coordinate coord) {
        return this.baseGraph.findNearestEdge(coord);
    }

    public ApriEdge findNearestEdge(Coordinate coord, double radius) {
        return this.baseGraph.findNearestEdge(coord, radius);
    }

    public ApriNode addStartNode(Coordinate coord) throws ApriException {
        ApriNode startNode = new ApriNode("START", coord);
        ApriEdge nearestEdge = baseGraph.findNearestEdge(coord);
        ApriEdge oppositEdge;
        if (nearestEdge == null)
            {System.err.println("Error on start : no nearest node found");
            throw new ApriException("No nearest node found. GraphView.addStartNode()", null);}
        ApriNode nodeA = nearestEdge.source;
        ApriNode nodeB = nearestEdge.target;
        for (ApriEdge edge : nodeA.endEdges) {
            if (edge.target == nodeB & edge.govRoadId.equals(nearestEdge.govRoadId)) {
                oppositEdge = edge;
            }
        }
        LengthIndexedLine indexedLine = new LengthIndexedLine(nearestEdge.geometry);
        double index = indexedLine.project(coord);
        Coordinate projectedCoord = indexedLine.extractPoint(index);
        LineString firstSeg = (LineString) indexedLine.extractLine(0.0, index);
        LineString secondSeg = (LineString) indexedLine.extractLine(index, indexedLine.getEndIndex());
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        LineString lineSP = geometryFactory.createLineString((new Coordinate[] {
                new Coordinate(startNode.coord),
                new Coordinate(projectedCoord)
        }));

        ApriNode nodeP = new ApriNode("ST_BRANCH", projectedCoord);
        ApriEdge edgeSP = new ApriEdge("SP", startNode, nodeP, lineSP, "road", "",null);
        ApriEdge edgePB = new ApriEdge("ST_PB", nodeP, nodeB, secondSeg, nearestEdge.edgeType, "",nearestEdge.govRoadId);
        ApriEdge edgePA = new ApriEdge("ST_BP", nodeP, nodeA, firstSeg.reverse(), nearestEdge.edgeType,
                "",nearestEdge.govRoadId);

        this.addVirtualEdge(edgeSP);
        this.addVirtualEdge(edgePB);
        this.addVirtualEdge(edgePA);
        this.addVirtualNode(startNode);
        this.addVirtualNode(nodeP);
        startNode.addStartEdge(edgeSP);
        nodeP.addStartEdge(edgePB);
        nodeP.addStartEdge(edgePA);

        return startNode;
    }

    public ApriNode addEndNode(Coordinate coord) throws ApriException {
        // create 2 nodes and 3 edges.
        // make three methods to return edges.
        // make a method to return p node.
        // this method should return P node.
        this.endNodeE = new ApriNode("START", coord);
        ApriEdge nearestEdge = baseGraph.findNearestEdge(coord);
        ApriEdge oppositEdge;
        if (nearestEdge == null){
            System.err.println("no ENDING FOUND!!");
            throw new ApriException("GVAE1 : No Ending Found", null);}
        ApriNode nodeA = nearestEdge.source;
        this.endNodeA = nodeA;
        ApriNode nodeB = nearestEdge.target;
        this.endNodeB = nodeB;
        for (ApriEdge edge : nodeA.endEdges) {
            if (edge.target == nodeB & edge.govRoadId.equals(nearestEdge.govRoadId)) {
                oppositEdge = edge;
            }
        }
        LengthIndexedLine indexedLine = new LengthIndexedLine(nearestEdge.geometry);
        double index = indexedLine.project(coord);
        Coordinate projectedCoord = indexedLine.extractPoint(index);

        // endNodeP created

        this.endNodeP = new ApriNode("endNodeP", projectedCoord);

        LineString firstSeg = (LineString) indexedLine.extractLine(0.0, index);
        LineString secondSeg = (LineString) indexedLine.extractLine(index, indexedLine.getEndIndex());
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        LineString linePE = geometryFactory.createLineString((new Coordinate[] {

                new Coordinate(projectedCoord),
                new Coordinate(this.endNodeE.coord)
        }));
        this.endAP = new ApriEdge("endAP", nodeA, this.endNodeP, firstSeg, "road", "",null);
        this.endBP = new ApriEdge("endBP", nodeB, this.endNodeP, secondSeg.reverse(), "road","", null);
        this.endPE = new ApriEdge("endPE", this.endNodeP, this.endNodeE, linePE, "road","", null);

        return this.endNodeE;
    }
    public ApriNode getEndNodeP() throws ApriException{
        if (this.endNodeP == null){
            throw new ApriException("GVGE1 : End node not Found", null);
        }
        else{
            return this.endNodeP;
        }
    }
    public ApriNode getEndNodeA(){
        if(this.endNodeA==null){
            return null;
        } else {
            return this.endNodeA;
        }
    }
    public ApriNode getEndNodeB(){
        if(this.endNodeB==null){
            return null;
        } else {
            return this.endNodeB;
        }
    }
    public ApriEdge getEndEdgeAP(){
        if(this.endAP == null);
            ;//throws error
        return this.endAP;
    }
    public ApriEdge getEndEdgeBP(){
        if(this.endBP == null);
            ;//thorws error
        return this.endBP;
    }
    public ApriEdge getEndEdgePE(){
        if(this.endPE == null);
            ;;// throws error
            return this.endPE;
    }

    // end must have 4 new things.
    // end node, end split, 2 splited edges.
    // end Node A, end Node B
    //

}
