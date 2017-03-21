package ca.polymtl.inf4410.tp2.loadbalancer.threads;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

import ca.polymtl.inf4410.tp2.loadbalancer.LoadBalancer;
import ca.polymtl.inf4410.tp2.loadbalancer.Task;
import ca.polymtl.inf4410.tp2.loadbalancer.exceptions.IllegalOperationException;
import ca.polymtl.inf4410.tp2.loadbalancer.exceptions.VerificationDoneException;
import ca.polymtl.inf4410.tp2.loadbalancer.threads.ComputingThread;
import ca.polymtl.inf4410.tp2.shared.ComputingServer;
import ca.polymtl.inf4410.tp2.shared.ServerCrashException;
import ca.polymtl.inf4410.tp2.shared.ServerOverloadedException;

/**
 * This thread double check every result received from the computation servers before adding it to the global result. 
 * 
 * @author gamyot
 *
 */
public class WorriedComputingThread extends ComputingThread {


	private final ArrayList<Task> tasksToBeVerifyed;
	private static final Semaphore taskSemaphore = new Semaphore(1);
	private Task taskToCheck;
	

	public WorriedComputingThread(LoadBalancer repart, ComputingServer server, ArrayList<String> calculations,
			 int[] globalResult, ArrayList<Task> tasksToBeVerifyedList) {
		super(repart, server, calculations, globalResult);

		tasksToBeVerifyed = tasksToBeVerifyedList;
	}

	@Override
	public void run() {
		while (threadShouldContinue()) {
			computeNextUnsafeOperations();
			verifyNextOperations();
		}
	}

	private void computeNextUnsafeOperations() {
			try {
				gatherNewOperations();
				int actualResult = computeOperationsRemotely(computingServerStub, ownedOperationList);
				addNewTask(computingServerStub, ownedOperationList,actualResult);
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

	private void verifyNextOperations() {
		try {
			taskToCheck = chooseTaskToBeChecked();
			int tryCount=0;
			while (!taskToCheck.isTaskChecked() || tryCount>10 ) {
				try {
					verifyComputationResult(taskToCheck);
					tryToIncreaseLoad();
				} catch (RemoteException e) {
					System.out.println("Remote exception " + e.getMessage());
				}
				tryCount++;
			}
			if (taskToCheck.isTaskCorrect()) {
				updateGlobalResult(taskToCheck.getSecondResult(), taskToCheck.getAmmountOfOperationsToBeChecked());
			} else {
				dropInvalidOperations(taskToCheck);
			}
			taskToCheck = null;
		} catch (VerificationDoneException e) {
			System.out.println("Result checked succesfully: " + e.getMessage());
		} catch (InterruptedException e) {
			System.out.println("Thread interupted unexpectly: " + e.getMessage());
		}finally {
			dropOwnedOperations();
		}
	}


	private void addNewTask(ComputingServer server,List<String> operationList, int result) throws InterruptedException {
		Task newTask = new Task(computingServerStub,operationList, result);
		taskSemaphore.acquire();
		tasksToBeVerifyed.add(newTask);
		taskSemaphore.release();
	}


	private Task chooseTaskToBeChecked() throws InterruptedException,  VerificationDoneException{
		Task pivot = null;
		taskSemaphore.acquire();
		for (Task task : tasksToBeVerifyed) {
			if (task.canBeCheckedBy(computingServerStub)) {
				pivot = task;
				pivot.setVerificationServer(computingServerStub);
				break;
			}
		}
		taskSemaphore.release();

		if (pivot == null)
			throw new VerificationDoneException("Verification Completed");

		return pivot;
	}

	private void dropInvalidOperations(Task task) throws InterruptedException {
		ArrayList<String> invalidResultOperationList = new ArrayList<>();
		for (String operation: task.getOperationList()){
			invalidResultOperationList.add(operation);
		}
		taskSemaphore.acquire();
		tasksToBeVerifyed.remove(task);
		taskSemaphore.release();
		operationSemaphore.acquire();
		operationList.addAll(invalidResultOperationList);
		operationSemaphore.release();
	}

	private void verifyComputationResult(Task task) throws RemoteException, InterruptedException {
		
		try {
			ownedOperationList = task.gatherOperationsToBeChecked(serverCapacity);
			int actualResult = computeOperationsRemotely(computingServerStub, ownedOperationList );
			taskToCheck.updateCheckedResult(actualResult, ownedOperationList);
			tryToIncreaseLoad();
		} catch (ServerOverloadedException e) {
			task.addOperationsToBeChecked(ownedOperationList);
			handleOverload();
		} catch (ServerCrashException e) {
			System.out.println("Server crashed:" + e.getMessage());
			dropOwnedOperations();
			dropInvalidOperations(task);
		}
	}

}
