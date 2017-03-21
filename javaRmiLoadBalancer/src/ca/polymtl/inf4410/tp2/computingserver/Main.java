package ca.polymtl.inf4410.tp2.computingserver;

public class Main {
	public static void main(String[] args){
		ComputingServerImpl computingServer;
		if(args.length != 4){
			System.out.println("Parametrees manquants : ./calculousServer CAPACITY FAITHTHRESHOLD IP PORT ");
			System.out.println("Lunching server with default configurations:(3,0,127.0.0.1, 5010)");
			computingServer = new ComputingServerImpl(3,0,"127.0.0.1", 5010);
		}else{
			int capacity = Integer.parseInt(args[0]);
			int faithThreshold = Integer.parseInt(args[1]);
			String ip = args[2];
			int port = Integer.parseInt(args[3]);
			computingServer = new ComputingServerImpl(capacity,faithThreshold,ip,port);
		}
		 computingServer.run();
	}
}
