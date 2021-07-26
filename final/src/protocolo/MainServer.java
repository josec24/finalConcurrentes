package protocolo;

import java.util.*;
import java.util.logging.Logger;

public class MainServer {
    private ServerManager serverManager;
    private static final Logger logger = Logger.getLogger(ServerManager.class.getName());
    private MyMap nodes = new MyMap();
    private HashMap<Integer, String> clients = new HashMap<>();
    private int idLeader = 0;
    private boolean leaderStatus = false;

    private void startServer() {
        serverManager = new ServerManager(this::receiveMessage, nodes, idLeader);
        new Thread(() -> {
            serverManager.startServer();
        }).start();

        new Thread(() -> {
            serverManager.checkConnection();
        }).start();

        new Thread(() -> {
            serverManager.updateConnected();
        }).start();

        logger.info("" +
                "Bienvenido al servidor principal\n" +
                "Ingrese exit para salir");

        String message = "";
        Scanner scanner = new Scanner(System.in);
        while (!message.equals("exit")) {
            message = scanner.nextLine();
        }
        serverManager.closeServer();
    }


    private void receiveMessage(String message) {
        if (message.contains("Hello_from")) {
            String id = message.split("\\s+")[1];
            int index = Integer.parseInt(id.split("=")[0]);
            nodes.put(index, new AbstractMap.SimpleEntry<>(nodes.get(index).getKey(), true));
            System.err.println(message);
        }

        if (message.contains("I_am SERV")) {
            String idServer = message.split("\\s+")[1];
            int indexServer = Integer.parseInt(message.split("\\s+")[2]);
            System.out.println("Servidor " + idServer + " conectado, index " + indexServer);

            if (!leaderStatus) {
                serverManager.sendMessageToClient("Soy el servidor lider!", idLeader);
                idLeader = indexServer;
                leaderStatus = true;
            }

            nodes.put(indexServer, new AbstractMap.SimpleEntry<>(idServer, true));
            serverManager.sendMessageAllNodes("NODES " + nodes.toString());
        }

        if (message.contains("I_am CLI")) {
            String idServer = message.split("\\s+")[1];
            int indexServer = Integer.parseInt(message.split("\\s+")[2]);
            System.out.println("Cliente " + idServer + " conectado, index " + indexServer);
            clients.put(indexServer, idServer);
            serverManager.sendMessageAllNodes("CLIENTS " + clients.toString());
        }

        if (message.contains("VOTO")) {
            System.out.println(message);
        }
    }


    public static void main(String[] args) {
        new MainServer().startServer();
    }
}
