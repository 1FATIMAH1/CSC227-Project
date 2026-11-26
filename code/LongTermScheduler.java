// Thread 2: moves jobs from the job queue to the ready queue (FIFO) if there is enough free memory

public class LongTermScheduler extends Thread {

    private SharedSystem sys;

    public LongTermScheduler(SharedSystem sys) {
        super("Thread-2 (Long-Term Scheduler)");
        this.sys = sys;
    }

    @Override
    public void run() {
        while (true) {
            try {
                sys.longTermStart.acquire();
            } catch (InterruptedException e) {
                return;
            }

            if (sys.simulationFinished) {
                break;
            }

            try {
                sys.mutex.acquire();
            } catch (InterruptedException e) {
                return;
            }
            loadJobs();
            sys.mutex.release();

            sys.longTermDone.release();
        }
    }

    private void loadJobs() {
        // a swapped out process gets the free memory first
        if (sys.mediumTermEnabled && !sys.suspendQueue.isEmpty()) {
            return;
        }

        while (!sys.jobQueue.isEmpty()) {
            PCB next = sys.jobQueue.getFirst();

            if (next.getMemoryRequired() <= sys.getFreeMemory()) {
                sys.jobQueue.removeFirst();
                sys.allocateMemory(next.getMemoryRequired());
                sys.addToReadyQueue(next);
                System.out.println("[Time " + sys.currentTime + " ms] Long-Term Scheduler: Process "
                        + next.getProcessID() + " loaded to ready queue (memory used: "
                        + sys.getUsedMemory() + "/" + SharedSystem.TOTAL_MEMORY + " MB)");
            } else {
                break; // not enough memory, wait until memory is freed
            }
        }
    }
}
