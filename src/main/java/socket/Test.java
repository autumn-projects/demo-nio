package socket;

import java.io.IOException;

public class Test {

    public static void main(String[] args) {
        try {
            Server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
