package lab1a;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

//TODO Handle unresponsive server

public class GuessTheWordClient {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Wrong number of arguments. \nCorrect usage: java GuessTheWordClient XXX.XXX.XXX.XXX PORT");
            System.exit(1);
        }
        System.out.println("To start, type \"REQ\"");
        Scanner input = new Scanner(System.in);
        boolean running = true;
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(6542);
            socket.setSoTimeout(1000);
            InetAddress toAddr;
            try {
                toAddr = InetAddress.getByName(args[0]);
                String message = "";
                do {

                    System.out.print("Send message: ");
                    message = input.nextLine();
                    byte[] data = message.getBytes();
                    DatagramPacket pack = new DatagramPacket(
                            data, data.length, toAddr, Integer.parseInt(args[1])
                    );
                    socket.send(pack);

                    //Start listening for message
                    byte[] receivedData = new byte[4096];
                    DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);

                    socket.receive(receivedPacket);

                    String receivedMessage = new String(receivedPacket.getData(), 0, receivedPacket.getLength());

                    //System.out.println("From server: " + receivedMessage);
                    String extractedKeyWord = extractKeyword(receivedMessage);
                    String[] receiveMessageSplit = receivedMessage.split(" ");
                    switch (extractedKeyWord) { //TOTO REJ, LSS
                        case "BSY": { // Server is busy with other player, (not used now)
                            System.out.println("Server response: BSY");
                            running = false;
                            break;
                        }
                        case "RDY": { // Server responding to a REQ
                            System.out.println("Server response: RDY");
                            System.out.println("Server waiting for SRT");
                            break;
                        }
                        case "GME": { // Welcome message
                            System.out.println("Server response: GME");
                            System.out.println("Guess the word\n" +
                                    "Number of letters: " + receiveMessageSplit[1]);
                            break;
                        }
                        case "CUR": { // Current situation
                            System.out.println("Server response: CUR");
                            System.out.println("WORD: " + receiveMessageSplit[2]);
                            System.out.println("Number of guesses: " + receiveMessageSplit[1]);
                            break;
                        }
                        case "IVD": { // Invalid Command
                            System.out.println("Server response: IVD");
                            System.out.println("Invalid command" +
                                    ", Correct use is: GUE *");
                            break;
                        }
                        case "ERR": { // Time limit expired
                            System.out.println("Server response: ERR");
                            System.out.println("Invalid protocol usage" +
                                    ", Game session reset by server.");
                            running = false;
                            break;
                        }
                        case "WIN": { // Win message
                            System.out.println("Server response: WIN");
                            System.out.println("CONGRATULATIONS - You found the word!");
                            System.out.println("You got the answer in " + receiveMessageSplit[1] + " guesses.");
                            System.out.println(receiveMessageSplit[2]);
                            running = false;
                            break;
                        }
                        case "LSS": { // Loss message
                            System.out.println("Server response: LSS");
                            System.out.println("FAILED - You did not find the word.. :(");
                            System.out.println("Correct word was " + receiveMessageSplit[1]);
                            running = false;
                            break;
                        }
                    }

                } while (running);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (SocketTimeoutException ste) {
                System.err.println("No response");
                System.err.println("Closing Client");
            }
            catch (IOException ioe) {
                System.err.println("Unknown IOExeption");;
            }
        } catch (SocketException se) {
            System.err.println("Socket exception");
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    private static String extractKeyword(String s) {
        if (s.isEmpty()) {
            return "";
        } else if (!Character.isLetter(s.charAt(0))) {
            return "";
        } else {
            String[] result = s.split(" ", 2);
            return result[0];
        }
    }

}