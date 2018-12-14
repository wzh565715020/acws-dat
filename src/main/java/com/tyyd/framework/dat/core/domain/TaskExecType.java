package com.tyyd.framework.dat.core.domain;


public enum TaskExecType {
	IMMEDIATELY("1"), SCHEDULETIME("2");

	private String code;

	private TaskExecType(String code) {
		this.code = code;
	}

	public String getCode() {
		return this.code;
	}

}
