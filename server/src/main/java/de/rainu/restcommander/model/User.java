package de.rainu.restcommander.model;

import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class represents a User. A user can have
 * * a name
 * * a password
 * * many Roles
 */
public class User {
	private String username;
	private String password;
	private List<GrantedAuthority> roles;

	public User(String name, String password, List<GrantedAuthority> roles) {
		this.username = name;
		this.password = password;
		this.roles = roles;
	}

	public User(String name, String password, GrantedAuthority... roles) {
		this(name, password, Arrays.asList(roles));
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<GrantedAuthority> getRoles() {
		return Collections.unmodifiableList(roles);
	}
}
