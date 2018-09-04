package lab1a;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

//TODO Handle unresponsive server

public class GuessTheWordClient {

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);

        DatagramSocket socket = null;
        try{
            socket = new DatagramSocket(6542);

            InetAddress toAddr;
            try {
                toAddr = InetAddress.getByName("localhost");
                String message = "";
                do {
                    System.out.print("Send message: ");
                    message = input.nextLine();
                    byte[] data = message.getBytes();
                    DatagramPacket pack = new DatagramPacket(
                            data, data.length, toAddr, 6543
                    );
                    socket.send(pack);

                    //Start listening for message
                    byte[] receivedData = new byte[4096];
                    DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);

                    socket.receive(receivedPacket);
                    String receivedMessage = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
                    System.out.println("From server: " + receivedMessage);
                }while(!message.equals("exit"));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException ioe){
                System.err.println("Unable to send packet");
            }
        }catch(SocketException se){
            System.err.println("Socket exception");
        }
        finally{
            if(socket != null){
                socket.close();
            }
        }
    }
}
