package de.rainu.restcommander.model;

import java.util.Map;

public class Process {
	private String id;
	private String commandline;
	private String user;
	private Map<String, String> environment;
	private boolean running;
	private Integer returnCode;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCommandline() {
		return commandline;
	}

	public void setCommandline(String commandline) {
		this.commandline = commandline;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public Map<String, String> getEnvironment() {
		return environment;
	}

	public void setEnvironment(Map<String, String> environment) {
		this.environment = environment;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public Integer getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(Integer returnCode) {
		this.returnCode = returnCode;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Process)) return false;

		Process process = (Process) o;

		return id != null ? id.equals(process.id) : process.id == null;
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}
}
