package de.rainu.restcommander.process;

import de.rainu.restcommander.model.Process;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
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
	private static final Pattern SIGNAL_PATTERN = Pattern.compile("[0-9]+");

	private Map<String, ProcessHandle> processHandles = new HashMap<>();

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

		final ProcessHandle handle;
		if(environment != null) {
			handle = executor.execute(line, environment, new DefaultExecuteResultHandler());
		} else {
			handle = executor.execute(line, new DefaultExecuteResultHandler());
		}

		final String pid = extractPid(handle.getProcess());
		if(pid != null) {
			processHandles.put(pid, handle);
		}

		return pid;
	}

	@Override
	public int sendSignal(String pid, String signal) throws IOException, ProcessNotFoundException {
		checkPid(pid);

		CommandLine killCommand = new CommandLine("kill");

		if(SIGNAL_PATTERN.matcher(signal).matches()) {
			killCommand.addArgument("-" + signal);
		}else {
			killCommand.addArgument("-s");
			killCommand.addArgument(signal);
		}
		killCommand.addArgument(pid);

		return new DefaultExecutor().execute(killCommand);
	}

	@Override
	public void sendInput(String pid, byte[] rawInput) throws IOException, ProcessNotFoundException {
		checkPid(pid);

		final ProcessHandle handle = processHandles.get(pid);
		if(handle == null){
			throw new ProcessNotFoundException(pid);
		}

		handle.getStdin().write(rawInput);
		handle.getStdin().flush();
	}

	private void checkPid(String pid) throws ProcessNotFoundException {
		if(pid == null || !PID_PATTERN.matcher(pid).matches()) {
			throw new ProcessNotFoundException(pid);
		}
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
