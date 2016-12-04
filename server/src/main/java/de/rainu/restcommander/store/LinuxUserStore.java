package de.rainu.restcommander.store;

import de.rainu.restcommander.model.User;
import de.rainu.restcommander.model.UserRole;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.springframework.security.core.GrantedAuthority;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class LinuxUserStore implements UserStore {

	private final Path ETC_PASSWD = new File("/etc/passwd").toPath();
	private final Path ETC_GROUP = new File("/etc/group").toPath();

	private Map<String, String> userAndGroup;
	private Map<String, User> allocatedUsers = new HashMap<>();
	private Set<String> rootUsers;

	@Override
	public User get(String username) {
		if(!allocatedUsers.containsKey(username) && !contains(username)){
			return null;
		}

		if(!allocatedUsers.containsKey(username)) {
			List<GrantedAuthority> authorityList = new ArrayList<>();
			authorityList.add(UserRole.USER);
			authorityList.add((GrantedAuthority) () -> getUserAndGroup().get(username));
			if(System.getenv("user.name").equals(username) || getRootUsers().contains(username)){
				authorityList.add(UserRole.ADMIN);
			}

			final User user = new User(username, null, authorityList);
			allocatedUsers.put(username, user);
		}

		return allocatedUsers.get(username);
	}

	@Override
	public boolean contains(String username) {
		return getUserAndGroup().containsKey(username);
	}

	@Override
	public boolean checkPassword(String username, String password) {
		CommandLine line = new CommandLine("su");
		line.addArgument(username);
		line.addArgument("-c");
		line.addArgument("echo");

		DefaultExecutor executor = new DefaultExecutor();
		executor.setStreamHandler(new ExecuteStreamHandler() {
			@Override
			public void setProcessInputStream(OutputStream os) throws IOException {
				os.write((password + "\n").getBytes());
				os.flush();
			}

			@Override
			public void setProcessErrorStream(InputStream is) throws IOException {}

			@Override
			public void setProcessOutputStream(InputStream is) throws IOException {}

			@Override
			public void start() throws IOException {}

			@Override
			public void stop() throws IOException {}
		});

		try {
			return executor.execute(line) == 0;
		} catch (IOException e) {
			return false;
		}
	}

	Set<String> getRootUsers(){
		if(rootUsers == null) {
			rootUsers = readRootFromGroup();
		}

		return rootUsers;
	}

	Map<String, String> getUserAndGroup() {
		if (userAndGroup == null) {
			//key -> username, value -> groupId
			Map<String, String> passwdEntries = readFromPasswd();

			//key -> groupId, value -> groupName
			Map<String, String> groupEntries = readFromGroup();

			userAndGroup = passwdEntries.entrySet().stream()
					  .collect(Collectors.toMap(
								 e -> e.getKey(),	//username itself
								 e -> groupEntries.get(e.getValue()) //groupname
					  ));
		}

		return userAndGroup;
	}

	Map<String, String> readFromPasswd() {
		try {
			return Files.lines(ETC_PASSWD)
					  .map(entry -> entry.split(":"))
					  .collect(Collectors.toMap(s -> s[0], s -> s[3]));
		} catch (IOException e) {
		}

		return new HashMap<>();
	}

	Map<String, String> readFromGroup(){
		try {
			return Files.lines(ETC_GROUP)
					  .map(entry -> entry.split(":"))
					  .collect(Collectors.toMap(s -> s[2], s -> s[0]));
		} catch (IOException e) {
		}

		return new HashMap<>();
	}

	Set<String> readRootFromGroup(){
		Set<String> result = new HashSet<>();

		try {
			Files.lines(ETC_GROUP)
				  .filter(line -> line.startsWith("root:"))
				  .map(entry -> entry.split(":")[3])
				  .filter(roots -> !"".equals(roots))
				  .map(roots -> roots.split(","))
				  .forEach(roots -> result.addAll(Arrays.asList(roots)));
		} catch (IOException e) {
		}

		return result;
	}
}
