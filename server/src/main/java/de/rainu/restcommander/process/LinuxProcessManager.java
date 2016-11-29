package de.rainu.restcommander.process;

import de.rainu.restcommander.model.Process;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LinuxProcessManager implements ProcessManager{
	private static final Pattern PID_PATTERN = Pattern.compile("[0-9]+");

	@Override
	public List<Process> listProcess() throws IOException {
		return Files.list(Paths.get(getProcDir()))
				  .filter(path -> path.toFile().isDirectory())
				  .map(path -> path.getFileName().toString())
				  .filter(fn -> PID_PATTERN.matcher(fn).matches())
				  .map(this::toProcess)
				  .collect(Collectors.toList());
	}

	private Process toProcess(String pid) {
		Process process = new Process();
		process.setId(pid);

		try {
			process.setCmdline(FileUtils.readFileToString(new File(getCommandlineFile(pid))).replace("\u0000", " "));
		} catch (IOException e) {
		}

		try {
			final String[] rawEnvironments = FileUtils.readFileToString(new File(getEnvironmentFile(pid))).split("\u0000");
			final Map<String, String> environments = new HashMap<>(rawEnvironments.length);

			Arrays.stream(rawEnvironments)
					  .map(entry -> entry.split("="))
					  .forEach(split -> {
					  		final String key = split[0];
					  		final String value = Arrays.stream(split).skip(1L).collect(Collectors.joining("="));

					  		environments.put(key, value);
					  });

			process.setEnv(environments);
		} catch (IOException e) {
		}

		return process;
	}

	@Override
	public String createProcess(String command, List<String> arguments,
										 Map<String, String> environment, String workingDirectory) throws IOException {

		CommandLine line = new CommandLine(command);
		if(arguments != null) arguments.stream().forEach(line::addArgument);

		CommandExecutor executor = new CommandExecutor(workingDirectory);

		final java.lang.Process process;
		if(environment != null) {
			process = executor.execute(line, environment, new DefaultExecuteResultHandler());
		} else {
			process = executor.execute(line, new DefaultExecuteResultHandler());
		}

		return extractPid(process);
	}

	private String extractPid(java.lang.Process process) {
		Field field = null;
		Object pid = null;

		try {
			field = process.getClass().getDeclaredField("pid");
			field.setAccessible(true);
			pid = field.get(process);
		} catch(Exception e) {
		} finally {
			if(field != null) {
				field.setAccessible(false);
			}
		}

		if(pid == null) return null;
		return String.valueOf(pid);
	}

	String getProcDir() {
		return "/proc/";
	}

	private String getCommandlineFile(String pid) {
		return getProcDir() + "/" + pid + "/cmdline";
	}

	private String getEnvironmentFile(String pid) {
		return getProcDir() + "/" + pid + "/environ";
	}
}
