package ca.polymtl.inf4410.tp2.computingserver;

import java.util.Arrays;

public enum OperationEnum {
	PEEL("pell"),
	PRIME("prime"),
	NONE("");
	
	private String commandName;
	private OperationEnum(String name){
		commandName = name;
	}
	public String value() {
		return commandName;
	}
	
	public static OperationEnum resolveCommandEnum(String commandName){
		for (OperationEnum commandEnum: OperationEnum.values()){
			if(commandEnum.value().equals(commandName)){
				return commandEnum;
			}
		}
		return OperationEnum.NONE;
	}

}
