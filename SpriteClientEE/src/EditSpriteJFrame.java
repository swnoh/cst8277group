
import javax.swing.JFrame;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


import java.rmi.RemoteException;

import business.Sprite;
import business.SpriteSessionRemote;
import business.ColorConverter;

/**
 *
 * @author Stanley Pieda
 */
public class EditSpriteJFrame extends JFrame {

    private final JLabel idLabel = new JLabel("ID:");
    private final JLabel dxLabel = new JLabel("DX:");
    private final JLabel dyLabel = new JLabel("DY:");
    private final JLabel panelHeightLabel = new JLabel("Panel Height:");
    private final JLabel panelWidthLabel = new JLabel("Panel Width:");
    private final JLabel xLabel = new JLabel("X:");
    private final JLabel yLabel = new JLabel("Y:");
    private final JLabel colorJLabel = new JLabel("Color:");
    private final JTextField idJTextField = new JTextField("");
    private final JTextField dxJTextField = new JTextField("");
    private final JTextField dyJTextField = new JTextField("");
    private final JTextField panelHeightJTextField = new JTextField("");
    private final JTextField panelWidthJTextField = new JTextField("");
    private final JTextField xJTextField = new JTextField("");
    private final JTextField yJTextField = new JTextField("");
    private final JTextField colorJTextField = new JTextField("");
    private final JButton updateButton = new JButton("Update And Close");

    private final Sprite sprite;
    private final SpriteSessionRemote session;

    private final ColorConverter colorConverter = new ColorConverter();

    public EditSpriteJFrame(SpriteSessionRemote session, Sprite sprite) {
        this.session = session;
        this.sprite = sprite;
        buildGUI();
        registerEvents();
        loadSpriteDataIntoGUI();
        this.pack();
    }

    private void buildGUI() {
        this.getContentPane().setLayout(new GridLayout(9, 2));

        idJTextField.setEditable(false);

        this.getContentPane().add(idLabel);
        this.getContentPane().add(idJTextField);

        this.getContentPane().add(dxLabel);
        this.getContentPane().add(dxJTextField);

        this.getContentPane().add(dyLabel);
        this.getContentPane().add(dyJTextField);

        this.getContentPane().add(panelHeightLabel);
        this.getContentPane().add(panelHeightJTextField);

        this.getContentPane().add(panelWidthLabel);
        this.getContentPane().add(panelWidthJTextField);

        this.getContentPane().add(xLabel);
        this.getContentPane().add(xJTextField);

        this.getContentPane().add(yLabel);
        this.getContentPane().add(yJTextField);

        this.getContentPane().add(colorJLabel);
        this.getContentPane().add(colorJTextField);

        this.getContentPane().add(updateButton);
    }

    private void registerEvents() {
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (sprite != null) {
                        sprite.setId(idJTextField.getText());
                        sprite.setDx(Integer.parseInt(dxJTextField.getText()));
                        sprite.setDy(Integer.parseInt(dyJTextField.getText()));
                        sprite.setPanelHeight(Integer.parseInt(panelHeightJTextField.getText()));
                        sprite.setPanelWidth(Integer.parseInt(panelWidthJTextField.getText()));
                        sprite.setX(Integer.parseInt(xJTextField.getText()));
                        sprite.setY(Integer.parseInt(yJTextField.getText()));
                        sprite.setColor(colorConverter.convertToEntityAttribute(colorJTextField.getText()));
                        session.updateSprite(sprite);
                        EditSpriteJFrame.this.setVisible(false);
                        EditSpriteJFrame.this.dispose();
                    }
                } 
                catch (RemoteException ex) {
                    ex.printStackTrace();
                }
                catch(NumberFormatException ex){
                    JOptionPane.showMessageDialog(EditSpriteJFrame.this, "Data not in correct format");
                }
                
            }

        });
    }

    private void loadSpriteDataIntoGUI() {
        if (sprite != null) {
            idJTextField.setText(sprite.getId());
            dxJTextField.setText(sprite.getDx() + "");
            dyJTextField.setText(sprite.getDy() + "");
            panelHeightJTextField.setText(sprite.getPanelHeight() + "");
            panelWidthJTextField.setText(sprite.getPanelWidth() + "");
            xJTextField.setText(sprite.getX() + "");
            yJTextField.setText(sprite.getY() + "");
            colorJTextField.setText(colorConverter.convertToDatabaseColumn(sprite.getColor()));
        }
    }
}
