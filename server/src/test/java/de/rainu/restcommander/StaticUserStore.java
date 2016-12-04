package de.rainu.restcommander;

import de.rainu.restcommander.model.User;
import de.rainu.restcommander.model.UserRole;
import de.rainu.restcommander.store.AbstractStore;
import de.rainu.restcommander.store.UserStore;
import org.springframework.stereotype.Component;

/**
 * This class represents a in-memory user store. For productive usage
 * this store should be a database.
 */
public class StaticUserStore extends AbstractStore<String, User> implements UserStore {

	public StaticUserStore() {
	}

	@Override
	protected void initilizeStore() {
		store.put("admin", new User("admin", "admin", UserRole.ADMIN, UserRole.USER));
		store.put("user", new User("user", "user", UserRole.USER));
	}

	@Override
	public User get(String username) {
		return super.get(username);
	}

	@Override
	public boolean contains(String username) {
		return super.contains(username);
	}

	@Override
	public boolean checkPassword(String username, String password) {
		return contains(username) && get(username).getPassword().equals(password);
	}
}
