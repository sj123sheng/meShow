package com.melot.kkgame.redis.support;

public class RedisException extends Exception {

	private static final long serialVersionUID = -703549884857704862L;

	private String errCode;
	private String errMsg;

	public RedisException() {
	}

	public RedisException(String message, Throwable cause) {
		super(message, cause);
	}

	public RedisException(String message) {
		super(message);
	}

	public RedisException(Throwable cause) {
		super(cause);
	}

	public RedisException(String errCode, String errMsg) {
		super(errCode + ": " + errMsg);
		this.errCode = errCode;
		this.errMsg = errMsg;
	}

	public String getErrCode() {
		return this.errCode;
	}

	public String getErrMsg() {
		return this.errMsg;
	}

}
