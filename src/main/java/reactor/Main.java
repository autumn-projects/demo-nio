package reactor;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            Reactor temp = new Reactor(6666);
            temp.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
