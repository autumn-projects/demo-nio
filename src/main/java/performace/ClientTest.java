package performace;

import socket.SocketClient;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class ClientTest {

    public static void main(String[] args) throws InterruptedException {
        int threadCount = 1000;

        ExecutorService executorService = Executors.newCachedThreadPool();
        Semaphore semaphore = new Semaphore(threadCount);//资源最多可被3个线程并发访问
        final CountDownLatch latch = new CountDownLatch(threadCount);

        long start = System.currentTimeMillis();
        for (int i = 0; i < threadCount; i++) {
            final int threadnum = i;
            executorService.execute(() -> {
                try {
                    System.out.println("current thread" + Thread.currentThread().getName());
                    semaphore.acquire(1);//获取许可
                    String expression = "tyuiopzxcasdfghjklqwertyuiopzxcasdfghjklqwertyuiopzxcasdfghjklqwertyuiopzxcasdfghjkl";
                    SocketClient.send(expression);
                    semaphore.release(1);//释放许可

                    latch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        latch.await();
        System.out.println("耗时：" + (System.currentTimeMillis() - start));
        System.out.println("失败次数：" + SocketClient.defaultTime);
        executorService.shutdown();//如果不shutdown工程不会结束
    }
}
