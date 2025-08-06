package graph_routing_01.Finder.exceptions;


/**
 * This exception is used indicate wrong format or unexpected resources.
 */
public class ResException extends Exception{
    public ResException(String errString, Throwable err){
        super(errString, err);
    }
}
