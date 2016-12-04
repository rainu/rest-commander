package de.rainu.restcommander.model.dto;

public class ProcessCreateResponse {
	private String pid;
	private boolean created;

	public ProcessCreateResponse() {
	}

	public ProcessCreateResponse(String pid) {
		this.pid = pid;
		this.created = pid != null;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public boolean isCreated() {
		return created;
	}

	public void setCreated(boolean created) {
		this.created = created;
	}
}
