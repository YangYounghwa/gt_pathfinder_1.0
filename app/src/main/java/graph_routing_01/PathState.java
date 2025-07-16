package graph_routing_01;

import graph_routing_01.model.ApriNode;

/**
 *  PathState used for priority queue in Dijkstra's shortest path search.
 *  PathState.prevType, PathState.prevName are used for transfer (bus, subway)
 *  PathState.time indicates path length in Dijkstra's path length
 */
public class PathState implements Comparable<PathState> {
    ApriNode node;
    String prevType;
    String prevName; //save bus name;
    double time;

    PathState(ApriNode node, String prevType,String prevName, double time) {
        this.node = node;
        this.prevType = prevType;
        this.prevName = prevName;
        this.time = time;
    }

    public int compareTo(PathState o) {
        return Double.compare(this.time, o.time);

    }

}
