package graph_routing_01.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
// import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.index.quadtree.Quadtree;
// import org.locationtech.jts.index.strtree.STRtree;



/** 
 * Both Spatial Tree and Index search possible.
 */
public class ApriGraph {
    protected final Map<String, ApriNode> nodes = new HashMap<>();
    protected final Map<String, ApriEdge> edges = new HashMap<>();
    private final Quadtree nodeSpatialIndex = new Quadtree();
    // spatialIndex.insert(new Envelope(node.coord), node)
    private final Quadtree edgeSpatialIndex = new Quadtree();
    private final double epsilon = 0.5;

    // tree no need to be exact -> use QuadTree,
    // tree should be able to add or remove.

    // spatialIndex.insert(new Envelope(edge.geometry),edge) (linestring) ))

    // After first query, no editing on the tree


    public ApriNode getNodeById(String id) {
        return nodes.get(id);
    }

    public ApriEdge getEdgeById(String id) {
        return edges.get(id);
    }

    public Collection<ApriNode> getNodes() {
        return nodes.values();
    }

    public Collection<ApriEdge> getEdges() {
        return edges.values();
    }

    public void addNode(ApriNode node) {
        nodes.put(node.id, node);
        Envelope expandedEnv = expandEnvelope(new Envelope(node.coord), epsilon);
        nodeSpatialIndex.insert(expandedEnv, node);
    }

    public void removeNode(ApriNode node) {
        nodes.remove(node.id);
        Envelope expandedEnv = expandEnvelope(new Envelope(node.coord), epsilon);
        nodeSpatialIndex.remove(expandedEnv, node);
    }

    public void removeNode(String id) {
        ApriNode node = nodes.get(id);
        nodes.remove(id);
        Envelope expandedEnv = expandEnvelope(new Envelope(node.coord), epsilon);
        nodeSpatialIndex.remove(expandedEnv, node);
    }

    public void addEdge(ApriEdge edge) {
        edges.put(edge.id, edge);
        edgeSpatialIndex.insert(edge.geometry.getEnvelopeInternal(), edge);
    }

    public void removeEdge(String id) {
        ApriEdge edge = edges.get(id);
        edges.remove(id);
        edgeSpatialIndex.remove(edge.geometry.getEnvelopeInternal(), edge);
    }

    public void removeEdge(ApriEdge edge) {
        edges.remove(edge.id);
        edgeSpatialIndex.remove(edge.geometry.getEnvelopeInternal(), edge);
    }

    public ApriEdge findNearestEdge(Coordinate coord) {
        return findNearestEdge(coord, 500.0);
    }
    

    // must be edited... 
    // check for edge type, and do it for that type.
    public ApriEdge findNearestEdge(Coordinate coord, double radius) {
        Envelope searchEnv = new Envelope(coord);
        searchEnv.expandBy(radius);
        @SuppressWarnings("unchecked")
        List<Object> candidateEdges = edgeSpatialIndex.query(searchEnv);
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        Point point = geometryFactory.createPoint(coord);
        ApriEdge nearestEdge = null;
        double minDistance = radius;

        for (Object edgeO : candidateEdges) {
            ApriEdge edge = (ApriEdge) edgeO;
            double distance = edge.geometry.distance(point);
            if (distance < minDistance || edge.edgeType.equals("road")) {
                minDistance = distance;
                nearestEdge = edge;
            }
        }
        return nearestEdge;
    }

    private static Envelope expandEnvelope(Envelope original, double epsilon) {
        return new Envelope(
                original.getMinX() - epsilon,
                original.getMaxX() + epsilon,
                original.getMinY() - epsilon,
                original.getMaxY() + epsilon);
    }
}
