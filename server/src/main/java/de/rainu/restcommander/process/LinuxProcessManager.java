package de.rainu.restcommander.process;

import de.rainu.restcommander.model.Process;
import org.apache.commons.exec.*;
import org.apache.commons.exec.util.StringUtils;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LinuxProcessManager implements ProcessManager {
	private static final Pattern PID_PATTERN = Pattern.compile("[0-9]+");
	private static final Pattern SIGNAL_PATTERN = Pattern.compile("[0-9]+");

	Map<String, ProcessHandle> processHandles = new HashMap<>();

	private final File processOutputDir;

	public LinuxProcessManager() {
		try {
			this.processOutputDir = File.createTempFile("process_output", "");
			this.processOutputDir.delete();
			this.processOutputDir.mkdir();
			this.processOutputDir.deleteOnExit();
		} catch (IOException e) {
			throw new RuntimeException("Could not create temp directory!", e);
		}
	}

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
		process.setRunning(new File(getProcessDir(pid)).exists());

		if(!process.isRunning()) {
			return process;
		}

		try{
			Path path = Paths.get(getCommandlineFile(pid));
			FileOwnerAttributeView ownerAttributeView = Files.getFileAttributeView(path, FileOwnerAttributeView.class);
			UserPrincipal owner = ownerAttributeView.getOwner();
			process.setUser(owner.getName());
		}catch (IOException e){
		}

		try {
			process.setCommandline(FileUtils.readFileToString(new File(getCommandlineFile(pid))).replace("\u0000", " "));
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

			process.setEnvironment(environments);
		} catch (IOException e) {
		}

		return process;
	}

	@Override
	public Process getProcess(String pid) throws ProcessNotFoundException {
		checkPid(pid);

		final Process process = toProcess(pid);
		if(!process.isRunning() && processHandles.containsKey(pid)) {
			// i can only know the return code if i starts the process!
			process.setReturnCode(processHandles.get(pid).getProcess().exitValue());
		}

		return process;
	}

	@Override
	public String startProcess(String command, List<String> arguments,
										Map<String, String> environment, String workingDirectory) throws IOException {

		CommandLine line = new CommandLine(command);
		if(arguments != null) arguments.stream().forEach(line::addArgument);

		return startProcess(line, environment, workingDirectory);
	}

	@Override
	public String startProcessAsUser(String username, String password,
												String command, List<String> arguments,
												Map<String, String> environment, String workingDirectory) throws IOException {

		if(username == null || password == null) {
			throw new IllegalArgumentException("Username and password are mandatory!");
		}

		CommandLine line = new CommandLine("su");
		line.addArgument(username);
		line.addArgument("-c");

		StringBuffer sb = new StringBuffer();
		sb.append(command);
		if(arguments != null) arguments.stream().forEach(a -> {
			sb.append(" \"");
			sb.append(StringUtils.quoteArgument(a));
			sb.append("\"");
		});
		line.addArgument(sb.toString(), false);

		final String pid = startProcess(line, environment, workingDirectory);

		if(pid != null) {
			processHandles.get(pid).getStdin().write((password + "\n").getBytes());
			processHandles.get(pid).getStdin().flush();
		}

		return pid;
	}

	String startProcess(CommandLine command, Map<String, String> environment, String workingDirectory) throws IOException {
		CommandExecutor executor = new CommandExecutor(workingDirectory);

		final ProcessHandle handle;
		if(environment != null) {
			handle = executor.execute(command, environment, new DefaultExecuteResultHandler());
		} else {
			handle = executor.execute(command, new DefaultExecuteResultHandler());
		}

		final String pid = extractPid(handle.getProcess());
		if(pid != null) {
			processHandles.put(pid, handle);

			//save stderr/stdout before closing streams
			executor.addStreamHandler(new StreamCloseListener(pid, handle));
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

		try {
			return new DefaultExecutor().execute(killCommand);
		}catch(ExecuteException e) {
			return e.getExitValue();
		}
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

	@Override
	public Data readOutput(String pid, Long start) throws IOException, ProcessNotFoundException {
		return read(pid, start, true);
	}

	@Override
	public Data readError(String pid, Long start) throws IOException, ProcessNotFoundException {
		return read(pid, start, false);
	}

	private Data read(String pid, Long startRange, boolean stdout) throws IOException, ProcessNotFoundException {
		checkPid(pid);

		if(!processHandles.containsKey(pid)) {
			// i can only read input from process which i started
			throw new ProcessNotFoundException(pid);
		}

		InputStream is = getAlreadyRead(pid, startRange, stdout);
		if(is != null) {
			return readFrom(startRange, is);
		}

		final Data data = readFrom(pid, stdout);
		if(data.read > 0) {
			save(pid, data, stdout);
		}

		return data;
	}

	private InputStream getAlreadyRead(String pid, Long startRange, boolean stdout) throws FileNotFoundException {
		File file = stdout ? getStdOutFile(pid) : getStdErrFile(pid);

		if(!file.exists() || file.length() < startRange) {
			return null;
		}

		return new FileInputStream(file);
	}

	private void save(String pid, Data data, boolean stdout) throws IOException {
		File file = stdout ? getStdOutFile(pid) : getStdErrFile(pid);

		if(file.exists()) {
			file.createNewFile();
			file.deleteOnExit();
		}

		FileOutputStream fos = new FileOutputStream(file, true);

		fos.write(data.content, 0, data.read);
		fos.close();
	}

	private File getStdOutFile(String pid) {
		return new File(this.processOutputDir, pid + ".out");
	}

	private File getStdErrFile(String pid) {
		return new File(this.processOutputDir, pid + ".err");
	}

	private Data readFrom(String pid, boolean stdout) throws IOException {
		final ProcessHandle handle = processHandles.get(pid);
		BufferedInputStream reader = new BufferedInputStream(stdout ? handle.getStdout() : handle.getStderr());

		boolean dataAvailable = true;
		try {
			if (reader.available() <= 0) {
				dataAvailable = false;
			}
		}catch (IOException e) {
			dataAvailable = false;
		}

		if(!dataAvailable) {
			return new Data(new byte[]{}, 0);
		}

		byte[] content = new byte[8192];
		final int read = reader.read(content);

		return new Data(content, read);
	}

	private Data readFrom(Long startRange, InputStream is) throws IOException {
		is.skip(startRange);

		byte[] content = new byte[8192];
		try {
			final int read = is.read(content);

			return new Data(content, read <= 0 ? 0 : read);
		}finally {
			is.close();
		}
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

	private String getProcessDir(String pid) {
		return getProcDir() + "/" + pid + "/";
	}

	private String getCommandlineFile(String pid) {
		return getProcessDir(pid) + "cmdline";
	}

	private String getEnvironmentFile(String pid) {
		return getProcessDir(pid) + "environ";
	}

	private class StreamCloseListener implements ExecuteStreamHandler {
		private final String pid;
		private final ProcessHandle handle;

		public StreamCloseListener(String pid, ProcessHandle handle) {
			this.pid = pid;
			this.handle = handle;
		}

		@Override
		public void setProcessInputStream(OutputStream os) throws IOException {}

		@Override
		public void setProcessErrorStream(InputStream is) throws IOException {}

		@Override
		public void setProcessOutputStream(InputStream is) throws IOException {}

		@Override
		public void start() throws IOException {}

		@Override
		public void stop() throws IOException {
			try {
				backupStream(true);
			}catch (IOException e) {}

			try {
				backupStream(false);
			}catch (IOException e) {}
		}

		private void backupStream(boolean stdout) throws IOException {
			InputStream stream = stdout ? handle.getStdout() : handle.getStderr();

			while(stream.available() > 0) {
				byte[] content = new byte[8192];
				int read = stream.read(content);
				Data data = new Data(content, read);

				save(pid, data, stdout);
			}
		}

	}
}
