//package com.skycrate.backend.skycrateBackend.config;
//
//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.fs.FileSystem;
//import org.apache.hadoop.security.UserGroupInformation;
//import org.springframework.context.annotation.Bean;
//
//import java.io.IOException;
//import java.net.URI;
//import java.security.PrivilegedExceptionAction;
//
//@org.springframework.context.annotation.Configuration
//public class HDFSConfig {
//
//    public static FileSystem getHDFS() throws Exception {
//        Configuration conf = new Configuration();
//        conf.set("fs.defaultFS", "hdfs://192.168.29.56:9000");
//        conf.set("hadoop.security.authentication", "simple");
//
//        // Disable security manager
//        System.setProperty("java.security.manager", "allow");
//
//        // Required for Java 17+: Explicitly configure UGI
//        UserGroupInformation.setConfiguration(conf);
//        UserGroupInformation ugi = UserGroupInformation.createRemoteUser("hdfs");
//
//        // Login user explicitly and return FileSystem instance
//        return ugi.doAs((PrivilegedExceptionAction<FileSystem>) () ->
//                FileSystem.get(new URI("hdfs://192.168.29.56:9000"), conf)
//        );
//    }
//}


package com.skycrate.backend.skycrateBackend.config;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.context.annotation.Bean;

import java.net.URI;
import java.security.PrivilegedExceptionAction;


public class HDFSConfig {
    public static FileSystem getHDFS() throws Exception {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://192.168.29.56:9000");
        return FileSystem.get(new URI("hdfs://192.168.29.56:9000"), conf);
    }
}
