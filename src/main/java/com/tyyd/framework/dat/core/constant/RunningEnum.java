package com.tyyd.framework.dat.core.constant;

public enum RunningEnum {
   NOT_RUNNING(0, "未运行"), RUNNING(1, "运行中");
	
	private Integer code;
	private String name;

	private RunningEnum(Integer code, String name) {
		this.name = name;
		this.code = code;
	}
	
	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
