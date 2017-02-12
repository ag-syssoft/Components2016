package comp.solver;

import org.apache.camel.main.Main;

public class CamelThread extends Thread {
    Main main;
    String[] args;

    CamelThread(Main main, String... args) {
        this.main = main;
        this.args = args;
    }

    @Override
    public void run() {
        try {
            main.run(args);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

