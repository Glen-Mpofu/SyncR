/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package syncr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

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
    
    private File jobFolderName;
    
    // sync counter
    private File job_counter_log;

    //sync type
    private JRadioButton twoWay;
    
    //methods and classes
    public ConfigManager(SyncRUI ui) {
        this.ui = ui;
        twoWay = ui.getTwoWay();
        props = new Properties();
        
        appFolder = new File(basePath(), "SyncR");
        if(!appFolder.exists()){
            appFolder.mkdirs();
        }
        
        jobMainFolder = new File(appFolder, "Saved Sync Jobs");
        if(!jobMainFolder.exists()) jobMainFolder.mkdir();
        
        job_counter_log = new File(jobMainFolder, "job_counter_log.txt");
        if(!job_counter_log.exists()) try {
            job_counter_log.createNewFile();          
        } catch (IOException ex) {
            Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        int jobNumber = incrementor();
        
        jobFolderName = new File(jobMainFolder,"SyncJob"+jobNumber);
        if(!jobFolderName.exists()) jobFolderName.mkdir();
        setSyncJobCounter(jobNumber);  
        
        configFile = new File(jobFolderName, "sync_job"+jobNumber+".properties");
        load();
        initializeCon();
        
        logFile = new File(jobFolderName,"log_file.txt");
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
            ui.appendToLogTextArea("No \"" + loc + "\" folder selected \n");
            appendToLogFile("No " + loc +" directory selected \n");
        }
        return selectedFile;
    } 
    
    public void saveSyncSession(){        
        
        saveSourceLoc();
        saveDestinationLoc(); 
        saveParameters();
        saveSyncType();
        
        JOptionPane.showMessageDialog(ui.getGui(), getJobFolderName() + " saved");
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
    
    private void load(File jobConfigFile) {
        if (jobConfigFile.exists()) {
            try (FileInputStream fis = new FileInputStream(jobConfigFile)) {
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
       String parameters = (!ui.getSelectedParameters().isEmpty())
               ? ui.getSelectedParameters().toString()
               : "[/MIR, /Z, /XO, /XX]";
        
        props.setProperty("Parameters", parameters);
       save();
    }
    
    private void saveSyncType(){
        String syncType;
        if(twoWay.isSelected()){
            syncType = "Two Way";
        }else{
            syncType = "One Way";
        }
        
        props.setProperty("SyncType", syncType);
        save();
    }
    
    private void saveSourceLoc(){
        String sourceLocation = (ui.getSourceLocation() != null) 
                ? ui.getSourceLocation().getAbsolutePath() 
                : "no location set yet";
        
        props.setProperty("SourceLocation", sourceLocation);
        save();
    }
    
    private void saveDestinationLoc(){
        String destPath = (ui.getDestinationLocation() != null) 
                    ? ui.getDestinationLocation().getAbsolutePath() 
                    : "no location set yet";
        props.setProperty("DestinationLocation", destPath);
        save();
    }
    
    private void initializeCon(){
        saveParameters();
        saveSourceLoc();
        saveDestinationLoc();
        saveSyncType();
    }
    
    //getting the infor stored in the properties folder
    public String getSourceLoc(File jobConfigFile){
        load(jobConfigFile);
        String source = props.getProperty("SourceLocation");
        
        return source;
    }
    
    public String getDestinationLoc(File jobConfigFile){
        load(jobConfigFile);
        String dest = props.getProperty("DestinationLocation");
        
        return dest;
    }
    
    public String getParameters(File jobConfigFile){
        load(jobConfigFile);
        String paramters = props.getProperty("Parameters");
        
        return paramters;
    }
    
    public String getSyncType(File jobConfigFile){
        load(jobConfigFile);
        String paramters = props.getProperty("SyncType");
        
        return paramters;
    } 
    
    public String getLog(File jobLogFile){
        FileReader fr = null;
            String log = "";
        try {
            fr = new FileReader(jobLogFile);
            BufferedReader br = new BufferedReader(fr);
            
            String token = br.readLine();
            while(token != null){
                log += token + "\n";
                
                token = br.readLine();
            }
            
        } catch (IOException ex) {
            Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fr.close();
            } catch (IOException ex) {
                Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return log;
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

    public int incrementor(){
        Integer stored_count = getSyncJobCounter();
        int increment_count;
        
        if(stored_count == null){
            increment_count = 1;
        }
        else{
            increment_count = stored_count+1;
        }
        return increment_count;
    }
    
    public void setSyncJobCounter(int jobNumber){
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(job_counter_log))){
            bw.write(String.valueOf(jobNumber));
        } catch (IOException ex) {
            Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 
    
    public Integer getSyncJobCounter() {
        try (BufferedReader br = new BufferedReader(new FileReader(job_counter_log))) {
            String val = br.readLine();
            return (val != null) ? Integer.parseInt(val.trim()) : null;
        } catch (IOException | NumberFormatException ex) {
            Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public String getJobFolderName() {
        return jobFolderName.getName();
    }

    public File getJobMainFolder() {
        return jobMainFolder;
    }

    public void setJobFolderName(File jobFolderName) {
        this.jobFolderName = jobFolderName;
    }

    public void setConfigFile(File configFile) {
        this.configFile = configFile;
    }    
        
}
