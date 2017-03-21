package ca.polymtl.inf4410.tp2.shared;

public class ServerCrashException  extends Exception{

	private static final long serialVersionUID = -5062957203133357685L;

	public ServerCrashException(String message){
		super(message);
	}

	@Override
	public String toString() {
		return this.getMessage();
	} 

}

