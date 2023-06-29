package com.wangxin.iot.constants;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * card状态
 */

public enum CardStatusConstants {

	ACTIVATION_READY_NAME("activation_ready_name",3),
	ACTIVATED_NAME("activated_name",4),
	DEACTIVATED_NAME("deactivated_name",5),
	RETIRED("retired",6),
	INVENTORY_NAME("inventory_name",2),
	TEST_READY_NAME("test_ready_name",1),
	;

	CardStatusConstants(String status,int code){
		this.status=status;
		this.code = code;
	}
	private String status;
	private int code;
	public String getMessage(){
		return this.status;
	}
	public static int getCode(String message){
		return Stream.of(CardStatusConstants.values()).filter(item -> item.getMessage().equalsIgnoreCase(message)).collect(Collectors.toList()).get(0).code;
	}

}
