/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package syncr;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author Reception
 */
public class ConfigManager {
    private SyncRUI ui;

    public ConfigManager(SyncRUI ui) {
        this.ui = ui;
    }
    
    public File fileChooser(String loc){
        
        JFileChooser fChooser = new JFileChooser();
        fChooser.setDialogTitle("Select " + loc + " Directory");
        fChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        File selectedFile = null;
        
        if(fChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
            
            int confirm = JOptionPane.showConfirmDialog(ui.getGui(), "Are you sure of the selected directory?");
            if(confirm == JOptionPane.YES_OPTION){
                selectedFile = fChooser.getSelectedFile();
                ui.appendToLogTextArea(loc+" directory selected \" " + selectedFile.getAbsolutePath() + " \" \n");    
            }
            else if(confirm == JOptionPane.NO_OPTION){
                System.out.println("no");
            } 
            else  if(confirm == JOptionPane.CANCEL_OPTION){
                JOptionPane.showMessageDialog(ui.getGui(), "No \"" + loc + "\" folder selected", "SyncR", JOptionPane.WARNING_MESSAGE);
                ui.appendToLogTextArea("No " + loc +" directory selected \n");
            }
        }
        else{
            JOptionPane.showMessageDialog(ui.getGui(), "No \"" + loc + "\" folder selected", "SyncR", JOptionPane.WARNING_MESSAGE);
            ui.appendToLogTextArea("No \"" + loc + "\" folder selected");
        }
        return selectedFile;
    } 
}
