package de.rainu.restcommander.controller;

import de.rainu.restcommander.config.security.AuthenticationToken;
import de.rainu.restcommander.config.security.annotation.IsAdmin;
import de.rainu.restcommander.config.security.annotation.IsUser;
import de.rainu.restcommander.model.Process;
import de.rainu.restcommander.model.UserRole;
import de.rainu.restcommander.model.dto.ProcessCreateResponse;
import de.rainu.restcommander.model.dto.ProcessInputRequest;
import de.rainu.restcommander.model.dto.ProcessRequest;
import de.rainu.restcommander.model.dto.ProcessSignalResponse;
import de.rainu.restcommander.process.ProcessManager;
import de.rainu.restcommander.process.ProcessNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@IsUser
@RestController
@RequestMapping(path = ProcessController.ENDPOINT)
public class ProcessController {
	public static final String ENDPOINT = "/process";

	private static final Pattern RANGE_PATTERN = Pattern.compile("([0-9]+)-");

	@Autowired
	ProcessManager processManager;

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public List<Process> listProcess() throws IOException {
		return processManager.listProcess();
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public ProcessCreateResponse createAsUser(
			  AuthenticationToken authToken,
			  @RequestBody ProcessRequest process) throws IOException {

		final String pid = processManager.startProcessAsUser(
				  authToken.getUser().getUsername(),
				  authToken.getUser().getPassword(),
				  process.getCommand(),
				  process.getArguments(),
				  process.getEnvironment(),
				  process.getWorkDirectory());

		return new ProcessCreateResponse(pid);
	}

	@IsAdmin
	@RequestMapping(path = "/admin", method = RequestMethod.POST)
	@ResponseBody
	public ProcessCreateResponse create(
			  @RequestBody ProcessRequest process) throws IOException {

		final String pid = processManager.startProcess(
				  process.getCommand(),
				  process.getArguments(),
				  process.getEnvironment(),
				  process.getWorkDirectory());

		return new ProcessCreateResponse(pid);
	}

	@RequestMapping(path = "/{pid}/{signal}", method = RequestMethod.POST)
	@ResponseBody
	public ProcessSignalResponse signal(AuthenticationToken token,
			  										@PathVariable("pid") String pid,
													@PathVariable("signal") String signal) throws IOException, ProcessNotFoundException {

		checkProcessOwner(pid, token);

		ProcessSignalResponse response = new ProcessSignalResponse();
		response.setReturnCode(processManager.sendSignal(pid, signal));

		return response;
	}

	@RequestMapping(path = "/{pid}", method = RequestMethod.POST)
	public void input(AuthenticationToken token,
			  				@PathVariable("pid") String pid,
							@RequestBody ProcessInputRequest processInput,
							HttpServletRequest request) throws IOException, ProcessNotFoundException {

		checkProcessOwner(pid, token);

		final byte[] rawInput;
		String encoding = request.getCharacterEncoding();
		if (encoding == null) encoding = "UTF-8";

		if (processInput.getRaw() != null) {
			rawInput = Base64.getDecoder().decode(processInput.getRaw());
		} else if (processInput.getInput() != null) {
			rawInput = replaceSpecialCharacters(processInput.getInput()).getBytes(encoding);
		} else {
			return;   //NO INPUT?!
		}

		processManager.sendInput(pid, rawInput);
	}

	@RequestMapping(path = "/{pid}", method = RequestMethod.GET)
	@ResponseBody
	public Process status(@PathVariable("pid") String pid) throws ProcessNotFoundException {
		return processManager.getProcess(pid);
	}

	@RequestMapping(path = "/{pid}/{stream}", method = RequestMethod.GET, produces = "application/octet-stream")
	public ResponseEntity output(
			  AuthenticationToken token,
			  @PathVariable("pid") String pid,
			  @PathVariable("stream") String stream,
			  @RequestHeader(value = "Range", defaultValue = "0-") String range) throws IOException, ProcessNotFoundException {

		checkProcessOwner(pid, token);

		final Matcher rangeMatcher = RANGE_PATTERN.matcher(range);

		if (!rangeMatcher.matches()) {
			return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build();
		}

		Long startRange = Long.parseLong(rangeMatcher.group(1));
		ProcessManager.Data rawData;

		switch (stream.toLowerCase()) {
			case "out":
				rawData = processManager.readOutput(pid, startRange);
				break;
			case "err":
				rawData = processManager.readError(pid, startRange);
				break;
			default:
				return ResponseEntity.badRequest().build();
		}

		return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
				  .contentLength(rawData.read)
				  .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
				  .header(HttpHeaders.ACCEPT_RANGES, "bytes")
				  .header(HttpHeaders.CONTENT_RANGE, "bytes " + range + (startRange + rawData.read) + "/*")
				  .body(rawData.content);
	}

	void checkProcessOwner(String pid, AuthenticationToken token) throws ProcessNotFoundException {
		boolean isAdmin = token.getUser().getRoles().stream()
				  .filter(r -> UserRole.ADMIN.getAuthority().equals(r.getAuthority()))
				  .count() > 0;

		if(isAdmin){
			return;	//the admin can control all processes!
		}

		final Process process = processManager.getProcess(pid);
		if(!process.getUser().equals(token.getUser().getUsername())){
			//the su command always run as root but the underlying process runs as the user
			if(!process.getCommandline().startsWith("su " + token.getUser().getUsername() + " ")) {
				throw new ProcessNotFoundException(pid);
			}
		}
	}

	private String replaceSpecialCharacters(String input) {
		return input.replace("\\\t", "\t")
				  .replace("\\\b", "\b")
				  .replace("\\\n", "\n")
				  .replace("\\\r", "\r")
				  .replace("\\\f", "\f");
	}
}
