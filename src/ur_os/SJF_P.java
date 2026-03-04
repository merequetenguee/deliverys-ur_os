package ur_os;

public class SJF_P extends Scheduler {

    public SJF_P(OS os) {
        super(os);
    }

    @Override
    public void getNext(boolean cpuEmpty) {
        if (!cpuEmpty || processes.isEmpty()) {
            return;
        }

        Process shortest = getShortestReadyProcess();
        if (shortest != null) {
            removeProcess(shortest);
            os.interrupt(InterruptType.SCHEDULER_RQ_TO_CPU, shortest);
        }
    }

    @Override
    public void newProcess(boolean cpuEmpty) {
        if (processes.isEmpty()) {
            return;
        }

        if (cpuEmpty) {
            getNext(true);
            return;
        }

        Process running = os.getProcessInCPU();
        Process shortest = getShortestReadyProcess();

        if (running == null || shortest == null) {
            return;
        }

        int shortestRemaining = shortest.getRemainingTimeInCurrentBurst();
        int runningRemaining = running.getRemainingTimeInCurrentBurst();
        boolean shorterBurst = shortestRemaining < runningRemaining;
        boolean sameBurstButHigherPriority = shortestRemaining == runningRemaining
                && tieBreaker(running, shortest).equals(shortest);

        if (shorterBurst || sameBurstButHigherPriority) {
            os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, running);
            removeProcess(shortest);
            os.interrupt(InterruptType.SCHEDULER_RQ_TO_CPU, shortest);
        }
    }

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {
        newProcess(cpuEmpty);
    }

    @Override
    public void update() {
        if (os.isCPUEmpty()) {
            getNext(true);
        } else {
            newProcess(false);
        }
    }

    private Process getShortestReadyProcess() {
        Process shortest = null;

        for (Process candidate : processes) {
            if (shortest == null) {
                shortest = candidate;
                continue;
            }

            int candidateRemaining = candidate.getRemainingTimeInCurrentBurst();
            int shortestRemaining = shortest.getRemainingTimeInCurrentBurst();

            if (candidateRemaining < shortestRemaining) {
                shortest = candidate;
            } else if (candidateRemaining == shortestRemaining) {
                shortest = tieBreaker(shortest, candidate);
            }
        }

        return shortest;
    }
}
