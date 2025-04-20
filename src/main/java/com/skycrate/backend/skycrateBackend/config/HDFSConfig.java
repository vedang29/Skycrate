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
        conf.set("fs.defaultFS", "hdfs://namenode:9000");
        return FileSystem.get(new URI("hdfs://namenode:9000"), conf);
    }


}
