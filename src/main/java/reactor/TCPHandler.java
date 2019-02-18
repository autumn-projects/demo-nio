package reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class TCPHandler implements Runnable {
    private final SelectionKey sk;
    private final SocketChannel sc;
    int state;

    public TCPHandler(SelectionKey sk, SocketChannel sc) {
        this.sk = sk;
        this.sc = sc;
        state = 0;
    }

    @Override
    public void run() {
        System.out.println("测试运行了几次");
        try {
            if (state == 0) {
                read();//读取客户端数据
            } else {
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

        String str = new String(inputBuffer.array());
        if ((str != null) && !str.equals("")) {
            process(str);

            System.out.println(sc.socket().getRemoteSocketAddress().toString() + ">" + str);
            state = 1;
            sk.interestOps(SelectionKey.OP_WRITE);
            sk.selector().wakeup();
        }

    }

    private void send() throws IOException {
        String str = "message has sent to " + sc.socket().getLocalSocketAddress().toString() + "\r\n";

        ByteBuffer outputBuffer = ByteBuffer.allocate(1024);

        outputBuffer.put(str.getBytes());
        outputBuffer.flip();
        sc.write(outputBuffer);
        state = 0;
        sk.interestOps(SelectionKey.OP_READ);
        sk.selector().wakeup();
    }

    private void process(String str) {
        System.out.println(str);
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
