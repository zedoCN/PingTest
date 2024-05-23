package top.zedo.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Ping2 {
    InetAddress inet;
    private PingState state;
    private int time;
    private String ip;
    private String host;
    private int timeout;


    public int getTime() {
        return time;
    }

    public PingState getState() {
        return state;
    }

    public String getIP() {
        return ip;
    }

    public Ping2(String host, int timeout) {
        this.host = host;
        this.timeout = timeout;
    }

    public Ping2(String host) {
        this(host, 2000);
    }

    public int analysis() {
        try {
            inet = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            state = PingState.UNREACHABLE;
            return -1;
        }
        try {
            long t = System.currentTimeMillis();
            state = (inet.isReachable(timeout) ? PingState.SUCCESS : PingState.TIMED_OUT);
            ip = inet.getHostAddress();
            time = (state == PingState.SUCCESS ? (int) (System.currentTimeMillis() - t) : -1);
        } catch (IOException _) {
            state = PingState.UNREACHABLE;
        }
        return time;
    }
}
