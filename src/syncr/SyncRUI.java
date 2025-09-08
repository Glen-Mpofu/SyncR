/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package syncr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
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
import javax.swing.JTextField;

/**
 *
 * @author Tshepo
 */
public class SyncRUI {

    private static JFrame gui; 
    
    private JPanel mainpanel; 
    private JLabel heading;
    private JTextField outputTf;
    private static JTextArea logTextArea;
    
    private JButton sourceBtn;
    
    private JButton destinationBtn;
    
    private JButton syncDataBtn;
    
    private ArrayList<String> selectedParameters = new ArrayList<>();

    private File destinationLocation;
    private File sourceLocation;
    
    private SyncManager syncManager = new SyncManager(this);
    private ConfigManager configManager = new ConfigManager(this);
    
    private final JLabel copyParametersLbl = new JLabel("Copy Parameters", JLabel.CENTER);
    
    private JButton syncOther;
    
    //menu
    private JMenu menu;
    private JMenuBar menuBar;    
    private JMenuItem syncJobMenuItem;
    
    public SyncRUI() {
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
        logTextArea.append("Please click the buttons \"Source\" and \"Destination\" to set the locations of the folders to sync\n");
        JScrollPane scrollPane = new JScrollPane(logTextArea);

        // Buttons at the bottom
        JPanel locationsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        sourceBtn = new JButton("Source");
        sourceBtn.addActionListener((e) -> {
            sourceLocation = configManager.fileChooser("Source");
        });
       
        destinationBtn = new JButton("Destination");
        destinationBtn.addActionListener((e) -> {
            destinationLocation = configManager.fileChooser("Destination");
        });

        locationsPanel.add(sourceBtn);
        locationsPanel.add(destinationBtn);     
       
        JPanel bottomPanel = new JPanel(new FlowLayout());
        syncDataBtn = new JButton("Sync Drives/Folders");
        syncDataBtn.addActionListener((e) -> {
            if(sourceLocation != null || destinationLocation != null){
                new Thread(() -> syncManager.sync()).start(); 
            }
            else{
                JOptionPane.showMessageDialog(gui, "Please click the buttons \"Source\" and \"Destination\" to set the locations of the folders to sync\n");
            }
        });

        syncOther = new JButton("Sync Other");
        syncOther.addActionListener((e) -> {
            int opt = JOptionPane.showConfirmDialog(gui, "Are you sure you'd like to start another \"Sync Session\"? ");
            if(opt == JOptionPane.YES_OPTION){
                System.out.println("new session");
                
                //save the data of the previous 
                configManager.saveSyncSession();
            }
        });
        
        bottomPanel.add(syncDataBtn, BorderLayout.NORTH);
        bottomPanel.add(syncOther, BorderLayout.CENTER);
        
        JPanel topPnl = new JPanel(new BorderLayout());
        topPnl.add(scrollPane, BorderLayout.NORTH);
       
        JPanel middlePnl = new JPanel(new BorderLayout());
            JPanel copyParamPnl = new JPanel(new GridLayout(4, 2));        
            Set<Map.Entry<String, JCheckBox>> entries = SyncR.getRobocopyParameters().entrySet();
             
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
                            logTextArea.append("\""+checkBox.getText()+"\" parameter added\n");
                        }                       
                    }
                    else{
                        selectedParameters.remove(paramKey);
                        logTextArea.append("\""+checkBox.getText()+"\" parameter removed\n");
                    }
                });
    
            }
        middlePnl.add(copyParametersLbl, BorderLayout.NORTH);
        middlePnl.add(copyParamPnl, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(locationsPanel, BorderLayout.CENTER);
        buttonPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        //menu
        menuBar = new JMenuBar();
        
        menu = new JMenu("Sync Jobs");
        menuBar.add(menu);
                
        JMenuItem saveJobMenuItem = new JMenuItem("One");
        menu.add(saveJobMenuItem);
        
        mainpanel.add(topPnl, BorderLayout.NORTH);
        mainpanel.add(middlePnl, BorderLayout.CENTER);
        mainpanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add main panel to frame
        gui.setJMenuBar(menuBar);
        gui.add(mainpanel);
        gui.setVisible(true);  

    }

    public JFrame getGui() {
        return gui;
    }

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
  
}
