/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package syncr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicBorders;

/**
 *
 * @author Tshepo
 */
public class SyncRUI {
    // GUI FRAME
    private static JFrame gui; 
    
    //PANELS
    private JPanel mainpanel; 
    
    //TEXT AREAS
    private static JTextArea logTextArea;
    
    // ALL MY BUTTONS
    private JButton sourceBtn;    
    private JButton destinationBtn;    
    private JButton syncDataBtn;
    private JButton saveSyncJob;
    
    //LISTS
        private ArrayList<String> selectedParameters = new ArrayList<>();
        //check boxes map
        private Set<Map.Entry<String, JCheckBox>> entries;
        
    // FILES
    private File destinationLocation;
    private File sourceLocation;
    
    // MANAGER CLASSES
    private SyncManager syncManager = new SyncManager(this);
    private ConfigManager configManager = new ConfigManager(this);
    
    // LABELS
    private final JLabel copyParametersLbl = new JLabel("Copy Parameters", JLabel.CENTER);    
    private JLabel jobHeading;
    
    //MENU, MENU BAR AND MENU ITEMS
    private JMenu menu;
    private JMenu newSyncJobMenu;
    private JMenuBar menuBar;        
    
    
    //CLASSES AND METHODS
    public SyncRUI() {
        gui = new JFrame("SyncR");
        gui.setSize(500, 420);
        gui.setTitle("SyncR");
        
        gui.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
               
        gui.setLocationRelativeTo(null);
        gui.setResizable(false);
        
        //SETTING THE ICON
        URL iconUrl = getClass().getResource("/resources/SyncRLogo.png");
        if(iconUrl != null){
            Image icon = Toolkit.getDefaultToolkit().getImage(iconUrl);
            gui.setIconImage(icon);
        }
        else{
            System.err.println("Icon not found");
        }
            
        
       // Main panel with BorderLayout
        mainpanel = new JPanel(new BorderLayout());

        // Text area in the center (with scroll if content grows)
        logTextArea = new JTextArea(10, 5);
        logTextArea.setEditable(false);
        logTextArea.append("Please click the buttons \"Source\" and \"Destination\" to set the locations of the folders to sync\n");
        //config append
        configManager.appendToLogFile("Please click the buttons \"Source\" and \"Destination\" to set the locations of the folders to sync\n");
        
        JScrollPane scrollPane = new JScrollPane(logTextArea);

        // Buttons at the bottom
        JPanel locationsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            //SETTING THE SOURCE LOCATION
            sourceBtn = new JButton("Source");
            sourceBtn.addActionListener((e) -> {
                sourceLocation = configManager.fileChooser("Source");
            });

            //SETTING THE DESTINATION LOCATION
            destinationBtn = new JButton("Destination");
            destinationBtn.addActionListener((e) -> {
                destinationLocation = configManager.fileChooser("Destination");
            });

        locationsPanel.add(sourceBtn);
        locationsPanel.add(destinationBtn);     
       
        JPanel bottomPanel = new JPanel(new FlowLayout());
            // SYNCING THE 2 SELECTED FOLDERS -> SOURCE AND DESTINATION
            syncDataBtn = new JButton("Sync Drives/Folders");
            syncDataBtn.addActionListener((e) -> {
                if(sourceLocation != null || destinationLocation != null){
                    new Thread(() -> syncManager.sync()).start(); 
                }
                else{
                    JOptionPane.showMessageDialog(gui, "Please click the buttons \"Source\" and \"Destination\" to set the locations of the folders to sync\n");
                }
            });

            //SAVING THE SYNC JOB DATA 
            saveSyncJob = new JButton("Save Sync Job");
            Border border = new BasicBorders.ButtonBorder(Color.yellow, Color.darkGray, Color.lightGray, Color.lightGray);
            saveSyncJob.setBorder(border);
            
            saveSyncJob.addActionListener((e) -> {
                configManager.saveSyncSession();
                JOptionPane.showMessageDialog(gui, "Changes Saved", "Save", JOptionPane.INFORMATION_MESSAGE);
            });
        
        bottomPanel.add(syncDataBtn, BorderLayout.NORTH);
        bottomPanel.add(saveSyncJob, BorderLayout.CENTER);
        
        // PANEL WITH THE TEXTAREA AND HEADING
        JPanel topPnl = new JPanel(new BorderLayout());
        // HEADING FOR EACH JOB -> DYNAMIC. NAMED AFTER THE RESPECTIVE JOB
            jobHeading = new JLabel(configManager.getJobFolderName());
            jobHeading.setHorizontalAlignment(JLabel.CENTER);
        topPnl.add(jobHeading, BorderLayout.NORTH);
        topPnl.add(scrollPane, BorderLayout.CENTER);
       
        // PANEL WITH THE CHECKBOXES
        JPanel middlePnl = new JPanel(new BorderLayout());
            JPanel copyParamPnl = new JPanel(new GridLayout(4, 2));        
            entries = SyncR.getRobocopyParameters().entrySet();
             
            // ITERATING THROUGH THE PARAMETERS AND ADDING THEM TO THE UI
            for (Map.Entry<String, JCheckBox> entry : entries) {
                String paramKey = entry.getKey();
                JCheckBox checkBox = entry.getValue();
                
                if(checkBox.isSelected() == true){
                    selectedParameters.add(paramKey);                    
                }
                copyParamPnl.add(checkBox);
                
                //check box action listener
                checkBox.addItemListener((e) -> {
                    if(checkBox.isSelected()){
                        if(!selectedParameters.contains(paramKey)){
                            selectedParameters.add(paramKey);  
                            logTextArea.append("\""+checkBox.getText()+" \"parameter added\n");                            
                        }                       
                    }
                    else{
                        selectedParameters.remove(paramKey);
                        logTextArea.append("\""+checkBox.getText()+" \"parameter removed\n");
                    }
                });    
            }
            
        middlePnl.add(copyParametersLbl, BorderLayout.NORTH);
        middlePnl.add(copyParamPnl, BorderLayout.CENTER);
        
        
        //PANEL WITH THE SOURCE AND DESTINATION PANEL, AND THE SAVE BUTTON
        JPanel buttonPanel = new JPanel(new BorderLayout());
            buttonPanel.add(locationsPanel, BorderLayout.CENTER);
            buttonPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // MENU CREATING AND INSTANTIATION
        menuBar = new JMenuBar();        
        menu = new JMenu("Sync Jobs");
        
        newSyncJobMenu = new JMenu("New Sync Job");
        JMenuItem newJobItem = new JMenuItem("New");
        
        //NEW JOB MENU ITEM. WHEN SELECTED IT INITIALISES A NEW SYNC JOB. CHANGES THE UI TO A DEFAULT ONE
        newJobItem.addActionListener((e) -> {
            int opt = JOptionPane.showConfirmDialog(gui, "Are you sure you'd like to start another \"Sync Session\"? ");
                if(opt == JOptionPane.YES_OPTION){                
                    //save the data of the previous 
                    configManager.saveSyncSession();
                    
                    // CREATING THE FOLDER OF THE NEW JOB 
                    int newJobNumber = configManager.incrementor();
                    File newJobFolder = new File(configManager.getJobMainFolder(), "SyncJob"+newJobNumber);
                    if(!newJobFolder.exists())  newJobFolder.mkdir();

                    configManager.setJobFolderName(newJobFolder);
                    configManager.setSyncJobCounter(newJobNumber);

                    // SETTING THESE TO NULL. IDK WHY I DID THIS :(
                    sourceLocation = null;
                    destinationLocation = null;
                    
                    //SETTING THE TEXT AREA TO A DEFAULT MESSAGE FOR THE RESTART
                    logTextArea.setText("Please click the buttons \"Source\" and \"Destination\" to set the locations of the folders to sync\n");
                    addingNewParameters("[/MIR, /Z, /XO, /XX]");

                    //SETTING THE HEADING TO THE NEW JOB NAME
                    jobHeading.setText(newJobFolder.getName());

                    
                    //NEW LOG FILE CREATION
                    File newLogFile = new File(newJobFolder, "log_file.txt");
                    if(!newLogFile.exists()) try {
                        newLogFile.createNewFile();
                    } catch (IOException ex) {
                        Logger.getLogger(SyncRUI.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    //NEW CONFIG FILE CREATION AND OVERRIDING THE OLD ONE
                    File newConfigFile = new File(newJobFolder, "sync_job"+newJobNumber+".properties");
                    configManager.setConfigFile(newConfigFile);

                    JOptionPane.showMessageDialog(gui, "New Sync Job. Set up the Required Parameters for it!!", "New Sync Job", JOptionPane.INFORMATION_MESSAGE);
                }
        });
        
        //loading saved sync jobs        
        loadingSyncJobs(menu);
        
        newSyncJobMenu.add(newJobItem);
        
        menuBar.add(menu);
        menuBar.add(newSyncJobMenu);
        
        mainpanel.add(topPnl, BorderLayout.NORTH);
        mainpanel.add(middlePnl, BorderLayout.CENTER);
        mainpanel.add(buttonPanel, BorderLayout.SOUTH);
        
        //listening to the UI os when it's closed, the current sync job is saved
        gui.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e){
                int choice = JOptionPane.showConfirmDialog(gui, "Do you want to save this Sync Job before exiting?", "Exit SyncR", JOptionPane.YES_NO_CANCEL_OPTION);
                
                if(choice == JOptionPane.YES_OPTION){
                    configManager.saveSyncSession();
                    gui.dispose();
                }
                else if (choice == JOptionPane.NO_OPTION){
                    //DELETE SYNCJOB FILE AND MINUS 1 FROM THE SAVED SYNC JOB COUNTER
                    File job_folder = new File(configManager.getJobMainFolder(),configManager.getJobFolderName());
                    
                    //deleting the files inside first
                    File files[] = job_folder.listFiles();
                    for (File file : files) {
                        file.delete();
                    }
                    
                    //deleting the folder
                    job_folder.delete();
                    
                    //System.out.println(job_folder.getAbsolutePath());
                    
                    configManager.setSyncJobCounter(configManager.getSyncJobCounter()-1);
                    gui.dispose();
                }                
            }
        });
        
        // Add main panel to frame
        gui.setJMenuBar(menuBar);
        gui.add(mainpanel);
        gui.setVisible(true);  
    }

    // I DID THIS SO ALL JOPTIONPANES IN THE OTHER CLASSES DISPLAY ON TOP OF THE GUI WHEN THEY APPEAR ON THE SCREEEN
    public JFrame getGui() {
        return gui;
    }

    // METHOD FOR LOGGING TO THE TEXT AREA
    public void appendToLogTextArea(String appMsg) {
        logTextArea.append(appMsg);
    }

    public File getDestinationLocation() {
        return destinationLocation;
    }

    public File getSourceLocation() {
        return sourceLocation;
    }    

    public ArrayList<String> getSelectedParameters() {
        return selectedParameters;
    }
    
    //loading the sync jobs to the menu
    public void loadingSyncJobs(JMenu menu){
        File[] savedSyncJobs = configManager.getJobMainFolder().listFiles();
        
        JMenuItem syncJobMenuItem;
        
        for (int i = 0; i < savedSyncJobs.length; i++) {
            File job = savedSyncJobs[i];
            
            syncJobMenuItem = new JMenuItem(job.getName());
            if(!job.getName().endsWith("txt")){
                syncJobMenuItem.addActionListener((e) -> {
                    jobHeading.setText(job.getName());
                    getJobParameters(job);
                });
                
                menu.add(syncJobMenuItem);
            }
        }
    }

    //retrieving/loading the selected job parameters
    private void getJobParameters(File job) {
        File[] listJobFiles = job.listFiles();
        File configFile = null;
        File logFile = null;
        for (File listJobFile : listJobFiles) {
            // LOOKING FOR THE CONFIG PROPERTIES FILE
            if(listJobFile.getName().endsWith(".properties")){
                configFile = listJobFile;
            }
            //LOOKING FOR THE LOG FILE
            else if(listJobFile.getName().endsWith(".txt")){
                logFile = listJobFile;
            }
        }
        
        //SETTING THE NEW SOURCE/DESTINATION LOCATION, AND PARAMETERS
        String newSource = configManager.getSourceLoc(configFile);
        String newDest = configManager.getDestinationLoc(configFile);
        String parameters = configManager.getParameters(configFile);
        addingNewParameters(parameters);
        
        String log = configManager.getLog(logFile);
        
        sourceLocation = new File(newSource);
        destinationLocation = new File(newDest);
        logTextArea.setText(log);
        
        configManager.setJobFolderName(job); 
    }    
    
    
    //METHOD FOR CHECKING OR UNCHECKING THE PARAMETERS BASED ON THE JOB LOADED
    public void addingNewParameters(String parameters){
        parameters = parameters.replaceAll("[\\[\\]]", "");
        String[] token = parameters.split("\\s*,\\s*");
        
        selectedParameters.clear();
        
        for (String string : token) {
            selectedParameters.add(string);
        }
        
        for (Map.Entry<String, JCheckBox> entry : entries) {
            String paramKey = entry.getKey();      
            JCheckBox cb = entry.getValue();
            if(!selectedParameters.contains(paramKey)){
                cb.setSelected(false);
            }   
            else{
                cb.setSelected(true);
            }
        }
        
        // REFRESHING SO NEW JOBS ARE ADDED IMMEDIATELY AFTER BEING CREATED
        menu.removeAll();
        loadingSyncJobs(menu);
        menu.revalidate();
        menu.repaint();
    }   
       
}