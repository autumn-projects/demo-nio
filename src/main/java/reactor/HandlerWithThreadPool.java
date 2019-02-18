package reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HandlerWithThreadPool implements Runnable {

    final SocketChannel socket;
    final Selector selector;
    final SelectionKey key;
    final ByteBuffer inputBuffer = ByteBuffer.allocate(1024);
    final ByteBuffer outputBuffer = ByteBuffer.allocate(1024);

    // 初始化一个线程池
    static ThreadPoolExecutor pool = new ThreadPoolExecutor(5, 10, 100, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    // 状态码，分别对应读状态，写状态和处理状态
    static final int READING = 1;
    static final int SENDING = 2;
    static final int PROCESSING = 3;
    // 初始的状态码是READING状态，因为Reactor分发任务时新建的Handler肯定是读就绪状态
    private int state = READING;

    public HandlerWithThreadPool(SocketChannel socket, Selector selector) throws IOException {
        this.socket = socket;
        this.selector = selector;

        key = socket.register(selector, 0);
        socket.configureBlocking(false);
        key.interestOps(SelectionKey.OP_READ);
        // attach(this)是为了dispatch()调用
        key.attach(this);
    }

    /**
     * 判断读写数据时候完成的方法
     **/
    private boolean inputIsCompelete() {
        return true;
    }

    private boolean outputIsCompelete() {
        return true;
    }

    /**
     * 对数据的处理类，比如HTTP服务器就会返回HTTP报文
     **/
    private void process() {
        // 自己实现的服务器功能
    }

    /**
     * 读入数据，确定通道内数据读完以后
     * 状态码要变为 PROCESSING
     * 需要特别注意的是，本方法是在Reactor线程中执行的
     *
     * @throws IOException
     */
    void read() throws IOException {

        socket.read(inputBuffer);
        if (inputIsCompelete()) {
            state = PROCESSING;
            pool.execute(new Processer());
        }
    }

    /**
     * 这个方法调用了process()方法
     * 而后修改了状态码和兴趣操作集
     * 注意本方法是同步的，因为多线程实际执行的是这个方法
     * 如果不是同步方法，有可能出现
     */
    synchronized void processAndHandOff() {
        process();
        state = SENDING;
        key.interestOps(SelectionKey.OP_WRITE);
    }

    /**
     * 这个内部类完全是为了使用线程池
     * 这样就可以实现数据的读写在主线程内
     * 而对数据的处理在其他线程中完成
     */
    class Processer implements Runnable {
        public void run() {
            processAndHandOff();
        }
    }

    @Override
    public void run() {
        if (state == READING) {
            try {
                read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (state == SENDING) {
            // 完成数据的发送即可
        }
    }

}

