/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package syncr;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Reception
 */
public class SyncManager {
    private SyncRUI ui;
    private static Long lastSyncCToE = 0l;
    private static Long lastSyncEToC = 0l;
    private static Long COOLDOWN = 5000l;
    private static boolean isSyncing = false;
    
    public SyncManager(SyncRUI ui) {
        this.ui = ui;
    }
    
    public void sync(){
        try {            
            WatchService eWatchService = FileSystems.getDefault().newWatchService();
            
            Path sourcePath = ui.getSourceLocation().toPath();
            Path destinationPath = ui.getDestinationLocation().toPath();
            
            WatchKey sourceKey = sourcePath.register(eWatchService, StandardWatchEventKinds.ENTRY_MODIFY, 
                    StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
            
            WatchKey destinationKey = destinationPath.register(eWatchService, StandardWatchEventKinds.ENTRY_MODIFY, 
                    StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
            
            Map<WatchKey, Path> watchKeyMap = new HashMap<>();
            watchKeyMap.put(destinationKey, destinationPath);
            watchKeyMap.put(sourceKey, sourcePath);
            
            ui.appendToLogTextArea("Watching Source and Destination directories...\n");

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
                    ArrayList<String> selectedParameters = ui.getSelectedParameters();
                    
                        String commandString = "robocopy \"" + destinationPath + "\"  \"" + sourcePath + "\" ";
                        for (int i = 0; i < selectedParameters.size(); i++) {
                            commandString+= selectedParameters.get(i) + " ";
                        }
                        commandString += "/copy:DATSO /R:5 /W:1 /MT:32 /DCOPY:DATEX /LOG:C:\\Logs\\source_robocopy_sync.log";
                    if(eventPath.equals(destinationPath) && (now - lastSyncEToC > COOLDOWN) && isSyncing == false){
                        
                        isSyncing = true;
                        ui.appendToLogTextArea("Syncing " + destinationPath + " to " + sourcePath + "\n");

                        Runtime.getRuntime().exec(commandString);
                        System.out.println(commandString);
                        lastSyncEToC = now;
                        Thread.sleep(5000);
                        isSyncing = false;
                        
                    }else if(eventPath.equals(sourcePath) && (now - lastSyncCToE > COOLDOWN) && isSyncing == false){
                        isSyncing = true;
                        
                        ui.appendToLogTextArea("Syncing " + sourcePath + " to " + destinationPath);
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
}
