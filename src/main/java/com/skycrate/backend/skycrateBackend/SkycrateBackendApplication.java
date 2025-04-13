//package com.skycrate.backend.skycrateBackend;
//
//import com.skycrate.backend.skycrateBackend.services.HDFSOperations;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
//public class SkycrateBackendApplication implements CommandLineRunner {
//
//	@Autowired
//	private HDFSOperations hdfsOperations;  // Autowire the service, not the controller
//
//	public static void main(String[] args) {
//		SpringApplication.run(SkycrateBackendApplication.class, args);
//	}
//
//	@Override
//	public void run(String... args) {
//		try {
//			// Example of calling the HDFS service directly
//			String localPath = "/path/to/local/file";
//			String hdfsPath = "/path/in/hdfs";
//			String uploadedFileName = "example.txt";
//			String username = "user123";
//
//			// Call HDFSOperations directly
//			hdfsOperations.uploadFile(localPath, hdfsPath, uploadedFileName, username);
//
//			System.out.println("File upload executed successfully");
//
//		} catch (Exception e) {
//			System.err.println("❌ Error running CLI: " + e.getMessage());
//			e.printStackTrace();
//		}
//	}
//}



package com.skycrate.backend.skycrateBackend;

import com.skycrate.backend.skycrateBackend.controller.HDFScontroller;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SkycrateBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkycrateBackendApplication.class, args);
	}
}
