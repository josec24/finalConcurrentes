package efinal;

import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ServerBalanceador {
    private final BlockingQueue<String> queueSolicitudes;
    private final BlockingQueue<String> queueRespuestas;
    private ServerManager serverManager;

    public ServerBalanceador(BlockingQueue<String> queueSolicitudes, BlockingQueue<String> queueRespuestas){
        this.queueSolicitudes = queueSolicitudes;
        this.queueRespuestas = queueRespuestas;
    }

    private void startServer() {
        // Inicia el servidor en un hilo
        // Hilo dedicado recibir las solicitudes
        new Thread(() -> {
            serverManager = new ServerManager(message -> {
                try {
                    receiveMessage(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            serverManager.startServer();
        }).start();

        // Escucha de la entrada de datos en el servidor en el hilo principal
        System.out.println("Bienvenido al servidor\nIngrese exit para salir");

        String message = "";
        Scanner scanner = new Scanner(System.in);
        while (!message.equals("exit")) {
            message = scanner.nextLine();
        }
        serverManager.closeServer();
    }


    private void receiveMessage(String message) throws InterruptedException {
        // Este mensaje es enviado por el cliente y el servidor lo envia a las replicas
        if (message.contains("Solicitud")) {
            // Almacenando las solicitudes en la cola
            queueSolicitudes.put(message.trim());

            String solicitud = queueSolicitudes.take();
            if (solicitud.split(":")[1].contains("A")) {
                serverManager.sendMessageAllReplicas(solicitud);
            } else {
                serverManager.sendMessageToFirstReplica(solicitud);
            }

            System.out.println(solicitud);
        }
        // Este mensaje es enviado por la replica al cliente
        if (message.contains("Respuesta")) {
            // Almacenando las respuestas en la cola
            queueRespuestas.put(message.trim());

            String respuesta = queueRespuestas.take();
            System.out.println("respuesta: " + respuesta);
            String[] splitRespuesta = respuesta.split(":");
            System.out.println("splitRespuesta: " + splitRespuesta[0]);
            
            if (splitRespuesta.length > 1) {
                int idCliente = Integer.parseInt(respuesta.split(":")[1].split("-")[0]);
                serverManager.sendMessageToClient(respuesta, idCliente);
                System.out.println(respuesta);
            }             
        }
    }

    public static void main(String[] args) {
        BlockingQueue<String> queueSolicitudes = new ArrayBlockingQueue<>(100);
        BlockingQueue<String> queueRespuestas = new ArrayBlockingQueue<>(100);
        new ServerBalanceador(queueSolicitudes, queueRespuestas).startServer();
    }
}
