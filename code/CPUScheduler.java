// Short-term scheduler (main thread): SJF, RR (q = 5 ms), Non-Preemptive Priority with aging
import java.util.ArrayList;

public class CPUScheduler {

    public static final int QUANTUM = 5;
    public static final int AGING_PERIOD = 4;

    private SharedSystem sys;
    private ArrayList<int[]> gantt = new ArrayList<int[]>(); // {pid, start, end}

    public CPUScheduler(SharedSystem sys) {
        this.sys = sys;
    }

    // one loop iteration = one time unit (1 ms)
    public void runSimulation() throws InterruptedException {
        int total = sys.allProcesses.size();
        int finished = 0;
        int time = 0;
        int sliceUsed = 0;
        PCB running = null;
        PCB preempted = null; // RR: process whose quantum just ended

        while (finished < total) {
            sys.currentTime = time;

            // 1) long-term scheduler (Thread 2)
            sys.longTermStart.release();
            sys.longTermDone.acquire();

            // 2) RR: preempted process goes behind any new process
            if (preempted != null) {
                sys.mutex.acquire();
                sys.addToReadyQueue(preempted);
                sys.mutex.release();
                preempted = null;
            }

            // 3) medium-term scheduler (Thread 3)
            sys.mediumTermStart.release();
            sys.mediumTermDone.acquire();

            sys.mutex.acquire();
            // 4) if the CPU is free choose the next process
            if (running == null) {
                running = selectNextProcess();
                if (running != null) {
                    sys.readyQueue.remove(running);
                    running.setState(PCB.RUNNING);
                    if (running.getStartTime() == -1) {
                        running.setStartTime(time);
                    }
                    sliceUsed = 0;
                    gantt.add(new int[]{running.getProcessID(), time, time});
                }
            }

            // run the CPU for 1 ms
            if (running != null) {
                running.setRemainingTime(running.getRemainingTime() - 1);
                sliceUsed++;
                gantt.get(gantt.size() - 1)[2] = time + 1;
            } else {
                addIdleTime(time);
            }

            time++;

            // processes in the ready queue waited 1 ms
            for (int i = 0; i < sys.readyQueue.size(); i++) {
                PCB p = sys.readyQueue.get(i);
                p.setTimeInReadyQueue(p.getTimeInReadyQueue() + 1);
                if (sys.algorithm.equals("PRIORITY")) {
                    checkStarvationAndAging(p, time);
                }
            }

            // check if the running process finished or its quantum ended
            if (running != null) {
                if (running.getRemainingTime() == 0) {
                    running.setState(PCB.TERMINATED);
                    running.setTerminationTime(time);
                    // all processes arrive at time 0
                    running.setTurnaroundTime(time - 0);
                    running.setWaitingTime(running.getTurnaroundTime() - running.getBurstTime());
                    sys.freeMemory(running.getMemoryRequired());
                    finished++;
                    running = null;
                } else if (sys.algorithm.equals("RR") && sliceUsed == QUANTUM) {
                    // back to the ready queue at the start of the next time unit
                    running.setState(PCB.READY);
                    preempted = running;
                    running = null;
                }
            }
            sys.mutex.release();
        }

        // wake up the threads so they can end
        sys.simulationFinished = true;
        sys.longTermStart.release();
        sys.mediumTermStart.release();
    }

    // returns the next process for the chosen algorithm (null if ready queue is empty)
    private PCB selectNextProcess() {
        if (sys.readyQueue.isEmpty()) {
            return null;
        }

        PCB chosen = sys.readyQueue.getFirst();

        switch (sys.algorithm) {
            case "SJF":
                // shortest burst, ties -> arrival order
                for (int i = 1; i < sys.readyQueue.size(); i++) {
                    PCB p = sys.readyQueue.get(i);
                    if (p.getBurstTime() < chosen.getBurstTime()
                            || (p.getBurstTime() == chosen.getBurstTime()
                                && p.getArrivalOrder() < chosen.getArrivalOrder())) {
                        chosen = p;
                    }
                }
                break;

            case "RR":
                chosen = sys.readyQueue.getFirst();
                break;

            case "PRIORITY":
                // smallest priority number, ties -> arrival order
                for (int i = 1; i < sys.readyQueue.size(); i++) {
                    PCB p = sys.readyQueue.get(i);
                    if (p.getPriority() < chosen.getPriority()
                            || (p.getPriority() == chosen.getPriority()
                                && p.getArrivalOrder() < chosen.getArrivalOrder())) {
                        chosen = p;
                    }
                }
                break;
        }
        return chosen;
    }

    // starved = waited more than N x 5 ms in the ready queue;
    // after that, every 4 ms the priority number decreases by 1 (aging)
    private void checkStarvationAndAging(PCB p, int time) {
        if (!p.isStarved()) {
            if (p.getTimeInReadyQueue() > p.getStarvationN() * 5) {
                p.setStarved(true);
                p.setAgingCounter(0);
                System.out.println("[Time " + time + " ms] Process " + p.getProcessID()
                        + " is starved (waited " + p.getTimeInReadyQueue() + " ms > N x 5 = "
                        + (p.getStarvationN() * 5) + " ms)");
            }
        } else {
            p.setAgingCounter(p.getAgingCounter() + 1);
            if (p.getAgingCounter() == AGING_PERIOD) {
                p.setAgingCounter(0);
                if (p.getPriority() > 1) {
                    p.setPriority(p.getPriority() - 1);
                    System.out.println("[Time " + time + " ms] Aging: Process " + p.getProcessID()
                            + " priority changed to " + p.getPriority());
                }
            }
        }
    }

    // adds 1 ms of idle time to the gantt chart
    private void addIdleTime(int time) {
        if (!gantt.isEmpty() && gantt.get(gantt.size() - 1)[0] == -1) {
            gantt.get(gantt.size() - 1)[2] = time + 1;
        } else {
            gantt.add(new int[]{-1, time, time + 1});
        }
    }

    public ArrayList<int[]> getGantt() {
        return gantt;
    }
}
