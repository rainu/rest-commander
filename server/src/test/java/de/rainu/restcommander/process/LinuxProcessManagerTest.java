package de.rainu.restcommander.process;

import de.rainu.restcommander.model.Process;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class LinuxProcessManagerTest {

	private String getTestProcDir(){
		URL url = LinuxProcessManagerTest.class.getResource("/proc");
		File file = new File(url.getPath());

		return file.getAbsolutePath();
	}

	@Test
	public void listProcess() throws IOException {
		LinuxProcessManager manager = spy(new LinuxProcessManager());

		doReturn(getTestProcDir()).when(manager).getProcDir();

		List<Process> processList = manager.listProcess();
		assertFalse(processList.isEmpty());
		assertEquals(1, processList.size());
		assertEquals("1312", processList.get(0).getId());
		assertEquals("/sbin/init", processList.get(0).getCmdline());
		assertEquals(2, processList.get(0).getEnv().size());
		assertEquals("VALUE1", processList.get(0).getEnv().get("ENV_1"));
		assertEquals("VALUE2", processList.get(0).getEnv().get("ENV_2"));
	}
}
