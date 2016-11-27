package de.rainu.restcommander.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import de.rainu.restcommander.IntegrationTest;
import de.rainu.restcommander.model.Process;
import de.rainu.restcommander.model.dto.LoginResponse;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import static de.rainu.restcommander.controller.ProcessController.ENDPOINT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProcessControllerIT extends IntegrationTest {

	@Test
	public void list_accessDenied(){
		ClientResponse response = client.resource(baseUrl + ENDPOINT)
				  .get(ClientResponse.class);

		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
	}

	@Test
	public void list_success() throws IOException {
		ClientResponse response = adminRequest(baseUrl + ENDPOINT)
				  .get(ClientResponse.class);

		JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, Process.class);
		List<Process> process = mapper.readValue(response.getEntity(String.class), type);

		assertEquals(HttpStatus.OK.value(), response.getStatus());
		assertFalse(process.isEmpty());

		//find current process
		final String ownPID = getOwnPID();
		Process ownProccess = process.stream().filter(p -> p.getId().equalsIgnoreCase(ownPID)).findFirst().get();

		assertTrue(ownProccess.getCmdline().contains("java"));
		System.getenv().entrySet().stream().forEach(env -> {
			assertEquals(env.getValue(), ownProccess.getEnv().get(env.getKey()));
		});
	}

	private String getOwnPID(){
		return ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
	}
}
