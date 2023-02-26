package com.bowei.community;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {

	@PostConstruct
	public void init(){
		// Resolving netty startup conflict issues
		// redis -> netty; es -> netty When redis starts, es will check netty, and if it is set, it will not start, so it will report an error
		System.setProperty("es.set.netty.runtime.available.processors","false");
	}
	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}

}
