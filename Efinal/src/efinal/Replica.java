package efinal;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

/**
 * Representa un servidor replicado, se encargara de realizar
 * la operacion asignada
 */
public class Replica {
    /**
     * @param request solicitud de lectura del dinero almacenado en el servidor
     */
    private static final int ACCOUNTS = 10000;
    private static final int PORT = 4444;
    private Socket socket;
    private DataInputStream inputSocket;
    private DataOutputStream outputSocket;
    private int idReplica = 0;
    private int send = 0;
    private final String pathDirectoryFile = "C:\\Users\\STUART\\Desktop\\Concurrencia\\finalConcurrentes\\account";

    public Replica() {
        try {
            // obteniendo la ip del servidor replica
            InetAddress ip = InetAddress.getByName("localhost");
            // estableciendo la conexion con el servidor que usa el puerto 4444
            socket = new Socket(ip, PORT);
            this.inputSocket = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            this.outputSocket = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendResultToServer(String line) {
        try {
            outputSocket.writeUTF("Respuesta:" + line.trim());
            outputSocket.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String lectura(String request) {
        String respuesta = "";
        try {
            // C:\Users\STUART\Desktop\Concurrencia\finalConcurrentes
            File fileAccounts = new File(pathDirectoryFile + ".txt");
            Scanner in = new Scanner(new FileInputStream(fileAccounts));
            while (in.hasNextLine()) {
                String[] lineaFile = in.nextLine().split(",");
                String[] lineaRequest = request.split(";");
                // compara si el codigo de la cuenta es igual al codigo de la solicitud
                if (lineaRequest[1].equals(lineaFile[0])) {
                    respuesta = request + ";" + lineaFile[1];
                    break;
                }
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return respuesta;
    }

    /**
     * @param request solicitud de actualizacion del dinero almacenado en el servidor
     */
    public String transferencia(String request) {
        String respuesta = "";
        try {
            // descompone la solicitud para la obtencion de parametros
            String[] req = request.split(";");
            String idSolicitud = req[0];

            int idFrom = Integer.parseInt(req[1]);
            int idTo = Integer.parseInt(req[2]);
            double money = Double.parseDouble(req[3]);
            this.send = Integer.parseInt(req[4]);

            // Lectura y almacenamiento del archivo en el arreglo moneyOfAccounts
            BufferedReader file = new BufferedReader(new FileReader(pathDirectoryFile + idReplica + ".txt"));
            double[] moneyOfAccounts = new double[ACCOUNTS];
            int count = 0;

            String line;
            while ((line = file.readLine()) != null) {
                String[] account = line.split(",");
                moneyOfAccounts[count] = Double.parseDouble(account[1]);
                count++;
            }
            file.close();

            // compara si hay dinero suficiente para la transaccion
            if (moneyOfAccounts[idFrom] >= money) {
                moneyOfAccounts[idFrom] -= money;
                moneyOfAccounts[idTo] += money;
                respuesta = String.format("%s;%d;%.2f;%d;%.2f", idSolicitud, idFrom, moneyOfAccounts[idFrom], idTo, moneyOfAccounts[idTo]);
            } else {
                respuesta = idSolicitud + ";sin dinero suficiente para la transaccion";
            }

            // reescribe el archivo completo con el contenido del arreglo moneyOfAccounts
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < count; i++) {
                buffer.append(String.format("%d,%.2f\n", i, moneyOfAccounts[i]));
            }

            FileOutputStream fileOut = new FileOutputStream(pathDirectoryFile + idReplica+".txt");
            fileOut.write(buffer.toString().getBytes());
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return respuesta.trim();
    }

    public void startConsumer() {
        try {
            String welcomeMessage = inputSocket.readUTF();
            if (welcomeMessage.contains("id")) {
                idReplica = Integer.parseInt(welcomeMessage.split(":")[1]);
            }

            System.out.println("" +
                    "----------------------------\n" +
                    "Servidor replica " + idReplica + " conectado\n" +
                    "----------------------------");
            // Duplicar archivo replica

            while (!socket.isClosed()) {
                String line = inputSocket.readUTF();
                if (line.contains("Solicitud")) {
                    String respuesta;
                    String solicitud = line.split(":")[1];
                    if (solicitud.contains("A")) {
                        respuesta = transferencia(solicitud);
                        System.out.println("Transferencia realizada.");
                    } else {
                        respuesta = lectura(solicitud);
                        System.out.println("Lectura realizada.");
                    }
                    // Todos actualizan pero solo envia el mensaje de respuesta el primer servidor replica
                    if (send == 0) {
                        this.sendResultToServer(respuesta.trim());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            outputSocket.close();
            inputSocket.close();
            socket.close();
        } catch (IOException iex) {
            System.out.println("Productor, IOException close conection: " + iex);
        }
    }

    public static void main(String[] args) {
        Replica replica = new Replica();
        new Thread(replica::startConsumer).start();
        // Lee el mensaje de la entrada del servidor replica, exit para salir
        String line = "";
        Scanner scanner = new Scanner(System.in);
        // La conexion sigue hasta que envia el comando exit
        while (!line.equals("exit")) {
            line = scanner.nextLine();
        }
        scanner.close();
        replica.closeConnection();
    }
}