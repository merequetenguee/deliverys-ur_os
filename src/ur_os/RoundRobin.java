/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ur_os;

/**
 *
 * @author prestamour
 */
public class RoundRobin extends Scheduler{

    int q;
    int currentq=0;
    int cont;
    boolean multiqueue;
    
    RoundRobin(OS os){
        super(os);
        q = 5;
        cont=0;
        currentq=5;
    }
    
    RoundRobin(OS os, int q){
        this(os);
        this.q = q;
        currentq=q;
    }

    RoundRobin(OS os, int q, boolean multiqueue){
        this(os);
        this.q = q;
        this.multiqueue = multiqueue;
        currentq=q;
    }
    

    
    void resetCounter(){
        cont=0;
    }
   
    @Override
    public void getNext(boolean cpuEmpty) {
        //System.out.println("currentlist");
        //for (  Process pro :this.processes){

            //System.out.println(pro.pid);
        //}
        //System.out.print(currentq);

        if (cpuEmpty){
            currentq=q;

            this.os.interrupt(InterruptType.SCHEDULER_RQ_TO_CPU,this.processes.getFirst());
            this.removeProcess(this.processes.getFirst());
            currentq=currentq-1;
            //System.out.print(currentq);
            //System.out.println("onif");




        } else{
            //System.out.println("onelse");

                if (currentq==0 && this.os.getProcessInCPU().getRemainingTimeInCurrentBurst()>=1){
                    this.os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ,this.os.getProcessInCPU());



                    this.getNext(true);


                } else{
                    currentq=currentq-1;
                }




        }







        //Insert code here
    }
    
    
    @Override
    public void newProcess(boolean cpuEmpty) {

    } //Non-preemtive in this event

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {} //Non-preemtive in this event
    
}
