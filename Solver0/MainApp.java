package comp.solver;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.camel.main.Main;

/**
 * A Camel Application
 */
public class MainApp {

    /**
     * A main() so we can easily run these routing rules in our IDE
     */

    static final String broker0 = "rabbitmq://136.199.51.111/inExchange?username=&password=&skipQueueDeclare=true";

    public static void main(String... args) throws Exception {
        Parser.setSender("smtps://smtp.gmail.com?username=&password=&subject="
                + "Solver" + "&to=" + "gmail.com" + "&from=Broker");
        Main main = new Main();

        main.addRouteBuilder(new MyRouteBuilder());
        CamelThread camelThread = new CamelThread(main, args);
        camelThread.start();
        RegisterThread registerThread = new RegisterThread(main);
        registerThread.start();
        RegisterThread unregisterThread = new RegisterThread(main, false);
        JPanel jpanel = new JPanel();
        JButton jButton = new JButton("Unregister");
        jButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                unregisterThread.start();
            }
        });
        jpanel.add(jButton);
        JFrame jFrame = new JFrame();
        jFrame.add(jpanel);
        jFrame.pack();
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }


}

