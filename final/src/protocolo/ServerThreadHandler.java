package protocolo;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ServerThreadHandler extends Thread {
    private DataInputStream inputSocket;
    private DataOutputStream outputSocket;
    private final int nodeId;
    private final Socket socket;
    private final ServerManager serverManager;
    private final HashMap<Integer,  Map.Entry<String, Boolean>> nodes;

    public ServerThreadHandler(Socket socket, ServerManager serverManager, int nodeId, HashMap<Integer,  Map.Entry<String, Boolean>> nodes) {
        this.socket = socket;
        this.nodeId = nodeId;
        this.serverManager = serverManager;
        this.nodes = nodes;

        try {
            this.inputSocket = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            this.outputSocket = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        ServerManager.OnMessageReceived messageListener = serverManager.getMessageListener();
        try {
            while (true) {
                String received = inputSocket.readUTF();
                if (messageListener != null) {
                    messageListener.messageReceived(received);
                }
                if (received.equals("exit")) {
                    System.out.println("Cerrando conexion");
                    this.closeConnection();
                    System.out.println("Conexion cerrada");
                    break;
                }
            }
        } catch (IOException e) {
            if (e.toString().contains("Socket closed") || e.toString().contains("Connection reset")) {
                System.out.println("ERROR " + e + ". Conexion finalizada.");
            }
        }
    }

    public void closeConnection() {
        try {
            socket.close();
            inputSocket.close();
            outputSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String request) {
        if (outputSocket != null) {
            try {
                outputSocket.writeUTF(request);
                outputSocket.flush();
            } catch (IOException e) {
                System.out.println("ERROR SERVERTHEARDHANDLER " + e);
            }
        }
    }
}
