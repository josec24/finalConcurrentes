package efinal;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

/**
 * Representa un cliente(productor) que realizara una solicitud de
 * actualizacion o lectura en el banco.
 */
public class Cliente implements Runnable {
    private static final int ACCOUNTS = 10000;
    private static final int DELAY = 10; // Tiempo de demora en milisegundos
    private static final double cantidadMax = 200; // Cantidad maxima por transferencia
    private static final int PORT = 4444;
    private Socket socket;
    private DataInputStream inputSocket;
    private DataOutputStream outputSocket;
    private int idCliente = 0;

    Cliente() {

        try {
            // obteniendo la ip del cliente
            InetAddress ip = InetAddress.getByName("localhost");
            // estableciendo la conexion con el servidor que usa el puerto 4444
            socket = new Socket(ip, PORT);
            this.inputSocket = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            this.outputSocket = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRequestToServer(String line) {
        try {
            outputSocket.writeUTF(line.trim());
            outputSocket.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startProducer() {
        try {
            String welcomeMessage = inputSocket.readUTF();
            if (welcomeMessage.contains("id")) {
                idCliente = Integer.parseInt(welcomeMessage.split(":")[1]);
            }

            System.out.println("" +
                    "-------------------\n" +
                    "Cliente " + idCliente + " conectado\n" +
                    "-------------------");
            while (!socket.isClosed()) {
                String line = inputSocket.readUTF();
                if (line.contains("Respuesta")) {
                    String resultado = line.split(":")[1];
                    System.out.println("Respuesta:" + resultado);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param min valor minimo del numero aleatorio que
     *            sera utilizado para la cuenta del cliente, de igual modo con max
     */
    public static int getRandom(int min, int max) {
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    /**
     * A cada cliente se le asigana un hilo para que realice
     * las solicitudes
     */
    @Override
    public void run() {
        try {
            // Cada cliente realizara como maximo 10 solicitudes cada 10 milisegundos
            for (int j = 1; j <= 1000; j++) {
                Thread.sleep(DELAY);
                //Main.solicitudNumber++;
                String request = "";
                // La mitad de solicitudes seran de actualizacion y la otra de lectura
                if (j % 2 == 0) {
                    // Solicitud de lectura de la cuenta: id cliente-L-2021;numero de cuenta
                    request = String.format("0%d-L-%04d;%d", idCliente, j, getRandom(0, 100));
                } else {
                    // Solicitud de actualizacion de la cuenta: id cliente-A-2021;de;para;dinero que sea transferido
                    request = String.format("0%d-A-%04d;%d;%d;%.2f", idCliente, j, getRandom(0, ACCOUNTS), getRandom(0, ACCOUNTS), Math.random() * cantidadMax);
                }
                // Envia la peticion al servidor
                this.sendRequestToServer("Solicitud:" + request);
                System.out.println("Solicitud:" + request);
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            outputSocket.close();
            inputSocket.close();
            socket.close();
        } catch (IOException iex) {
            System.out.println("Cliente, IOException close conection: " + iex);
        }
    }

    public static void main(String[] args) {
        Cliente cliente = new Cliente();

        // Envia las solicitudes al servidor distribuidor
        new Thread(cliente).start();
        // A la escucha de las respuestas
        new Thread(cliente::startProducer).start();

        String line = "";
        Scanner scanner = new Scanner(System.in);
        // La conexion sigue hasta que envia el comando exit
        while (!line.equals("exit")) {
            // Lee lo que ingresa el productor
            line = scanner.nextLine();
        }
        scanner.close();
        cliente.closeConnection();
    }
}