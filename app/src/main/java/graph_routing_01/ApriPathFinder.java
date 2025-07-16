package graph_routing_01;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Stack;
import java.io.Reader;
import java.io.Serializable;

import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.data.FeatureWriter;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.data.Transaction;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.NoSuchAuthorityCodeException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.GeometryCoordinateSequenceTransformer;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.graph.build.feature.FeatureGraphGenerator;
import org.geotools.graph.build.line.LineStringGraphGenerator;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.linearref.LengthIndexedLine;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import graph_routing_01.exceptions.ApriException;
import graph_routing_01.model.ApriEdge;
import graph_routing_01.model.ApriGraph;
import graph_routing_01.model.ApriNode;
import graph_routing_01.model.ApriPath;

public class ApriPathFinder {
    private String ROADNAME_SHP = "RN";
    private String ROADCODE_SHP = "RN_CD";
    private ApriGraph apGraph = null;
    private List<ApriNode> csvNodes = new ArrayList<>();

    private long projCount = 0;
    private long busStopCount = 0;
    

    ApriPathFinder() {

    }

    public ApriPath pathToWGS84(ApriPath path) throws ApriException{
        try{
            CoordinateReferenceSystem wgs84 = CRS.decode("EPSG:4236",false);
            CoordinateReferenceSystem KGD2002 = CRS.decode("EPSG:5179",true);
            MathTransform transform = CRS.findMathTransform(KGD2002, wgs84);
            GeometryCoordinateSequenceTransformer geoTransformer = new GeometryCoordinateSequenceTransformer();
            geoTransformer.setMathTransform(transform);
            ApriPath transformedPath = new ApriPath(path.totalTime); 
            for (LineString ls : path.lineSeg){
                LineString ls2 = (LineString) geoTransformer.transform(ls);
                transformedPath.lineSeg.add(ls2);
            }
            transformedPath.roadName = path.roadName;
            transformedPath.roadType = path.roadType;
            return transformedPath;

        } catch (TransformException | FactoryException e){
        throw new ApriException("ApriPathFinder.pathToWGS84()", e);        

        }
    }

    /**
     * receives coordinate in EPSG a.k.a WGS84
     * transforms them into EPSG:5179 then returns ApriPath
     * @param stLon start longitude
     * @param stLat start latitude
     * @param endLon end longitude
     * @param endLat end latitude
     * @return ApriPath instance, null if path not found
     * @throws ApriException
     */ 
    public ApriPath findPath(double stLon, double stLat, double endLon, double endLat) throws ApriException {
        //default findPath
        return findPath(stLon, stLat, endLon, endLat,0.0,false,0);
    }

    /**
     * receives coordinate in EPSG a.k.a WGS84
     * transforms them into EPSG:5179 then returns ApriPath
     * multiple findPath methods can be called at the same time.
     * Shared memory are not modified. (ApriGraph)
     * @param stLon start longitude
     * @param stLat start latitude
     * @param endLon end longitude
     * @param endLat end latitude
     * @param startTime start time in seconds. in 
     * @param timeCheck check time for bus and subways.
     * @param dayType 1:Saturday, 2:Sundays and holidays. else 0. Usually Korea's transit systems use 3 types of days for operation.
     * @return ApriPath instance, null if path not found
     * @throws ApriException
     */
    public ApriPath findPath(double stLon, double stLat, double endLon, double endLat,double startTime,Boolean timeCheck,int dayType) throws ApriException {
        if (apGraph == null) {
            System.err.println("GraphView must be built in advance.");
            throw new ApriException("GraphView not constructed", null);
        }
        GraphView graphView = new GraphView(this.apGraph);

        CoordinateReferenceSystem wgs84;
        CoordinateReferenceSystem KGD2002;

        try {
            wgs84 = CRS.decode("EPSG:4326", true);
            KGD2002 = CRS.decode("EPSG:5179", true);
        } catch (NoSuchAuthorityCodeException e) {
            System.err.println("EPSG decode fail, no such Authority");
            e.printStackTrace();
            throw new ApriException("EPSG decode failure, no such authority", e);
        } catch (FactoryException e) {
            e.printStackTrace();

            throw new ApriException("EPSG decode failed. FactoryException.", e);
        }

        MathTransform transform = null;
        Coordinate wgsCoordStart = null;
        Coordinate startCoord = null;
        Coordinate wgsCoordEnd = null;
        Coordinate endCoord = null;
        try {
            transform = CRS.findMathTransform(wgs84, KGD2002);
            wgsCoordStart = new Coordinate(stLon, stLat);
            wgsCoordEnd = new Coordinate(endLon, endLat);
            startCoord = JTS.transform(wgsCoordStart, null, transform);
            endCoord = JTS.transform(wgsCoordEnd, null, transform);

        } catch (FactoryException e) {
            e.printStackTrace();
        } catch (TransformException e) {

            e.printStackTrace();
            throw new ApriException("Coordinate transformation failed", e);
        }

        ApriNode stNode = graphView.addStartNode(startCoord);
        ApriNode endNode = graphView.addEndNode(endCoord);

        ApriNode nearEndA = graphView.getEndNodeA();
        ApriNode nearEndB = graphView.getEndNodeB();
        ApriNode endP = graphView.getEndNodeP();
        ApriEdge nearEndAP = graphView.getEndEdgeAP();
        ApriEdge nearEndBP = graphView.getEndEdgeBP();
        ApriEdge nearEndPE = graphView.getEndEdgePE();

        // Check each node and save its minimum path length
        Map<String, Double> visited = new HashMap<>();
        // For each node that has been checked, save where its path is from.
        Map<String, ApriEdge> shortestFrom = new HashMap<>();
        // PrioritiyQueue for Dijkstra's algorithm.
        PriorityQueue<PathState> pq = new PriorityQueue<>();

        Boolean isA = false; // when connected, check if it goes though node A or B in last edge.

        Boolean isPathFound = false;

        PathState startState = new PathState(stNode, "road", "walk", 0.0);
        pq.add(startState);
        visited.put(startState.node.id, 0.0);
        double totalLength = 0;

        while (!pq.isEmpty()) {
            PathState currentPathState = pq.poll();

            ApriNode currentNode = currentPathState.node;
            for (Object edgeO : currentNode.startEdges) {
                ApriEdge searchEdge = (ApriEdge) edgeO;

                // After adding bus routes, edit this part.
                // searchEdge to route time.
                double pathLength = currentPathState.time + searchEdge.walkTLength;
                ApriNode targetNode = searchEdge.target;

                if (targetNode == nearEndA | targetNode == nearEndB) {
                    if (targetNode == nearEndA)
                        isA = true;
                    PathState targetState = new PathState(targetNode, searchEdge.edgeType, searchEdge.edgeTrackName,
                            pathLength);
                    pq.offer(targetState);
                    visited.put(targetNode.id, pathLength);
                    totalLength = pathLength;
                    shortestFrom.put(targetNode.id, searchEdge);
                    isPathFound = true;
                    pq.clear();
                } else if (visited.containsKey(targetNode.id)) {
                    if (visited.get(targetNode.id) > pathLength) {
                        // Only when pathLength is shorten than the previous one.
                        PathState targetState = new PathState(targetNode, searchEdge.edgeType, searchEdge.edgeTrackName,
                                pathLength);
                        pq.offer(targetState);
                        visited.put(targetNode.id, pathLength);
                        shortestFrom.put(targetNode.id, searchEdge);
                    }
                } else { // The node has not been visited.
                    PathState targetState = new PathState(targetNode, searchEdge.edgeType, searchEdge.edgeTrackName,
                            pathLength);
                    pq.offer(targetState);
                    visited.put(targetNode.id, pathLength);
                    shortestFrom.put(targetNode.id, searchEdge);
                }
            }
        }
        if(isPathFound == false) {
            return null;
        }

        ApriNode AorB;
        ApriEdge APorBP;
        if (isA) {
            AorB = nearEndA;
            APorBP = nearEndAP;
        } else {
            AorB = nearEndB;
            APorBP = nearEndBP;
        }

        ApriPath path = new ApriPath(totalLength);
        Stack<ApriEdge> edgeStack = new Stack<>();
        edgeStack.add(nearEndPE);
        edgeStack.add(APorBP);
        ApriNode inverseNode = AorB;

        while (inverseNode != stNode) {
            ApriEdge edge = shortestFrom.get(inverseNode.id);
            edgeStack.add(edge);
            inverseNode = edge.source;
        }
        while (!edgeStack.isEmpty()) {
            ApriEdge edge = edgeStack.pop();
            path.addEdge(edge.geometry, edge.edgeTrackName, edge.edgeType);
        }

        return path;
    }

    public void pathToShp(ApriPath path, String filename) throws ApriException {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();

        typeBuilder.setName("LineFeature");
        CoordinateReferenceSystem crsSave = null;
        try {

            crsSave = CRS.decode("EPSG:5179");
        } catch (NoSuchAuthorityCodeException e) {
            e.printStackTrace();
        } catch (FactoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        typeBuilder.setCRS(crsSave);
        typeBuilder.add("the_geom", LineString.class);
        typeBuilder.add("name", String.class);
        typeBuilder.add("type", String.class);

        SimpleFeatureType featureType = typeBuilder.buildFeatureType();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder((featureType));

        DefaultFeatureCollection featureCollection = new DefaultFeatureCollection(null, featureType);
        for (int i = 0; i < path.lineSeg.size(); i++) {
            featureBuilder.add(path.lineSeg.get(i));
            featureBuilder.add(path.roadName.get(i));
            featureBuilder.add(path.roadType.get(i));
            SimpleFeature feature = featureBuilder.buildFeature(null);
            featureCollection.add(feature);
        }
        SimpleFeatureCollection finalCollection = (SimpleFeatureCollection) featureCollection;
        File outFile = new File(filename);
        try{
            this.writeShapefile(finalCollection, outFile);
        }
        catch (IOException | ApriException e){

            throw new ApriException("IOException while filesaving process. ApriPathFinder.pathToShp()", e);
        }
    }

    private void writeShapefile(SimpleFeatureCollection featureCollection, File outputFile) throws ApriException, IOException {
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
        Map<String, Serializable> params = new HashMap<>();
        params.put("url", outputFile.toURI().toURL());
        params.put("charset", "EUC-KR");
        params.put("create spatial index", Boolean.TRUE);
        ShapefileDataStore dataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        dataStore.createSchema(featureCollection.getSchema());
        Transaction transaction = new DefaultTransaction("create");
        try (FeatureWriter<SimpleFeatureType, SimpleFeature> writer = dataStore.getFeatureWriterAppend(transaction)) {
            try (SimpleFeatureIterator iterator = featureCollection.features()) {
                while (iterator.hasNext()) {
                    SimpleFeature feature = iterator.next();
                    SimpleFeature toWrite = writer.next();
                    toWrite.setAttributes(feature.getAttributes());
                    writer.write();
                }
            }
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new ApriException("File save failed", e);
        } finally {
            transaction.close();
        }
    }

    public void buildBaseGraph(SimpleFeatureCollection collection) throws ApriException {

        LineStringGraphGenerator lineGen = new LineStringGraphGenerator();
        FeatureGraphGenerator fGraphGen = new FeatureGraphGenerator(lineGen);

        // org.geotools.graph.structure.Edge
        Map<Edge, SimpleFeature> edgeFeatureMap = new HashMap<>();
        SimpleFeatureIterator iterator = collection.features();
        while (iterator.hasNext()) {
            SimpleFeature feature = iterator.next();
            Object graphElement = fGraphGen.add(feature);
            if (graphElement instanceof Edge) {
                Edge edge = (Edge) graphElement;
                edgeFeatureMap.put(edge, feature);
            }
        }
        // org.geotools.graph.structure.Graph
        Graph gtGraph = fGraphGen.getGraph();

        Map<String, ApriNode> coordToNodeMap = new HashMap();
        ApriGraph customGraph = new ApriGraph();
        for (Object obj : gtGraph.getNodes()) {
            // org.geotools.graph.structure.Node
            Node gtNode = (Node) obj;
            Coordinate coord = (Coordinate) ((Point) gtNode.getObject()).getCoordinate();
            String nodeKey = "N" + gtNode.getID();
            if (!coordToNodeMap.containsKey(nodeKey)) {
                ApriNode customNode = new ApriNode(nodeKey, coord);
                coordToNodeMap.put(nodeKey, customNode);
                customGraph.addNode(customNode);
            }
        }

        for (Object obj : gtGraph.getEdges()) {
            Edge gtEdge = (Edge) obj;
            Node nodeA = gtEdge.getNodeA();
            Node nodeB = gtEdge.getNodeB();
            Coordinate coordA = (Coordinate) ((Point) nodeA.getObject()).getCoordinate();
            Coordinate coordB = (Coordinate) ((Point) nodeB.getObject()).getCoordinate();

            String keyA = "N" + nodeA.getID();
            String keyB = "N" + nodeB.getID();
            ApriNode sourceNode = coordToNodeMap.get(keyA);
            ApriNode targetNode = coordToNodeMap.get(keyB);

            SimpleFeature feature = edgeFeatureMap.get(gtEdge);
            String RN = (String) feature.getAttribute(ROADNAME_SHP);
            String GN = (String) feature.getAttribute(ROADCODE_SHP);

            Object objLS = gtEdge.getObject();
            SimpleFeature featLS = (SimpleFeature) objLS;
            Object geometryObj = featLS.getDefaultGeometry();
            LineString geometry = null;
            if (geometryObj instanceof LineString) {
                // The SHP files are expected to consist of "MultiLineString" geometries.
                LineString line = (LineString) geometryObj;
                geometry = line;
            } else if (geometryObj instanceof MultiLineString) {
                MultiLineString multiLine = (MultiLineString) geometryObj;
                if (multiLine.getNumGeometries() > 1)
                    throw new ApriException(
                            "The base SHP file must have one single LineString in each of MultiLineString", null);
                for (int i = 0; i < multiLine.getNumGeometries(); i++) {
                    LineString line = (LineString) multiLine.getGeometryN(i);
                    geometry = line;
                    // if(geometry == null) {
                    // geometry = line;
                    // } else if (geometry.getLength() < line.getLength())
                    // geometry = line;
                }
            } else if (geometryObj == null) {
                // PASS, A row can be empty?
            } else {
                throw new ApriException("The SHP file must consist of LineString or MultiLineString", null);
                // should raise error at this point?
            }
            ApriEdge customEdge1 = new ApriEdge("Ea" + gtEdge.getID(), sourceNode, targetNode, geometry, "road", RN,
                    GN);

            ApriEdge customEdge2 = new ApriEdge("Eb" + gtEdge.getID(), targetNode, sourceNode, geometry, "road", RN,
                    GN);
            customGraph.addEdge(customEdge1);
            customGraph.addEdge(customEdge2);
            // node should have reference to its connected edges
            sourceNode.addStartEdge(customEdge1);
            sourceNode.addEndEdge(customEdge1);
            targetNode.addStartEdge(customEdge2);
            targetNode.addEndEdge(customEdge2);

        }
        // Save to memory.
        this.apGraph = customGraph;

    }
    public void addCsvNodes(String filename) throws ApriException{
        File file = new File(filename);
        addCsvNodes(file,null,true);
    }
    public void addCsvNodes(File file, String charSet, Boolean isHeadered) throws ApriException {
        if (charSet == null) {
            charSet = "UTF-8";
        }
        try {
            Reader reader = new InputStreamReader(new FileInputStream(file), charSet);
            CSVReader csvReader = new CSVReader(reader);
            String[] header;
            if (isHeadered)
                header = csvReader.readNext();
            String[] record;
            while ((record = csvReader.readNext()) != null) {

                String s1 = record[0].trim();// name : Bus Stop names
                String s2 = record[1].trim();// BS_NM : Bus Stop code
                String s3 = record[2].trim();// BS_ID : Bus Stop Node ID
                double posX = Double.parseDouble(record[3].trim());
                double posY = Double.parseDouble(record[4].trim());
                // Beaware that (posY, posX) in the coordinate system.
                Coordinate coord = new Coordinate(posY, posX);
                // "B"+s3,coord, name, "BS",BS_ID,BS_NM
                ApriNode bsNode = new ApriNode("B" + s3, coord, s1, "BS", s3, s2);
                csvNodes.add(bsNode);
            }
        } catch (FileNotFoundException e) {
            System.err.println("CSV file not Found.");
            throw new ApriException("CSV file now found in ApriPathFinder.addCsvNodes()", e);
        } catch (CsvValidationException e) {
            System.err.println("Check csv format.");
            e.printStackTrace();
            throw new ApriException("Check csv format", e);

        } catch (NumberFormatException e) {
            System.err.println("CSV have wrong coordinate format.");
            e.printStackTrace();
            throw new ApriException("Check number doubles in csv.", e);

        } catch (IOException e) {
            e.printStackTrace();
            throw new ApriException("Error while opening the file, ApriPathFinder.addCsvNodes()", e);
        }

        for (ApriNode csvNode : csvNodes) {
            ApriEdge nearestEdge1 = this.apGraph.findNearestEdge(csvNode.coord);

            if (nearestEdge1 != null) {
                ApriNode nodeA = nearestEdge1.source;
                ApriNode nodeB = nearestEdge1.target;
                this.apGraph.removeEdge(nearestEdge1);
                for (ApriEdge edge : nodeA.endEdges) {
                    if (edge.target == nodeB & edge.govRoadId.equals(nearestEdge1.govRoadId)) {
                        this.apGraph.removeEdge(edge);
                    }
                }
                LengthIndexedLine indexedLine = new LengthIndexedLine(nearestEdge1.geometry);
                double index = indexedLine.project(csvNode.coord);
                Coordinate projectedCoord = indexedLine.extractPoint(index);
                LineString firstSeg = (LineString) indexedLine.extractLine(0.0, index);
                LineString secondSeg = (LineString) indexedLine.extractLine(index, indexedLine.getEndIndex());

                ApriNode nodeP = new ApriNode("P" + projCount, projectedCoord);
                String eType = "";
                String gId = nearestEdge1.govRoadId;

                ApriEdge edgeAP = new ApriEdge("AP" + this.projCount, nodeA, nodeP, firstSeg, eType, "", gId);
                ApriEdge edgePA = new ApriEdge("PA" + projCount, nodeP, nodeA, firstSeg.reverse(), eType, "", gId);
                ApriEdge edgeBP = new ApriEdge("BP" + this.projCount, nodeB, nodeP, secondSeg.reverse(), eType, "",
                        gId);
                ApriEdge edgePB = new ApriEdge("PB" + this.projCount, nodeP, nodeB,
                        secondSeg, eType, "", gId);

                GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
                LineString lineBSP = geometryFactory.createLineString((new Coordinate[] {
                        new Coordinate(csvNode.coord),
                        new Coordinate(projectedCoord)
                }));
                ApriEdge edgeBSP = new ApriEdge("BSP" + busStopCount, csvNode,
                        nodeP, lineBSP, "road", "", null);
                ApriEdge edgePBS = new ApriEdge("PBS" + busStopCount, nodeP, csvNode, lineBSP.reverse(), "road", "",
                        null);
                nodeA.addStartEdge(edgeAP);
                nodeA.addEndEdge(edgePA);
                nodeB.addStartEdge(edgeBP);
                nodeB.addEndEdge(edgePB);
                csvNode.addStartEdge(edgeBSP);
                csvNode.addEndEdge(edgePBS);

                nodeP.addStartEdge(edgePA);
                nodeP.addEndEdge(edgeAP);
                nodeP.addStartEdge(edgePBS);
                nodeP.addEndEdge(edgeBSP);
                nodeP.addStartEdge(edgePB);
                nodeP.addEndEdge(edgeBP);

                // 6 edges added
                this.apGraph.addEdge(edgeBSP);
                this.apGraph.addEdge(edgePBS);
                this.apGraph.addEdge(edgePA);
                this.apGraph.addEdge(edgeAP);
                this.apGraph.addEdge(edgePB);
                this.apGraph.addEdge(edgeBP);

                // 2 nodes added
                this.apGraph.addNode(csvNode);
                this.apGraph.addNode(nodeP);
            } else {
                // If there are no close edges, Just add orphan nodes.
                this.apGraph.addNode(csvNode);
            }

        }

    }

    /**
     * Overloading, calling default readSHP
     * 
     * @param filename
     */
    public SimpleFeatureCollection readSHP(String filename) throws ApriException {
        File file = new File(filename);
        return readSHP(file);
    }

    public SimpleFeatureCollection readSHP(String filename, String crsName, String charSet) throws ApriException {
        File file = new File(filename);
        return readSHP(file, crsName, charSet);
    }

    /**
     * Overloading, calling default readSHP
     * 
     * @param filename
     * @throws ApriException
     */
    public SimpleFeatureCollection readSHP(File file) throws ApriException {
        return readSHP(file, "EPSG:5174", "EUC-KR");
    }

    public SimpleFeatureCollection readSHP(File file, String crsName, String charSet) throws ApriException {
        Map<String, Object> params = new HashMap<>();
        try {
            params.put("url", file.toURI().toURL());
        } catch (MalformedURLException e) {
            System.err.println("Invalid URL, Check file name.");
            throw new ApriException("MalformedURLException", e);
        }
        params.put("charset", charSet);
        DataStore dataStore;
        try {
            dataStore = DataStoreFinder.getDataStore(params);
        } catch (IOException e) {
            System.err.println("Unable to open file" + params.get("url").toString());
            System.err.println(e.toString());
            throw new ApriException("Unable to open file" + params.get("url"), e);
        }

        SimpleFeatureCollection collection = null;
        try {
            String typeName = dataStore.getTypeNames()[0];
            SimpleFeatureSource source = dataStore.getFeatureSource(typeName);
            collection = source.getFeatures();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return collection;

    }

}
