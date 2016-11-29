package de.rainu.restcommander.controller;

import de.rainu.restcommander.config.security.annotation.IsAdmin;
import de.rainu.restcommander.model.Process;
import de.rainu.restcommander.model.dto.*;
import de.rainu.restcommander.process.ProcessManager;
import de.rainu.restcommander.process.ProcessNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@IsAdmin
@RestController
@RequestMapping(path = ProcessController.ENDPOINT)
public class ProcessController {
	public static final String ENDPOINT = "/process";

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

	private String replaceSpecialCharacters(String input) {
		return input.replace("\\\t", "\t")
				  .replace("\\\t", "\t")
				  .replace("\\\b", "\b")
				  .replace("\\\n", "\n")
				  .replace("\\\r", "\r")
				  .replace("\\\f", "\f");
	}
}
