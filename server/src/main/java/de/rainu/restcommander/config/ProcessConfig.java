package de.rainu.restcommander.config;

import de.rainu.restcommander.process.LinuxProcessManager;
import de.rainu.restcommander.process.ProcessManager;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProcessConfig {

	@Bean
	public ProcessManager processManager(@Value("${os.name}") String os) {
		switch (os.toLowerCase()) {
			case "linux":
				return new LinuxProcessManager();
			default:
				throw new NoSuchBeanDefinitionException("No ProcessManager found for OS '" + os + "'!");
		}
	}
}
