package ur_os;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton estimator that keeps per-process next-burst predictions using
 * exponential averaging:
 *
 * τ_{n+1} = α * t_n + (1 - α) * τ_n
 *
 * where t_n is the actual measured burst and τ_n is the previous estimate.
 *
 * After each real burst completes, call recordActual() to update the estimate
 * AND accumulate bias statistics.
 */
public class BurstEstimator {

    // ── Singleton ──────────────────────────────────────────────────────────────
    private static BurstEstimator instance;

    public static BurstEstimator getInstance() {
        if (instance == null) {
            instance = new BurstEstimator();
        }
        return instance;
    }

    /** Call this at the start of each simulation run to reset all state. */
    public static void reset() {
        instance = new BurstEstimator();
    }

    // ── Configuration ──────────────────────────────────────────────────────────
    private static final double DEFAULT_ALPHA = 0.5;
    private static final double INITIAL_ESTIMATE = 5.0; // τ_0 for every new process

    private final double alpha;

    // ── Per-process estimates pid -> current τ ────────────────────────────────
    private final Map<Integer, Double> estimates = new HashMap<>();

    // ── Bias tracking ─────────────────────────────────────────────────────────
    private int totalPredictions = 0;
    private int underEstimations = 0; // predicted < actual (under-estimate)
    private int overEstimations = 0; // predicted > actual (over-estimate)
    // exact hits (predicted == actual) are counted in neither bucket

    // ── Constructor ───────────────────────────────────────────────────────────
    private BurstEstimator() {
        this(DEFAULT_ALPHA);
    }

    private BurstEstimator(double alpha) {
        this.alpha = alpha;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns the current estimate τ_n for the given process.
     * If no estimate exists yet, returns the initial estimate τ_0.
     */
    public double getEstimate(int pid) {
        return estimates.getOrDefault(pid, INITIAL_ESTIMATE);
    }

    /**
     * Called when a CPU burst for the given process has truly finished.
     *
     * @param pid         process id
     * @param actualBurst the real number of CPU cycles just consumed (t_n)
     */
    public void recordActual(int pid, int actualBurst) {
        double tau = estimates.getOrDefault(pid, INITIAL_ESTIMATE);

        // Always compare the current estimate against the real burst,
        // including τ_0 (initial estimate) for the very first burst.
        if (tau < actualBurst) {
            underEstimations++;
        } else if (tau > actualBurst) {
            overEstimations++;
        }
        totalPredictions++;

        // Exponential averaging: τ_{n+1} = α * t_n + (1 - α) * τ_n
        double nextTau = alpha * actualBurst + (1.0 - alpha) * tau;
        estimates.put(pid, nextTau);

    }

    // ── Bias Report ───────────────────────────────────────────────────────────

    public int getTotalPredictions() {
        return totalPredictions;
    }

    public int getUnderEstimations() {
        return underEstimations;
    }

    public int getOverEstimations() {
        return overEstimations;
    }

    /**
     * Percentage of predictions that were strictly under the actual burst.
     * Returns 0 if no predictions have been recorded.
     */
    public double getUnderPercent() {
        if (totalPredictions == 0)
            return 0.0;
        return 100.0 * underEstimations / totalPredictions;
    }

    /**
     * Percentage of predictions that were strictly over the actual burst.
     */
    public double getOverPercent() {
        if (totalPredictions == 0)
            return 0.0;
        return 100.0 * overEstimations / totalPredictions;
    }

    /** Human-readable bias line for the performance report. */
    public String getBiasReport() {
        return String.format("Estimation Bias: %.1f%% Under / %.1f%% Over  (α=%.2f, n=%d predictions)",
                getUnderPercent(), getOverPercent(), alpha, totalPredictions);
    }
}
