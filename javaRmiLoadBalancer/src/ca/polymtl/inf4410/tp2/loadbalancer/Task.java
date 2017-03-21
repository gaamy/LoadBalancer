package ca.polymtl.inf4410.tp2.loadbalancer;


import ca.polymtl.inf4410.tp2.shared.ComputingServer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Task {

	private final ArrayList<String> operationList;
	
	//unsafe related
	private ArrayList<String> operationToBeCheckedList;
	private ArrayList<String> operationCheckedList;
	private int totalAmountOfOperations;
	private int ammountOfOperationsChecked;
	private ComputingServer firstServer;
	private ComputingServer secondServer;
	private int firstResult;
	private int secondResult;

	public Task(ComputingServer server, List<String> operations, int result) {
		operationList = new ArrayList<>();
		operationList.addAll(operations);
		operationToBeCheckedList = new ArrayList<>(operationList);
		operationCheckedList = new ArrayList<>();
		firstServer = server;
		secondServer = null;
		firstResult = result;
		secondResult = 0;
		totalAmountOfOperations = operations.size();
		ammountOfOperationsChecked = 0;
	}


	public void updateCheckedResult(int newResult, List<String> computedOperationList) {
		//secondResult = newResult;
		secondResult += newResult;
		secondResult %= 4000;
		ammountOfOperationsChecked += computedOperationList.size();
		operationCheckedList.addAll(computedOperationList);	
	}

	public boolean canBeCheckedBy(ComputingServer otherServer) {
		return otherServer != null && !(firstServer.equals(otherServer));
	}

	public int getSecondResult() {
		return secondResult;
	}

	public int getAmmountOfOperationsToBeChecked() {
		return totalAmountOfOperations;
	}

	public boolean isTaskCorrect() {
		
		return secondResult == firstResult;
	}

	public boolean isTaskChecked() {
		return operationToBeCheckedList.isEmpty() && (ammountOfOperationsChecked == totalAmountOfOperations);
	}

	public void setVerificationServer(ComputingServer server) {
		secondServer = server;
	}

	public ArrayList<String> getOperationList() {
		return operationList;
	}


	public ArrayList<String> gatherOperationsToBeChecked(int ammountOfOperationsAsked) {
		ArrayList<String> resultingOperationsList = new ArrayList<>();
		for (int i = 0; (i < ammountOfOperationsAsked) && (i < operationToBeCheckedList.size()); i++) {
			resultingOperationsList.add(operationToBeCheckedList.remove(i));
		}
		return resultingOperationsList;
	}


	public void addOperationsToBeChecked(List<String> calcs) {
		operationToBeCheckedList.addAll(calcs);
	}

}
