package com.tyyd.framework.dat.core.domain;


public enum TaskType {
	SINGLE("1"), CRON("2"),LOOP("3"),LIMIT("4");

	private String code;

	private TaskType(String code) {
		this.code = code;
	}

	public String getCode() {
		return this.code;
	}

}
