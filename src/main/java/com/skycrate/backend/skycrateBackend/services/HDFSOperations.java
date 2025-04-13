package com.skycrate.backend.skycrateBackend.services;


import com.skycrate.backend.skycrateBackend.config.HDFSConfig;
import com.skycrate.backend.skycrateBackend.dto.User;
import org.apache.hadoop.fs.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class HDFSOperations {
    public static void uploadFile(String localPath, String hdfsPath, String uploadedFileName, User user) throws Exception {
        FileSystem fs = HDFSConfig.getHDFS();
        byte[] data = Files.readAllBytes(Paths.get(localPath)); // Read file as bytes
        byte[] encryptedData = EncryptionUtil.encrypt(data, user.getKeyPair().getPublic());

        // Save encryptedData to a temporary file and upload it
        String tempFilePath = localPath + ".enc";
        Files.write(Paths.get(tempFilePath), encryptedData); // Write bytes to temp file

        // Construct the final HDFS path using the provided uploaded file name
        String finalHdfsPath = hdfsPath.endsWith("/") ? hdfsPath + uploadedFileName : hdfsPath + "/" + uploadedFileName;

        fs.copyFromLocalFile(new Path(tempFilePath), new Path(finalHdfsPath));
        System.out.println("✅ File uploaded: " + finalHdfsPath);

        // Clean up temporary file
        Files.delete(Paths.get(tempFilePath));
    }

    public static void downloadFile(String hdfsPath, String localPath, User user) throws Exception {
        FileSystem fs = HDFSConfig.getHDFS();
        String tempFilePath = localPath + ".enc";

        fs.copyToLocalFile(new Path(hdfsPath), new Path(tempFilePath));

        // Read the encrypted file as bytes
        byte[] encryptedData = Files.readAllBytes(Paths.get(tempFilePath));
        byte[] decryptedData = EncryptionUtil.decrypt(encryptedData, user.getKeyPair().getPrivate());

        Files.write(Paths.get(localPath), decryptedData); // Write decrypted bytes to local file
        System.out.println("✅ File downloaded: " + localPath);

        // Clean up temporary file
        Files.delete(Paths.get(tempFilePath));
    }

    public static void createFolder(String hdfsPath) throws Exception {
        FileSystem fs = HDFSConfig.getHDFS();
        Path path = new Path(hdfsPath);
        if (!fs.exists(path)) {
            fs.mkdirs(path);
            System.out.println("✅ Folder created: " + hdfsPath);
        } else {
            System.out.println("⚠️ Folder already exists: " + hdfsPath);
        }
    }

    public static void deleteFile(String hdfsFilePath) throws Exception {
        FileSystem fs = HDFSConfig.getHDFS();
        Path path = new Path(hdfsFilePath);
        if (fs.exists(path)) {
            fs.delete(path, false); // false means do not recursively delete
            System.out.println("✅ File deleted: " + hdfsFilePath);
        } else {
            System.out.println("⚠️ File does not exist: " + hdfsFilePath);
        }
    }

    public static void deleteFolder(String hdfsFolderPath) throws Exception {
        FileSystem fs = HDFSConfig.getHDFS();
        Path path = new Path(hdfsFolderPath);
        if (fs.exists(path)) {
            fs.delete(path, true); // true means recursively delete
            System.out.println("✅ Folder deleted: " + hdfsFolderPath);
        } else {
            System.out.println("⚠️ Folder does not exist: " + hdfsFolderPath);
        }
    }

    public static void listFilesAndFolders(String hdfsPath) throws Exception {
        FileSystem fs = HDFSConfig.getHDFS();
        Path path = new Path(hdfsPath);

        if (!fs.exists(path)) {
            System.out.println("⚠️ Path does not exist: " + hdfsPath);
            return;
        }

        System.out.println("Listing files and folders in: " + hdfsPath);
        listFilesAndFoldersRecursively(fs, path, "");
    }

    private static void listFilesAndFoldersRecursively(FileSystem fs, Path path, String indent) throws IOException {
        FileStatus[] fileStatuses = fs.listStatus(path);
        for (FileStatus fileStatus : fileStatuses) {
            System.out.println(indent + (fileStatus.isDirectory() ? "📁 " : "📄 ") + fileStatus.getPath().getName());
            if (fileStatus.isDirectory()) {
                listFilesAndFoldersRecursively(fs, fileStatus.getPath(), indent + "   "); // Indent for subdirectories
            }
        }
    }
}
