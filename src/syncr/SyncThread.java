/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package syncr;

/**
 *
 * @author Tshepo
 */
public class SyncThread extends Thread{
    private SyncRUI ui;
    
    public void run(){
        SyncR.initializeRobocopyParameters();
        ui = new SyncRUI();
    }
}
