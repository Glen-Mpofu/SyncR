/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package syncr;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 *
 * @author Reception
 */
public class SyncR{
    private static JFrame gui; 
    
    private JPanel mainpanel; 
    private JLabel heading;
    private JTextField outputTf;
    private static JTextArea logTextArea;
    
    private JButton sourceBtn;
    private static File sourceLocation;
    
    private JButton destinationBtn;
    private static File destinationLocation;
    
    private JButton syncData;

    private static Long lastSyncCToE = 0l;
    private static Long lastSyncEToC = 0l;
    private static Long COOLDOWN = 5000l;
    
    private static boolean isSyncing = false;
    
    private static Map<String, JCheckBox> robocopyParameters;
    private final JLabel copyParametersLbl = new JLabel("Copy Parameters", JLabel.CENTER);
    private static ArrayList<String> selectedParameters = new ArrayList<>();
    
    public SyncR(){  
        gui = new JFrame("SyncR");
        gui.setSize(500, 400);
        gui.setTitle("SyncR");
        gui.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        gui.setLocationRelativeTo(null);
        gui.setResizable(false);

       // Main panel with BorderLayout
        mainpanel = new JPanel(new BorderLayout());

        // Text area in the center (with scroll if content grows)
        logTextArea = new JTextArea(10, 5);
        logTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logTextArea);

        // Buttons at the bottom
        JPanel locationsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        sourceBtn = new JButton("Source");
        sourceBtn.addActionListener((e) -> {
            sourceLocation = fileChooser("Source");
        });
       
        destinationBtn = new JButton("Destination");
        destinationBtn.addActionListener((e) -> {
            destinationLocation = fileChooser("Destination");
        });

        locationsPanel.add(sourceBtn);
        locationsPanel.add(destinationBtn);     
       
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        syncData = new JButton("Sync Drives/Folders");
        syncData.addActionListener((e) -> {
            new Thread(() -> sync()).start();                   
        });

        bottomPanel.add(syncData);

        JPanel topPnl = new JPanel(new BorderLayout());
        topPnl.add(scrollPane, BorderLayout.NORTH);
       
        JPanel middlePnl = new JPanel(new BorderLayout());
            JPanel copyParamPnl = new JPanel(new GridLayout(4, 2));        
            Set<Entry<String, JCheckBox>> entries = robocopyParameters.entrySet();
 
            int count = 0;
            for (Entry<String, JCheckBox> entry : entries) {
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
                            System.out.println(selectedParameters);
                        }                       
                    }
                    else{
                        selectedParameters.remove(paramKey);
                        System.out.println(selectedParameters);
                    }
                });
                         
            }
        middlePnl.add(copyParametersLbl, BorderLayout.NORTH);
        middlePnl.add(copyParamPnl, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(locationsPanel, BorderLayout.CENTER);
        buttonPanel.add(bottomPanel, BorderLayout.SOUTH);

        mainpanel.add(topPnl, BorderLayout.NORTH);
        mainpanel.add(middlePnl, BorderLayout.CENTER);
        mainpanel.add(buttonPanel, BorderLayout.SOUTH);
        // Add main panel to frame
        gui.add(mainpanel);
        gui.setVisible(true);  
}
    
    public static void main(String[] args) { 
        initializeRobocopyParameters();
        new SyncR();
    }
    
    private static void initializeRobocopyParameters(){
        robocopyParameters = new HashMap<>();
        
        // /mir
        robocopyParameters.put("/MIR", new JCheckBox("Mirror", true));
        
        // /Z        
        robocopyParameters.put("/Z", new JCheckBox("Restartable Mode", true));
        
        // /XO
        robocopyParameters.put("/XO", new JCheckBox("Skip Older Files", true));
        
        // /XX
        robocopyParameters.put("/XX", new JCheckBox("Skip Extra Files", true));
        
        // /XN      
        robocopyParameters.put("/XN", new JCheckBox("Exclude Newer Files", false));
    }
    
    private static void sync(){
        try {            
            WatchService eWatchService = FileSystems.getDefault().newWatchService();
            
            Path sourcePath = sourceLocation.toPath();
            Path destinationPath = destinationLocation.toPath();
            
            WatchKey sourceKey = sourcePath.register(eWatchService, StandardWatchEventKinds.ENTRY_MODIFY, 
                    StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
            
            WatchKey destinationKey = destinationPath.register(eWatchService, StandardWatchEventKinds.ENTRY_MODIFY, 
                    StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
            
            Map<WatchKey, Path> watchKeyMap = new HashMap<>();
            watchKeyMap.put(destinationKey, destinationPath);
            watchKeyMap.put(sourceKey, sourcePath);
            
            logTextArea.append("Watching Source and Destination directories...\n");

            while(true){
                WatchKey key = eWatchService.take();                
                Path eventPath = (Path) watchKeyMap.get(key);
                
                if(eventPath == null){
                    System.out.println("Wrong key!!!! Angiyazi");
                    continue;
                }
                                
                for (WatchEvent<?> event : key.pollEvents()) {  
                    
                    WatchEvent.Kind<?> kind = event.kind();                    
                    Path changed = (Path)event.context();
                    Path fullPath = eventPath.resolve(changed);                    
                    
                    System.out.println(fullPath + " " + kind.name());
                    
                    long now = System.currentTimeMillis();
                    //adding parameters to the command
                        String commandString = "robocopy \"" + destinationPath + "\"  \"" + sourcePath + "\" ";
                        for (int i = 0; i < selectedParameters.size(); i++) {
                            commandString+= selectedParameters.get(i) + " ";
                        }
                        commandString += "/copy:DATSO /R:5 /W:1 /MT:32 /DCOPY:DATEX /LOG:C:\\Logs\\source_robocopy_sync.log";
                    if(eventPath.equals(destinationPath) && (now - lastSyncEToC > COOLDOWN) && isSyncing == false){
                        
                        isSyncing = true;
                        logTextArea.append("Syncing " + destinationPath + " to " + sourcePath);

                        Runtime.getRuntime().exec(commandString);
                        System.out.println(commandString);
                        lastSyncEToC = now;
                        Thread.sleep(5000);
                        isSyncing = false;
                        
                    }else if(eventPath.equals(sourcePath) && (now - lastSyncCToE > COOLDOWN) && isSyncing == false){
                        isSyncing = true;
                        
                        logTextArea.append("Syncing " + sourcePath + " to " + destinationPath);
                        System.out.println(commandString);
                        Runtime.getRuntime().exec(commandString);
                        
                        lastSyncCToE = now;
                        Thread.sleep(5000);
                        isSyncing = false;
                    }
                    
                    System.out.println();
                }
                if (!key.reset()) break;
            }            
            
        } catch (IOException ex) {
            Logger.getLogger(SyncR.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(SyncR.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static File fileChooser(String loc){
        
        JFileChooser fChooser = new JFileChooser();
        fChooser.setDialogTitle("Select " + loc + " Directory");
        fChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        File selectedFile = new File("");
        
        if(fChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
            
            int confirm = JOptionPane.showConfirmDialog(gui, "Are you sure of the selected directory?");
            if(confirm == 0){
                selectedFile = fChooser.getSelectedFile();
                logTextArea.append(loc+" directory selected \" " + selectedFile.getAbsolutePath() + " \" \n");
                System.out.println(selectedParameters);
            }
            else if(confirm == 1){
                System.out.println("no");
            } 
            else{
                logTextArea.append("No " + loc +" directory selected \n");
            }
        }

        return selectedFile;
    } 
}
