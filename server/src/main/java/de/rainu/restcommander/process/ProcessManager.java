package de.rainu.restcommander.process;

import de.rainu.restcommander.model.Process;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ProcessManager {

	/**
	 * Returns a list of all running/found processes.
	 *
	 * @return
	 * @throws IOException
	 */
	List<Process> listProcess() throws IOException;

	/**
	 * Gets a process by his id.
	 *
	 * @param pid The process id.
	 * @return The {@link Process} if one found.
	 * @throws ProcessNotFoundException If no process found for the given id.
	 */
	Process getProcess(String pid) throws ProcessNotFoundException;

	/**
	 * Starts a new process.
	 *
	 * @param command          The command
	 * @param arguments        The commands arguments
	 * @param environment      The environment variables of the process
	 * @param workingDirectory The working directory of the process
	 * @return The process id of the created process.
	 * @throws IOException
	 */
	String createProcess(String command, List<String> arguments, Map<String, String> environment, String workingDirectory) throws IOException;

	/**
	 * Sends a signal to the given process.
	 *
	 * @param pid    The id of the process.
	 * @param signal The signal to send to the process.
	 * @return The return code of the sending signal.
	 * @throws IOException
	 * @throws ProcessNotFoundException If no process found for the given id.
	 */
	int sendSignal(String pid, String signal) throws IOException, ProcessNotFoundException;

	/**
	 * Send a input to the given process.
	 *
	 * @param pid      The id of the process.
	 * @param rawInput The raw input for the process.
	 * @throws IOException
	 * @throws ProcessNotFoundException If no process found for the given id. Or the process is not supported.
	 */
	void sendInput(String pid, byte[] rawInput) throws IOException, ProcessNotFoundException;

	/**
	 * Read the stdout from the given process.
	 *
	 * @param pid   The id of the process.
	 * @param start The start byte to read.
	 * @return
	 * @throws IOException
	 * @throws ProcessNotFoundException If no process found for the given id. Or the process is not supported.
	 */
	Data readOutput(String pid, Long start) throws IOException, ProcessNotFoundException;

	/**
	 * Read the stderr from the given process.
	 *
	 * @param pid   The id of the process.
	 * @param start The start byte to read.
	 * @return
	 * @throws IOException
	 * @throws ProcessNotFoundException If no process found for the given id. Or the process is not supported.
	 */
	Data readError(String pid, Long start) throws IOException, ProcessNotFoundException;

	class Data {
		public final byte[] content;
		public final int read;

		public Data(byte[] content, int read) {
			this.content = content;
			this.read = read;
		}
	}
}
