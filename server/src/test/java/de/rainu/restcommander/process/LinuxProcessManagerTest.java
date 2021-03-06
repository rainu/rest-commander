package de.rainu.restcommander.process;

import de.rainu.restcommander.model.Process;
import org.apache.commons.exec.CommandLine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class LinuxProcessManagerTest {

	LinuxProcessManager toTest;

	private String getTestProcDir() {
		URL url = LinuxProcessManagerTest.class.getResource("/proc");
		File file = new File(url.getPath());

		return file.getAbsolutePath();
	}

	@Before
	public void setup() {
		toTest = spy(new LinuxProcessManager());
	}

	private void assumeLinux() {
		assumeTrue(System.getProperty("os.name").equalsIgnoreCase("linux"));
	}

	@Test
	public void listProcess() throws IOException {
		doReturn(getTestProcDir()).when(toTest).getProcDir();

		List<Process> processList = toTest.listProcess();
		assertFalse(processList.isEmpty());
		assertEquals(1, processList.size());
		assertEquals(System.getProperty("user.name"), processList.get(0).getUser());
		assertEquals("1312", processList.get(0).getId());
		assertEquals("0", processList.get(0).getParent());
		assertEquals("/sbin/init", processList.get(0).getCommandline());
		assertEquals(2, processList.get(0).getEnvironment().size());
		assertEquals("VALUE1", processList.get(0).getEnvironment().get("ENV_1"));
		assertEquals("VALUE2", processList.get(0).getEnvironment().get("ENV_2"));
	}

	@Test
	public void listProcess_integration() throws IOException {
		assumeLinux();

		final String ownPID = getOwnPID();
		Process ownProccess = toTest.listProcess().stream().filter(p -> p.getId().equalsIgnoreCase(ownPID)).findFirst().get();

		assertTrue(ownProccess.getCommandline().contains("java"));
		System.getenv().entrySet().stream().forEach(env -> {
			assertEquals(env.getValue(), ownProccess.getEnvironment().get(env.getKey()));
		});
	}

	private String getOwnPID() {
		return ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
	}

	@Test
	public void getProcess() throws ProcessNotFoundException {
		assumeLinux();

		Process result = toTest.getProcess(getOwnPID());

		assertEquals(getOwnPID(), result.getId());
		assertNotEquals(getOwnPID(), result.getParent());
		assertTrue(result.getCommandline().contains("java "));
		assertNull(result.getReturnCode());
		assertEquals(System.getenv(), result.getEnvironment());
		assertEquals(System.getProperty("user.name"), result.getUser());
	}

	@Test(expected = ProcessNotFoundException.class)
	public void getProcess_NoProcessFound() throws ProcessNotFoundException {
		assumeLinux();

		toTest.getProcess(String.valueOf(Long.MAX_VALUE));
	}

	@Test
	public void startAndReadProcess() throws IOException, ProcessNotFoundException, InterruptedException {
		assumeLinux();

		Map<String, String> environment = new HashMap<>();
		environment.put("env1", "value1");
		environment.put("env2", "value2");

		final String pid = toTest.startProcess("env", null, environment, System.getProperty("java.io.tmpdir"));
		Thread.sleep(1000);
		ProcessManager.Data data = toTest.readOutput(pid, 0L);

		environment.entrySet().stream().forEach(env -> {
			assertTrue(new String(data.content).contains(env.getKey() + "=" + env.getValue()));
		});
	}

	@Test
	public void startProcessAsUser() throws IOException {
		final ProcessHandle handle = new ProcessHandle(null, new ByteArrayOutputStream(), null, null);
		doAnswer(invocation -> {
			toTest.processHandles.put("1312", handle);
			return "1312";
		}).when(toTest).startProcess(any(), anyMap(), anyString());

		final String pid = toTest.startProcessAsUser("<username>", "<password>",
				  "<command>",
				  Arrays.asList("<arg1>", "<arg2>"),
				  System.getenv(),
				  "<workDir>");
		assertEquals("1312", pid);

		ArgumentCaptor<CommandLine> cmdCap = ArgumentCaptor.forClass(CommandLine.class);
		ArgumentCaptor<Map> envCap = ArgumentCaptor.forClass(Map.class);
		ArgumentCaptor<String> workCap = ArgumentCaptor.forClass(String.class);

		verify(toTest).startProcess(cmdCap.capture(), envCap.capture(), workCap.capture());

		assertEquals("su", cmdCap.getValue().getExecutable());
		assertArrayEquals(new String[]{
				  "<username>", "-c", "<command> \"<arg1>\" \"<arg2>\""
		}, cmdCap.getValue().getArguments());

		assertEquals(System.getenv(), envCap.getValue());
		assertArrayEquals(
				  ("<password>\n").getBytes(),
				  ((ByteArrayOutputStream)handle.getStdin()).toByteArray());
	}

	@Test
	public void sendSignal_bySignalInt() throws IOException, ProcessNotFoundException {
		assumeLinux();

		final String pid = toTest.startProcess("sh", null, null, null);
		assertTrue(toTest.getProcess(pid).isRunning());

		assertEquals(new Integer(0), new Integer(toTest.sendSignal(pid, "9")));
		assertFalse(toTest.getProcess(pid).isRunning());
	}

	@Test
	public void sendSignal_bySignlaName() throws IOException, ProcessNotFoundException {
		assumeLinux();

		final String pid = toTest.startProcess("sh", null, null, null);
		assertTrue(toTest.getProcess(pid).isRunning());

		assertEquals(new Integer(0), new Integer(toTest.sendSignal(pid, "TERM")));
		assertFalse(toTest.getProcess(pid).isRunning());
	}

	@Test
	public void sendSignal_alreadyKilledProcess() throws IOException, ProcessNotFoundException, InterruptedException {
		assumeLinux();

		final String pid = toTest.startProcess("echo", null, null, null);
		Thread.sleep(500);
		assertFalse(toTest.getProcess(pid).isRunning());

		assertNotEquals(new Integer(0), new Integer(toTest.sendSignal(pid, "TERM")));
	}

	@Test(expected = ProcessNotFoundException.class)
	public void sendSignal_NoProcessFound() throws ProcessNotFoundException, IOException {
		toTest.sendSignal(String.valueOf(Long.MAX_VALUE), "9");
	}

	@Test(expected = ProcessNotFoundException.class)
	public void input_NoProcessFound() throws ProcessNotFoundException, IOException {
		toTest.sendInput(String.valueOf(Long.MAX_VALUE), new byte[]{});
	}

	@Test
	public void readAndWrite_stdout() throws IOException, ProcessNotFoundException, InterruptedException {
		assumeLinux();

		final String pid = toTest.startProcess("/bin/sh", null, null, System.getProperty("java.io.tmpdir"));

		toTest.sendInput(pid, new String("echo HelloWorld!\n").getBytes());
		Thread.sleep(1000);
		String output = new String(toTest.readOutput(pid, 0L).content);

		assertTrue(output.contains("HelloWorld!"));

		output = new String(toTest.readOutput(pid, 0L).content);
		assertTrue(output.contains("HelloWorld!"));
	}

	@Test(expected = ProcessNotFoundException.class)
	public void readOutput_NoProcessFound() throws ProcessNotFoundException, IOException {
		toTest.readOutput(String.valueOf(Long.MAX_VALUE), 0L);
	}

	@Test
	public void readAndWrite_stderr() throws IOException, ProcessNotFoundException, InterruptedException {
		assumeLinux();

		final String pid = toTest.startProcess("/bin/sh", null, null, System.getProperty("java.io.tmpdir"));

		toTest.sendInput(pid, new String(">&2 echo HelloWorld!\n").getBytes());
		Thread.sleep(1000);
		String error = new String(toTest.readError(pid, 0L).content);

		assertTrue(error.contains("HelloWorld!"));

		error = new String(toTest.readError(pid, 0L).content);
		assertTrue(error.contains("HelloWorld!"));
	}

	@Test(expected = ProcessNotFoundException.class)
	public void readError_NoProcessFound() throws ProcessNotFoundException, IOException {
		toTest.readError(String.valueOf(Long.MAX_VALUE), 0L);
	}
}
