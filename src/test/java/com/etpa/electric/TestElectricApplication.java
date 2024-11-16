package com.etpa.electric;

import org.springframework.boot.SpringApplication;

public class TestElectricApplication {

	public static void main(String[] args) {
		SpringApplication.from(ElectricApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
