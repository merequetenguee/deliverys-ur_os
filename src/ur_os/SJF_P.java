/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ur_os;

public class SJF_P extends Scheduler {

    public SJF_P(OS os) {
        super(os);
    }

    //  Seleccionar el siguiente proceso y pasarlo a la CPU
    @Override
    public void getNext(boolean cpuEmpty) {
       if (!cpuEmpty || processes.isEmpty()) {
            return;
        }
        Process shortest=processes.getFirst();
        for( Process candidate: processes){
            int candidaterem=candidate.getRemainingTimeInCurrentBurst();
            int shortestrem= shortest.getRemainingTimeInCurrentBurst();
            if (candidaterem==shortestrem && candidate.getPid()<shortest.getPid()){

                shortest=candidate;

            }
        }
      processes.remove(shortest);
      os.interrupt(InterruptType.SCHEDULER_RQ_TO_CPU, shortest);
      addContextSwitch();

    }

    //  Cuando entra un proceso nuevo
    @Override
    public void newProcess(boolean cpuEmpty) {
        if(cpuEmpty){
            getNext(true);
        }
        Process running=os.getProcessInCPU();
        if(running==null|| processes.isEmpty()){
            return;

        }
        Process shortest = processes.getFirst();
        for (Process candidate : processes) {
            int candidateRemaining = candidate.getRemainingTimeInCurrentBurst();
            int shortestRemaining = shortest.getRemainingTimeInCurrentBurst();

             if (candidateRemaining < shortestRemaining
                    || (candidateRemaining == shortestRemaining && candidate.getPid() < shortest.getPid())) {
                shortest = candidate;
            }
        }
        if (shortest.getRemainingTimeInCurrentBurst() < running.getRemainingTimeInCurrentBurst()) {
            os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, shortest);
            processes.remove(shortest);
            addContextSwitch();

        }
    }

    // Cuando un proceso vuelve de I/O
    @Override
    public void IOReturningProcess(boolean cpuEmpty) {
        newProcess(cpuEmpty); // misma lógica que newProcess
    }

    @Override
    public String toString() {
        return "SJF_P ReadyQueue: " + processes.toString();
    }
}
