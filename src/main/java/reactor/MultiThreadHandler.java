package reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MultiThreadHandler implements Runnable {

    private final SelectionKey sk;
    private final SocketChannel sc;
    private static final int READING = 1;
    private static final int SENDING = 2;
    private static final int PROCESSING = 3;
    private int state = READING;

    // 初始化一个线程池
    static ThreadPoolExecutor pool = new ThreadPoolExecutor(8, 8, 100, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    public MultiThreadHandler(SelectionKey selectionKey, SocketChannel socketChannel) {
        sk = selectionKey;
        sc = socketChannel;
    }

    @Override
    public void run() {
        try {
//            System.out.println("MultiThreadHandler:" + sc.validOps());
            if (state == READING) {
                read();//读取客户端数据
            } else if (state == SENDING) {
                send();//向客户端发送反馈数据
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void read() throws IOException {
        ByteBuffer inputBuffer = ByteBuffer.allocate(1024);
        inputBuffer.clear();

        int numBytes = sc.read(inputBuffer);
        if (numBytes == -1) {
            System.out.println("a client has been closed");
            closeChannel();
            return;
        }

//        String str = "";
//        while(sc.read(inputBuffer) != 0){
//            str += new String(inputBuffer.array());
//        }
//        System.out.println("sc.read(inputBuffer):" + sc.read(inputBuffer));

        String str = new String(inputBuffer.array());
        if ((str != null) && !str.equals("")) {
            state = PROCESSING;

            pool.execute(() -> {
                process(str);
                System.out.println(sc.socket().getRemoteSocketAddress().toString() + ">" + str);
                sk.interestOps(SelectionKey.OP_WRITE);
                sk.selector().wakeup();

                String str2 = "message has sent to " + sc.socket().getLocalSocketAddress().toString() + "\r\n";

                ByteBuffer outputBuffer = ByteBuffer.allocate(1024);

                outputBuffer.put(str2.getBytes());
                outputBuffer.flip();
                try {
                    sc.write(outputBuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

    }

    private void send() throws IOException {

        state = READING;
        sk.interestOps(SelectionKey.OP_READ);
        sk.selector().wakeup();
    }

    private void process(String str) {
        System.out.println(str);
        state = SENDING;
    }

    private void closeChannel() {
        try {
            sk.cancel();
            sc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
