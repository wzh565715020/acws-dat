package com.tyyd.framework.dat.core.domain;


public enum TaskType {
	SINGLE("1"), LOOP("2"),LIMIT("3");

	private String code;

	private TaskType(String code) {
		this.code = code;
	}

	public String getCode() {
		return this.code;
	}

}
