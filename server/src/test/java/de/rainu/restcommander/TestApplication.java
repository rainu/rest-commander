package de.rainu.restcommander;

import de.rainu.restcommander.process.ProcessManager;
import de.rainu.restcommander.store.UserStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

@SpringBootApplication
@EnableAutoConfiguration
public class TestApplication {
	public static void main(String[] args) {
		SpringApplication.run(TestApplication.class, args);
	}

	@Bean
	public UserStore userStore(){
		return new StaticUserStore();
	}

	@Bean
	public ProcessManager processManager() {
		return mock(ProcessManager.class);
	}
}
