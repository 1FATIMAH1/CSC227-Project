// Process Control Block: all the information of one process
public class PCB {

    public static final String NEW = "New";
    public static final String READY = "Ready";
    public static final String RUNNING = "Running";
    public static final String SUSPENDED = "Suspended";
    public static final String TERMINATED = "Terminated";

    private int processID;
    private String state;
    private int burstTime;
    private int remainingTime;
    private int priority;         // 1 = highest , 30 = lowest
    private int originalPriority; // priority before aging
    private int memoryRequired;
    private int waitingTime;
    private int turnaroundTime;
    private int startTime;        // first time the process gets the CPU
    private int terminationTime;
    private int arrivalOrder;     // order in job.txt (used for ties)

    // used for starvation and aging (priority algorithm)
    private int timeInReadyQueue;
    private int starvationN;      // N = processes in ready queue when it entered
    private boolean starved;
    private int agingCounter;     // ms since the last aging step

    public PCB(int processID, int burstTime, int priority, int memoryRequired, int arrivalOrder) {
        this.processID = processID;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = priority;
        this.originalPriority = priority;
        this.memoryRequired = memoryRequired;
        this.arrivalOrder = arrivalOrder;
        this.state = NEW;
        this.startTime = -1;      // -1 means it did not start yet
        this.terminationTime = 0;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
        this.timeInReadyQueue = 0;
        this.starvationN = 0;
        this.starved = false;
        this.agingCounter = 0;
    }

    public int getProcessID() {
        return processID;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getOriginalPriority() {
        return originalPriority;
    }

    public int getMemoryRequired() {
        return memoryRequired;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    public int getTurnaroundTime() {
        return turnaroundTime;
    }

    public void setTurnaroundTime(int turnaroundTime) {
        this.turnaroundTime = turnaroundTime;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getTerminationTime() {
        return terminationTime;
    }

    public void setTerminationTime(int terminationTime) {
        this.terminationTime = terminationTime;
    }

    public int getArrivalOrder() {
        return arrivalOrder;
    }

    public int getTimeInReadyQueue() {
        return timeInReadyQueue;
    }

    public void setTimeInReadyQueue(int timeInReadyQueue) {
        this.timeInReadyQueue = timeInReadyQueue;
    }

    public int getStarvationN() {
        return starvationN;
    }

    public void setStarvationN(int starvationN) {
        this.starvationN = starvationN;
    }

    public boolean isStarved() {
        return starved;
    }

    public void setStarved(boolean starved) {
        this.starved = starved;
    }

    public int getAgingCounter() {
        return agingCounter;
    }

    public void setAgingCounter(int agingCounter) {
        this.agingCounter = agingCounter;
    }

    public String toString() {
        return "P" + processID + " [burst=" + burstTime + ", priority=" + priority
                + ", memory=" + memoryRequired + "MB, state=" + state + "]";
    }
}
