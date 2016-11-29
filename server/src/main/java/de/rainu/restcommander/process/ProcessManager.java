package de.rainu.restcommander.process;

import de.rainu.restcommander.model.Process;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ProcessManager {
	List<Process> listProcess() throws IOException;

	Process getProcess(String pid) throws ProcessNotFoundException;

	String createProcess(String command, List<String> arguments, Map<String, String> environment, String workingDirectory) throws IOException;

	int sendSignal(String pid, String signal) throws IOException, ProcessNotFoundException;

	void sendInput(String pid, byte[] rawInput) throws IOException, ProcessNotFoundException;
}
