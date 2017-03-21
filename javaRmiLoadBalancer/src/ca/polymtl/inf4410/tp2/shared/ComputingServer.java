package ca.polymtl.inf4410.tp2.shared;

import java.rmi.RemoteException;
import java.rmi.Remote;

public interface ComputingServer extends Remote {
	int compute(String[] op) throws RemoteException, ServerOverloadedException, ServerCrashException;
}
