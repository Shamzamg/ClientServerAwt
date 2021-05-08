import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static java.lang.Thread.sleep;

public class Com{

    private DatagramSocket socket;
    private InetAddress clientAddress;
    private int clientPort;
    private String clientUsername;
    private static Server server;
    private int comPort;
    private boolean communicate;
    private long threadPause;

    public Com(InetAddress address, int portClient, String username, int cPort){
        try{
            clientAddress = address;
            clientPort = portClient;
            clientUsername = username;
            comPort = cPort;
            socket = new DatagramSocket(comPort);
            communicate = true;
        } catch(IOException ex){
            System.out.println("Client error: " + ex.getMessage() + " address:" + clientAddress);
            ex.printStackTrace();
        }
    }

    public void start(long _pause, Server srv) {
        server = srv;

        //once we start, we tell the Client our address
        String comMsg = "^^!-°)°6§è+=4-°%communication/" + comPort;
        byte[] buff = comMsg.getBytes();

        threadPause = _pause;

        DatagramPacket startMessage = new DatagramPacket(buff, buff.length, clientAddress, clientPort);

        try{
            socket.send(startMessage);
        } catch(IOException ex){
            System.out.println("Client error: " + ex.getMessage() + " address:" + clientAddress);
            ex.printStackTrace();
        }

        Thread communication = new Thread(() -> {
            try {
                while(communicate){
                    byte[] buffer = new byte[512];
                    DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                    socket.receive(response);

                    String message = new String(buffer, 0, response.getLength());

                    //if Client wants to disconnect
                    if(message.contains("/quit")){
                        //we then stop the connection
                        System.out.println(server);
                        socket.close();
                        server.delete(this);
                        communicate = false;
                    } else {
                        message = clientUsername + "€€é&/:ù%" + message;
                        byte[] pongBuffer = message.getBytes();

                        //we pong at every client known from the server
                        for(Com c: server.getComList()){
                            InetAddress cAddress = c.getClientAddress();
                            int cPort = c.getClientPort();
                            DatagramPacket pongResponse = new DatagramPacket(pongBuffer, pongBuffer.length, cAddress, cPort);
                            socket.send(pongResponse);
                        }

                    }
                    sleep(threadPause);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        communication.start();

    }

    public int getComPort(){
        return comPort;
    }

    public InetAddress getClientAddress(){ return clientAddress; }

    public int getClientPort(){ return clientPort; }

}
