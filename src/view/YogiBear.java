package view;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;

/**
 *
 * @author Patrik Bogdan
 */
public class YogiBear {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        YogiBearGUI gui = new YogiBearGUI();
        gui.setVisible(true);
        gui.setResizable(false);
    }

}
