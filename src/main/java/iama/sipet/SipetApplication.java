package iama.sipet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SipetApplication {

	public static void main(String[] args) {
		SpringApplication.run(SipetApplication.class, args);
	}

}
