package de.rainu.restcommander.store;

import de.rainu.restcommander.model.User;

public interface UserStore {
	User get(String username);

	boolean contains(String username);

	boolean checkPassword(String username, String password);
}
