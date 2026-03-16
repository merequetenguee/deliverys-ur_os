package ur_os;
/**
 *
 * @author prestamour
 */
import static ur_os.InterruptType.SCHEDULER_RQ_TO_CPU;
public class SJF_NP extends Scheduler{

    @SuppressWarnings("unused")
     public SJF_NP(OS os){
        super(os);
        System.out.println(">>> SJF_NP scheduler ACTIVADO");
        
    }
    
   
   @Override
public void getNext(boolean cpuEmpty) {

    // Si el CPU NO está vacío, no hacemos nada.
    // En SJF Non-Preemptive nunca interrumpimos un proceso en ejecución.
    if(!cpuEmpty){
        return;
    }

    // Si no hay procesos en la Ready Queue tampoco hay nada que ejecutar
    if(processes.isEmpty()){
        return;
    }

    // Tomamos el primer proceso de la lista como candidato inicial
    Process shortest = processes.getFirst();

    // Recorremos todos los procesos de la Ready Queue
    for (Process p : processes) {
            if (p.getRemainingTimeInCurrentBurst() <
                shortest.getRemainingTimeInCurrentBurst()) {

                shortest = p;

            } else if (p.getRemainingTimeInCurrentBurst() ==
                       shortest.getRemainingTimeInCurrentBurst()) {

                shortest = tieBreaker(shortest, p);
            }
        }

        // Removerlo de la Ready Queue
        processes.remove(shortest);

        // Enviarlo al CPU
        os.interrupt(SCHEDULER_RQ_TO_CPU, shortest);

        // Contar el cambio de contexto
        addContextSwitch();
}
    
    @Override
    public void newProcess(boolean cpuEmpty) {
      
    } //Non-preemtive

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {
 
    } //Non-preemtive
    
}
