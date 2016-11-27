package de.rainu.restcommander.process;

import de.rainu.restcommander.model.Process;

import java.io.IOException;
import java.util.List;

public interface ProcessManager {
	List<Process> listProcess() throws IOException;
}
