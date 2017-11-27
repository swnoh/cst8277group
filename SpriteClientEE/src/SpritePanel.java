
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.List;

import javax.swing.JPanel;

import business.Sprite;
import business.SpriteSessionRemote;
import java.awt.Rectangle;

public class SpritePanel extends JPanel implements Runnable {

    List<Sprite> sprites;
    SpriteSessionRemote session;

    public SpritePanel(SpriteSessionRemote session) {
        this.session = session;
        this.addMouseListener(new Mouse());
    }

    @Override
    public void run() {
        System.out.println("now animating...");
        //try{ 
        while (true) {
            try {
                sprites = session.getSpriteList();
            } catch (Exception e) { //catch(javax.ejb.NoSuchEJBException e)
                System.out.println("Lost contact with server, exiting...");
                System.exit(1);
            }
            repaint();
            //sleep while waiting to display the next frame of the animation
            try {
                Thread.sleep(200);  // wake up roughly 25 frames per second
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }
//		}catch(RemoteException e){
//			System.out.println("game over? exiting...");
//		}
    }

    private class Mouse extends MouseAdapter {

        @Override
        public void mousePressed(final MouseEvent event) {
            try {
                Sprite sprite = isSpriteClicked(event);
                if (sprite == null) {
                    System.out.println("Creating a new sprite");
                    session.newSprite(event);
                } else {
                    sprite.setDx(0);
                    sprite.setDy(0);
                    session.updateSprite(sprite);
                    EditSpriteJFrame editor = new EditSpriteJFrame(session, sprite);
                    editor.setVisible(true);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private Sprite isSpriteClicked(final MouseEvent event) {
        int mouseX = event.getX();
        int mouseY = event.getY();
        for (Sprite sprite : sprites) {
            Rectangle spriteRectangle = new Rectangle(sprite.getX(), sprite.getY(), sprite.SIZE, sprite.SIZE);
            if (spriteRectangle.contains(mouseX, mouseY)) {
                return sprite;
            }
        }
        return null;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (sprites != null) {
            for (Sprite s : sprites) {
                s.draw(g);
            }
        }
    }
}
