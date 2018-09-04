package lab1a;

import java.io.IOException;
import java.net.*;

public class GuessTheWordClient {

    public static void main(String[] args) {

        DatagramSocket socket = null;
        try{
            socket = new DatagramSocket(6542);

            InetAddress toAddr;
            try {
                toAddr = InetAddress.getByName("localhost");

                String message = "Test message sent from client";
                byte[] data = message.getBytes();
                DatagramPacket pack = new DatagramPacket(
                        data, data.length, toAddr, 6543
                );
                socket.send(pack);
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
