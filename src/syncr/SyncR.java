/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package syncr;

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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Reception
 */
public class SyncR {

    private static Long lastSyncCToE = 0l;
    private static Long lastSyncEToC = 0l;
    private static Long COOLDOWN = 5000l;
    
    private static boolean syncing = false;
    
    public static void main(String[] args) {
        try {            
            WatchService eWatchService = FileSystems.getDefault().newWatchService();
            
            Path e_path = Paths.get("E:\\Documents");
            Path c_path = Paths.get("C:\\Users\\Reception\\Documents");
            
            WatchKey e_key = e_path.register(eWatchService, StandardWatchEventKinds.ENTRY_MODIFY, 
                    StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
            WatchKey c_key = c_path.register(eWatchService, StandardWatchEventKinds.ENTRY_MODIFY, 
                    StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
            
            Map<WatchKey, Path> watchKeyMap = new HashMap<>();
            watchKeyMap.put(c_key, c_path);
            watchKeyMap.put(e_key, e_path);
            
            System.out.println("Watching C and E directories...");
            System.out.println();
            
            
            
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
                    if(eventPath.equals(c_path) && (now - lastSyncEToC > COOLDOWN) && syncing == false){
                        
                        syncing = true;
                        System.out.println("Syncing E to C");
                        Runtime.getRuntime().exec("C:\\Users\\Reception\\OneDrive - Tshwane University of Technology\\Desktop\\Tshepo\\Sync Codes\\sync_documents_c.bat");
                        lastSyncEToC = now;
                        Thread.sleep(5000);
                        syncing = false;
                        
                    }else if(eventPath.equals(e_path) && (now - lastSyncCToE > COOLDOWN) && syncing == false){
                        syncing = true;
                        
                        System.out.println("Syncing C to E");
                        Runtime.getRuntime().exec("C:\\Users\\Reception\\OneDrive - Tshwane University of Technology\\Desktop\\Tshepo\\Sync Codes\\sync_documents.bat");
                        lastSyncCToE = now;
                        Thread.sleep(5000);
                        syncing = false;
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
