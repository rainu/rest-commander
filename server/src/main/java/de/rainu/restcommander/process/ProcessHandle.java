package de.rainu.restcommander.process;

import java.io.InputStream;
import java.io.OutputStream;

public class ProcessHandle {
	private Process process;
	private OutputStream stdin;
	private InputStream stderr;
	private InputStream stdout;

	public ProcessHandle(Process process, OutputStream stdin, InputStream stderr, InputStream stdout) {
		this.process = process;
		this.stdin = stdin;
		this.stderr = stderr;
		this.stdout = stdout;
	}

	public Process getProcess() {
		return process;
	}

	public OutputStream getStdin() {
		return stdin;
	}

	public InputStream getStderr() {
		return stderr;
	}

	public InputStream getStdout() {
		return stdout;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ProcessHandle)) return false;

		ProcessHandle that = (ProcessHandle) o;

		return process.equals(that.process);
	}

	@Override
	public int hashCode() {
		return process.hashCode();
	}
}
