package com.secure.notes.security;


import com.secure.notes.models.AppRole;
import com.secure.notes.models.Role;
import com.secure.notes.models.User;
import com.secure.notes.repositories.RoleRepository;
import com.secure.notes.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;



import java.time.LocalDate;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(withDefaults())
                .authorizeHttpRequests(auth ->
                        auth
                                .anyRequest().authenticated()
                );

        return http.build();
    }


    //In Memory/DB Auth(InMemoryUserDetailsManager()/JdbcUserDetailsManager(DataSource datasource))
//    @Bean
//    public UserDetailsService userDetailsService(DataSource datasource){
//        JdbcUserDetailsManager manager=new JdbcUserDetailsManager(datasource);
//        if(!manager.userExists("user1")){
//            manager.createUser(
//                    User.withUsername("user1")
//                            .password("{noop}password1")
//                            .roles("USER")
//                            .build()
//            );
//        }
//        if(!manager.userExists("admin")){
//            manager.createUser(
//                    User.withUsername("admin")
//                            .password("{noop}adminPass")
//                            .roles("ADMIN")
//                            .build()
//            );
//        }
//        return manager;
//    }


    @Bean
    public CommandLineRunner seedData(RoleRepository roleRepository,
                                      UserRepository userRepository) {
        return args->{
            Role userRole=roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseGet(()->roleRepository.save(new Role(AppRole.ROLE_USER)));

            Role adminRole=roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                    .orElseGet(()->roleRepository.save(new Role(AppRole.ROLE_ADMIN)));

            if (!userRepository.existsByUserName("user1")) {
                User user1 = new User("user1", "user1@example.com","{noop}password1");
                user1.setAccountNonLocked(false);
                user1.setAccountNonExpired(true);
                user1.setCredentialsNonExpired(true);
                user1.setEnabled(true);
                user1.setCredentialsExpiryDate(LocalDate.now().plusYears(1));
                user1.setAccountExpiryDate(LocalDate.now().plusYears(1));
                user1.setTwoFactorEnabled(false);
                user1.setSignUpMethod("email");
                user1.setRole(userRole);
                userRepository.save(user1);
            }

            if (!userRepository.existsByUserName("admin")) {
                User admin = new User("admin", "admin@example.com","{noop}adminPass");
                admin.setAccountNonLocked(true);
                admin.setAccountNonExpired(true);
                admin.setCredentialsNonExpired(true);
                admin.setEnabled(true);
                admin.setCredentialsExpiryDate(LocalDate.now().plusYears(1));
                admin.setAccountExpiryDate(LocalDate.now().plusYears(1));
                admin.setTwoFactorEnabled(false);
                admin.setSignUpMethod("email");
                admin.setRole(adminRole);
                userRepository.save(admin);
            }
        };
    }









}
