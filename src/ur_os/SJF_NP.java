package ur_os;

/**
 *
 * @author prestamour
 */
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
    for(Process p : processes){

        // Tiempo restante del proceso que estamos evaluando
        int remainingP = p.getRemainingTimeInCurrentBurst();

        // Tiempo restante del proceso más corto encontrado hasta ahora
        int remainingShortest = shortest.getRemainingTimeInCurrentBurst();

        // Si encontramos un proceso con menor tiempo de CPU
        if(remainingP < remainingShortest){

            // se convierte en el nuevo proceso más corto
            shortest = p;

        // Si tienen el mismo tiempo usamos el tie breaker del sistema
        }else if(remainingP == remainingShortest){

            shortest = tieBreaker(shortest, p);

        }
    }

    // Removemos el proceso seleccionado de la Ready Queue
    removeProcess(shortest);

    // Lo enviamos al CPU usando una interrupción del scheduler
    os.interrupt(InterruptType.SCHEDULER_RQ_TO_CPU, shortest);
}
    
    @Override
    public void newProcess(boolean cpuEmpty) {
          System.out.println("SJF_NP getNext llamado. cpuEmpty=" + cpuEmpty);
       if(cpuEmpty){
        getNext(true);
        }
    } //Non-preemtive

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {
         if(cpuEmpty){
        getNext(true);
    }
    } //Non-preemtive
    
}
