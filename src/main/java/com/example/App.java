package com.example;

import java.io.IOException;
import java.util.Scanner;

import com.example.connection.impl.FTPActiveMode;
import com.example.connection.impl.FTPPassiveMode;
import com.example.constants.FTPConnectionMode;

public class App {
    public static void main( String[] args ) {
        Scanner scanner = new Scanner(System.in);
        FTPClient ftpClient = null;
        String input;

        while (true) {
            System.out.print("> ");
            input = scanner.nextLine().trim();

            if(input.equals("exit")) {
                System.out.println("See you next time!");
                scanner.close();
                break;
            }

            String[] inputParts = input.split(" ", 2);
            String command = inputParts[0];
            String params = inputParts.length > 1 ? inputParts[1] : "";

            switch (command) {
                case "connect":
                    if (params.isEmpty()) {
                        System.out.println("Please provide a server, port, username, and password");
                        break;
                    }

                    String[] connectParts = params.split(" ");
                    String hostPart = connectParts[0];
                    String portPart = connectParts[1];
                    String usernamePart = connectParts[2];
                    String passwordPart = connectParts[3];
                    String modePart = connectParts[4];

                    String host = hostPart.replace("--host=", "");
                    String port = portPart.replace("--port=", "");
                    String username = usernamePart.replace("--username=", "");
                    String password = passwordPart.replace("--password=", "");
                    String mode = modePart.replace("--mode=", "");

                    try {
                        ftpClient = new FTPClient(host, Integer.parseInt(port), username, password);
                        boolean loginResponse = ftpClient.login();
                        if (!loginResponse) {
                            System.out.println("Failed to connect to server: " + host + ":" + port);
                            break;
                        }
                        System.out.println("Connected to server: " + host + ":" + port);
                        if (mode.equals(FTPConnectionMode.ACTIVE.getMode())) {
                            ftpClient.setFTPConnectionMode(new FTPActiveMode());
                        } else if (mode.equals(FTPConnectionMode.PASSIVE.getMode())) {
                            ftpClient.setFTPConnectionMode(new FTPPassiveMode());
                        } else {
                            ftpClient.setFTPConnectionMode(new FTPPassiveMode());
                        }
                    } catch (IOException e) {
                        System.out.println("Error connecting to server: " + e.getMessage());
                    }
                    
                    break;

                case "pwd": 
                    try {
                        ftpClient.currentDir();
                    } catch (IOException e) {
                        System.out.println("Error pwd command: " + e.getMessage());
                    } 
                    break;

                case "list":
                    try {
                        ftpClient.list();
                    } catch (IOException e) {
                        System.out.println("Error listing files: " + e.getMessage());
                    }
                    break;

                case "upload":
                    try {
                        if (params.isEmpty()) {
                            System.out.println("Please provide a server, port, username, and password");
                            break;
                        }

                        String[] uploadParts = params.split(" ");
                        String filePathPart = uploadParts[0];
                        String remoteFileNamePart = uploadParts[1];

                        String filePath = filePathPart.replace("--file=", "");
                        String remoteFileName = remoteFileNamePart.replace("--remote-file=", "");

                        ftpClient.upload(filePath, remoteFileName);
                    } catch (IOException e) {
                        System.out.println("Error uploading file: " + e.getMessage());
                    }
                    break;

                case "download":
                    try {
                        if (params.isEmpty()) {
                            System.out.println("Please provide a remote file path and local file path");
                            break;
                        }

                        String[] downloadParts = params.split(" ");
                        String remoteFileNamePart = downloadParts[0];
                        String localFilePathPart = downloadParts[1];

                        String remoteFileName = remoteFileNamePart.replace("--remote-file=", "");
                        String localFilePath = localFilePathPart.replace("--local-file=", "");

                        ftpClient.download(remoteFileName, localFilePath);
                    } catch (IOException e) {
                        System.out.println("Error download file: " + e.getMessage());
                    }
                    break;

                case "downloadDir":
                    try {
                        if (params.isEmpty()) {
                            System.out.println("Please provide a remote folder path and local folder path");
                            break;
                        }
    
                        String[] downloadParts = params.split(" ");
                        String remoteFolderPart = downloadParts[0];
                        String localFolderPart = downloadParts[1];
    
                        String remoteFolderPath = remoteFolderPart.replace("--remote-folder=", "");
                        String localFolderPath = localFolderPart.replace("--local-folder=", "");
    
                        ftpClient.downloadDir(remoteFolderPath, localFolderPath);
                    } catch (IOException e) {
                        System.out.println("Error download directory: " + e.getMessage());
                    }

                    break;

                case "cd": 
                    try {
                        ftpClient.changeDirectory(params);
                    } catch (IOException e) {
                        System.out.println("Error changing directory: " + e.getMessage());
                    }
                    break;
                
                case "switch": 
                    String switchedMode = params.replace("--mode=", "");
                    if (switchedMode.equals(FTPConnectionMode.ACTIVE.getMode())) {
                        ftpClient.setFTPConnectionMode(new FTPActiveMode());
                        System.out.println("Switch to active mode");
                    } else if (switchedMode.equals(FTPConnectionMode.PASSIVE.getMode())) {
                        ftpClient.setFTPConnectionMode(new FTPPassiveMode());
                        System.out.println("Switch to passive mode");
                    } else {
                        ftpClient.setFTPConnectionMode(new FTPPassiveMode());
                        System.out.println("Switch to passive mode");
                    }
                    break;
                    
                default:
                    System.out.println("Invalid command");
                    break;
            }
        }
    }
}
