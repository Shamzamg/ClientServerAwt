import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.*;

public class Client {

    private JFrame jf;
    private String username;

    private TextArea chatTextArea;
    private String chatText = null;
    private Label errorLabel = null;


    private static InetAddress address;
    private static DatagramSocket socket;
    private static int port;
    private static boolean connected = false;


    public Client(){
        jf = new JFrame("Client");
        jf.setResizable(false);

        jf.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (connected) {
                    disconnect();
                }
                System.exit(0);
            }
        });
    }

    private void setErrorLabel(String error){
        if(errorLabel != null){
            jf.remove(errorLabel);
        }
        errorLabel = new Label(error);
        errorLabel.setForeground(Color.RED);
        errorLabel.setBounds(20,20, 380,30);
        jf.add(errorLabel);
    }

    private void connectEvent(TextField ipText, TextField portText, TextField usernameText){
        if(!ipText.getText().isEmpty()){
            if(!portText.getText().isEmpty()){
                if(!usernameText.getText().isEmpty()){
                    //there we can connect
                    try {
                        //initialize the connection with the server
                        System.out.println("address: " + ipText.getText().toString());
                        address = InetAddress.getByName(ipText.getText().toString());
                        port = Integer.valueOf(portText.getText());
                        username = usernameText.getText();
                    } catch (UnknownHostException e) {
                        setErrorLabel("IP or PORT is incorrect ! Input a real address.");
                        e.printStackTrace();
                    }
                    try {
                        connect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else{
                    setErrorLabel("USERNAME should not be blank !");
                    return;
                }
            }else{
                setErrorLabel("PORT should not be blank !");
                return;
            }
        } else{
            setErrorLabel("IP should not be blank !");
            return;
        }
    }

    private void connect() throws IOException {

        socket = new DatagramSocket();
        //we send a first message to be known from the server
        byte[] requestBuffer = username.getBytes();
        DatagramPacket request = new DatagramPacket(requestBuffer, requestBuffer.length, address, port);
        socket.send(request);

        //we then wait for his response
        byte[] connectionResponseBuffer = new byte[512];

        DatagramPacket connectionResponse = new DatagramPacket(connectionResponseBuffer, connectionResponseBuffer.length);
        socket.receive(connectionResponse);

        String connectionMessage = new String(connectionResponseBuffer, 0, connectionResponse.getLength());

        if(connectionMessage.contains("Maximum number of clients reached")){
            setErrorLabel(connectionMessage);
            return;
        }

        //if we receive a new communication port
        if(connectionMessage.contains("^^!-°)°6§è+=4-°%communication")){
            String [] s = connectionMessage.split("/");
            port = Integer.parseInt(s[1]);
            System.out.println("Connected on port: " + port);
            connected = true;
        }

        connectedInterface();

        //if all ok
        Thread receive = new Thread(() -> {
            try {
                while(connected){

                    byte[] responseBuffer = new byte[512];

                    DatagramPacket response = new DatagramPacket(responseBuffer, responseBuffer.length);
                    socket.receive(response);

                    String message = new String(responseBuffer, 0, response.getLength());

                    if(!(chatText == null)){
                        chatText = chatText + '\n' + message;
                    } else{
                        chatText = message;
                    }
                    chatTextArea.setText(chatText);

                    jf.validate();
                }
            } catch (IOException e) {
                System.out.println("Socket communication closed");
            }
        });
        receive.start();
    }

    private void disconnect(){
        if(sendMessage("/quit")){
            address = null;
            port = -9999;
            username = null;
            connected = false;
            chatText = null;
            chatTextArea = null;
            socket.close();
        }
    }

    private boolean sendMessage(String message){
        try{
            byte[] sendBuffer;
            sendBuffer = message.getBytes();

            DatagramPacket out = new DatagramPacket(sendBuffer, sendBuffer.length, address, port);
            socket.send(out);

        } catch(IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void connectedInterface(){

        //clean everything
        jf.getContentPane().removeAll();

        chatTextArea = new TextArea();
        chatTextArea.setEditable(false);
        chatTextArea.setFont(new Font("Serif", Font.ITALIC, 16));
        chatTextArea.setBounds(40,20, 320,280);

        TextField messageText = new TextField();
        messageText.setText("say something to start ...");
        messageText.setBounds(40,310, 200,30);

        Button sendButton = new Button("SEND");
        sendButton.setBounds(260,310,120,30);
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!messageText.getText().isEmpty()){
                    sendMessage(messageText.getText());
                    messageText.setText("");
                }
            }
        });

        Button disconnectButton = new Button("DISCONNECT");
        disconnectButton.setBounds(260,340,120,30);
        disconnectButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                //tf.setText("Welcome to Javatpoint.");
                disconnect();
                setupInterface();
            }
        });

        jf.add(chatTextArea); jf.add(messageText);
        jf.add(disconnectButton); jf.add(sendButton);

        jf.validate();
    }

    public void setupInterface(){

        //clean everything
        jf.getContentPane().removeAll();

        Label ipLabel = new Label("IP: ");
        ipLabel.setBounds(20,90, 20,30);
        TextField ipText = new TextField();
        ipText.setBounds(40,90, 150,30);

        Label portLabel = new Label("PORT: ");
        portLabel.setBounds(250,90, 50,30);
        TextField portText = new TextField();
        portText.setBounds(300,90, 50,30);

        Label usernameLabel = new Label("USERNAME: ");
        usernameLabel.setBounds(20,210, 100,30);
        TextField usernameText = new TextField();
        usernameText.setBounds(120,210, 150,30);

        Button connectButton = new Button("CONNECT");
        connectButton.setBounds(260,310,120,30);
        connectButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                //tf.setText("Welcome to Javatpoint.");
                connectEvent(ipText, portText, usernameText);
            }
        });

        jf.add(ipLabel); jf.add(portLabel); jf.add(usernameLabel);
        jf.add(ipText); jf.add(portText); jf.add(usernameText);

        jf.add(connectButton);

        jf.setSize(400,400);
        jf.setLayout(null);
        jf.setVisible(true);
        jf.validate();
    }

}
