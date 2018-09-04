package lab1a;

public class Client {

    private int port;
    private String ipAddress;

    public Client(int port, String ipAddress){
        this.port = port;
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
