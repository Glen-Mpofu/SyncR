/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package syncr;

import java.nio.file.WatchService;

/**
 *
 * @author Reception
 */
public class JobState {
    long lastSyncCToE = 0L;
    long lastSyncEToC = 0L;
    boolean isSyncing = false;
    Process process;
    WatchService watcher;
}
