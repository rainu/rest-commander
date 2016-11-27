package de.rainu.restcommander.model;

import java.util.List;
import java.util.Map;

public class Process {
	private String id;
	private String cmdline;
	private Map<String, String> env;

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
