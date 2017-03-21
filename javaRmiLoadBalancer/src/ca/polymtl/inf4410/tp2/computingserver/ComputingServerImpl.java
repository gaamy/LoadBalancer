package ca.polymtl.inf4410.tp2.computingserver;

import ca.polymtl.inf4410.tp2.shared.ComputingServer;
import ca.polymtl.inf4410.tp2.shared.ServerCrashException;
import ca.polymtl.inf4410.tp2.shared.ServerOverloadedException;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;


public class ComputingServerImpl implements ComputingServer {

	private int capacity;
	private int maliciousLevel;
	private String serverName;
	private int rmiRegistryPort;
	private static final Operations computingOperations = new Operations();;

	/**
	 * capacity, confidence, ip, rmiRegistryPort
	 */
	public ComputingServerImpl(int capacity, int maliciousLevel, String ip, int rmiRegistryPort) {
		super();
		this.capacity = capacity;
		this.maliciousLevel = maliciousLevel;
		this.serverName = ip;
		this.rmiRegistryPort = rmiRegistryPort;
	}
	

	/**
	 * @input rmiRegistryPort
	 * @input ip
	 */
	void run() {
		if (System.getSecurityManager() == null) {
			//System.setSecurityManager(new SecurityManager());
		}
		try{
			ComputingServer stub = (ComputingServer)UnicastRemoteObject.exportObject(this, 0);
			Registry registry = LocateRegistry.getRegistry(rmiRegistryPort);
			registry.rebind(serverName, stub);
			System.out.println("Server ready to compute.");
		}catch(RemoteException e){
			System.out.println("Error trying to start the server. RMI registry unavaliable.");
			System.out.println("Details:"+e.getMessage());
		}
	}

	@Override
	public int compute(String[] operationsToBeExecuted)
			throws RemoteException, ServerOverloadedException, ServerCrashException {
		handleServerLoad(capacity, operationsToBeExecuted.length);

		if (isPhoney(maliciousLevel)) {
			return (int) (Math.random() * 4000);
		} else {
			return executeOperations(operationsToBeExecuted);
		}

	}

	private int executeOperations(String[] operations) throws ServerCrashException {
		int result = 0;
		HashMap<OperationEnum, Integer> operationMap = extractOperations(operations);
		for (OperationEnum command : operationMap.keySet()) {
			Integer parameter = operationMap.get(command);
			switch (command) {
			case PEEL:
				result += modularyseResult(Operations.pell(parameter));
				break;
			case PRIME:
				result += modularyseResult(Operations.prime(parameter));
				break;
			default:
				// Should never reach this point: extractOperations should
				// handle this case
				break;
			}
		}
		return modularyseResult(result);
	}

	private HashMap<OperationEnum, Integer> extractOperations(String[] operations) throws ServerCrashException {
		HashMap<OperationEnum, Integer> extractedOperations = new HashMap<>();
		for (int i = 0; i < operations.length; i++) {
			if (operations[i] != null) {
				String[] splittedOperation = operations[i].split(" ");
				OperationEnum currentCommand = OperationEnum.resolveCommandEnum(splittedOperation[0]);
				if (!currentCommand.equals(OperationEnum.NONE)) {
					extractedOperations.put(currentCommand, Integer.parseInt(splittedOperation[1]));
				} else {
					throw new ServerCrashException("ServerId: '"+serverName+"'. Error extracting operations:  The command  '" + splittedOperation[0]
							+ "' is not supported");
				}
			}
		}
		return extractedOperations;
	}

	/**
	 * 
	 * @param serverCapacity
	 * @param amountOfOperations
	 * @throws ServerOverloadedException
	 */
	private void handleServerLoad(int serverCapacity, int amountOfOperations) throws ServerOverloadedException {
		if (isOverloaded(serverCapacity, amountOfOperations)) {
			String errorMessage = "Server '[SERVER_NAME/RMI_PORT]: [" + this.serverName + "/" + this.rmiRegistryPort + "]'"
					+ "cant accept the requested operations because of an overload."
					+ " The amount of operations requested is:'" + amountOfOperations + "'";
			throw new ServerOverloadedException(errorMessage);

		}
	}

	/**
	 * 
	 * Prevent from an overflow on the result applying modulo of 4000 to it.
	 * 
	 * @param rawNumber
	 *            number to be modularized.
	 * @return rawNumber modulo 4000
	 */
	private int modularyseResult(int rawNumber) {
		return rawNumber % 4000;
	}

	/**
	 * Determines if will return a reliable result or a phoney result
	 * 
	 * @param maliciousLevel
	 * @return a randomly selected result
	 */
	private boolean isPhoney(int maliciousLevel) {
		return (int) (Math.random() * 101) < maliciousLevel;
	}

	/**
	 * Algorithm used to simulate the load on the computing server. This
	 * algorithm is based on the equation: <<T = (U-Q)/(5*Q) * 100 >> where Q
	 * represents the capacity of the server and U represents the amount of
	 * operations requested by the caller.
	 * 
	 * @param Q Amount of operations that can be handled by the actual server.
	 * @param U Amount of operations requested by the caller.
	 * @return true if the server is overloaded and false if not.
	 */
	private boolean isOverloaded(int Q, int U) {
		return (U - Q) / (5 * Q) > Math.random();
	}

}
