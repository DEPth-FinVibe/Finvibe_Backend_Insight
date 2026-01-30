package finvibe.insight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class InsightApplication {

	public static void main(String[] args) {
		SpringApplication.run(InsightApplication.class, args);
	}

}
