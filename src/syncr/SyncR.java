/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package syncr;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JCheckBox;

/**
 *
 * @author Tshepo
 */
public class SyncR{

    private static Map<String, JCheckBox> robocopyParameters;
    private static SyncRUI ui;
    
    public SyncR(){  
        robocopyParameters = new HashMap<>();
    }
    
    public static void main(String[] args) { 
        syncThread();
    }
    
    public static void syncThread(){        
        initializeNewSyncJob();
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
    
    public static void initializeNewSyncJob(){
        SyncThread syncThread = new SyncThread();
        syncThread.run();
    }
}
