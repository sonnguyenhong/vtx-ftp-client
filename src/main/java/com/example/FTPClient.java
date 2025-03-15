package com.example;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.connection.FTPConnectionMode;
import com.example.constants.FTPResponseCode;

public class FTPClient {
    private ControlSocketHandler controlSocketHandler;
    private static ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    private String username;
    private String password;
    private FTPConnectionMode ftpConnectionMode;

    public FTPClient() {}

    public FTPClient(String host, int port, String username, String password) throws IOException {
        this.controlSocketHandler = new ControlSocketHandler(connect(host, port));
        this.controlSocketHandler.receive();
        this.username = username;
        this.password = password;
    }

    public Socket connect(String host, int port) throws IOException {
        Socket controlSocket = new Socket(host, port);
        return controlSocket;
    }

    public boolean login() throws IOException {
        controlSocketHandler.send("USER " + this.username);
        String userResponse = controlSocketHandler.receive();
        if (!userResponse.startsWith(FTPResponseCode.REQUIRES_PASSWORD.getCode())) {
            return false;
        }

        controlSocketHandler.send("PASS " + this.password);
        String passwordResponse = controlSocketHandler.receive();
        if(!passwordResponse.startsWith(FTPResponseCode.LOGGED_IN.getCode())) {
            return false;
        }

        return true;
    }

    public void currentDir() throws IOException {
        String currentDirResponse = ftpConnectionMode.currentDir(controlSocketHandler);
        System.out.println(currentDirResponse);
    }

    public void list() throws IOException {
        String listResponse = ftpConnectionMode.list(this.controlSocketHandler, executorService);
        System.out.println(listResponse);
    }

    public void upload(String localFilePath, String remoteFileName) throws IOException {
        ftpConnectionMode.upload(localFilePath, remoteFileName, controlSocketHandler, executorService);
    }

    public void download(String remoteFileName, String localFilePath) throws IOException {
        ftpConnectionMode.download(remoteFileName, localFilePath, controlSocketHandler, executorService);
    }

    public void downloadDir(String remoteFolder, String localFolder) throws IOException {
        ftpConnectionMode.downloadDir(remoteFolder, localFolder, controlSocketHandler, executorService);
    }

    public void changeDirectory(String directory) throws IOException {
       ftpConnectionMode.changeDirectory(directory, controlSocketHandler);
    }

    // Getters and Setters
    public void setFTPConnectionMode(FTPConnectionMode ftpConnectionMode) {
        this.ftpConnectionMode = ftpConnectionMode;
    }
}