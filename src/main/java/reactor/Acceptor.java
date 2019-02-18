package reactor;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Acceptor implements Runnable {

    private Reactor reactor;

    public Acceptor(Reactor reactor) {
        this.reactor = reactor;
    }

    @Override
    public void run() {
        try {
            SocketChannel sc = reactor.serverSocketChannel.accept();
            System.out.println(sc.socket().getRemoteSocketAddress().toString() + " is connected.");

//            System.out.println("Acceptor:" + sc.validOps());
            if (sc != null) {
                sc.configureBlocking(false);
                SelectionKey sk = sc.register(reactor.selector, SelectionKey.OP_READ);
                // 使一个阻塞住selector操作立即返回
                reactor.selector.wakeup();
                // 通过key为新的通道绑定一个附加的TCPHandler对象
                // 2. register handler
                sk.attach(new MultiThreadHandler(sk, sc));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
