package de.rainu.restcommander.controller;

import de.rainu.restcommander.model.dto.ErrorResponse;
import de.rainu.restcommander.process.ProcessNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ExceptionController {

	@ExceptionHandler(ProcessNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ResponseBody
	public ErrorResponse handleProcessNotFound(ProcessNotFoundException e){
		return new ErrorResponse(e.getMessage());
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorResponse handleOtherExceptions(Exception e){
		e.printStackTrace();
		return new ErrorResponse("An error occurs. See logs for details.");
	}
}