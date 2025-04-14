package com.skycrate.backend.skycrateBackend.services;

import com.skycrate.backend.skycrateBackend.config.HDFSConfig;
import com.skycrate.backend.skycrateBackend.utils.KeyUtil;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

@Service
public class HDFSOperations {

    public void uploadFile(String localPath, String hdfsPath, String uploadedFileName, String username) {
        try {
            FileSystem fs = HDFSConfig.getHDFS();
            byte[] data = Files.readAllBytes(Paths.get(localPath)); // Read file as bytes

            // Encrypt file (consider adding encryption here as needed)
            byte[] encryptedData = data;

            String tempFilePath = localPath + ".enc";
            Files.write(Paths.get(tempFilePath), encryptedData);

            String finalHdfsPath = hdfsPath.endsWith("/") ? hdfsPath + uploadedFileName : hdfsPath + "/" + uploadedFileName;
            fs.copyFromLocalFile(new Path(tempFilePath), new Path(finalHdfsPath));

            Files.delete(Paths.get(tempFilePath));
        } catch (IOException e) {
            // Handle I/O exception and log the error
            throw new RuntimeException("Failed to upload file due to I/O issue: " + e.getMessage(), e);
        } catch (Exception e) {
            // Catch any other exceptions
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    public void downloadFile(String hdfsPath, String localPath, String username) {
        try {
            FileSystem fs = HDFSConfig.getHDFS();
            String tempFilePath = localPath + ".enc";

            fs.copyToLocalFile(new Path(hdfsPath), new Path(tempFilePath));

            byte[] encryptedData = Files.readAllBytes(Paths.get(tempFilePath));
            byte[] decryptedData = encryptedData; // Decrypt if needed

            Files.write(Paths.get(localPath), decryptedData);
            Files.delete(Paths.get(tempFilePath));
        } catch (IOException e) {
            // Handle I/O exception and log the error
            throw new RuntimeException("Failed to download file due to I/O issue: " + e.getMessage(), e);
        } catch (Exception e) {
            // Catch any other exceptions
            throw new RuntimeException("Failed to download file: " + e.getMessage(), e);
        }
    }

    public void createFolder(String hdfsPath) {
        try {
            FileSystem fs = HDFSConfig.getHDFS();
            Path path = new Path(hdfsPath);
            if (!fs.exists(path)) {
                fs.mkdirs(path);
            }
        } catch (IOException e) {
            // Handle I/O exception and log the error
            throw new RuntimeException("Failed to create folder in HDFS due to I/O issue: " + e.getMessage(), e);
        } catch (Exception e) {
            // Catch any other exceptions
            throw new RuntimeException("Failed to create folder: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String hdfsFilePath) {
        try {
            FileSystem fs = HDFSConfig.getHDFS();
            Path path = new Path(hdfsFilePath);
            if (fs.exists(path)) {
                fs.delete(path, false);
            }
        } catch (IOException e) {
            // Handle I/O exception and log the error
            throw new RuntimeException("Failed to delete file due to I/O issue: " + e.getMessage(), e);
        } catch (Exception e) {
            // Catch any other exceptions
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    public void deleteFolder(String hdfsFolderPath) {
        try {
            FileSystem fs = HDFSConfig.getHDFS();
            Path path = new Path(hdfsFolderPath);
            if (fs.exists(path)) {
                fs.delete(path, true);
            }
        } catch (IOException e) {
            // Handle I/O exception and log the error
            throw new RuntimeException("Failed to delete folder due to I/O issue: " + e.getMessage(), e);
        } catch (Exception e) {
            // Catch any other exceptions
            throw new RuntimeException("Failed to delete folder: " + e.getMessage(), e);
        }
    }

    public List<String> listFilesAndFolders(String hdfsPath) {
        List<String> results = new ArrayList<>();
        try {
            FileSystem fs = HDFSConfig.getHDFS();
            Path path = new Path(hdfsPath);

            if (fs.exists(path)) {
                listFilesAndFoldersRecursively(fs, path, "", results);
            } else {
                throw new RuntimeException("HDFS path does not exist: " + hdfsPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to list files and folders due to I/O issue: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to list files and folders: " + e.getMessage(), e);
        }

        return results;
    }

    private void listFilesAndFoldersRecursively(FileSystem fs, Path path, String indent, List<String> results) throws IOException {
        FileStatus[] fileStatuses = fs.listStatus(path);
        for (FileStatus fileStatus : fileStatuses) {
            String entry = indent + (fileStatus.isDirectory() ? "📁 " : "📄 ") + fileStatus.getPath().getName();
            results.add(entry);

            if (fileStatus.isDirectory()) {
                listFilesAndFoldersRecursively(fs, fileStatus.getPath(), indent + "   ", results);
            }
        }
    }
}