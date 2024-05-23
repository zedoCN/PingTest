package top.zedo.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class CMD {
    private final PrintWriter writer;
    private final BufferedReader reader;
    private final Process process;

    public static void main(String[] args) {
        CMD cmd = new CMD();
        cmd.exec("ping baidu.com");
        while (true) {
            System.out.println("> " + cmd.readLine());
        }
    }

    public CMD() {
        try {
            process = Runtime.getRuntime().exec(new String[]{"cmd"});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        writer = new PrintWriter(process.outputWriter(StandardCharsets.UTF_8));
        reader = process.inputReader(StandardCharsets.UTF_8);
        exec("@echo off");
        exec("chcp 65001");
        for (int i = 0; i < 6; i++) {
            readLine();
        }
    }

    public String readLine() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void exec(String cmd) {
        writer.println(cmd);
        writer.flush();
    }
}
