package com.example.connection;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import com.example.ControlSocketHandler;

public interface FTPConnectionMode {
    public String currentDir(ControlSocketHandler controlSocketHandler) throws IOException;
    public String list(ControlSocketHandler controlSocketHandler, ExecutorService executorService) throws IOException;
    public void upload(String localFilePath, String remoteFileName, ControlSocketHandler controlSocketHandler, ExecutorService executorService) throws IOException;
    public void download(String remoteFileName, String localFilePath, ControlSocketHandler controlSocketHandler, ExecutorService executorService) throws IOException;
    public void downloadDir(String remoteFolder, String localFolder, ControlSocketHandler controlSocketHandler, ExecutorService executorService) throws IOException;
    public void changeDirectory(String directory, ControlSocketHandler controlSocketHandler) throws IOException;
}
