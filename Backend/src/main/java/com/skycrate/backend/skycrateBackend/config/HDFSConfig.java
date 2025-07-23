package com.skycrate.backend.skycrateBackend.config;

import org.apache.hadoop.conf.Configuration; // Hadoop Configuration
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.context.annotation.Bean;

import java.net.URI;
import java.security.PrivilegedExceptionAction;

@org.springframework.context.annotation.Configuration
public class HDFSConfig {

    private static final String HDFS_URI = System.getenv("HDFS_URI"); // e.g., hdfs://namenode:9000
    private static final String HDFS_USER = System.getenv("HDFS_USER"); // e.g., hdfsuser

    @Bean
    public FileSystem fileSystem() throws Exception {
        return getHDFS();
    }

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