package lab1a;

import java.io.IOException;
import java.net.*;

public class GuessTheWordServer {

    //Fel, klienten som spelar börjar prata strunt. Klienten ska bort och servern blir ledig
    //Fel, En ny klient kommer och att man inte uppfattar att det är en annan klient


    private Client currentClient;
    private String word;
    private int port;
    private boolean isRunning;
    private ServerState serverState;

    public GuessTheWordServer(String word, int port){
        this.word = word;
        this.port = port;
        currentClient = null;
        isRunning = false;
        serverState = ServerState.READY;
    }

    public void start(){

        isRunning = true;

        DatagramSocket socket = null;
        try {
            System.out.println("Creating DatagramSocket...");
            socket = new DatagramSocket(port);

            while(isRunning){

                //Start listening for message
                byte[] data = new byte[4096];
                DatagramPacket datagramPacket = new DatagramPacket(data, data.length);
                try {
                    //RECIEVE UDP PACKAGE
                    System.out.println("Listening for incoming packages...");
                    socket.receive(datagramPacket);

                    //EXTRACT CLIENT DATA
                    InetAddress clientAddress = datagramPacket.getAddress();
                    int clientPort = datagramPacket.getPort();

                    //CHECK IF NEW CLIENT
                    switch(serverState){
                        case READY: {
                            System.out.println("READY");
                            currentClient = new Client(clientPort, clientAddress.getHostAddress());
                            serverState = ServerState.BUSY;
                        }
                        case BUSY: {
                            System.out.println("BUSY");
                            if(!currentClient.getIpAddress().equals(clientAddress.getHostAddress())){
                                System.out.println("SetIp: " + currentClient.getIpAddress());
                                System.out.println("ConnectedIp: " + clientAddress.getHostAddress());
                                System.out.println("WRONG CLIENT");
                                //WRONG CLIENT, REJECT
                                break;
                            }
                            else{
                                //CONTINUE CONVERSATION
                                //Extract Message
                                String message = new String(datagramPacket.getData(), 0, datagramPacket.getLength());

                                //Print message information
                                System.out.println("Package recieved:");
                                System.out.println("Adress: " + clientAddress.getHostAddress());
                                System.out.println("Port: " + clientPort);
                                System.out.println("Message: " + message);
                            }
                        }
                    }
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
