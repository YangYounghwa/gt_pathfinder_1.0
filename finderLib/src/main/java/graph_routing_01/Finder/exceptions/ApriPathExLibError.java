package graph_routing_01.Finder.exceptions;


/**
 * This exception is used to indicate errors that occur during pathfinding
 * operations, particularly those involving interactions with external libraries.
 */
public class ApriPathExLibError extends Exception{
    public ApriPathExLibError(String errString, Throwable err) {
        super(errString, err);
    }

    public ApriPathExLibError(String errString) {
        super(errString);
    }

}
