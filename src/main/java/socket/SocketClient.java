package socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

public class SocketClient {

    public static AtomicInteger defaultTime = new AtomicInteger(0);

    public static void send(String message){
        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1",6666);
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(message);
            out.flush();

            System.out.println("server:" + in.readLine());
        } catch (IOException e) {
            defaultTime.addAndGet(1);
            e.printStackTrace();
        } finally {
            try {
                if(socket != null){
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) throws UnknownHostException {
        SocketClient.send("aaaaa");
    }

}
