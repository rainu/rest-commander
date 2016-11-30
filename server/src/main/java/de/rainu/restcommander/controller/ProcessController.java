package de.rainu.restcommander.controller;

import de.rainu.restcommander.config.security.annotation.IsAdmin;
import de.rainu.restcommander.model.Process;
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

@IsAdmin
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
	public ProcessCreateResponse create(@RequestBody ProcessRequest process) throws IOException {
		final String pid = processManager.createProcess(process.getCmd(),
				  process.getArgs(),
				  process.getEnv(),
				  process.getWorkdir());

		return new ProcessCreateResponse(pid);
	}

	@RequestMapping(path = "/{pid}/{signal}", method = RequestMethod.PATCH)
	@ResponseBody
	public ProcessSignalResponse signal(@PathVariable("pid") String pid,
													@PathVariable("signal") String signal) throws IOException, ProcessNotFoundException {

		ProcessSignalResponse response = new ProcessSignalResponse();
		response.setReturnCode(processManager.sendSignal(pid, signal));

		return response;
	}

	@RequestMapping(path = "/{pid}", method = RequestMethod.POST)
	public void input(@PathVariable("pid") String pid,
							@RequestBody ProcessInputRequest processInput,
							HttpServletRequest request) throws IOException, ProcessNotFoundException {

		final byte[] rawInput;
		String encoding = request.getCharacterEncoding();
		if(encoding == null) encoding = "UTF-8";

		if(processInput.getRaw() != null) {
			rawInput = Base64.getDecoder().decode(processInput.getRaw());
		}else if(processInput.getInput() != null) {
			rawInput = replaceSpecialCharacters(processInput.getInput()).getBytes(encoding);
		} else {
			return;	//NO INPUT?!
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
			  @PathVariable("pid") String pid,
			  @PathVariable("stream") String stream,
			  @RequestHeader(value = "Range", defaultValue = "0-") String range) throws IOException, ProcessNotFoundException {

		final Matcher rangeMatcher = RANGE_PATTERN.matcher(range);

		if(!rangeMatcher.matches()) {
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

	private String replaceSpecialCharacters(String input) {
		return input.replace("\\\t", "\t")
				  .replace("\\\t", "\t")
				  .replace("\\\b", "\b")
				  .replace("\\\n", "\n")
				  .replace("\\\r", "\r")
				  .replace("\\\f", "\f");
	}
}
