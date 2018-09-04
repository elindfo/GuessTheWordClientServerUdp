package lab1a;

import java.io.IOException;
import java.net.*;

//Fel, klienten som spelar börjar prata strunt. Klienten ska bort och servern blir ledig
//Fel, En ny klient kommer och att man inte uppfattar att det är en annan klient

//TODO Take action if wrong message arrives, e.g. terminate session or request a new message
//TODO Handle timeout from client with System.getCurrentTimeMillis(). Reset server state.
//TODO If new client sends anything other than
//TODO Handle exceptions
//TODO Check if REJ in state BUSY works

public class GuessTheWordServer {

    private Client currentClient;
    private int port;
    private boolean isRunning;
    private ServerState serverState;

    private String word;
    private String expectedKeyword;
    private int noOfGuesses;
    private char[] correctLettersGuessed;

    private static final int MAX_NO_OF_GUESSES = 10;

    public GuessTheWordServer(String word, int port){
        this.word = word.toUpperCase();
        this.port = port;
        reset();
    }

    public void start(){

        System.out.println("Current Word: " + word);

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

                    //EXTRACT MESSAGE
                    String message = new String(datagramPacket.getData(), 0, datagramPacket.getLength()).toUpperCase();
                    String keyword = extractKeyword(message);

                    //EXTRACT CLIENT DATA
                    InetAddress clientAddress = datagramPacket.getAddress();
                    int clientPort = datagramPacket.getPort();

                    //CHECK IF NEW CLIENT
                    switch(serverState){
                        case READY: {
                            currentClient = new Client(clientPort, clientAddress);
                            System.out.println("case READY: noOfWords="+message.split(" ").length);
                            if(expectedKeyword.equals(keyword) && message.split(" ").length == 1){
                                sendToClient("RDY", currentClient, socket);
                                serverState = ServerState.BUSY;
                                expectedKeyword = "SRT";
                            }
                            else{
                                System.out.println("case READY: else");
                                sendToClient("ERR", currentClient, socket);
                                currentClient = null;
                            }
                            break;
                        }
                        case BUSY: {
                            if(!currentClient.getInetAddress().getHostAddress().equals(clientAddress.getHostAddress())){
                                sendToClient("REJ", new Client(
                                        datagramPacket.getPort(),
                                        clientAddress), socket);
                                break;
                            }
                            else{
                                //CONTINUE CONVERSATION
                                if(expectedKeyword.equals(keyword)){
                                    switch(expectedKeyword){
                                        case "SRT": {
                                            sendToClient("GME " + word.length(), currentClient, socket);
                                            expectedKeyword = "GUE";
                                            break;
                                        }
                                        case "GUE": {
                                            //Check if right format
                                            String[] splitMessage = message.split(" ");
                                            if(splitMessage.length != 2){
                                                sendToClient("Invalid character", currentClient, socket);
                                            }
                                            else{
                                                if(Character.isLetter(splitMessage[1].charAt(0)) && splitMessage[1].length() == 1){
                                                    updateLetters(splitMessage[1].charAt(0));
                                                    if(hasAllLettersFound()){
                                                        StringBuffer sb = new StringBuffer("CONGRATULATIONS - You found the word!");
                                                        sb.append("\n");
                                                        sb.append("Word: ");
                                                        sb.append(word);
                                                        sendToClient(sb.toString(), currentClient, socket);
                                                        reset();
                                                    }
                                                    else{
                                                        sendToClient(getFoundLettersAsString(), currentClient, socket);
                                                    }

                                                }
                                                else{
                                                    sendToClient("Invalid character", currentClient, socket);
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }
                                else{
                                    //Drop client and reset since wrong keyword was sent
                                    System.out.println("Drop client...");
                                    sendToClient("ERR", currentClient, socket);
                                    reset();
                                }
                            }
                        }
                    }
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
