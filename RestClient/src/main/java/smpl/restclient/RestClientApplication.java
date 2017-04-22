package smpl.restclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RestClientApplication {


	private static final Logger log = LoggerFactory.getLogger(RestClientApplication.class);

	public static void main(String args[]) {
		SpringApplication.run(RestClientApplication.class);
		log.info("Rest Client Started");
	}


}
