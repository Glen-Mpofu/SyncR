/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package syncr;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
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
 * @author Tshepo
 */
public class SyncManager {
    private SyncRUI ui;
    
    private final Map<String, JobState> jobs = new HashMap<>();
        
    private Map<String, Process> runningJobs = new HashMap<>();
    private final Map<String, Boolean> jobActive = new HashMap<>();
    private final Map<String, WatchService> jobWatchers = new HashMap<>();
    
    private static Long COOLDOWN = 5000l;
    
    public SyncManager(SyncRUI ui) {
        this.ui = ui;
    }
    
    public void sync(String jobName) {
        try {
            JobState state = new JobState();
            jobs.put(jobName, state);

            WatchService eWatchService = FileSystems.getDefault().newWatchService();
            state.watcher = eWatchService;

            Path sourcePath = ui.getSourceLocation().toPath();
            Path destinationPath = ui.getDestinationLocation().toPath();

            WatchKey sourceKey = sourcePath.register(eWatchService, 
                    StandardWatchEventKinds.ENTRY_MODIFY, 
                    StandardWatchEventKinds.ENTRY_CREATE, 
                    StandardWatchEventKinds.ENTRY_DELETE);

            WatchKey destinationKey = destinationPath.register(eWatchService, 
                    StandardWatchEventKinds.ENTRY_MODIFY, 
                    StandardWatchEventKinds.ENTRY_CREATE, 
                    StandardWatchEventKinds.ENTRY_DELETE);

            Map<WatchKey, Path> watchKeyMap = new HashMap<>();
            watchKeyMap.put(destinationKey, destinationPath);
            watchKeyMap.put(sourceKey, sourcePath);

            ui.appendToLogTextArea("Watching Source and Destination directories...\n");

            while (true) {
                try {
                    WatchKey key = eWatchService.take();
                    Path eventPath = watchKeyMap.get(key);

                    if (eventPath == null) continue;

                    for (WatchEvent<?> event : key.pollEvents()) {
                        long now = System.currentTimeMillis();
                        ArrayList<String> selectedParameters = ui.getSelectedParameters();

                        if (eventPath.equals(destinationPath) &&
                            (now - state.lastSyncEToC > COOLDOWN) &&
                            !state.isSyncing) {

                            state.isSyncing = true;
                            ui.appendToLogTextArea("Syncing " + destinationPath + " to " + sourcePath + "\n");

                            String command = buildCommand(destinationPath, sourcePath, selectedParameters, "dest_to_source.log");
                            state.process = Runtime.getRuntime().exec(command);

                            state.lastSyncEToC = now;
                            Thread.sleep(5000);
                            state.isSyncing = false;

                        } else if (eventPath.equals(sourcePath) &&
                                   (now - state.lastSyncCToE > COOLDOWN) &&
                                   !state.isSyncing) {

                            state.isSyncing = true;
                            ui.appendToLogTextArea("Syncing " + sourcePath + " to " + destinationPath + "\n");

                            String command = buildCommand(sourcePath, destinationPath, selectedParameters, "source_to_dest.log");
                            state.process = Runtime.getRuntime().exec(command);

                            state.lastSyncCToE = now;
                            Thread.sleep(5000);
                            state.isSyncing = false;
                        }
                    }

                    if (!key.reset()) break;

                } catch (ClosedWatchServiceException ex) {
                    ui.appendToLogTextArea("Watcher closed for job: " + jobName + "\n");
                    break;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(SyncManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void stopJob(String jobName) {
        JobState state = jobs.remove(jobName);
        if (state == null) return;

        if (state.watcher != null) {
            try { state.watcher.close(); } catch (IOException ignored) {}
        }

        if (state.process != null && state.process.isAlive()) {
            state.process.destroyForcibly();
        }

        ui.appendToLogTextArea("Stopped Sync Job " + jobName + "\n");
    }

    
    private String buildCommand(Path from,Path to,ArrayList<String>params,String logFile){
        String cmd="robocopy ";
        cmd+="\"" + from.toString() + "\" ";
        cmd+="\"" + to.toString() + "\" ";
        for(String p: params){
            cmd+=p+" ";
        }
        cmd+="/copy:DATSO /R:5 /W:1 /MT:32 /DCOPY:DATEX ";
        cmd+="/LOG:C:\\Logs\\"+logFile;
        
        return cmd;
    }    
}