package ca.polymtl.inf4410.tp2.loadbalancer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

import ca.polymtl.inf4410.tp2.loadbalancer.enums.ErrorCodeEnum;
import ca.polymtl.inf4410.tp2.loadbalancer.threads.ComputingThread;
import ca.polymtl.inf4410.tp2.loadbalancer.threads.SynchronizationThread;
import ca.polymtl.inf4410.tp2.loadbalancer.threads.WorriedComputingThread;
import ca.polymtl.inf4410.tp2.shared.ComputingServer;

public class LoadBalancer {

	//constants
	private final String SERVER_CONFIG_PATH = "./LoadBalancerConfig/serverSocket.config";
	//Collections
	private ArrayList<Task> operationsToBeVeryfied;
	private ArrayList<Thread> activeThreads;
	private ArrayList<ComputingServer> computingServerList;
	//Booleans
	private boolean isInSafeMode;
	private boolean shouldTerminateThreads = false;
	//Result
	private int[] finalResult;
	//private int amountOfExecutedOperations;
	

	
	public LoadBalancer(boolean isSafe) {
		initializeLoadBalancer(isSafe);
		if (System.getSecurityManager() == null) {
			//System.setSecurityManager(new SecurityManager());
		}
	}
	
	private void initializeLoadBalancer(boolean isSafe){
		//initialize members
		isInSafeMode = isSafe;
		finalResult = new int[2];
		Arrays.fill(finalResult, 0);
		operationsToBeVeryfied = new ArrayList<>();
		activeThreads = new ArrayList<>();
		shouldTerminateThreads = false;
		computingServerList = loadServers();
	}


	public static void main(String[] args) throws InterruptedException {
		boolean isSafe = true;
		if (args.length >= 1 && "-S".equals(args[0])) {
			System.out.println("Load Balancer initialized in 'Safe' mode. The calculations provided by the computing server will be checked.");
		} else {
			isSafe = false;
			System.out.println("Load Balancer initialized in 'Non Safe' mode. The calculations provided by the computing server will be accepted without verification.");
		}
		LoadBalancer loadBalancer = new LoadBalancer(isSafe);
		loadBalancer.run();
	}

	private void run() throws InterruptedException {
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String argument = null;
		try {
			while ((argument = reader.readLine()) != null) {
				String arguments[] = argument.split(" ");
				switch (arguments[0]) {
					case "compute":
						computeOperation(arguments[1]);
						break;
					case "exit":
						System.exit(0);
						break;
					default:
						System.out.println("Unsuported command. Waiting for commands ('compute <<fileName>>' or 'exit')");
						break;
				}
			}
		} catch (IOException e) {
			System.err.println("Error reading System input.");
			e.printStackTrace();
			System.exit(ErrorCodeEnum.IO_ERROR.value());
		}
	}


	private void computeOperation(String operationFileName) throws IOException, InterruptedException {
		ArrayList<String> opList = new ArrayList<>();
		try {
			opList = loadOperations(operationFileName);
		} catch (IOException e) {
			throw new IOException("Error parsing the sample file " + operationFileName ,e);
		}
		dispatchOperations(opList);
	}

	


	private ArrayList<ComputingServer> loadComputingServerStub(String rmiRegistryHostname, int port) {
		ArrayList<ComputingServer> stubList = new ArrayList<>();
		try {
			Registry registry = LocateRegistry.getRegistry(rmiRegistryHostname, port);
			List<String> serverList = Arrays.asList(registry.list());
			for(String serverName: serverList){
				stubList.add((ComputingServer) registry.lookup(serverName));
			}
			
		} catch (NotBoundException e) {
			System.err.println("Error:   " + e.getMessage() + " not defined in the rmi registry");
			System.exit(ErrorCodeEnum.NOT_BOUND_ERROR.value());
		} catch (AccessException e) {
			System.err.println("Error : " + e.getMessage());
			System.exit(ErrorCodeEnum.ACCES_ERROR.value());
		} catch (RemoteException e) {
			System.err.println("Error: Failing to connect to '" + rmiRegistryHostname + ":" + port+"'");
		}
		return stubList;
	}


	private void dispatchOperations(ArrayList<String> operationList) throws IOException, InterruptedException {
		
		Thread synchronizationThread = new SynchronizationThread(this,finalResult, operationList.size());
		synchronizationThread.start();
		long startTime = System.currentTimeMillis();
		if (!isInSafeMode) { 
			for (ComputingServer server : computingServerList) {
				ComputingThread thread = new ComputingThread(this, server, operationList, finalResult);
				activeThreads.add(thread);
				thread.start();
			}
		}
		else {
			for (ComputingServer server : computingServerList) {
				WorriedComputingThread thread = new WorriedComputingThread(this, server, operationList, finalResult, operationsToBeVeryfied);
				activeThreads.add(thread);
				thread.start();
			}
		}


		synchronizationThread.join();
		for (Thread thread : activeThreads) {
			thread.join();
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("The result is " + finalResult[0]+". Ammount of OP:"+finalResult[1]);
		System.out.println("Done in : " + (endTime - startTime) + " milliseconds");
		initializeLoadBalancer(isInSafeMode);
	}
	
	
	private ArrayList<ComputingServer> loadServers() {
		ArrayList<ComputingServer> loadedServers = new ArrayList<>();
		try {
			FileReader fileReader = new FileReader(SERVER_CONFIG_PATH);
			BufferedReader bufferReader = new BufferedReader(fileReader);
			String line = null;
			while ((line = bufferReader.readLine()) != null) {
				String[] array = line.split(" ");
				if (array.length != 0) {
					ArrayList<ComputingServer> serverList = loadComputingServerStub(array[0], Integer.parseInt(array[1]));
					loadedServers.addAll(serverList);
				}else{
					continue;
				}
			}
			bufferReader.close();
			System.out.println("Servers succesfully loaded. Waiting for commands ('compute <<fileName>>' or 'exit')");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return loadedServers;
	}

	/**
	 * Load the operations to be executed from the file <<filename>>
	 * 
	 * @param filename
	 * @return an array containing the operations from the file, one entry per line
	 * @throws IOException
	 */
	private ArrayList<String> loadOperations(String filename) throws IOException {
		ArrayList<String> loadedOperations = new ArrayList<>();
		String nextLine = null;
		FileInputStream fileInputStream = new FileInputStream(filename);
		InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, Charset.forName("UTF-8"));
		BufferedReader bufferReader = new BufferedReader(inputStreamReader);
		while ((nextLine = bufferReader.readLine()) != null) {
			loadedOperations.add(nextLine);
		}
		bufferReader.close();
		return loadedOperations;
	}

	public boolean isSafeMode() {
		return this.isInSafeMode;
	}

	public boolean threadsShouldContinue() {
		return !shouldTerminateThreads;
	}

	public void terminateThreads() {
		shouldTerminateThreads = true;
	}
}

