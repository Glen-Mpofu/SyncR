/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package syncr;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 *
 * @author Tshepo
 */
public class SyncManager {
    private SyncRUI ui;
    private ConfigManager configManager;
    
    private final Map<String, JobState> jobs = new HashMap<>();
        
    private Map<String, Process> runningJobs = new HashMap<>();
    private final Map<String, Boolean> jobActive = new HashMap<>();
    private final Map<String, WatchService> jobWatchers = new HashMap<>();
    
    private static Long COOLDOWN = 5000l;
    
    private boolean syncTracker;
    
    public SyncManager(SyncRUI ui, ConfigManager configManager) {
        this.ui = ui;
        this.configManager = configManager;
    }
    
    public void sync(String jobName) {
        try {
            JTextArea area = ui.getLogAreaForJob(jobName);
            //disable the sync button when a sync job is taking place
            ui.getSyncDataBtn().setEnabled(false);
            
            JobState state = new JobState();
            jobs.put(jobName, state);

            WatchService eWatchService = FileSystems.getDefault().newWatchService();
            state.watcher = eWatchService;

            Path sourcePath = ui.getSourceLocation().toPath();
            Path destinationPath = ui.getDestinationLocation().toPath();
            
            Map<WatchKey, Path> watchKeyMap = new HashMap<>();
            registerAll(sourcePath, eWatchService, watchKeyMap);
            registerAll(destinationPath, eWatchService, watchKeyMap);

            area.append("Watching Source and Destination directories...\n");
           
            while (state.isSyncing || true) {
                try {
                    WatchKey key = eWatchService.take();
                    Path eventPath = watchKeyMap.get(key);

                    if (eventPath == null) continue;

                    for (WatchEvent<?> event : key.pollEvents()) {
                        long now = System.currentTimeMillis();
                        ArrayList<String> selectedParameters = ui.getSelectedParameters();

                        //if a new subfolder is created
                        if(event.kind() == StandardWatchEventKinds.ENTRY_CREATE){
                            Path child = eventPath.resolve((Path) event.context());
                            if(Files.isDirectory(child)){
                                registerAll(child, eWatchService, watchKeyMap);
                            }
                        }
                            configManager.saveIsSyncing(true);
                            syncTracker = true;
                        
                        //two way sync
                        if(!(ui.getOneWay().isSelected())){
                            if (eventPath.startsWith(sourcePath) &&
                                (now - state.lastSyncDToS > COOLDOWN) &&
                                !state.isSyncing) {

                                // files/folders in the source will be taken to the destination 
                                state.isSyncing = true;
                                
                                
                                area.append("Two Way Syncing " + sourcePath + " to " + destinationPath + "\n");
                                
                                String command = buildCommand(sourcePath, destinationPath, selectedParameters, "dest_to_source.log");
                                state.process = Runtime.getRuntime().exec(command);

                                state.lastSyncDToS = now;
                                Thread.sleep(5000);
                                                             
                                state.isSyncing = false;

                            } else if (eventPath.startsWith(destinationPath) &&
                                       (now - state.lastSyncSToD > COOLDOWN) &&
                                       !state.isSyncing) { 

                                // files/folders in the destination will be taken to the source 
                                state.isSyncing = true;
                                area.append("Two Way Syncing " + destinationPath + " to " + sourcePath + "\n");
                                
                                String command = buildCommand(destinationPath, sourcePath, selectedParameters, "source_to_dest.log");
                                state.process = Runtime.getRuntime().exec(command);

                                state.lastSyncSToD = now;
                                Thread.sleep(5000);
                                state.isSyncing = false;
                            }
                        }
                        //one way
                        else{
                            if(eventPath.startsWith(destinationPath)) continue;
                            
                            if (eventPath.startsWith(sourcePath) &&
                                (now - state.lastSyncDToS > COOLDOWN) &&
                                !state.isSyncing) {

                                // files/folders in the source will be taken to the destination 
                                state.isSyncing = true;
                                area.append("One Way Syncing " + sourcePath + " to " + destinationPath + "\n");
                                
                                ArrayList<String> param = new ArrayList<>(selectedParameters);                                                                    
                                    //if(!param.contains("/E")) param.add("/E");                                       
                                
                                selectedParameters = param;
                                
                                String command = buildCommand(sourcePath, destinationPath, param, "one_way_sync.log");
                                state.process = Runtime.getRuntime().exec(command);

                                state.lastSyncDToS = now;
                                Thread.sleep(5000);
                                state.isSyncing = false;
                            }                            
                        }
                    }

                    if (!key.reset()) break;

                } catch (ClosedWatchServiceException ex) {
                    area.append("Watcher closed for job: " + jobName + "\n");
                    break;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(SyncManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void stopJob(String jobName) {
        JobState state = jobs.remove(jobName);
        state.isSyncing = false;
        configManager.saveIsSyncing(false);
        syncTracker = false;
        
        if (state == null) return;

        if (state.watcher != null) {
            try { state.watcher.close(); } catch (IOException ignored) {}
        }

        if (state.process != null && state.process.isAlive()) {
            state.process.destroyForcibly();
        }

        SwingUtilities.invokeLater(() -> {
            JTextArea area = ui.getLogAreaForJob(jobName);
            ui.getSyncDataBtn().setEnabled(true);
            area.append("Stopped " + jobName + "\n"); 
        });        
    }

    
    private String buildCommand(Path from, Path to,ArrayList<String>params,String logFile){
        String cmd="robocopy ";
        cmd+="\"" + from.toString() + "\" ";
        cmd+="\"" + to.toString() + "\" ";
        for(String p: params){
            cmd+=p+" ";
        }
        
        // removed SO from /copy and TEX from /DCOPY to ensure only data and attributes 
        // of a file are copied. this reduces the buggy two way sync that happens when a one way sync is run
        // this in no way affects the actual 2 way sync the app also does
        cmd+="/copy:DAT /R:5 /W:5 /MT:32 /DCOPY:DAT ";
        cmd+="/LOG:C:\\Logs\\"+logFile;        
        
        System.out.println(cmd);
        return cmd;
    }    
    
    // Method for registering the subdirectories to ensure syncing of files deeper in the directory tree
    public void registerAll(final Path start, WatchService watcher, Map<WatchKey, Path> watchKeyMap) throws IOException{
        // register directory and subdirectories

        // this walks through the subdirectories inside the main folder
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException{
                WatchKey key = dir.register(watcher, 
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE
                );
                watchKeyMap.put(key, dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    public boolean getSyncing(){
        return syncTracker;
    }
}