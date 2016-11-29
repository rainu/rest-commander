package de.rainu.restcommander.config.security;

import de.rainu.restcommander.controller.AuthenticationController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;

/**
 * This class is responsible for configure our security settings.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@ConditionalOnExpression("not environment.containsProperty('debug_mode')")
	@EnableGlobalMethodSecurity(prePostEnabled = true)
	public static class EnablePrePost {
	}

	@Value("#{environment.debug_mode ?: false}")
	private boolean debugMode;

	@Bean
	public AuthenticationProvider createCustomAuthenticationProvider() {
		return new AuthProvider();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		if(debugMode) {
			http.anonymous()
			  .and().csrf().disable();
			return;
		}

		http
		  .addFilterBefore(createCustomFilter(), AnonymousAuthenticationFilter.class)
		  .csrf().disable();
	}

	//Note, we don't register this as a bean as we don't want it to be added to the main Filter chain, just the spring security filter chain
	protected AbstractAuthenticationProcessingFilter createCustomFilter() throws Exception {
		//here we define the interfaces which don't need any authorisation
		AuthFilter filter = new AuthFilter(new NegatedRequestMatcher(
		  new AndRequestMatcher(
			 new AntPathRequestMatcher(AuthenticationController.LOGIN_PATH),
			 new AntPathRequestMatcher("/health")
		  )
		));
		filter.setAuthenticationManager(authenticationManagerBean());
		return filter;
	}
}
