package graph_routing_01.exceptions;


/**
 * This exception is used indicate wrong format or unexpectes resources.
 */
public class ResException extends Exception{
    public ResException(String errString, Throwable err){
        super(errString, err);
    }
}
