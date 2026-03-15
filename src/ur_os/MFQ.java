/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ur_os;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author prestamour
 */
public class MFQ extends Scheduler{

    int currentScheduler;
    
    private ArrayList<Scheduler> schedulers;
    //This may be a suggestion... you may use the current sschedulers to create the Multilevel Feedback Queue, or you may go with a more tradicional way
    //based on implementing all the queues in this class... it is your choice. Change all you need in this class.
    
    MFQ(OS os){
        super(os);
        currentScheduler = -1;
        schedulers = new ArrayList();
    }
    
    MFQ(OS os, Scheduler... s){ //Received multiple arrays
        this(os);
        schedulers.addAll(Arrays.asList(s));
        if(s.length > 0)
            currentScheduler = 0;
    }
        
    @Override
    public void addProcess(Process p){
        System.out.println(p.getState());
        System.out.println("p sch:"+p.currentScheduler);
        System.out.println("currentsch:"+currentScheduler);
        if (p.getState()==ProcessState.NEW){
            System.out.println("in new");
            System.out.println("Pid:"+p.getPid());
            p.currentScheduler=0;

            schedulers.get(0).addProcess(p);
            this.currentScheduler=0;
        } else if (p.getState()==ProcessState.CPU) {
            //into second
            System.out.println("in second");
            System.out.println("Pid:"+p.getPid());
            if (p.currentScheduler<schedulers.size()-1){
                schedulers.get(p.currentScheduler+1).addProcess(p);
                p.currentScheduler++;


            }
            System.out.println("loaded");








        } else if (p.getState()==ProcessState.IO){
            System.out.println("in IO");
            System.out.println("Pid:"+p.getPid());
            p.currentScheduler=0;
            if  (!(p.currentScheduler>this.currentScheduler)){
                this.currentScheduler=p.currentScheduler;
            }

            this.schedulers.get(p.currentScheduler).processes.addFirst(p);



        }
        while (schedulers.get(this.currentScheduler).isEmpty() && this.currentScheduler<schedulers.size()-1){
            this.currentScheduler=this.currentScheduler+1;

        }
        System.out.println("currentsch:"+this.currentScheduler);
        //Overwriting the parent's addProcess(Process p) method may be necessary in order to decide what to do with process coming from the CPU.
        
    }
    
    void defineCurrentScheduler(){
        //This methos is siggested to help you find the scheduler that should be the next in line to provide processes... perhaps the one with process in the queue?
    }
    
   
    @Override
    public void getNext(boolean cpuEmpty) {
        System.out.println(this.currentScheduler);
        System.out.println("cpu empty:"+cpuEmpty);
        //if (schedulers.get(currentScheduler).isEmpty()){
            //return;
        //}

       schedulers.get(this.currentScheduler).getNext(cpuEmpty);
        //Suggestion: now that you know on which scheduler a process is, you need to keep advancing that scheduler. If it a preemptive one, you need to notice the changes
        //that it may have caused and verify if the change is coherent with the priority policy for the queues.
  
    }
    
    @Override
    public void newProcess(boolean cpuEmpty) {} //Non-preemtive in this event

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {} //Non-preemtive in this event
    
}
