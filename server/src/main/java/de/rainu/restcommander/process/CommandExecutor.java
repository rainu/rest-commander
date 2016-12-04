package de.rainu.restcommander.process;

import org.apache.commons.exec.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class CommandExecutor {
	private final DefaultExecutor executor;
	private final ProcessInterceptor processInterceptor;
	private final StreamInterceptor streamInterceptor;

	public CommandExecutor(String workdir) {
		this.executor = new DefaultExecutor();

		if(workdir != null) {
			executor.setWorkingDirectory(new File(workdir));
		}

		this.processInterceptor = new ProcessInterceptor();
		this.executor.setWatchdog(processInterceptor);

		this.streamInterceptor = new StreamInterceptor();
		this.executor.setStreamHandler(streamInterceptor);
	}

	public ProcessHandle execute(CommandLine command, ExecuteResultHandler handler) throws IOException {
		executor.execute(command, handler);

		return buildProcessHandle();
	}

	public ProcessHandle execute(CommandLine command, Map<String, String> environment, ExecuteResultHandler handler) throws IOException {
		executor.execute(command, environment, handler);

		return buildProcessHandle();
	}

	private ProcessHandle buildProcessHandle() {
		try {
			synchronized (processInterceptor) {
				processInterceptor.wait();
			}

			if(!streamInterceptor.triggerd) {
				synchronized (streamInterceptor) {
					streamInterceptor.wait();
				}
			}
		} catch (InterruptedException e) {
		}

		return new ProcessHandle(processInterceptor.process,
				  streamInterceptor.stdin,
				  streamInterceptor.stderr,
				  streamInterceptor.stdout);
	}

	static class ProcessInterceptor extends ExecuteWatchdog {
		Process process;

		public ProcessInterceptor() {
			super(INFINITE_TIMEOUT);
		}

		@Override
		public synchronized void start(Process processToMonitor) {
			this.process = processToMonitor;

			synchronized (this) {
				notify();
			}

			super.start(processToMonitor);
		}
	}

	class StreamInterceptor implements ExecuteStreamHandler {
		boolean triggerd = false;
		OutputStream stdin;
		InputStream stdout;
		InputStream stderr;

		@Override
		public void setProcessInputStream(OutputStream os) throws IOException {
			stdin = os;
		}

		@Override
		public void setProcessErrorStream(InputStream is) throws IOException {
			stderr = is;
		}

		@Override
		public void setProcessOutputStream(InputStream is) throws IOException {
			stdout = is;
		}

		@Override
		public void start() throws IOException {
			synchronized (this) {
				notify();
			}
			triggerd = true;
		}

		@Override
		public void stop() throws IOException {
			synchronized (this) {
				notify();
			}
			triggerd = true;
		}
	}

}
