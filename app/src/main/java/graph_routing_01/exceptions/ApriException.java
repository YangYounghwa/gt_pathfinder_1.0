package graph_routing_01.exceptions;


/**
 * Nothing Special, just indicating that this error are predicted at dev process.
 * Other exceptions are not predicted, which can lead to critical failure.
 */
public class ApriException extends Exception{
    public ApriException(String errString, Throwable err){
        super(errString, err);
    }
}
