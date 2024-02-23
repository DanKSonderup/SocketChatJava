package peer2peergui.nameserver;

public class Host {
    private String hostIp;
    private int serverPort;

    public Host(String hostIp, int serverPort) {
        this.hostIp = hostIp;
        this.serverPort = serverPort;
    }

    public String getHostIp() {
        return hostIp;
    }

    public int getServerPort() {
        return serverPort;
    }
}
