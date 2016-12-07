package de.rainu.restcommander.controller;

import de.rainu.restcommander.config.security.AuthenticationToken;
import de.rainu.restcommander.model.User;
import de.rainu.restcommander.model.UserRole;
import de.rainu.restcommander.process.ProcessManager;
import de.rainu.restcommander.process.ProcessNotFoundException;
import de.rainu.restcommander.model.Process;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ProcessControllerTest {

	ProcessController toTest;

	@Before
	public void setup(){
		toTest = new ProcessController();
		toTest.processManager = mock(ProcessManager.class);
	}

	@Test
	public void checkProcessOwner_isAdmin() throws ProcessNotFoundException {
		AuthenticationToken token = new AuthenticationToken("<token>", new User("<name>", "<password>", UserRole.USER, UserRole.ADMIN));
		Process process = new Process();
		process.setUser("root");

		doReturn(process).when(toTest.processManager).getProcess(anyString());

		toTest.checkProcessOwner("<pid>", token);
	}

	@Test
	public void checkProcessOwner_isUser() throws ProcessNotFoundException {
		AuthenticationToken token = new AuthenticationToken("<token>", new User("root", "<password>", UserRole.USER));
		Process process = new Process();
		process.setUser("root");

		doReturn(process).when(toTest.processManager).getProcess(anyString());

		toTest.checkProcessOwner("<pid>", token);
	}

	@Test
	public void checkProcessOwner_isUserSu() throws ProcessNotFoundException {
		AuthenticationToken token = new AuthenticationToken("<token>", new User("user", "<password>", UserRole.USER));
		Process process = new Process();
		process.setUser("root");
		process.setCommandline("su user top");

		doReturn(process).when(toTest.processManager).getProcess(anyString());

		toTest.checkProcessOwner("<pid>", token);
	}

	@Test(expected = ProcessNotFoundException.class)
	public void checkProcessOwner_wrongUser() throws ProcessNotFoundException {
		AuthenticationToken token = new AuthenticationToken("<token>", new User("user", "<password>", UserRole.USER));
		Process process = new Process();
		process.setUser("root");
		process.setCommandline("top");

		doReturn(process).when(toTest.processManager).getProcess(anyString());

		toTest.checkProcessOwner("<pid>", token);
	}
}
