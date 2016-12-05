package de.rainu.restcommander.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.ClientResponse;
import de.rainu.restcommander.IntegrationTest;
import de.rainu.restcommander.model.Process;
import de.rainu.restcommander.model.dto.ProcessCreateResponse;
import de.rainu.restcommander.model.dto.ProcessInputRequest;
import de.rainu.restcommander.model.dto.ProcessRequest;
import de.rainu.restcommander.model.dto.ProcessSignalResponse;
import de.rainu.restcommander.process.ProcessManager;
import de.rainu.restcommander.process.ProcessNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.*;

import static de.rainu.restcommander.controller.ProcessController.ENDPOINT;
import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class ProcessControllerIT extends IntegrationTest {

	@Autowired
	ProcessManager processManager;
	ObjectMapper mapper = new ObjectMapper();

	@Before
	public void setup(){
		Mockito.reset(processManager);
	}

	@Test
	public void list_accessDenied(){
		ClientResponse response = client.resource(baseUrl + ENDPOINT)
				  .get(ClientResponse.class);

		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
	}

	@Test
	public void list_success() throws IOException {
		doReturn(Collections.EMPTY_LIST).when(processManager).listProcess();

		ClientResponse response = adminRequest(baseUrl + ENDPOINT)
				  .get(ClientResponse.class);

		JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, Process.class);
		List<Process> process = mapper.readValue(response.getEntity(String.class), type);

		assertEquals(HttpStatus.OK.value(), response.getStatus());
		assertTrue(process.isEmpty());

		verify(processManager, times(1)).listProcess();
	}

	@Test
	public void createAsUser_accessDenied() throws JsonProcessingException {
		ProcessRequest processRequest = new ProcessRequest();
		ClientResponse response = client.resource(baseUrl + ENDPOINT)
				  .type(MediaType.APPLICATION_JSON)
				  .post(ClientResponse.class, mapper.writeValueAsString(processRequest));

		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
	}

	@Test
	public void createAsUser_success() throws IOException {
		doReturn("1312").when(processManager).startProcessAsUser(anyString(), anyString(), anyString(), anyList(), anyMap(), anyString());

		ProcessRequest processRequest = new ProcessRequest();
		processRequest.setCommand("command");
		processRequest.setArguments(Arrays.asList("arg1", "arg2"));
		processRequest.setEnvironment(new HashMap<>());
		processRequest.setWorkDirectory("workdir");

		ClientResponse response = adminRequest(baseUrl + ENDPOINT)
				  .type(MediaType.APPLICATION_JSON)
				  .post(ClientResponse.class, mapper.writeValueAsString(processRequest));

		assertEquals(HttpStatus.OK.value(), response.getStatus());
		ProcessCreateResponse result = mapper.readValue(response.getEntity(String.class), ProcessCreateResponse.class);

		assertTrue(result.isCreated());
		assertEquals("1312", result.getPid());
		verify(processManager, times(1)).startProcessAsUser(
				  eq("admin"), eq("admin"),
				  eq(processRequest.getCommand()), eq(processRequest.getArguments()),
				  eq(processRequest.getEnvironment()), eq(processRequest.getWorkDirectory())
		);
	}

	@Test
	public void create_accessDenied() throws JsonProcessingException {
		ProcessRequest processRequest = new ProcessRequest();
		ClientResponse response = client.resource(baseUrl + ENDPOINT + "/admin")
				  .type(MediaType.APPLICATION_JSON)
				  .post(ClientResponse.class, mapper.writeValueAsString(processRequest));

		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
	}

	@Test
	public void create_success() throws IOException {
		doReturn("1312").when(processManager).startProcess(anyString(), anyList(), anyMap(), anyString());

		ProcessRequest processRequest = new ProcessRequest();
		processRequest.setCommand("command");
		processRequest.setArguments(Arrays.asList("arg1", "arg2"));
		processRequest.setEnvironment(new HashMap<>());
		processRequest.setWorkDirectory("workdir");

		ClientResponse response = adminRequest(baseUrl + ENDPOINT + "/admin")
				  .type(MediaType.APPLICATION_JSON)
				  .post(ClientResponse.class, mapper.writeValueAsString(processRequest));

		assertEquals(HttpStatus.OK.value(), response.getStatus());
		ProcessCreateResponse result = mapper.readValue(response.getEntity(String.class), ProcessCreateResponse.class);

		assertTrue(result.isCreated());
		assertEquals("1312", result.getPid());
		verify(processManager, times(1)).startProcess(
				  eq(processRequest.getCommand()), eq(processRequest.getArguments()),
				  eq(processRequest.getEnvironment()), eq(processRequest.getWorkDirectory())
		);
	}

	@Test
	public void signal_accessDenied() {
		ClientResponse response = client.resource(baseUrl + ENDPOINT + "/1312/9")
				  .post(ClientResponse.class);

		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
	}

	@Test
	public void signal_success() throws IOException, ProcessNotFoundException {
		doReturn(1308).when(processManager).sendSignal(anyString(), anyString());
		ClientResponse response = adminRequest(baseUrl + ENDPOINT + "/1312/9")
				  .post(ClientResponse.class);

		assertEquals(HttpStatus.OK.value(), response.getStatus());

		ProcessSignalResponse result = mapper.readValue(response.getEntity(String.class), ProcessSignalResponse.class);

		assertEquals(new Integer(1308), result.getReturnCode());
		verify(processManager, times(1)).sendSignal("1312", "9");
	}

	@Test
	public void input_accessDenied() throws JsonProcessingException {
		ProcessInputRequest processRequest = new ProcessInputRequest();
		ClientResponse response = client.resource(baseUrl + ENDPOINT + "/1312")
				  .type(MediaType.APPLICATION_JSON)
				  .post(ClientResponse.class, mapper.writeValueAsString(processRequest));

		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
	}

	@Test
	public void input_sucess() throws IOException, ProcessNotFoundException {
		ProcessInputRequest processRequest = new ProcessInputRequest();
		processRequest.setInput("\\\t\\\b\\\n\\\r\\\f");

		ClientResponse response = adminRequest(baseUrl + ENDPOINT + "/1312")
				  .type(MediaType.APPLICATION_JSON)
				  .post(ClientResponse.class, mapper.writeValueAsString(processRequest));

		assertEquals(HttpStatus.OK.value(), response.getStatus());

		verify(processManager, times(1)).sendInput(
				  eq("1312"),
				  aryEq("\t\b\n\r\f".getBytes()));
	}

	@Test
	public void input_sucess_raw() throws IOException, ProcessNotFoundException {
		ProcessInputRequest processRequest = new ProcessInputRequest();
		processRequest.setRaw(Base64.getEncoder().encodeToString(new byte[]{0,1,2,3,4,5,6,7,8,9,10}));

		ClientResponse response = adminRequest(baseUrl + ENDPOINT + "/1312")
				  .type(MediaType.APPLICATION_JSON)
				  .post(ClientResponse.class, mapper.writeValueAsString(processRequest));

		assertEquals(HttpStatus.OK.value(), response.getStatus());

		verify(processManager, times(1)).sendInput(
				  eq("1312"),
				  aryEq(new byte[]{0,1,2,3,4,5,6,7,8,9,10}));
	}

	@Test
	public void status_accessDenied() {
		ClientResponse response = client.resource(baseUrl + ENDPOINT + "/1312")
				  .get(ClientResponse.class);

		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
	}

	@Test
	public void status_success() throws IOException, ProcessNotFoundException {
		Process process = new Process();
		process.setReturnCode(12);
		process.setEnvironment(new HashMap<>());
		process.setCommandline("echo");
		process.setId("1312");
		process.setRunning(false);

		doReturn(process).when(processManager).getProcess(anyString());
		ClientResponse response = adminRequest(baseUrl + ENDPOINT + "/1312")
				  .get(ClientResponse.class);

		assertEquals(HttpStatus.OK.value(), response.getStatus());

		Process result = mapper.readValue(response.getEntity(String.class), Process.class);

		assertEquals(process, result);
		verify(processManager, times(1)).getProcess("1312");
	}

	@Test
	public void output_accessDenied() {
		ClientResponse response = client.resource(baseUrl + ENDPOINT + "/1312/out")
				  .accept(MediaType.APPLICATION_OCTET_STREAM_TYPE)
				  .get(ClientResponse.class);

		assertNotEquals(HttpStatus.OK.value(), response.getStatus());
	}

	@Test
	public void output_out_success() throws IOException, ProcessNotFoundException {
		ProcessManager.Data data = new ProcessManager.Data(new byte[]{13,12}, 2);
		doReturn(data).when(processManager).readOutput(anyString(), anyLong());

		ClientResponse response = adminRequest(baseUrl + ENDPOINT + "/1312/out")
				  .accept(MediaType.APPLICATION_OCTET_STREAM_TYPE)
				  .get(ClientResponse.class);

		assertEquals(HttpStatus.PARTIAL_CONTENT.value(), response.getStatus());
		assertEquals(String.valueOf(data.read), response.getHeaders().get(HttpHeaders.CONTENT_LENGTH).get(0));
		assertEquals("application/octet-stream", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
		assertEquals("bytes", response.getHeaders().get(HttpHeaders.ACCEPT_RANGES).get(0));
		assertEquals("bytes 0-2/*", response.getHeaders().get(HttpHeaders.CONTENT_RANGE).get(0));

		byte[] rawResponse = new byte[data.read];
		response.getEntityInputStream().read(rawResponse);
		assertArrayEquals(data.content, rawResponse);

		verify(processManager, times(1)).readOutput("1312", 0L);
	}

	@Test
	public void output_err_success() throws IOException, ProcessNotFoundException {
		ProcessManager.Data data = new ProcessManager.Data(new byte[]{13,12}, 2);
		doReturn(data).when(processManager).readError(anyString(), anyLong());

		ClientResponse response = adminRequest(baseUrl + ENDPOINT + "/1312/err")
				  .accept(MediaType.APPLICATION_OCTET_STREAM_TYPE)
				  .get(ClientResponse.class);

		assertEquals(HttpStatus.PARTIAL_CONTENT.value(), response.getStatus());
		assertEquals(String.valueOf(data.read), response.getHeaders().get(HttpHeaders.CONTENT_LENGTH).get(0));
		assertEquals("application/octet-stream", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
		assertEquals("bytes", response.getHeaders().get(HttpHeaders.ACCEPT_RANGES).get(0));
		assertEquals("bytes 0-2/*", response.getHeaders().get(HttpHeaders.CONTENT_RANGE).get(0));

		byte[] rawResponse = new byte[data.read];
		response.getEntityInputStream().read(rawResponse);
		assertArrayEquals(data.content, rawResponse);

		verify(processManager, times(1)).readError("1312", 0L);
	}

	@Test
	public void output_badRequest() throws IOException, ProcessNotFoundException {
		ClientResponse response = adminRequest(baseUrl + ENDPOINT + "/1312/in")
				  .accept(MediaType.APPLICATION_OCTET_STREAM_TYPE)
				  .get(ClientResponse.class);

		assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
	}
}
