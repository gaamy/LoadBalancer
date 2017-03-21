package ca.polymtl.inf4410.tp2.shared;

public class ServerOverloadedException extends Exception{

	private static final long serialVersionUID = 1950747955898445257L;

	public ServerOverloadedException(String message){
		super(message);
	}

	@Override
	public String toString() {
		return this.getMessage();
	} 

}
