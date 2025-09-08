/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package syncr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 *
 * @author Tshepo
 */
public class ConfigManager {
    private SyncRUI ui;
    
    private File configFile;
    private Properties props;
    private File appFolder;
    
    private File jobMainFolder;
    
    private File logFile;
    
    private File jobFolder;
    
    public ConfigManager(SyncRUI ui) {
        this.ui = ui;
        props = new Properties();
        
        appFolder = new File(basePath(), "SyncR");
        if(!appFolder.exists()){
            appFolder.mkdirs();
        }
        
        jobMainFolder = new File(appFolder, "Saved Sync Jobs");
        if(!jobMainFolder.exists()) jobMainFolder.mkdir();
        
        jobFolder = new File(jobMainFolder,"SyncJob"+1);
        if(!jobFolder.exists()) jobFolder.mkdir();
        
        logFile = new File(jobFolder,"log_file.txt");
        if(!logFile.exists()) try {
            logFile.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                appendToLogFile(loc+" directory selected \" " + selectedFile.getAbsolutePath() + " \" \n");
            }
            else if(confirm == JOptionPane.NO_OPTION){
                System.out.println("no");
            } 
            else  if(confirm == JOptionPane.CANCEL_OPTION){
                JOptionPane.showMessageDialog(ui.getGui(), "No \"" + loc + "\" folder selected", "SyncR", JOptionPane.WARNING_MESSAGE);
                ui.appendToLogTextArea("No " + loc +" directory selected \n");
                appendToLogFile("No " + loc +" directory selected \n");
            }
        }
        else{
            JOptionPane.showMessageDialog(ui.getGui(), "No \"" + loc + "\" folder selected", "SyncR", JOptionPane.WARNING_MESSAGE);
            ui.appendToLogTextArea("No \"" + loc + "\" folder selected");
            appendToLogFile("No " + loc +" directory selected \n");
        }
        return selectedFile;
    } 
    
    public void saveSyncSession(){        
        
        //System.out.println(name);

        String src = ui.getSourceLocation().getAbsolutePath();
        String dest = ui.getDestinationLocation().getAbsolutePath();
        
        String name = src.substring(src.lastIndexOf("\\")+1, src.length()) + "to" + dest.substring(dest.lastIndexOf("\\")+1, dest.length()) + ".properties";
        
        configFile = new File(jobFolder, name.toLowerCase());
        
        load();
        
        saveSourceLoc();
        saveDestinationLoc(); 
        saveParameters();
        
        JOptionPane.showMessageDialog(ui.getGui(), "SyncJob1 saved");
    }
    
    public String basePath(){
        return System.getenv("ProgramData");
    }

    private void load() {
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
            } catch (IOException ignored) {}
        }
    }
    
    private void save() {
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            props.store(fos, "Sync Job Config");
        } catch (IOException ignored) {}
    }
    
    private void saveParameters(){
       props.setProperty("Parameters", ui.getSelectedParameters().toString());
       save();
    }
    
    private void saveSourceLoc(){
        props.setProperty("SourceLocation", ui.getSourceLocation().getAbsolutePath());
        save();
    }
    
    private void saveDestinationLoc(){
        props.setProperty("DestinationLocation", ui.getDestinationLocation().getAbsolutePath());
        save();
    }
    
    public void appendToLogFile(String msg){
        try {
            FileWriter fw = new FileWriter(logFile, true);
            BufferedWriter bw = new BufferedWriter(fw);
            
            bw.append(msg);
            
            bw.close();
            fw.close();
            
        } catch (IOException ex) {
            Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void loadingSyncJobs(JMenu menu){
        File[] savedSyncJobs = appFolder.listFiles();
        
        JMenuItem syncJobMenuItem = new JMenuItem("");
        menu.add(syncJobMenuItem);
        
        for (int i = 0; i < savedSyncJobs.length; i++) {
            File job = savedSyncJobs[i];
            
            syncJobMenuItem = new JMenuItem(job.getName());
            menu.add(syncJobMenuItem);
        }
    }
    
    public void setSyncJobCounter(){
        File job_counter_log = new File(jobMainFolder, "job_counter_log.txt");
        if(!job_counter_log.exists()) try {
            job_counter_log.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
}
