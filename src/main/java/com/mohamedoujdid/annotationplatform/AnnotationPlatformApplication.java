package com.mohamedoujdid.annotationplatform;

import com.mohamedoujdid.annotationplatform.user.enums.Role;
import com.mohamedoujdid.annotationplatform.user.model.User;
import com.mohamedoujdid.annotationplatform.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class AnnotationPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnnotationPlatformApplication.class, args);
	}

	@Bean
	CommandLineRunner initAdmin(UserRepository userRepo, PasswordEncoder encoder) {
		return args -> {
			if (userRepo.findByEmail("admin@example.com").isEmpty()) {
				User admin = User.builder()
						.email("admin@example.com")
						.password(encoder.encode("AdminPassword123!"))
						.role(Role.ADMIN)
						.accountNonLocked(true)
						.passwordChangeRequired(false)
						.build();
				userRepo.save(admin);
			}
		};
	}
}
