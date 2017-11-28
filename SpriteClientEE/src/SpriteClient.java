
//%W%	%G%
/*
 This app bounces a blue ball inside a JPanel.  The ball is created and begins
 moving with a mousePressed event.  When the ball hits the edge of
 the JPanel, it bounces off the edge and continues in the opposite
 direction.  
 */
import java.awt.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.JFrame;
import javax.swing.JPanel;

import business.SpriteSessionRemote;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class SpriteClient {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Bouncing Sprites");
        SpriteSessionRemote session = null;
        System.setProperty("org.omg.CORBA.ORBInitialHost", "127.0.0.1");
        System.setProperty("org.omg.CORBA.ORBInitialPort", "3700");

        try {
            System.out.println("about to try for a session...");
            InitialContext ic = new InitialContext();
            session
                    = //(SpriteSessionRemote) new InitialContext().lookup("java:global/SpriteEE/SpriteEE-ejb/SpriteSession");
                    (SpriteSessionRemote) ic.lookup("java:global/SpriteEE/SpriteEE-ejb/SpriteSession");

            System.out.println("I got a session");
            System.out.println("This is the height: " + session.getHeight());
        } catch (NamingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (session != null) {
            System.out.println("I got game");
        } else {
            System.err.println("Could not contact game server");
            System.exit(1);
        }
        try {
            frame.setSize(session.getHeight(), session.getWidth());
        } catch (RemoteException e) {
            System.err.println("Could not get one or both of HEIGHT, WIDTH for this game");
            e.printStackTrace();
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        SpritePanel panel = new SpritePanel(session);
        frame.add(panel);
        frame.validate();
        new Thread(panel).start();
    }
}
