package efinal;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

// La clase ServerThreadHava se encargara de la asignacion de un hilo a cada socket que se conecta al servidor.
public class ServerThreadHandler extends Thread {

    private DataInputStream inputSocket;
    private DataOutputStream outputSocket;
    private final Socket socket;
    private final int nodeId;
    private final ServerManager serverManager;

    public ServerThreadHandler(Socket socket, ServerManager serverManager, int nodeId) {
        this.socket = socket;
        this.nodeId = nodeId;
        this.serverManager = serverManager;
    }

    @Override
    public void run() {
        String received;
        try {
            // Entrada y salida de los datos en el socket
            this.inputSocket = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            this.outputSocket = new DataOutputStream(socket.getOutputStream());

            // envia el id al cliente
            this.outputSocket.writeUTF("id:" + nodeId);
            ServerManager.OnMessageReceived messageListener = serverManager.getMessageListener();

            while (true) {
                received = new String(inputSocket.readUTF());
                if (messageListener != null) {
                    messageListener.messageReceived(received);
                }
                // se cierra la conexion con el socket del cliente en caso envie exit
                if (received.equals("exit")) {
                    System.out.println("Consumidor " + this.socket + "envia exit");
                    System.out.println("Cerrando conexion");
                    this.socket.close();
                    this.inputSocket.close();
                    this.outputSocket.close();
                    System.out.println("Conexion cerrada");
                    break;
                }
            }
        } catch (IOException ex) {
            System.out.println("Client Handled, error: " + ex.getMessage());
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
                e.printStackTrace();
            }
        }
    }
}
