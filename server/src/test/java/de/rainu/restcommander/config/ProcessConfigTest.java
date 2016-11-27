package de.rainu.restcommander.config;

import de.rainu.restcommander.process.LinuxProcessManager;
import de.rainu.restcommander.process.ProcessManager;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import static org.junit.Assert.assertTrue;

public class ProcessConfigTest {

	@Test
	public void processManager_linux(){
		final ProcessManager result = new ProcessConfig().processManager("linux");
		assertTrue(result instanceof LinuxProcessManager);
	}

	@Test(expected = NoSuchBeanDefinitionException.class)
	public void processManager_other(){
		new ProcessConfig().processManager("windows");
	}
}
