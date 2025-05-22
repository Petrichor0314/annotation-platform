package com.mohamedoujdid.annotationplatform;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mohamedoujdid.annotationplatform.user.model.Admin;
import com.mohamedoujdid.annotationplatform.user.model.Annotator;
import com.mohamedoujdid.annotationplatform.user.model.Role;
import com.mohamedoujdid.annotationplatform.user.repository.RoleRepository;
import com.mohamedoujdid.annotationplatform.user.repository.UserRepository;

@SpringBootApplication
@EnableAsync
public class AnnotationPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnnotationPlatformApplication.class, args);
	}

	@Bean
	CommandLineRunner initUsers(RoleRepository roleRepo, UserRepository userRepo, PasswordEncoder encoder) {
		return args -> {
			// Create roles if they don't exist
			Role adminRole = roleRepo.findByName("ADMIN").orElseGet(() -> {
				Role r = new Role();
				r.setName("ADMIN");
				return roleRepo.save(r);
			});

			Role annotatorRole = roleRepo.findByName("ANNOTATOR").orElseGet(() -> {
				Role r = new Role();
				r.setName("ANNOTATOR");
				return roleRepo.save(r);
			});

			// Create admin user if not exists
			if (userRepo.findByEmail("admin@example.com").isEmpty()) {
				Admin admin = new Admin();
				admin.setFirstName("System");
				admin.setLastName("Admin");
				admin.setEmail("admin@example.com");
				admin.setPassword(encoder.encode("AdminPassword123!"));
				admin.setRole(adminRole);
				admin.setAccountNonLocked(true);
				admin.setPasswordChangeRequired(false);
				userRepo.save(admin);
			}

			// Create annotator user if not exists
			if (userRepo.findByEmail("user@example.com").isEmpty()) {
				Annotator annotator = new Annotator();
				annotator.setFirstName("Default");
				annotator.setLastName("User");
				annotator.setEmail("user@example.com");
				annotator.setPassword(encoder.encode("UserPassword123!"));
				annotator.setRole(annotatorRole);
				annotator.setAccountNonLocked(true);
				annotator.setPasswordChangeRequired(false);
				userRepo.save(annotator);
			}
		};
	}



}
