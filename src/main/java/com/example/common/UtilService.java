package com.example.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Random;
import java.net.URI;

public class UtilService {
    private static final int MIN_PORT = 40000;
    private static final int MAX_PORT = 65535;

    public static String getClientIPAddress() throws MalformedURLException, IOException {
        URL url = URI.create("http://checkip.amazonaws.com").toURL();
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String ip = in.readLine();
        in.close();
        return ip;
    }

    public static String constructIP(String clientIP, ServerSocket serverSocket) {
        int localPort = serverSocket.getLocalPort();
        int highByte = localPort / 256;
        int lowByte = localPort % 256;

        return clientIP.replace(".", ",") + "," + highByte + "," + lowByte;
    }

    public static String extractIPAndPortFromPASVResponse(String pasvResponse) {
        String[] parts = pasvResponse.split("\\(");
        String ipAndPort = parts[1].substring(0, parts[1].length() - 2);
        return ipAndPort;
    }

    public static String constructIPFromIPAndPort(String ipAndPort) {
        String[] parts = ipAndPort.split(",");
        return parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3];
    }

    public static int extractPortFromIPAndPort(String ipAndPort) {  
        String[] parts = ipAndPort.split(",");
        return Integer.parseInt(parts[4]) * 256 + Integer.parseInt(parts[5]);
    }

    public static int getRandomPort() {
        Random random = new Random();
        return random.nextInt(MAX_PORT - MIN_PORT + 1) + MIN_PORT;
    }

    public static boolean isDirectory(String fileInfo) {
        return fileInfo.startsWith("d");
    }

    public static String getFileName(String fileInformation) {
        String[] singleLineArray = fileInformation.trim().split("\\s+", 9);
        return singleLineArray[singleLineArray.length - 1];
    }

    public static String getCurrentDirectoryFromResponse(String response) {
        return response.split("\"")[1];
    }

    public static void createFolderIfNotExists(String localFolder) {
        File folder = new File(localFolder);
        if (!folder.exists()) {
            folder.mkdirs();    
        }
    }
}
