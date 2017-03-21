package ca.polymtl.inf4410.tp2.loadbalancer.exceptions;

public class IllegalOperationException extends Exception { 

	private static final long serialVersionUID = -6758320892592231968L;

	public IllegalOperationException(String message){
		super(message);
	}

	@Override
	public String toString() {
		return this.getMessage();
	} 

}
