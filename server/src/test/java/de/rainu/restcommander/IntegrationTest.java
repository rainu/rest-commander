package de.rainu.restcommander;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import de.rainu.restcommander.config.security.AuthFilter;
import de.rainu.restcommander.model.dto.LoginResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTest {

	@LocalServerPort
	protected int randomServerPort;
	protected String baseUrl;
	protected Client client;
	protected ObjectMapper mapper;

	@Before
	public void setup() {
		client = Client.create();
		baseUrl = "http://localhost:" + randomServerPort + "/";
		mapper = new ObjectMapper();
	}

	@After
	public void clean() {
		client.destroy();
	}

	public LoginResponse login(String username, String password) throws IOException {
		ClientResponse response = client.resource(baseUrl + "/login")
				  .type(MediaType.APPLICATION_JSON)
				  .post(ClientResponse.class, "{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}");

		assertEquals(HttpStatus.OK.value(), response.getStatus());

		LoginResponse data = mapper.readValue(response.getEntity(String.class), LoginResponse.class);
		assertFalse(data.getToken().isEmpty());

		return data;
	}

	public WebResource.Builder adminRequest(String endpoint) throws IOException {
		LoginResponse data = login("admin", "admin");

		return client.resource(endpoint)
				  .header(AuthFilter.TOKEN_HEADER, data.getToken());
	}

}
