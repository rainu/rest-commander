package de.rainu.restcommander.config;

import de.rainu.restcommander.store.LinuxUserStore;
import de.rainu.restcommander.store.UserStore;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserStoreConfig {

	@Bean
	public UserStore userStore(@Value("${os.name}") String os){
		switch (os.toLowerCase()) {
			case "linux": return new LinuxUserStore();
			default:
				throw new NoSuchBeanDefinitionException("No UserStore found for OS '" + os + "'!");
		}
	}
}
