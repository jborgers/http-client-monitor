package com.jpinpoint.monitor.demo;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

/*  example call:
curl "localhost:8080/greetingMany?threads=4" */

@RestController
@Slf4j
public class GreetingController {

	private final AtomicLong greetingManyCounter = new AtomicLong();
	private final AtomicLong helloCounter = new AtomicLong();
	@Value("${server.port}")
	private String serverPort;
	private volatile RestClient restClient = null;
	private final Object lock = new Object();

	@GetMapping("/greetingMany")
	public Greeting greetingMany(@RequestParam(value = "threads", defaultValue = "8") String threads) {
		synchronized (lock) {
			if (restClient == null) restClient = new RestClient();
		}
		long count = greetingManyCounter.incrementAndGet();
		log.debug("Hi! I am GreetingMany call number {} and running on port {}", count, serverPort);
		String otherPort = "8081";
		if (serverPort.equals("8081")) {
			otherPort = "8080";
		}
		try {
			log.info("{} - I call {} Hello services on {} ", count, threads, otherPort);
			String response = restClient.callManyHellos(threads, otherPort);
			log.debug("{} - I called {} Hello services on {}, response= {}", count, threads, otherPort, response);
		}
		catch (RuntimeException e) {
			log.info(e.getMessage());
		}
		return new Greeting(count, "Called " + threads + " Hello's");
	}

	@GetMapping("/hello")
	public Greeting hello() {
		long count = helloCounter.incrementAndGet();
		log.debug("Hi! I am Hello number {} and running on port {}", count, serverPort);

		try {
				log.info("{} - Hello!", count);
				Thread.sleep(4000);
		}
		catch (RuntimeException | InterruptedException e) {
			log.debug(e.getMessage());
		}
		return new Greeting(count, "Hello!");
	}
}
