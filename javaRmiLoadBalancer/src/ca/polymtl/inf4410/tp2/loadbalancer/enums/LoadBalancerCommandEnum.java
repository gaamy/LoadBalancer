package ca.polymtl.inf4410.tp2.loadbalancer.enums;

import ca.polymtl.inf4410.tp2.computingserver.OperationEnum;

public enum LoadBalancerCommandEnum {
	COMPUTE("compute"),
	EXIT("exit"),
	NONE("");
	
	private String value;
	private LoadBalancerCommandEnum(String value){
		this.value = value;
	}
	
	public String value() {
		return value;
	}
	
	public static LoadBalancerCommandEnum resolveCommandEnum(String commandName){
		for (LoadBalancerCommandEnum commandEnum: LoadBalancerCommandEnum.values()){
			if(commandEnum.value().equals(commandName)){
				return commandEnum;
			}
		}
		return LoadBalancerCommandEnum.NONE;
	}


}
