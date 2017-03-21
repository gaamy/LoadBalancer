package ca.polymtl.inf4410.tp2.loadbalancer.exceptions;

public class VerificationDoneException extends Exception{
	
	private static final long serialVersionUID = 2755697852551595914L;

	public VerificationDoneException(String message){
		super(message);
	}

	@Override
	public String toString() {
		return this.getMessage();
	} 

}
