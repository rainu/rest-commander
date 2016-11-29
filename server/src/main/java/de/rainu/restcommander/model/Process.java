package de.rainu.restcommander.model;

import java.util.Map;

public class Process {
	private String id;
	private String cmdline;
	private Map<String, String> env;
	private boolean running;
	private Integer returnCode;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCmdline() {
		return cmdline;
	}

	public void setCmdline(String cmdline) {
		this.cmdline = cmdline;
	}

	public Map<String, String> getEnv() {
		return env;
	}

	public void setEnv(Map<String, String> env) {
		this.env = env;
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
