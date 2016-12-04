package de.rainu.restcommander.controller;

import de.rainu.restcommander.config.security.AuthenticationToken;
import de.rainu.restcommander.model.User;
import de.rainu.restcommander.model.dto.AuthDTO;
import de.rainu.restcommander.model.dto.ErrorResponse;
import de.rainu.restcommander.model.dto.LoginResponse;
import de.rainu.restcommander.store.TokenStore;
import de.rainu.restcommander.store.UserStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * This controller contains the endpoints for handling user tokens.
 */
@RestController
public class AuthenticationController {
	private static final String ROOT_PATH = "/auth";
	public static final String LOGIN_PATH = ROOT_PATH + "/login";
	public static final String LOGOUT_PATH = ROOT_PATH + "/logout";

	@Autowired
	UserStore userStore;

	@Autowired
	TokenStore tokenStore;

	@RequestMapping(path = LOGIN_PATH, method = RequestMethod.POST)
	@ResponseBody
	public Object login(@RequestBody AuthDTO auth) {
		if(!userStore.checkPassword(auth.getUsername(), auth.getPassword())) {
			return new ResponseEntity(new ErrorResponse("Username or password are incorrect!"), HttpStatus.BAD_REQUEST);
		}

		User user = userStore.get(auth.getUsername());
		user.setPassword(auth.getPassword());

		final String token = UUID.randomUUID().toString();
		tokenStore.put(token, auth.getUsername());

		return new LoginResponse(token);
	}

	@RequestMapping(path = LOGOUT_PATH, method = RequestMethod.POST)
	public void logout(AuthenticationToken authToken) {
		tokenStore.remove(authToken.getToken());
	}
}
