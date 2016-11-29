package de.rainu.restcommander.controller;

import de.rainu.restcommander.config.security.annotation.IsAdmin;
import de.rainu.restcommander.model.Process;
import de.rainu.restcommander.model.dto.ProcessCreateResponse;
import de.rainu.restcommander.model.dto.ProcessRequest;
import de.rainu.restcommander.process.ProcessManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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
}