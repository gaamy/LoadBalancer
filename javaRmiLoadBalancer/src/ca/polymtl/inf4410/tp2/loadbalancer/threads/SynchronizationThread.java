package ca.polymtl.inf4410.tp2.loadbalancer.threads;

import java.util.concurrent.Semaphore;

import ca.polymtl.inf4410.tp2.loadbalancer.LoadBalancer;

/**
 * this class is responsible for 
 * @author gamyot
 *
 */
public class SynchronizationThread extends Thread {
	
	//constant
	private static final int VERIFICATION_SLEEP_TIME = 5;
	private final LoadBalancer repartitor;
	private final int[] result;
	private static final Semaphore resultSemaphore = new Semaphore(1);
	private final int ammountOfOperationReceived;

	public SynchronizationThread(LoadBalancer repart, int[] globalResult, int totalOperationAmmount) {
		repartitor = repart;
		result = globalResult;
		ammountOfOperationReceived = totalOperationAmmount;
	}

	@Override
	public void run() {
		while (repartitor.threadsShouldContinue()) {
			try {
				resultSemaphore.acquire();
				if (result[1] >= ammountOfOperationReceived) { //checking if we finished our job 
					repartitor.terminateThreads();
				}
				sleep(VERIFICATION_SLEEP_TIME);
			} catch (InterruptedException e) {
				System.err.println("Interruption in the SyncronizationThread "+ e.getMessage());
			} finally {
				resultSemaphore.release();
			}
		}
	}
}