package socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class Server {

    static ThreadPoolExecutor pool = new ThreadPoolExecutor(5, 100, 100, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    // 端口号
    private static int DEFAULT_PORT = 6666;

    // 单例的ServerSocket
    private static ServerSocket serverSocket;

    //
    public static void start() throws IOException {
        //
        start(DEFAULT_PORT);
    }

    public synchronized static void start(int port) throws IOException {
        if (serverSocket != null) return;

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("服务端已启动，端口号:" + port);

            while (true) {
                Socket socket = serverSocket.accept();
                pool.execute(new ServerHandler(socket));
            }
        } finally {
            if (serverSocket != null) {
                System.out.println("服务端已关闭");
                serverSocket.close();
                serverSocket = null;
            }
        }
    }
}
