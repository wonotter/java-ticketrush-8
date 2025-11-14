package wonotter.java_ticketrush_8;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing // BaseTimeEntity 동작
@SpringBootApplication
public class JavaTicketrush8Application {

	public static void main(String[] args) {
		SpringApplication.run(JavaTicketrush8Application.class, args);
	}

}
