package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import com.example.common.UtilService;
import com.example.constants.FTPResponseCode;

public class FTPPassiveMode implements FTPConnectionMode {

    @Override
    public String currentDir(ControlSocketHandler controlSocketHandler) throws IOException {
        controlSocketHandler.send("PWD");
        String pwdResponse = controlSocketHandler.receive();
        String response = UtilService.getCurrentDirectoryFromResponse(pwdResponse);

        return response;
    }

    @Override
    public String list(ControlSocketHandler controlSocketHandler, ExecutorService executorService) throws IOException {
        StringBuilder listFileResponse = new StringBuilder();
        // Send PASV command
        controlSocketHandler.send("PASV");
        String pasvResponse = controlSocketHandler.receive();
        if (!pasvResponse.startsWith(FTPResponseCode.ENTERING_PASSIVE_MODE.getCode())) {
            return "PASV error";
        }
        
        String ipAndPort = UtilService.extractIPAndPortFromPASVResponse(pasvResponse);
        String serverIP = UtilService.constructIPFromIPAndPort(ipAndPort);
        int serverPort = UtilService.extractPortFromIPAndPort(ipAndPort);

        try (Socket dataSocket = new Socket(serverIP, serverPort)) {
            controlSocketHandler.send("LIST");
            controlSocketHandler.receive();
            BufferedReader reader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                listFileResponse.append(line).append("\n");
            }
        } finally {
            String listResponse = controlSocketHandler.receive();
            if (
                !listResponse.startsWith(FTPResponseCode.OPENING_DATA_CONNECTION.getCode()) &&
                !listResponse.startsWith(FTPResponseCode.DIRECTORY_SEND_OK.getCode())
            ) {
                return null;
            }
        }

        return listFileResponse.toString();
    }

    @Override
    public void upload(String localFilePath, String remoteFileName, ControlSocketHandler controlSocketHandler,
            ExecutorService executorService) throws IOException {
        File file = new File(localFilePath);
        controlSocketHandler.send("PASV");
        String pasvResponse = controlSocketHandler.receive();
        if (!pasvResponse.startsWith(FTPResponseCode.ENTERING_PASSIVE_MODE.getCode())) {
            return;
        }

        String ipAndPort = UtilService.extractIPAndPortFromPASVResponse(pasvResponse);
        String serverIP = UtilService.constructIPFromIPAndPort(ipAndPort);
        int serverPort = UtilService.extractPortFromIPAndPort(ipAndPort);

        executorService.execute(() -> {
            try (
                Socket dataSocket = new Socket(serverIP, serverPort);
                FileInputStream fileInputStream = new FileInputStream(file);
                OutputStream outputStream = dataSocket.getOutputStream();
            ) {
                controlSocketHandler.send("STOR " + remoteFileName);
                String storeResponse = controlSocketHandler.receive();
                if (!storeResponse.startsWith(FTPResponseCode.OPENING_DATA_CONNECTION.getCode())) {
                    return;
                }

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                System.err.println("Error in data transfer: " + e.getMessage());
            } finally {
                try {
                    // Receive final response
                    controlSocketHandler.receive();
                } catch (IOException e) {
                    System.err.println("Error in data transfer: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void download(String remoteFileName, String localFilePath, ControlSocketHandler controlSocketHandler, ExecutorService executorService) throws IOException {
        controlSocketHandler.send("PASV");
        String pasvResponse = controlSocketHandler.receive();
        if (!pasvResponse.startsWith(FTPResponseCode.ENTERING_PASSIVE_MODE.getCode())) {
            return;
        }

        String ipAndPort = UtilService.extractIPAndPortFromPASVResponse(pasvResponse);
        String serverIP = UtilService.constructIPFromIPAndPort(ipAndPort);
        int serverPort = UtilService.extractPortFromIPAndPort(ipAndPort);

        executorService.execute(() -> {
            File file = new File(localFilePath);
            try (
                Socket dataSocket = new Socket(serverIP, serverPort);
                FileOutputStream fileOutputStream = new FileOutputStream(file);
            ) {
                controlSocketHandler.send("RETR " + remoteFileName);
                String retrResponse = controlSocketHandler.receive();
                if (
                    !retrResponse.startsWith(FTPResponseCode.COMMAND_SUCCESS.getCode()) &&
                    !retrResponse.startsWith(FTPResponseCode.OPENING_DATA_CONNECTION.getCode())
                ) {
                    return;
                }
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = dataSocket.getInputStream().read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                System.err.println("Error in data transfer: " + e.getMessage());
            } finally {
                try {
                    // Receive final response
                    controlSocketHandler.receive();
                } catch (IOException e) {
                    System.out.println("Error in data transfer: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void changeDirectory(String directory, ControlSocketHandler controlSocketHandler) throws IOException {
        controlSocketHandler.send("CWD " + directory);
        String cwdResponse = controlSocketHandler.receive();
        if (
            !cwdResponse.startsWith(FTPResponseCode.DIRECTORY_SEND_OK.getCode()) &&
            !cwdResponse.startsWith(FTPResponseCode.FILE_ACTION_OK.getCode()) 
        ) {
            System.out.println("Error occur");
            return;
        }
        System.out.println("Change directory to " + directory);
    }

    @Override
    public void downloadDir(String remoteFolder, String localFolder, ControlSocketHandler controlSocketHandler,
            ExecutorService executorService) throws IOException {
        
    }
}
