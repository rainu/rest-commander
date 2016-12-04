package de.rainu.restcommander.store;

import de.rainu.restcommander.model.User;
import de.rainu.restcommander.model.UserRole;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class LinuxUserStoreTest {

	final static Path passwd = new File(LinuxUserStoreTest.class.getResource("/passwd").getPath()).toPath();
	final static Path group = new File(LinuxUserStoreTest.class.getResource("/group").getPath()).toPath();

	LinuxUserStore toTest;

	@Before
	public void precondition(){
		toTest = spy(new LinuxUserStore());

		assumeTrue(System.getProperty("os.name").equalsIgnoreCase("linux"));
	}

	@Test
	public void readFromPasswd(){
		Map<String, String> result = toTest.readFromPasswd(passwd);

		assertTrue(result.containsKey("root"));
		assertEquals("0", result.get("root"));
		assertTrue(result.containsKey("rainu"));
		assertEquals("1000", result.get("rainu"));
	}

	@Test
	public void readFromGroup(){
		Map<String, String> result = toTest.readFromGroup(group);

		assertTrue(result.containsKey("0"));
		assertEquals("root", result.get("0"));
		assertTrue(result.containsKey("1000"));
		assertEquals("bin", result.get("1000"));
	}

	@Test
	public void readRootFromGroup(){
		Set<String> result = toTest.readRootFromGroup(group);

		assertTrue(result.contains("root"));
		assertTrue(result.contains("rainu"));
	}

	@Test
	public void getUserAndGroup() {
		Map<String, String> result = toTest.getUserAndGroup(passwd, group);

		assertEquals("root", result.get("root"));
		assertEquals("bin", result.get("rainu"));
	}

	@Test
	public void getRootUsers(){
		Set<String> result = toTest.getRootUsers(group);

		assertTrue(result.contains("root"));
		assertTrue(result.contains("rainu"));
	}

	@Test
	public void contains(){
		Map<String, String> userAndGroups = new HashMap<>();
		userAndGroups.put("rainu", "bin");

		doReturn(userAndGroups).when(toTest).getUserAndGroup(any(), any());

		assertTrue(toTest.contains("rainu"));
		assertFalse(toTest.contains("test"));
	}

	@Test
	public void get_notContains(){
		doReturn(false).when(toTest).contains(anyString());
		assertNull(toTest.get("user"));
	}

	@Test
	public void get_noRoot(){
		doReturn(true).when(toTest).contains(anyString());

		Map<String, String> userAndGroups = new HashMap<>();
		userAndGroups.put("user", "bin");
		doReturn(userAndGroups).when(toTest).getUserAndGroup(any(), any());

		Set<String> rootUsers = new HashSet<>();
		doReturn(rootUsers).when(toTest).getRootUsers(any());

		User result = toTest.get("user");
		Set<String> authorities = result.getRoles().stream()
				  .map(ga -> ga.getAuthority())
				  .collect(Collectors.toSet());

		assertFalse(authorities.contains(UserRole.ADMIN.getAuthority()));
		assertTrue(authorities.contains(UserRole.USER.getAuthority()));
		assertTrue(authorities.contains("bin"));
	}

	@Test
	public void get_root(){
		doReturn(true).when(toTest).contains(anyString());

		Map<String, String> userAndGroups = new HashMap<>();
		userAndGroups.put("user", "bin");
		doReturn(userAndGroups).when(toTest).getUserAndGroup(any(), any());

		Set<String> rootUsers = new HashSet<>();
		rootUsers.add("user");
		doReturn(rootUsers).when(toTest).getRootUsers(any());

		User result = toTest.get("user");
		Set<String> authorities = result.getRoles().stream()
				  .map(ga -> ga.getAuthority())
				  .collect(Collectors.toSet());

		assertTrue(authorities.contains(UserRole.ADMIN.getAuthority()));
		assertTrue(authorities.contains(UserRole.USER.getAuthority()));
		assertTrue(authorities.contains("bin"));
	}

	@Test
	public void get_systemUser(){
		doReturn(true).when(toTest).contains(anyString());

		Map<String, String> userAndGroups = new HashMap<>();
		userAndGroups.put(System.getProperty("user.name"), "bin");
		doReturn(userAndGroups).when(toTest).getUserAndGroup(any(), any());

		Set<String> rootUsers = new HashSet<>();
		doReturn(rootUsers).when(toTest).getRootUsers(any());

		User result = toTest.get(System.getProperty("user.name"));
		Set<String> authorities = result.getRoles().stream()
				  .map(ga -> ga.getAuthority())
				  .collect(Collectors.toSet());

		assertTrue(authorities.contains(UserRole.ADMIN.getAuthority()));
		assertTrue(authorities.contains(UserRole.USER.getAuthority()));
		assertTrue(authorities.contains("bin"));
	}

	@Test
	public void checkPassword() throws IOException {
		doReturn(0).when(toTest).execute(any(), any());

		assertTrue(toTest.checkPassword("<username>", "<password>"));

		ArgumentCaptor<DefaultExecutor> executorCap = ArgumentCaptor.forClass(DefaultExecutor.class);
		ArgumentCaptor<CommandLine> lineCap = ArgumentCaptor.forClass(CommandLine.class);

		verify(toTest).execute(executorCap.capture(), lineCap.capture());

		assertEquals("su", lineCap.getValue().getExecutable());
		assertArrayEquals(new String[]{
				  "<username>", "-c", "echo"
		}, lineCap.getValue().getArguments());

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ExecuteStreamHandler handler = executorCap.getValue().getStreamHandler();
		handler.setProcessInputStream(stream);

		assertArrayEquals(
				  ("<password>\n").getBytes(),
				  stream.toByteArray());
	}
}
