package com.skycrate.backend.skycrateBackend;

import com.skycrate.backend.skycrateBackend.controller.HDFScontroller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SkycrateBackendApplication implements CommandLineRunner {

	@Autowired
	private HDFScontroller hdfsController;

	public static void main(String[] args) {
		SpringApplication.run(SkycrateBackendApplication.class, args);
	}

	@Override
	public void run(String... args) {
		try {
			hdfsController.run();  // ✅ Run the instance method, not static
		} catch (Exception e) {
			System.err.println("❌ Error running CLI: " + e.getMessage());
			e.printStackTrace();
		}
	}
}


//package com.skycrate.backend.skycrateBackend;
//
//import com.skycrate.backend.skycrateBackend.controller.HDFScontroller;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
//public class SkycrateBackendApplication {
//
//	public static void main(String[] args) {
//		SpringApplication.run(SkycrateBackendApplication.class, args);
//	}
//
//	@Override
//	public void run(String... args) {
//		try {
//			HDFScontroller.run();  // ✅ Run the instance method, not static
//		} catch (Exception e) {
//			System.err.println("❌ Error running CLI: " + e.getMessage());
//			e.printStackTrace();
//		}
//	}
//
//}
