package com.skycrate.backend.skycrateBackend.config;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.context.annotation.Bean;

import java.net.URI;
import java.security.PrivilegedExceptionAction;

 // HDFS configuration bean to securely connect to a remote Hadoop cluster.
@Configuration
public class HDFSConfig {

    private static final String HDFS_URI = System.getenv("HDFS_URI"); // export HDFS_URI=hdfs://192.168.29.30:9000
    private static final String HDFS_USER = System.getenv("HDFS_USER"); // Hadoop user (if needed)

     // Configures and returns a secured HDFS FileSystem instance.
    @Bean
    public FileSystem fileSystem() throws Exception {
        return getHDFS(); // use the static method internally
    }

     // Static method to get a FileSystem instance. Used by other classes like HDFSController.
    public static FileSystem getHDFS() throws Exception {
        if (HDFS_URI == null || HDFS_URI.isBlank()) {
            throw new IllegalStateException("HDFS_URI environment variable not set.");
        }

        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", HDFS_URI);

        if (HDFS_USER != null && !HDFS_USER.isBlank()) {
            return UserGroupInformation.createRemoteUser(HDFS_USER)
                    .doAs((PrivilegedExceptionAction<FileSystem>) () ->
                            FileSystem.get(new URI(HDFS_URI), conf)
                    );
        } else {
            return FileSystem.get(new URI(HDFS_URI), conf);
        }
    }
}