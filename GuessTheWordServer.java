package lab1a;

import java.io.IOException;
import java.net.*;

public class GuessTheWordServer {

    private String currentHostAddress;
    private String word;
    private int port;

    public GuessTheWordServer(String word, int port){
        this.word = word;
        this.port = port;
    }

    public void start(){
        DatagramSocket socket = null;
        try {
            System.out.println("Creating DatagramSocket...");
            socket = new DatagramSocket(port);

            boolean running = true;

            while(running){

                //Start listening for message
                byte[] data = new byte[4096];
                DatagramPacket datagramPacket = new DatagramPacket(data, data.length);
                try {
                    System.out.println("Listening for incoming packages...");
                    socket.receive(datagramPacket);
                    //Extract string
                    InetAddress clientAdress = datagramPacket.getAddress();
                    int clientPort = datagramPacket.getPort();
                    String message = new String(datagramPacket.getData(), 0, datagramPacket.getLength());

                    //Print message information
                    System.out.println("Package recieved:");
                    System.out.println("Adress: " + clientAdress.getHostAddress());
                    System.out.println("Port: " + clientPort);
                    System.out.println("Message: " + message);

                } catch (IOException e) {
                    System.err.println("DatagramSocket recieve error.");
                    break;
                }
            }


        } catch (SocketException e) {
            System.err.println("Unable to create datagram socket on port: " + port);
        }
        finally{
            if(socket != null){
                socket.close();
            }
        }
    }

    public static void main(String[] args) {
        if(args.length != 1){
            System.out.println("You need to enter a word.");
            System.out.println("Usage: java guessthewordserver [word]");
            System.exit(1);
        }

        System.out.println("Server: Word set to: " + args[0]);

        int port = 6543;

        GuessTheWordServer server = new GuessTheWordServer(args[0], port);
        server.start();
    }
}
