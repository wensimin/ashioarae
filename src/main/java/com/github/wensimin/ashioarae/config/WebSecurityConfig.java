package com.github.wensimin.ashioarae.config;

import com.github.wensimin.ashioarae.entity.SysUser;
import com.github.wensimin.ashioarae.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .authorizeRequests()
                .requestMatchers(EndpointRequest.toAnyEndpoint())
                .hasAuthority(SysUser.ROLE_ADMIN)
                .anyRequest().authenticated()
                .and()
                .httpBasic();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/file/public/**");
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth, SysUserService userService)
            throws Exception {
        auth.userDetailsService(userService);
    }
}
