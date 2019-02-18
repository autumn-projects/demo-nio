package reactor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    public static void main(String[] args) throws UnknownHostException {
        String hostName = InetAddress.getLocalHost().toString();
        int port = 6666;

        try {
            Socket client = new Socket("127.0.0.1", port);
            System.out.println("Connected to " + InetAddress.getLocalHost().toString());

            PrintWriter out = new PrintWriter(client.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

            String input;

            while ((input = stdIn.readLine()) != null) {
                out.println(input);
                out.flush();

                if (input.equals("exit")) {
                    break;
                }
                System.out.println("server:" + in.readLine());
            }
            client.close();
            System.out.println("client stop");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
