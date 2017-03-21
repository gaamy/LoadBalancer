package ca.polymtl.inf4410.tp2.loadbalancer.threads;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

import ca.polymtl.inf4410.tp2.loadbalancer.LoadBalancer;
import ca.polymtl.inf4410.tp2.loadbalancer.exceptions.IllegalOperationException;
import ca.polymtl.inf4410.tp2.shared.ComputingServer;
import ca.polymtl.inf4410.tp2.shared.ServerCrashException;
import ca.polymtl.inf4410.tp2.shared.ServerOverloadedException;
/**
 * This thread is responsible for dispatching the operations to the servers
 * and orchestrating them to do the calculations.
 * @author gamyot
 *
 */
public class ComputingThread extends Thread {
	// Constants
	final int INITIAL_CAPACITY = 3;

	// Project data structures
	protected ComputingServer computingServerStub;
	protected LoadBalancer loadBalancer;

	// collections
	protected ArrayList<String> operationList;
	protected ArrayList<String> ownedOperationList;

	// semaphores
	protected static final Semaphore operationSemaphore = new Semaphore(1);
	protected static final Semaphore finalResultSemaphore = new Semaphore(1);
	protected static final Semaphore terminateSemaphore = new Semaphore(1);

	// boolean states
	protected boolean isServerOverloaded;

	protected int serverCapacity;
	protected int[] globalResult;// [result,ammount of computed results]

	public ComputingThread(LoadBalancer newLoadBalancer, ComputingServer newOomputingServer,
			ArrayList<String> newOperationList, int[] newFinalResult) {
		loadBalancer = newLoadBalancer;
		computingServerStub = newOomputingServer;
		operationList = newOperationList;
		globalResult = newFinalResult;
		serverCapacity = INITIAL_CAPACITY;
		isServerOverloaded = false;
		ownedOperationList = new ArrayList<>();
	}
	


	public void run() {
		while (threadShouldContinue()) {
			computeNextOperations();
		}
	}



	protected void computeNextOperations() {
		try {
			gatherNewOperations();
			int actualResult = computeOperationsRemotely(computingServerStub, ownedOperationList);
			updateGlobalResult(actualResult, ownedOperationList.size());
			tryToIncreaseLoad();
		} catch (IllegalOperationException e) {
			System.out.println(e.getMessage());
		} catch (RemoteException e) {
			System.out.println("Problem occured on server:" + e.getMessage());
		} catch (ServerOverloadedException e) {
			System.out.println("Server overloaded:" + e.getMessage());
			handleOverload();
		} catch (ServerCrashException e) {
			System.out.println("Server crashed:" + e.getMessage());
			dropOwnedOperations();
		} catch (InterruptedException e) {
			System.out.println("Impossible to update global result from thread:  " + e.getMessage());
		}
		finally {
			dropOwnedOperations();
		}
	}
	
	protected boolean threadShouldContinue(){
		try {
			terminateSemaphore.acquire();
		} catch (InterruptedException e) {
			System.out.println("Error aquiring 'terminateSemaphore'  " + e.getMessage());
		}
		boolean threadShouldContinue = loadBalancer.threadsShouldContinue();
		terminateSemaphore.release();
		return threadShouldContinue;
	}

	protected ArrayList<String> gatherNewOperations() throws IllegalOperationException, InterruptedException {

		int ammountOfOperationsAquired = 0;
		ArrayList<String> colectedOperations = new ArrayList<>();
		operationSemaphore.acquire();
		if (operationList.isEmpty()) {
			throw new IllegalOperationException("Trying to compute an empty list, the work is probably all done!");
		}
		Iterator<String> i = operationList.iterator();
		while (i.hasNext() && ammountOfOperationsAquired < serverCapacity) {
			colectedOperations.add(i.next());
			i.remove();
			ammountOfOperationsAquired++;
		}
		ownedOperationList = colectedOperations;
		operationSemaphore.release();
		return colectedOperations;
	}

	protected void updateGlobalResult(int toAdd, int operationCount) throws InterruptedException {
		finalResultSemaphore.acquire();
		globalResult[0] += toAdd;
		globalResult[0] %= 4000;
		globalResult[1] += operationCount;
		finalResultSemaphore.release();
	}

	/**
	 * Sending back operations taken by the thread to the shared operation list
	 */
	protected void dropOwnedOperations() {
		try {
			operationSemaphore.acquire();
			if (ownedOperationList != null){
				for (String operation : ownedOperationList) {
					if (!operation.isEmpty()) {
						operationList.add(operation);
					}
				}
			}
		} catch (InterruptedException e) {
			System.out.println("Safe thread interrupted trying to drop operations" + e.getMessage());
		} finally {
			operationSemaphore.release();
			ownedOperationList = null;
		}

	}

	protected int computeOperationsRemotely(ComputingServer server, ArrayList<String> operations)
			throws RemoteException, ServerOverloadedException, ServerCrashException {
		String[] serialisableOperations = operations.toArray(new String[operations.size()]);
		return server.compute(serialisableOperations);
	}

	protected void handleOverload() {
		isServerOverloaded = true;
		serverCapacity--;
	}

	protected void tryToIncreaseLoad() {
		if (!isServerOverloaded) {
			serverCapacity++;
		}
	}
}
