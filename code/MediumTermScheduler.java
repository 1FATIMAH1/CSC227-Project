// Thread 3: usage > 90% -> swap out the largest ready process, usage < 70% -> swap one back in
// Written by: Noura bin moammar

public class MediumTermScheduler extends Thread {

    private SharedSystem sys;
    private int interventions = 0;

    public MediumTermScheduler(SharedSystem sys) {
        super("Thread-3 (Medium-Term Scheduler)");
        this.sys = sys;
    }

    @Override
    public void run() {
        while (true) {
            try {
                sys.mediumTermStart.acquire();
            } catch (InterruptedException e) {
                return;
            }

            if (sys.simulationFinished) {
                break;
            }

            if (sys.mediumTermEnabled) {
                try {
                    sys.mutex.acquire();
                } catch (InterruptedException e) {
                    return;
                }
                checkMemory();
                sys.mutex.release();
            }

            sys.mediumTermDone.release();
        }
    }

    private void checkMemory() {
        double usage = sys.getUtilization();

        // don't swap out the only process if the CPU is free
        boolean canSwapOut = sys.readyQueue.size() > 1 || (sys.readyQueue.size() == 1 && sys.cpuBusy);
        if (usage > SharedSystem.HIGH_THRESHOLD * 100 && canSwapOut) {
            swapOut(usage);
        } else if (usage < SharedSystem.LOW_THRESHOLD * 100 && !sys.suspendQueue.isEmpty()) {
            PCB p = sys.suspendQueue.getFirst();
            int after = sys.getUsedMemory() + p.getMemoryRequired();
            // safe swap in: only if usage stays <= 90% (or memory is empty)
            if (after <= SharedSystem.HIGH_THRESHOLD * SharedSystem.TOTAL_MEMORY || sys.getUsedMemory() == 0) {
                swapIn(p, usage);
            }
        }
    }

    // victim = the process with the largest memory in the ready queue
    private void swapOut(double usageBefore) {
        PCB victim = sys.readyQueue.getFirst();
        for (PCB p : sys.readyQueue) {
            if (p.getMemoryRequired() > victim.getMemoryRequired()) {
                victim = p;
            }
        }

        String oldState = victim.getState();
        sys.readyQueue.remove(victim);
        sys.freeMemory(victim.getMemoryRequired());
        victim.setState(PCB.SUSPENDED);
        sys.suspendQueue.addLast(victim);

        printIntervention(usageBefore, "Swap out", victim, oldState);
    }

    private void swapIn(PCB p, double usageBefore) {
        String oldState = p.getState();
        sys.suspendQueue.remove(p);
        sys.allocateMemory(p.getMemoryRequired());
        sys.addToReadyQueue(p);

        printIntervention(usageBefore, "Swap in", p, oldState);
    }

    private void printIntervention(double before, String action, PCB p, String oldState) {
        interventions++;
        System.out.println();
        System.out.println("[Time " + sys.currentTime + " ms]");
        System.out.println("Medium-Term Scheduler Intervention");
        System.out.printf("Memory utilization: %.2f%%%n", before);
        System.out.println("Action: " + action + " Process " + p.getProcessID());
        System.out.println("Process " + p.getProcessID() + ": " + oldState + " -> " + p.getState());
        System.out.printf("Memory utilization after intervention: %.2f%%%n", sys.getUtilization());
        System.out.println();
    }

    public int getInterventions() {
        return interventions;
    }
}
