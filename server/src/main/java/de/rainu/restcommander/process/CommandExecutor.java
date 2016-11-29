package de.rainu.restcommander.process;

import org.apache.commons.exec.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class CommandExecutor {
	private final DefaultExecutor executor;
	private final ProcessInterceptor processInterceptor;

	public CommandExecutor(String workdir) {
		this.executor = new DefaultExecutor();

		if(workdir != null) {
			executor.setWorkingDirectory(new File(workdir));
		}

		this.processInterceptor = new ProcessInterceptor();
		this.executor.setWatchdog(processInterceptor);
	}

	public Process execute(CommandLine command, ExecuteResultHandler handler) throws IOException {
		ExecuteResultHandler internalHandler = new ExecuteResultHandlerWrapper(handler);
		executor.execute(command, internalHandler);

		try {
			synchronized (internalHandler) {
				internalHandler.wait();
			}
		} catch (InterruptedException e) {
		}
		return processInterceptor.process;
	}

	public Process execute(CommandLine command, Map<String, String> environment, ExecuteResultHandler handler) throws IOException {
		ExecuteResultHandler internalHandler = new ExecuteResultHandlerWrapper(handler);
		executor.execute(command, environment, internalHandler);

		try {
			synchronized (internalHandler) {
				internalHandler.wait();
			}
		} catch (InterruptedException e) {
		}
		return processInterceptor.process;
	}

	public static class ProcessInterceptor extends ExecuteWatchdog {
		Process process;

		public ProcessInterceptor() {
			super(INFINITE_TIMEOUT);
		}

		@Override
		public synchronized void start(Process processToMonitor) {
			this.process = processToMonitor;

			super.start(processToMonitor);
		}
	}

	public static class ExecuteResultHandlerWrapper implements ExecuteResultHandler {
		private final ExecuteResultHandler handler;

		public ExecuteResultHandlerWrapper(ExecuteResultHandler handler) {
			this.handler = handler;
		}

		public void onProcessComplete(int exitValue) {
			synchronized (this) {
				notify();
			}
			handler.onProcessComplete(exitValue);
		}

		public void onProcessFailed(ExecuteException e) {
			synchronized (this) {
				notify();
			}
			handler.onProcessFailed(e);
		}
	}
}
