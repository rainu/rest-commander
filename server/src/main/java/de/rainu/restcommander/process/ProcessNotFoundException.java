package de.rainu.restcommander.process;

public class ProcessNotFoundException extends Exception {
	public ProcessNotFoundException(String pid) {
		super("No process found for " + pid);
	}
}
