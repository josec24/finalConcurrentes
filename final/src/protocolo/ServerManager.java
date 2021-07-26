package protocolo;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerManager {
    private static final Logger logger = Logger.getLogger(ServerManager.class.getName());
    private static final int SERVER_PORT = 4444;
    private final MyMap nodes;
    private ServerSocket server;
    public static int counter = 0;
    private final ServerThreadHandler[] clients = new ServerThreadHandler[10];
    private final OnMessageReceived messageListener;
    private int idLeader;

    public ServerManager(OnMessageReceived messageListener, MyMap nodes, int idLeader) {
        this.messageListener = messageListener;
        this.nodes = nodes;
        this.idLeader = idLeader;
    }

    public OnMessageReceived getMessageListener() {
        return this.messageListener;
    }

    public void startServer() {
        try {
            this.server = new ServerSocket(SERVER_PORT);
            logger.info("" +
                    "Servidor principal iniciado HazelDev\n" +
                    "Esperando a que se conecten los nodos...");

            while (true) {
                Socket socket = server.accept();
                logger.info("Servidor conectado");
                this.clients[counter] = new ServerThreadHandler(socket, this, counter, nodes);
                this.clients[counter].start();
                this.clients[counter].sendMessage("Who_are? " + counter);
                counter++;
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Ocurrio un error en el servidor", ex);
        }
    }

    public void closeServer() {
        try {
            this.server.close();
        } catch (Exception e) {
            System.out.println("Error al cerrar servidor");
        }
    }

    public synchronized void sendMessageAllNodes(String request) {
        for (int i = 0; i < counter; i++) {
            clients[i].sendMessage(request);
        }
    }

    public void sendMessageToClient(String message, int idCliente) {
        clients[idCliente].sendMessage(message);
    }

    public void sendMessageToLeader(String request) {
        clients[0].sendMessage(request);
    }

    public interface OnMessageReceived {
        void messageReceived(String message);
    }

    public void checkConnection() {
        try {
            while (true) {
                Thread.sleep(3000);
                for (Map.Entry<Integer, Map.Entry<String, Boolean>> entry : nodes.entrySet()) {
                    Integer index = entry.getKey();
                    Map.Entry<String, Boolean> value = entry.getValue();
                    nodes.put(index, new AbstractMap.SimpleEntry<>(nodes.get(index).getKey(), false));
                    if (value.getValue()) {
                        this.sendMessageToClient("Hello", index);
                    }
                }
            }
        } catch (InterruptedException ex) {
            System.out.println("ERROR " + ex);
        }
    }

    public void updateConnected() {
        try {
            while (true) {
                Thread.sleep(5000);
                nodes.forEach((index, value) -> {
                    if (value.getValue()) {
                        clients[index].sendMessage("NODES " + nodes.toString());
                        if (!nodes.get(idLeader).getValue()){
                            idLeader = nodes.getLeader((new Random()).nextInt(nodes.activeSize()));
                            System.out.println("El nuevo lider es " + idLeader);
                        }
                    }
                });
            }
        } catch (InterruptedException ex) {
            System.out.println("ERROR " + ex);
        }
    }
}
