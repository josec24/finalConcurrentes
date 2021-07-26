package protocolo;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Logger;

public class Node {
    private static final int PORT = 4444;
    private static final Logger logger = Logger.getLogger(Node.class.getName());
    private Socket socket;
    private DataInputStream inputSocket;
    private DataOutputStream outputSocket;
    private final String serverID = "SERV-" + UUID.randomUUID().toString();
    private int serverIndex = 0;

    public Node() {
        try {
            InetAddress ip = InetAddress.getByName("localhost");
            socket = new Socket(ip, PORT);
            logger.info("" +
                    "Servidor iniciado\n" +
                    "Ingrese exit para salir");
            inputSocket = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            outputSocket = new DataOutputStream(socket.getOutputStream());
            sendMessageToServer("ID " + serverID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startNode() {
        try {
            while (!socket.isClosed()) {
                String message = inputSocket.readUTF();
                if (message.contains("NODES")) {
                    System.out.println("\nServidores conectados:");
                    String nodes = message.split("\\s+", 2)[1];
                    nodes = nodes.replace(" ", ";").replace(",", "");
                    nodes = nodes.substring(1, nodes.length() - 1);
                    for (String node : nodes.split(";")) {
                        String id[] = node.split("=");
                        System.out.println(id[1]);
                    }
                }
                if (message.contains("Who_are?")) {
                    serverIndex = Integer.parseInt(message.split("\\s+")[1]);
                    sendMessageToServer("I_am " + serverID + " " + serverIndex);
                }

                if (message.contains("Hello")) {
                    sendHello();
                }
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("ERROR NODE: " + e.getMessage());
        }
    }

    public void sendHello() {
        try {
            outputSocket.writeUTF("Hello_from " + serverIndex + "=" + serverID + "=" + "true");
            outputSocket.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToServer(String message) {
        try {
            outputSocket.writeUTF(message.trim());
            outputSocket.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        Node node = new Node();
        new Thread(node::startNode).start();

        String line = "";
        Scanner scanner = new Scanner(System.in);

        while (!line.equals("exit")) {
            line = scanner.nextLine();
        }
        node.closeConnection();
        node.sendMessageToServer("Socket closed");
        scanner.close();
    }

    private void closeConnection() {
        try {
            socket.close();
            outputSocket.close();
            inputSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
