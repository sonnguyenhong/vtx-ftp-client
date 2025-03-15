package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import com.example.common.UtilService;
import com.example.constants.FTPResponseCode;

public class FTPActiveMode implements FTPConnectionMode {

    private static final String CLIENT_IP_PART = "192,168,19,126";

    @Override
    public String currentDir(ControlSocketHandler controlSocketHandler) throws IOException {
        controlSocketHandler.send("PWD");
        String pwdResponse = controlSocketHandler.receive();
        String response = UtilService.getCurrentDirectoryFromResponse(pwdResponse);

        return response;
    }

    @Override
    public String list(ControlSocketHandler controlSocketHandler, ExecutorService executorService) throws IOException {
        String clientIPPart = CLIENT_IP_PART;
        ServerSocket serverSocket = null;
        StringBuilder listFileResponse = new StringBuilder();

        int randomPort = UtilService.getRandomPort();
        serverSocket = new ServerSocket(randomPort);
        final ServerSocket finalServerSocket = serverSocket;

        String ftpPortString = UtilService.constructIP(clientIPPart, serverSocket);
        controlSocketHandler.send("PORT " + ftpPortString);
        String portResponse = controlSocketHandler.receive();
        // Check error
        if(!portResponse.startsWith(FTPResponseCode.PORT_COMMAND_SUCCESS.getCode())) {
            return "PORT error";
        }

        controlSocketHandler.send("LIST");
        String listResponse = controlSocketHandler.receive();
        if (
            !listResponse.startsWith(FTPResponseCode.OPENING_DATA_CONNECTION.getCode()) &&
            !listResponse.startsWith(FTPResponseCode.COMMAND_SUCCESS.getCode())
        ) {
            return "LIST error";
        }

        try (
            Socket dataSocket = finalServerSocket.accept();
            BufferedReader reader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                listFileResponse.append(line).append("\n");
            }
        } finally {
            // Receive final response
            controlSocketHandler.receive();
            if (finalServerSocket != null && !finalServerSocket.isClosed()) {
                finalServerSocket.close();
            }
        }

        return listFileResponse.toString();
    }

    @Override
    public void upload(String localFilePath, String remoteFileName, ControlSocketHandler controlSocketHandler, ExecutorService executorService) throws IOException {
        String clientIPPart = CLIENT_IP_PART;
        File file = new File(localFilePath);
        ServerSocket serverSocket = null;

        int randomPort = UtilService.getRandomPort();
        serverSocket = new ServerSocket(randomPort);
        final ServerSocket finalServerSocket = serverSocket;

        String ftpPortString = UtilService.constructIP(clientIPPart, serverSocket);
        controlSocketHandler.send("PORT " + ftpPortString);
        String portResponse = controlSocketHandler.receive();
        // Check error
        if(!portResponse.startsWith(FTPResponseCode.PORT_COMMAND_SUCCESS.getCode())) {
            return;
        }

        controlSocketHandler.send("STOR " + remoteFileName);
        String storeResponse = controlSocketHandler.receive();
        if (
            !storeResponse.startsWith(FTPResponseCode.COMMAND_SUCCESS.getCode()) &&
            !storeResponse.startsWith(FTPResponseCode.OPENING_DATA_CONNECTION.getCode())
        ) {
            return;
        }

        executorService.execute(() -> {
            try (
                Socket dataSocket = finalServerSocket.accept();
                FileInputStream fileInputStream = new FileInputStream(file);
                OutputStream outputStream = dataSocket.getOutputStream();
            ) {
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
                    if (finalServerSocket != null && !finalServerSocket.isClosed()) {
                        finalServerSocket.close();
                    }
                } catch (IOException e) {
                    System.err.println("Error closing server socket: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void download(String remoteFileName, String localFilePath, ControlSocketHandler controlSocketHandler, ExecutorService executorService) throws IOException {
        String clientIPPart = CLIENT_IP_PART;
        ServerSocket serverSocket = null;

        int randomPort = UtilService.getRandomPort();
        serverSocket = new ServerSocket(randomPort);
        final ServerSocket finalServerSocket = serverSocket;

        String ftpPortString = UtilService.constructIP(clientIPPart, serverSocket);
        controlSocketHandler.send("PORT " + ftpPortString);
        String portResponse = controlSocketHandler.receive();
        if (!portResponse.startsWith(FTPResponseCode.PORT_COMMAND_SUCCESS.getCode())) {
            return;
        }

        controlSocketHandler.send("RETR " + remoteFileName);
        String retrResponse = controlSocketHandler.receive();
        if (
            !retrResponse.startsWith(FTPResponseCode.COMMAND_SUCCESS.getCode()) &&
            !retrResponse.startsWith(FTPResponseCode.OPENING_DATA_CONNECTION.getCode())
        ) {
            return;
        }

        executorService.execute(() -> {
            File file = new File(localFilePath);
            try (
                Socket dataSocket = finalServerSocket.accept();
                FileOutputStream fileOutputStream = new FileOutputStream(file);
            ) {
                byte[] bytes = new byte[4096];
                int bytesRead;
                while ((bytesRead = dataSocket.getInputStream().read(bytes)) != -1) {
                    fileOutputStream.write(bytes, 0, bytesRead);
                }
            } catch (IOException e) {
                System.err.println("Error in data transfer: " + e.getMessage());
            } finally {
                try {
                    // Receive final response
                    controlSocketHandler.receive();
                    if (finalServerSocket != null && !finalServerSocket.isClosed()) {
                        finalServerSocket.close();
                    }
                } catch (IOException e) {
                    System.err.println("Error closing server socket: " + e.getMessage());
                }
            }
        });            
    }

    @Override
    public void changeDirectory(String directory,ControlSocketHandler controlSocketHandler) throws IOException {
        controlSocketHandler.send("CWD " + directory);
        String cwdResponse = controlSocketHandler.receive();
        if (!cwdResponse.startsWith(FTPResponseCode.FILE_ACTION_OK.getCode())) {
            System.out.println("Error occur");
            return;
        }
        System.out.println("Change directory to " + directory);
    }

    @Override
    public void downloadDir(String remoteFolder, String localFolder, ControlSocketHandler controlSocketHandler,
            ExecutorService executorService) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'downloadDir'");
    }
}
