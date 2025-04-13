package com.skycrate.backend.skycrateBackend.controller;

import com.skycrate.backend.skycrateBackend.dto.User;
import com.skycrate.backend.skycrateBackend.repository.UserManager;
import com.skycrate.backend.skycrateBackend.services.HDFSOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class HDFScontroller implements Runnable {

    private final UserManager userManager;
    private final HDFSOperations hdfsOperations;

    @Autowired
    public HDFScontroller(UserManager userManager, HDFSOperations hdfsOperations) {
        this.userManager = userManager;
        this.hdfsOperations = hdfsOperations;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Enter your username: ");
            String username = scanner.nextLine();

            System.out.print("Enter your password: ");
            String password = scanner.nextLine();

            if (!userManager.authenticate(username, password)) {
                System.out.println("❌ Authentication failed. Exiting...");
                return;
            }

            User user;
            try {
                user = userManager.getUser(username);
            } catch (Exception e) {
                System.out.println("❌ Error creating user: " + e.getMessage());
                return;
            }

            while (true) {
                System.out.println("\nChoose an option:");
                System.out.println("1. Create a folder");
                System.out.println("2. Upload a file (encrypted)");
                System.out.println("3. Upload a file to a specific folder (encrypted)");
                System.out.println("4. Download a file");
                System.out.println("5. Delete a file");
                System.out.println("6. Delete a folder");
                System.out.println("7. List files and folders");
                System.out.println("8. Exit");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                try {
                    switch (choice) {
                        case 1:
                            System.out.print("Enter folder name: ");
                            hdfsOperations.createFolder(scanner.nextLine());
                            break;

                        case 2:
                            System.out.print("Enter file path to upload: ");
                            String filePath = scanner.nextLine();
                            System.out.print("Enter HDFS destination path: ");
                            String hdfsDestinationFolder = scanner.nextLine();
                            System.out.print("Enter the name for the uploaded file: ");
                            String uploadedFileName = scanner.nextLine();
                            hdfsOperations.uploadFile(filePath, hdfsDestinationFolder, uploadedFileName, user);
                            break;

                        case 3:
                            System.out.print("Enter folder name to upload to: ");
                            String targetFolder = scanner.nextLine();
                            System.out.print("Enter file path to upload: ");
                            String fileToUpload = scanner.nextLine();
                            System.out.print("Enter the name for the uploaded file: ");
                            String uploadedFileNameSpecific = scanner.nextLine();
                            hdfsOperations.uploadFile(fileToUpload, targetFolder, uploadedFileNameSpecific, user);
                            break;

                        case 4:
                            System.out.print("Enter HDFS file path to download: ");
                            String hdfsFilePath = scanner.nextLine();
                            System.out.print("Enter local path to save the downloaded file: ");
                            String localDownloadPath = scanner.nextLine();
                            hdfsOperations.downloadFile(hdfsFilePath, localDownloadPath, user);
                            break;

                        case 5:
                            System.out.print("Enter HDFS file path to delete: ");
                            hdfsOperations.deleteFile(scanner.nextLine());
                            break;

                        case 6:
                            System.out.print("Enter HDFS folder path to delete: ");
                            hdfsOperations.deleteFolder(scanner.nextLine());
                            break;

                        case 7:
                            System.out.print("Enter HDFS path to list files and folders: ");
                            hdfsOperations.listFilesAndFolders(scanner.nextLine());
                            break;

                        case 8:
                            System.out.println("✅ Exiting...");
                            return;

                        default:
                            System.out.println("⚠️ Invalid option. Please try again.");
                    }
                } catch (Exception e) {
                    System.out.println("❌ Operation failed: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}


//package com.skycrate.backend.skycrateBackend.controller;
//
//import com.skycrate.backend.skycrateBackend.dto.User;
//import com.skycrate.backend.skycrateBackend.repository.UserManager;
//import com.skycrate.backend.skycrateBackend.services.HDFSOperations;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.Scanner;
//
//@Component
//public class HDFScontroller implements Runnable {
//
//    private final UserManager userManager;
//    private final HDFSOperations hdfsOperations;
//
//    @Autowired
//    public HDFScontroller(UserManager userManager, HDFSOperations hdfsOperations) {
//        this.userManager = userManager;
//        this.hdfsOperations = hdfsOperations;
//    }
//
//    @Override
//    public void run() {
//        Scanner scanner = new Scanner(System.in);
//
//        while (true) {
//            System.out.print("Enter your username: ");
//            String username = scanner.nextLine();
//
//            System.out.print("Enter your password: ");
//            String password = scanner.nextLine();
//
//            if (!userManager.authenticate(username, password)) {
//                System.out.println("X Authentication failed. Exiting...");
//                return;
//            }
//
//            User user;
//            try {
//                user = userManager.getUser(username);
//            } catch (Exception e) {
//                System.out.println("X Error creating user: " + e.getMessage());
//                return;
//            }
//
//            while (true) {
//                System.out.println("\nChoose an option:");
//                System.out.println("1. Create a folder");
//                System.out.println("2. Upload a file (encrypted)");
//                System.out.println("3. Upload a file to a specific folder (encrypted)");
//                System.out.println("4. Download a file");
//                System.out.println("5. Delete a file");
//                System.out.println("6. Delete a folder");
//                System.out.println("7. List files and folders");
//                System.out.println("8. Exit");
//
//                int choice = scanner.nextInt();
//                scanner.nextLine(); // Consume newline
//
//                switch (choice) {
//                    case 1:
//                        System.out.print("Enter folder name: ");
//                        hdfsOperations.createFolder(scanner.nextLine());
//                        break;
//                    case 2:
//                        System.out.print("Enter file path to upload: ");
//                        String filePath = scanner.nextLine();
//                        System.out.print("Enter HDFS destination path: ");
//                        String hdfsDestinationFolder = scanner.nextLine();
//                        System.out.print("Enter the name for the uploaded file: ");
//                        String uploadedFileName = scanner.nextLine();
//                        hdfsOperations.uploadFile(filePath, hdfsDestinationFolder, uploadedFileName, user);
//                        break;
//                    case 3:
//                        System.out.print("Enter folder name to upload to: ");
//                        String targetFolder = scanner.nextLine();
//                        System.out.print("Enter file path to upload: ");
//                        String fileToUpload = scanner.nextLine();
//                        System.out.print("Enter the name for the uploaded file: ");
//                        String uploadedFileNameSpecific = scanner.nextLine();
//                        hdfsOperations.uploadFile(fileToUpload, targetFolder, uploadedFileNameSpecific, user);
//                        break;
//                    case 4:
//                        System.out.print("Enter HDFS file path to download: ");
//                        String hdfsFilePath = scanner.nextLine();
//                        System.out.print("Enter local path to save the downloaded file: ");
//                        String localDownloadPath = scanner.nextLine();
//                        hdfsOperations.downloadFile(hdfsFilePath, localDownloadPath, user);
//                        break;
//                    case 5:
//                        System.out.print("Enter HDFS file path to delete: ");
//                        hdfsOperations.deleteFile(scanner.nextLine());
//                        break;
//                    case 6:
//                        System.out.print("Enter HDFS folder path to delete: ");
//                        hdfsOperations.deleteFolder(scanner.nextLine());
//                        break;
//                    case 7:
//                        System.out.print("Enter HDFS path to list files and folders: ");
//                        hdfsOperations.listFilesAndFolders(scanner.nextLine());
//                        break;
//                    case 8:
//                        System.out.println("Exiting...");
//                        return;
//                    default:
//                        System.out.println("Invalid option. Please try again.");
//                }
//            }
//        }
//    }
//}
