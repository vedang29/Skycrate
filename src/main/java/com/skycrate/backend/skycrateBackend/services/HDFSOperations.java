package com.skycrate.backend.skycrateBackend.services;

import com.skycrate.backend.skycrateBackend.config.HDFSConfig;
import com.skycrate.backend.skycrateBackend.entity.User;
import com.skycrate.backend.skycrateBackend.repository.UserRepository;
import com.skycrate.backend.skycrateBackend.utils.RSAKeyUtil;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.File;
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
    private final UserRepository userRepository;

    @Autowired
    public HDFSOperations(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

//    public void uploadFile(byte[] fileData, String hdfsPath, String uploadedFileName, String username) {
//        try {
//            FileSystem fs = HDFSConfig.getHDFS();
//
//            // Create an InputStream from the byte array
//            ByteArrayInputStream inputStream = new ByteArrayInputStream(fileData);
//
//            // Prepare the path for HDFS
//            String finalHdfsPath = hdfsPath.endsWith("/") ? hdfsPath + uploadedFileName : hdfsPath + "/" + uploadedFileName;
//
//            // Upload the file directly to HDFS from the InputStream
//            Path hdfsFilePath = new Path(finalHdfsPath);
//            FSDataOutputStream outputStream = fs.create(hdfsFilePath);
//            IOUtils.copyBytes(inputStream, outputStream, 4096, true);
//
//        } catch (IOException e) {
//            // Handle I/O exception and log the error
//            throw new RuntimeException("Failed to upload file to HDFS: " + e.getMessage(), e);
//        } catch (Exception e) {
//            // Catch any other exceptions
//            throw new RuntimeException("Failed to upload file to HDFS: " + e.getMessage(), e);
//        }
//    }
//
//    public void downloadFile(String hdfsEncPath, String localPathWithoutExt, String username) {
//        try {
//            FileSystem fs = HDFSConfig.getHDFS();
//
//            // Extract file name and extension
//            String encFileName = new File(hdfsEncPath).getName();
//            String originalFileName = encFileName.replace(".enc", "");
//            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
//
//            String fullDecryptedPath = localPathWithoutExt + "/" + originalFileName;
//            String encFilePath = fullDecryptedPath + ".enc";
//            String keyFilePath = fullDecryptedPath + ".key";
//
//            // Download encrypted file and AES key from HDFS
//            fs.copyToLocalFile(new Path(hdfsEncPath), new Path(encFilePath));
//            fs.copyToLocalFile(new Path(hdfsEncPath.replace(".enc", ".key")), new Path(keyFilePath));
//
//            // Read the encrypted AES key
//            byte[] encryptedAesKey = Files.readAllBytes(Paths.get(keyFilePath));
//            System.out.println("Length of encrypted AES key: " + encryptedAesKey.length);
//
//            // Retrieve the RSA private key for the user
//            User user = userRepository.findByUsername(username)
//                    .orElseThrow(() -> new RuntimeException("User not found"));
//            PrivateKey privateKey = RSAKeyUtil.getPrivateKeyFromBytes(user.getPrivateKey());
//
//            Cipher rsaCipher = Cipher.getInstance("RSA");
//            rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
//            byte[] aesKeyBytes = rsaCipher.doFinal(encryptedAesKey);
//
//            // Ensure valid AES key length
//            if (aesKeyBytes.length != 16 && aesKeyBytes.length != 24 && aesKeyBytes.length != 32) {
//                throw new RuntimeException("Invalid AES key length: " + aesKeyBytes.length + " bytes");
//            }
//
//            SecretKey aesKey = new SecretKeySpec(aesKeyBytes, 0, aesKeyBytes.length, "AES");
//
//            // Read the encrypted file content
//            byte[] encryptedFileContent = Files.readAllBytes(Paths.get(encFilePath));
//
//            // Decrypt the file content using AES
//            Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); // Specify padding
//            aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
//            byte[] decryptedFileContent = aesCipher.doFinal(encryptedFileContent);
//
//            // Write the decrypted content to the original file
//            Files.write(Paths.get(fullDecryptedPath + "." + fileExtension), decryptedFileContent);
//
//            // Cleanup temporary files
//            Files.deleteIfExists(Paths.get(encFilePath));
//            Files.deleteIfExists(Paths.get(keyFilePath));
//
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to download or decrypt file: " + e.getMessage(), e);
//        }
//    }

//    public void uploadFile(byte[] fileData, String hdfsPath, String uploadedFileName, String username) {
//        try {
//            FileSystem fs = HDFSConfig.getHDFS();
//            ByteArrayInputStream inputStream = new ByteArrayInputStream(fileData);
//            String finalHdfsPath = hdfsPath.endsWith("/") ? hdfsPath + uploadedFileName : hdfsPath + "/" + uploadedFileName;
//            Path hdfsFilePath = new Path(finalHdfsPath);
//            try (FSDataOutputStream outputStream = fs.create(hdfsFilePath)) {
//                IOUtils.copyBytes(inputStream, outputStream, 4096, true);
//            }
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to upload file to HDFS: " + e.getMessage(), e);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public void downloadFile(String hdfsEncPath, String localPathWithoutExt, String username) {
//        try {
//            FileSystem fs = HDFSConfig.getHDFS();
//            String encFilePath = localPathWithoutExt + ".enc";
//            fs.copyToLocalFile(new Path(hdfsEncPath), new Path(encFilePath));
//
//            User user = userRepository.findByUsername(username)
//                    .orElseThrow(() -> new RuntimeException("User not found"));
//            PrivateKey privateKey = RSAKeyUtil.getPrivateKeyFromBytes(user.getPrivateKey());
//
//            byte[] encryptedFileContent = Files.readAllBytes(Paths.get(encFilePath));
//            byte[] decryptedFileContent = RSAKeyUtil.decrypt(encryptedFileContent, privateKey);
//
//            Files.write(Paths.get(localPathWithoutExt), decryptedFileContent);
//            Files.deleteIfExists(Paths.get(encFilePath));
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to download or decrypt file: " + e.getMessage(), e);
//        }
//    }

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