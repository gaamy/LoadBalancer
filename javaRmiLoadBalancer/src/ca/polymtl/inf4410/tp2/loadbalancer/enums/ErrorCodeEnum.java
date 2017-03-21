package ca.polymtl.inf4410.tp2.loadbalancer.enums;

import ca.polymtl.inf4410.tp2.computingserver.OperationEnum;

public enum ErrorCodeEnum {
	//TODO:: change the default values of the errors
	IO_ERROR(-10),
	NOT_BOUND_ERROR(-30),
	ACCES_ERROR(-40);
	
	private int value;
	private ErrorCodeEnum(int value){
		this.value = value;
	}
	
	public int value() {
		return value;
	}

}
