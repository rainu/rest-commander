package de.rainu.restcommander.model.dto;

public class ProcessStatusResponse {
	private int returnCode;
	private boolean exits;

	public int getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}

	public boolean isExits() {
		return exits;
	}

	public void setExits(boolean exits) {
		this.exits = exits;
	}
}
