package de.rainu.restcommander;

import com.sun.jersey.api.client.ClientResponse;
import de.rainu.restcommander.config.security.AuthFilter;
import de.rainu.restcommander.model.dto.ErrorResponse;
import de.rainu.restcommander.model.dto.LoginResponse;
import org.junit.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static de.rainu.restcommander.controller.AuthenticationController.*;

public class SecurityIT extends IntegrationTest {
	@Test
	public void healthCheck() {
		ClientResponse response = client.resource(baseUrl + "/health")
				  .get(ClientResponse.class);

		assertEquals(HttpStatus.OK.value(), response.getStatus());
	}

	@Test
	public void notLoggedIn() {
		ClientResponse response = client.resource(baseUrl + "/hello")
				  .get(ClientResponse.class);

		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
	}

	@Test
	public void userDoesNotExists() throws IOException {
		ClientResponse response = client.resource(baseUrl + LOGIN_PATH)
				  .type(MediaType.APPLICATION_JSON)
				  .post(ClientResponse.class, "{\"username\":\"wronguser\", \"password\":\"password\"}");

		assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

		ErrorResponse data = mapper.readValue(response.getEntity(String.class), ErrorResponse.class);
		assertEquals("Username or password are incorrect!", data.getMessage());
	}

	@Test
	public void wrongPassword() throws IOException {
		ClientResponse response = client.resource(baseUrl + LOGIN_PATH)
				  .type(MediaType.APPLICATION_JSON)
				  .post(ClientResponse.class, "{\"username\":\"admin\", \"password\":\"password\"}");

		assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

		ErrorResponse data = mapper.readValue(response.getEntity(String.class), ErrorResponse.class);
		assertEquals("Username or password are incorrect!", data.getMessage());
	}

	@Test
	public void noRights() throws IOException {
		LoginResponse data = login("user", "user");

		ClientResponse response = client.resource(baseUrl + "/hello/admin")
				  .header(AuthFilter.TOKEN_HEADER, data.getToken())
				  .get(ClientResponse.class);

		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
	}

	@Test
	public void enoughRights() throws IOException {
		LoginResponse data = login("admin", "admin");

		ClientResponse response = client.resource(baseUrl + "/hello")
				  .header(AuthFilter.TOKEN_HEADER, data.getToken())
				  .get(ClientResponse.class);

		assertEquals(HttpStatus.OK.value(), response.getStatus());

		response = client.resource(baseUrl + "/hello/admin")
				  .header(AuthFilter.TOKEN_HEADER, data.getToken())
				  .get(ClientResponse.class);

		assertEquals(HttpStatus.OK.value(), response.getStatus());
	}

	@Test
	public void logout() throws IOException {
		LoginResponse data = login("admin", "admin");

		ClientResponse response = client.resource(baseUrl + LOGOUT_PATH)
				  .header(AuthFilter.TOKEN_HEADER, data.getToken())
				  .post(ClientResponse.class);

		assertEquals(HttpStatus.OK.value(), response.getStatus());

		response = client.resource(baseUrl + "/hello/admin")
				  .header(AuthFilter.TOKEN_HEADER, data.getToken())
				  .get(ClientResponse.class);

		assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
	}
}
