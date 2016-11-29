package de.rainu.restcommander.process;

import de.rainu.restcommander.model.Process;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ProcessManager {
	List<Process> listProcess() throws IOException;

	String createProcess(String command, List<String> arguments, Map<String, String> environment, String workingDirectory) throws IOException;
}
