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

    private static Map<String, JCheckBox> robocopyParameters;
    private static SyncRUI ui;
    
    public SyncR(){  
        robocopyParameters = new HashMap<>();
    }
    
    public static void main(String[] args) { 
        initializeRobocopyParameters();
        ui = new SyncRUI();
    }
    
    public static void initializeRobocopyParameters(){
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

    public static Map<String, JCheckBox> getRobocopyParameters() {
        return robocopyParameters;
    }
}
