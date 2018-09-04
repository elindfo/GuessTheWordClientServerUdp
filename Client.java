package lab1a;

import java.net.InetAddress;

public class Client {

    private int port;
    private InetAddress inetAddress;

    public Client(int port, InetAddress inetAddress){
        this.port = port;
        this.inetAddress = inetAddress;
    }

    public int getPort() {
        return port;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }
}
