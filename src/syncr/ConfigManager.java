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
import javax.swing.JTextArea;

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
        
        // views the top most syncjob when the app starts
        int lastJobNumber = getSyncJobCounter();
        if(lastJobNumber != 0){
            jobFolderName = new File(jobMainFolder, "SyncJob"+lastJobNumber);
            if(!jobFolderName.exists()){
                jobFolderName.mkdir();
            }
        }else{
            lastJobNumber = 1;
            jobFolderName = new File(jobMainFolder, "SyncJob1");
            jobFolderName.mkdir();
        }
        setSyncJobCounter(lastJobNumber);
        configFile = new File(jobFolderName, "sync_job"+lastJobNumber+".properties");
        
        if(!configFile.exists()){
            initializeCon();
        }else{
            load();
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
                JTextArea area = ui.getLogAreaForJob(jobFolderName.getName());
                area.append(loc+" directory selected \" " + selectedFile.getAbsolutePath() + " \" \n");   
                
                appendToLog(jobFolderName.getName(), loc+" directory selected \" " + selectedFile.getAbsolutePath() + " \"");
            }
            else if(confirm == JOptionPane.NO_OPTION){
                System.out.println("no");
            } 
            else  if(confirm == JOptionPane.CANCEL_OPTION){
                JOptionPane.showMessageDialog(ui.getGui(), "No \"" + loc + "\" folder selected", "SyncR", JOptionPane.WARNING_MESSAGE);
                JTextArea area = ui.getLogAreaForJob(jobFolderName.getName());
                area.append("No " + loc +" directory selected \n");
                appendToLog(jobFolderName.getName(), "No " + loc +" directory selected");
            }
        }
        else{
            JOptionPane.showMessageDialog(ui.getGui(), "No \"" + loc + "\" folder selected", "SyncR", JOptionPane.WARNING_MESSAGE);
            JTextArea area = ui.getLogAreaForJob(jobFolderName.getName());
            area.append("No \"" + loc + "\" folder selected \n");
            appendToLog(jobFolderName.getName(), "No " + loc +" directory selected");
        }
        return selectedFile;
    } 
    
    public void saveSyncSession(){    
        saveSourceLoc();
        saveDestinationLoc(); 
        saveParameters();
        saveSyncType();
        //saveTextAreaLog(jobFolderName.getName());        
        
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
    
    //////////////////////////SAVING////////////////////////////////////
        private void save() {
            try (FileOutputStream fos = new FileOutputStream(configFile)) {
                props.store(fos, "Sync Job Config");
            } catch (IOException ignored) {}
        }

        //saving the copy parameters
        private void saveParameters(){
           String parameters = (!ui.getSelectedParameters().isEmpty())
                   ? ui.getSelectedParameters().toString()
                   : "[/MIR, /Z, /XO, /XX]";

            props.setProperty("Parameters", parameters);
           save();
        }

        // saving the sync type information
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

        //saving the source location to the config file
        private void saveSourceLoc(){
            String sourceLocation = (ui.getSourceLocation() != null) 
                    ? ui.getSourceLocation().getAbsolutePath().replace("\\", "/")
                    : "no location set yet";

            props.setProperty("SourceLocation", sourceLocation);
            save();
        }

        //saving the destination location to the config file
        private void saveDestinationLoc(){
            String destPath = (ui.getDestinationLocation() != null) 
                        ? ui.getDestinationLocation().getAbsolutePath().replace("\\", "/")
                        : "no location set yet";
            props.setProperty("DestinationLocation", destPath);
            save();
        }

        //saving the log details
        // change it to a txt file
        public void appendToLog(String jobName, String msg) {
            File jobLog = getJobLogFile(jobName);

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(jobLog, true))) {
                bw.write(msg);
                bw.newLine();  // ensure each log entry is on its own line
            } catch (IOException e) {
                Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, e);
            }

        }

        
        private void initializeCon(){
            props.setProperty("Parameters", "no parameters set yet");
            props.setProperty("SourceLocation", "no location set yet");
            props.setProperty("DestinationLocation", "no location set yet");
            props.setProperty("SyncType", "Two Way");
            save(); // write once
        }
        
        public void saveIsSyncing(boolean isSyncing){   
            String val = "Not Syncing";
            if(isSyncing == true){
                val = "Syncing";
            }
            
            props.setProperty("isSyncing", val);
            save();
        }
    ////////////////////////////////////////////////////////////////////////////////
        
    //GETTERS//////////////////////////////////////////////////////////////////////
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
        
        public File getJobFolder() {
            return jobFolderName;
        }

        public File getJobMainFolder() {
            return jobMainFolder;
        }
        
        public String getTALog(File logTextFile) throws IOException{
            String data = "";
            
            FileReader fr = new FileReader(logTextFile);
            BufferedReader br = new BufferedReader(fr);
            
            String log = br.readLine();
            while(log != null){
                data += log + "\n";
                
                log = br.readLine();
            }
            
            br.close();
            fr.close();
            
            return data;
        }
        
        public boolean isSyncing(File jobConfigFile){
            load(jobConfigFile);
            boolean isSyncing = false;
            
            String savedData = props.getProperty("isSyncing");
            if(savedData.equals("Syncing")){
                isSyncing = true;
            }
                    
            return isSyncing;
        }
        
        private File getJobLogFile(String jobName) {
            File jobFolder = new File(jobMainFolder, jobName);
            if (!jobFolder.exists()) jobFolder.mkdir();
            File logFile = new File(jobFolder, "log_file.txt");
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile();
                } catch (IOException ex) {
                    Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return logFile;
        }

    //////////////////////////////////////////////////////////////////////////////
        
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
    
    ////////////////////////////////SETTERS/////////////////////////////////////////////////////////////
        public void setSyncJobCounter(int jobNumber){
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(job_counter_log))){
                bw.write(String.valueOf(jobNumber));
            } catch (IOException ex) {
                Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 

        public void setJobFolderName(File jobFolderName) {
            this.jobFolderName = jobFolderName;
        }

        public void setConfigFile(File configFile) {
            this.configFile = configFile;
        }    
        
       
}