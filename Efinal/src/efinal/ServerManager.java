package efinal;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerManager {
    private static final int MAX_REPLICAS = 3;
    private static final int SERVER_PORT = 4444;
    private ServerSocket server;
    public static int counter = 0;
    private final ServerThreadHandler[] clients = new ServerThreadHandler[10];
    private final OnMessageReceived messageListener;

    public ServerManager(OnMessageReceived messageListener) {
        this.messageListener = messageListener;
    }

    public OnMessageReceived getMessageListener() {
        return this.messageListener;
    }

    public void startServer() {
        try {
            this.server = new ServerSocket(SERVER_PORT);
            System.out.println("" +
                    "-------------------------------------\n" +
                    "Servidor distribuidor iniciado.      \n" +
                    "Esperando a que se conecte un cliente\n" +
                    "-------------------------------------");

            while (true) {
                // El metodo accept() bloquea el servidor hasta que se conecta un cliente
                Socket socket = server.accept();
                if (counter >= MAX_REPLICAS) {
                    System.out.println("Cliente " + counter + " conectado");
                    System.out.println("Asignando un nuevo hilo al cliente " + counter);
                } else {
                    System.out.println("Servidor replica " + counter + " conectado");
                    System.out.println("Asignando un nuevo hilo al servidor replica " + counter);
                }

                this.clients[counter] = new ServerThreadHandler(socket, this, counter);
                this.clients[counter].start();
                counter++;
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void closeServer() {
        try {
            this.server.close();
            for (int i = 0; i < counter; i++) {
                clients[i].closeConnection();
            }
        } catch (IOException ex) {
            System.out.println("Error al cerrar el servidor: " + ex.getMessage());
        }
    }

    public synchronized void sendMessageAllReplicas(String request) {
        clients[0].sendMessage(request + ";0");
        for (int i = 1; i < MAX_REPLICAS; i++) {
            clients[i].sendMessage(request + ";1");
        }
    }

    public void sendMessageToClient(String message, int idCliente) {
        clients[idCliente].sendMessage(message);
    }

    public void sendMessageToFirstReplica(String request) {
        clients[0].sendMessage(request);
    }

    public interface OnMessageReceived {
        void messageReceived(String message);
    }
}
