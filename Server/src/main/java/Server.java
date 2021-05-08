import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server{

    private DatagramSocket socket;
    private static int maxCom = 2;
    ArrayList<Com> comList = new ArrayList<>();
    ArrayList<Integer> comPortList = new ArrayList<>();
    private int comPortMin = 15000;
    private int comPort = 15000;

    public Server(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Syntax: Server <port> <maxClients>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        maxCom = Integer.parseInt(args[1]);

        try {
            Server server = new Server(port);
            server.start();
        } catch (SocketException ex) {
            System.out.println("Socket error: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }

    private void addCom(InetAddress clientAddress, int clientPort, String clientUsername, int port){
        Com com = new Com(clientAddress, clientPort, clientUsername, port);
        comList.add(com);
        comPortList.add(port);
        com.start(200, this);

        System.out.println("New used port : " + port);
    }

    private void start() throws IOException {
        while (true) {
            byte[] buff = new byte[512];
            DatagramPacket request = new DatagramPacket(buff, buff.length);
            socket.receive(request);

            String clientUsername = new String(buff, 0, request.getLength());

            System.out.println("first request; address: " + request.getAddress() + "; port: " + request.getPort());

            InetAddress clientAddress = request.getAddress();
            int clientPort = request.getPort();

            //On cherche un port disponsible
            boolean found = false;
            for(int i=comPortMin;i<comPortMin + maxCom;i++){
                if (!(comPortList.contains(i)) && (comPortList.size() < maxCom)){
                    addCom(clientAddress, clientPort, clientUsername, i);
                    found = true;
                    break;
                }
            }
            //si on n'en trouve pas
            if(!found){
                String quote = "Maximum number of clients reached, try again later ...";
                byte[] buffer = quote.getBytes();

                DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
                socket.send(response);
                System.out.println("Connection failed: too much clients connected.");
            }

            System.out.println("ComPortList: " + comPortList);
        }
    }

    public ArrayList<Com> getComList(){
        return comList;
    }

    public void delete(Com c) {
        System.out.println("Port libéré: " + c.getComPort());
        comPortList.remove(Integer.valueOf(c.getComPort()));
        comList.remove(c);
    }

}