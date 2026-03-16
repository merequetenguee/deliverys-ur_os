package ur_os;

import static ur_os.InterruptType.SCHEDULER_RQ_TO_CPU;


/**
 * SJF with Next-Burst Estimation (Non-Preemptive).
 *
 * Ordering key: the estimated next CPU burst  τ_n  (shorter = higher priority).
 * Estimation is updated dynamically each time a real burst finishes:
 *
 *   τ_{n+1} = α * t_n  +  (1 - α) * τ_n
 *
 * Bias statistics (under-estimates vs over-estimates) are tracked in
 * BurstEstimator so SystemOS can report them.
 */
public class SJF_E extends Scheduler {


    public SJF_E(OS os) {
        super(os);
        System.out.println(">>> SJF_E (Next Burst Estimation) scheduler ACTIVATED");
        BurstEstimator.reset(); // start fresh each run
    }

    // ── addProcess ────────────────────────────────────────────────────────────

    @Override
    public void addProcess(Process p) {
        // --- Dynamic estimation hook ---
        // In non-preemptive SJF_E a process never returns from the CPU mid-burst.
        // When a CPU burst finishes normally the OS sends it to the IOQueue.
        // After IO finishes it returns here with state == IO.
        // At that moment the burst list looks like:
        //   [CPU(done), IO(done), --> next CPU]   i.e. currentBurst points to next CPU
        // So the CPU burst that just ran is at index currentBurst - 2.
        if (p.getState() == ProcessState.IO) {
            recordCPUBurstBeforeIO(p);
        }

        p.setState(ProcessState.READY);
        processes.add(p);
    }

    /**
     * When the process returns from IO, its currentBurst already advanced past
     * the IO burst, so:
     *   currentBurst - 1 = the IO burst that just finished
     *   currentBurst - 2 = the CPU burst we want to measure
     */
    private void recordCPUBurstBeforeIO(Process p) {
        ProcessBurstList pbl = p.getPBL();
        int cpuIdx = pbl.currentBurst - 2;
        if (cpuIdx >= 0 && pbl.bursts.get(cpuIdx).getType() == ProcessBurstType.CPU) {
            int actualCycles = pbl.bursts.get(cpuIdx).getCycles();
            BurstEstimator.getInstance().recordActual(p.getPid(), actualCycles);
            System.out.printf("[SJF_E] PID %d: actual CPU burst=%d, next estimate=%.2f%n",
                    p.getPid(), actualCycles,
                    BurstEstimator.getInstance().getEstimate(p.getPid()));
        }
    }

    // ── getNext ───────────────────────────────────────────────────────────────

    @Override
    public void getNext(boolean cpuEmpty) {
        if (!cpuEmpty || processes.isEmpty()) {
            return;
        }

        // Pick the process with the smallest estimated next burst.
        Process shortest = processes.getFirst();
        for (Process candidate : processes) {
            double candEst = BurstEstimator.getInstance().getEstimate(candidate.getPid());
            double shortEst = BurstEstimator.getInstance().getEstimate(shortest.getPid());

            if (candEst < shortEst) {
                shortest = candidate;
            } else if (candEst == shortEst) {
                shortest = tieBreaker(shortest, candidate);
            }
        }

        processes.remove(shortest);
        os.interrupt(SCHEDULER_RQ_TO_CPU, shortest);
        addContextSwitch();

        System.out.printf("[SJF_E] Dispatching PID %d (estimate=%.2f)%n",
                shortest.getPid(),
                BurstEstimator.getInstance().getEstimate(shortest.getPid()));
    }

    // ── Preemption hooks (non-preemptive) ─────────────────────────────────────

    @Override
    public void newProcess(boolean cpuEmpty) {
        // Non-preemptive: never interrupt current execution.
    }

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {
        // Non-preemptive: never interrupt current execution.
    }
}
