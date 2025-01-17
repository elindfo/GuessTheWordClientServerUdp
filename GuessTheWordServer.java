package lab1a;

import java.io.IOException;
import java.net.*;

public class GuessTheWordServer {

    private Client currentClient;
    private int port;
    private boolean isRunning;
    private ServerState serverState;

    private String word;
    private String expectedKeyword;
    private int noOfGuesses;
    private char[] correctLettersGuessed;

    private int maxNrOfGuesses;

    private static final int TIMEOUT_IN_SECONDS = 10;

    public GuessTheWordServer(String word, int port){
        this.word = word.toUpperCase();
        this.maxNrOfGuesses = word.length() * 2;
        this.port = port;
        reset();
    }

    public void start(){

        System.out.println("Current Word: " + word);

        isRunning = true;

        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(port);
            int noOfPackages = 0;
            while(isRunning){

                //Start listening for message
                byte[] data = new byte[4096];
                DatagramPacket datagramPacket = new DatagramPacket(data, data.length);
                long currentTime = 0;
                try {
                    //RECIEVE UDP PACKAGE
                    socket.receive(datagramPacket);

                    //SAVE TIMESTAMP
                    currentTime = System.currentTimeMillis();

                    //EXTRACT MESSAGE
                    String message = new String(datagramPacket.getData(), 0, datagramPacket.getLength()).toUpperCase();
                    String keyword = extractKeyword(message);

                    //EXTRACT CLIENT DATA
                    InetAddress clientAddress = datagramPacket.getAddress();
                    int clientPort = datagramPacket.getPort();

                    System.out.println("\n------INFO------");
                    System.out.printf("%-10s : %s\n", "Pno", ++noOfPackages);
                    System.out.printf("%-10s : %s\n", "IP", clientAddress.getHostAddress());
                    System.out.printf("%-10s : %s\n", "Port", clientPort);

                    if(serverState == ServerState.BUSY){
                        long timeDifference = currentTime - currentClient.getTimeOfCurrentDatagram();
                        if(timeDifference > TIMEOUT_IN_SECONDS * 1000){
                            System.out.printf("%-10s : %s\n", "Timer", "EXPIRED - Difference:" + timeDifference + "ms");
                            reset();
                        }
                        else{
                            if(currentClient.getInetAddress().getHostAddress().equals(clientAddress.getHostAddress())){
                                currentClient.setTimeOfCurrentDatagram(currentTime);
                                System.out.printf("%-10s : %s\n", "Timer", "UPDATED - Difference:" + timeDifference + "ms");
                            }
                            else{
                                System.out.printf("%-10s : %s\n", "Timer", "RUNNING - Difference:" + timeDifference + "ms");
                            }
                        }
                    }
                    System.out.printf("%-10s : %s\n", "State", serverState);
                    if(expectedKeyword.equals("GUE")){
                        System.out.printf("%-10s : %s\n", "Expected", expectedKeyword + " [letter]");
                    }
                    else{
                        System.out.printf("%-10s : %s\n", "Expected", expectedKeyword);
                    }
                    System.out.printf("%-10s : %s\n", "Recieved", message);

                    //CHECK IF NEW CLIENT
                    switch(serverState){
                        case READY: {
                            currentClient = new Client(clientPort, clientAddress);
                            if(expectedKeyword.equals(keyword) && message.split(" ").length == 1){
                                sendToClient("RDY", currentClient, socket);
                                serverState = ServerState.BUSY;
                                expectedKeyword = "SRT";
                                currentClient.setTimeOfCurrentDatagram(currentTime);
                                System.out.printf("%-10s : %s\n", "Status", "Client CONNECTED");

                            }
                            else{
                                sendToClient("ERR", currentClient, socket);
                                currentClient = null;
                                System.out.printf("%-10s : %s\n", "Status", "Invalid message received");

                            }
                            break;
                        }
                        case BUSY: {
                            if(!currentClient.getInetAddress().getHostAddress().equals(clientAddress.getHostAddress())){
                                sendToClient("BSY", new Client(
                                        datagramPacket.getPort(),
                                        clientAddress), socket);
                                System.out.printf("%-10s : %s\n", "Status", "Client REJECTED");
                                break;
                            }
                            else{
                                //CONTINUE CONVERSATION
                                if(expectedKeyword.equals(keyword)){
                                    switch(expectedKeyword){
                                        case "SRT": {
                                            sendToClient("GME " + word.length(), currentClient, socket);
                                            expectedKeyword = "GUE";
                                            System.out.printf("%-10s : %s\n", "Status", "Game STARTED - Word length sent to client");
                                            break;
                                        }
                                        case "GUE": {
                                            //Check if right format
                                            String[] splitMessage = message.split(" ");
                                            if(splitMessage.length != 2){
                                                sendToClient("IVD", currentClient, socket);
                                                System.out.printf("%-10s : %s\n", "Status", "Invalid character recieved");
                                            }
                                            else{
                                                if(Character.isLetter(splitMessage[1].charAt(0)) && splitMessage[1].length() == 1){
                                                    updateLetters(splitMessage[1].charAt(0));
                                                    noOfGuesses++;
                                                    if(hasAllLettersFound()){
                                                        sendToClient("WIN " + noOfGuesses + " " + word, currentClient, socket);
                                                        System.out.printf("%-10s : %s\n", "Status", "Game Over: WIN");
                                                        reset();
                                                    }
                                                    else if(noOfGuesses >= maxNrOfGuesses){
                                                        sendToClient("LSS " + word, currentClient, socket);
                                                        System.out.printf("%-10s : %s\n", "Status", "Game Over: LOSS");
                                                        reset();
                                                    }
                                                    else{
                                                        sendToClient("CUR " + noOfGuesses + " " + getFoundLettersAsString(), currentClient, socket);
                                                        System.out.printf("%-10s : %s\n", "Status", "Playing - Characters found: " + getFoundLettersAsString());
                                                    }
                                                }
                                                else{
                                                    sendToClient("IVD", currentClient, socket);
                                                    System.out.printf("%-10s : %s\n", "Status", "Invalid character recieved");
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }
                                else{
                                    //Drop client and reset since wrong keyword was sent
                                    sendToClient("ERR", currentClient, socket);
                                    System.out.printf("%-10s : %s\n", "Status", "Wrong keyword recieved. Client dropped.");
                                    reset();
                                }
                            }
                        }
                    }
                    System.out.printf("%-10s : %s\n", "NoGuesses", noOfGuesses);
                    System.out.printf("%-10s : %s\n", "Word", word);

                    if(currentClient != null) {
                        System.out.printf("%-10s : %s\n", "Connected", currentClient.getInetAddress().getHostAddress());
                    }
                    else{
                        System.out.printf("%-10s : %s\n", "Connected", "not connected");
                    }
                    System.out.println("----------------");
                } catch (IOException e) {
                    System.err.println("Socket error");
                    reset();
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

    public boolean isRunning(){
        return isRunning;
    }

    private void reset(){
        serverState = ServerState.READY;
        expectedKeyword = "REQ";
        noOfGuesses = 0;
        correctLettersGuessed = new char[word.length()];
        currentClient = null;
    }

    private boolean updateLetters(char letter){
        for(int i = 0; i < correctLettersGuessed.length; i++){
            if(letter == correctLettersGuessed[i]){
                return false;
            }
        }
        boolean foundNewLetter = false;
        for(int i = 0; i < word.length(); i++){
            if(letter == word.charAt(i)){
                correctLettersGuessed[i] = letter;
                foundNewLetter = true;
            }
        }
        return foundNewLetter;
    }

    private boolean hasAllLettersFound(){
        for(int i = 0; i < word.length(); i++){
            if(word.charAt(i) != correctLettersGuessed[i]){
                return false;
            }
        }
        return true;
    }

    private String getFoundLettersAsString(){
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < word.length(); i++){
            if(word.charAt(i) == correctLettersGuessed[i]){
                sb.append(word.charAt(i));
            }
            else{
                sb.append("*");
            }
        }
        return sb.toString();
    }

    private void sendToClient(String text, Client currentClient, DatagramSocket socket) throws IOException{
        byte[] bytes = text.getBytes();
        DatagramPacket pkt = new DatagramPacket(
                bytes,
                bytes.length,
                currentClient.getInetAddress(),
                currentClient.getPort()
        );
        socket.send(pkt);
    }

    private String extractKeyword(String s){
        if(s.isEmpty()){
            return "";
        }
        else if(!Character.isLetter(s.charAt(0))){
            return "";
        }
        else{
            String[] result =  s.split(" ", 2);
            return result[0];
        }
    }

    public static void main(String[] args) {
        if(args.length != 1){
            System.out.println("You need to enter a word.");
            System.out.println("Usage: java guessthewordserver [word]");
            System.exit(1);
        }

        int port = 6543;

        GuessTheWordServer server = new GuessTheWordServer(args[0], port);
        server.start();
    }
}
