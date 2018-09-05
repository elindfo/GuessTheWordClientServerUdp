package lab1a;

import java.net.InetAddress;

public class Client {

    private int port;
    private InetAddress inetAddress;
    private long timeOfCurrentDatagram;

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

    public long getTimeOfCurrentDatagram() {
        return timeOfCurrentDatagram;
    }

    public void setTimeOfCurrentDatagram(long timeOfCurrentDatagram) {
        this.timeOfCurrentDatagram = timeOfCurrentDatagram;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer("Client[");
        sb.append("ip:");
        sb.append(inetAddress.getHostAddress());
        sb.append(", port:");
        sb.append(port);
        sb.append("]");
        return sb.toString();
    }
}
