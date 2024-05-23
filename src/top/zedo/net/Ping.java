package top.zedo.net;

public class Ping {
    private final CMD cmd = new CMD();
    private PingState state;
    private int time;
    private String ip;
    private int ttl;


    public PingState getState() {
        return state;
    }

    public static void main(String[] args) {
        Ping ping = new Ping("baidu.com");
        while (true) {
            ping.analysis();
            System.out.println(ping.getTime());
        }
    }

    public int getTime() {
        return time;
    }

    public String getIP() {
        return ip;
    }

    public int getTTL() {
        return ttl;
    }

    public void analysis() {
        String msg = cmd.readLine();
        if (msg == null) {
            throw new RuntimeException();
        } else if (msg.contains("Reply from")) {
            //Reply from 49.70.151.137: bytes=32 time=4ms TTL=62
            state = PingState.SUCCESS;
            int i;
            String s;

            i = msg.indexOf("from ") + "from ".length();
            s = msg.substring(i, msg.indexOf(":"));
            ip = s;

            i = msg.indexOf("time=") + "time=".length();
            s = msg.substring(i, msg.indexOf("ms TTL"));
            time = Integer.parseInt(s);

            i = msg.indexOf("TTL=") + "TTL=".length();
            s = msg.substring(i);
            ttl = Integer.parseInt(s);
            return;
        } else if (msg.contains("Request timed out")) {
            //Request timed out.
            state = PingState.TIMED_OUT;
        } else if (msg.contains("Destination host unreachable")) {
            //Destination host unreachable - No routes to the destination.
            state = PingState.UNREACHABLE;
        }
        ip = null;
        time = -1;
        ttl = -1;
    }

    public Ping(String host) {
        cmd.exec("ping " + host + " -t");
        for (int i = 0; i < 3; i++) {
            cmd.readLine();
        }
    }
}
