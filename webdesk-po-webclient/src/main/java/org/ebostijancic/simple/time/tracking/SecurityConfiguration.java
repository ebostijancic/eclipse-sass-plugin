package org.ebostijancic.simple.time.tracking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;

@Configuration
@EnableWebSecurity
@EnableWebMvcSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication().withUser("admin").password("b6f864df").roles("ADMIN");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		//super.configure(http);

		// only users having role ADMIN can access admin area (/admin/**)
		http.authorizeRequests().antMatchers("/admin/**").hasRole("ADMIN");

		// all other request with unauthenticated users forward to login form.
		http.authorizeRequests().anyRequest().authenticated()
			.and()
		.formLogin().loginPage("/login").failureUrl("/login?error").permitAll();

		// go to /login on successful logout.
		http.logout().logoutSuccessUrl("/login");
		
		// permit access to /signup and /about by default
		http.authorizeRequests().anyRequest().anonymous()
			.antMatchers("/signup", "/about", "/login", "/logout", "/user/**").permitAll();
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/assets/**");
	}
}
