// Data shared by all threads: queues, memory, clock and semaphores
// Written by: Noura bin moammar

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class SharedSystem {

    public static final int TOTAL_MEMORY = 2048;     // MB
    public static final double HIGH_THRESHOLD = 0.90; // swap out when usage > 90%
    public static final double LOW_THRESHOLD = 0.70;  // swap in when usage < 70%

    public LinkedList<PCB> jobQueue = new LinkedList<PCB>();
    public LinkedList<PCB> readyQueue = new LinkedList<PCB>();
    public LinkedList<PCB> suspendQueue = new LinkedList<PCB>(); // swapped out processes only
    public ArrayList<PCB> allProcesses = new ArrayList<PCB>();

    private int usedMemory = 0;

    public int currentTime = 0;              // simulation clock (1 unit = 1 ms)
    public boolean simulationFinished = false;
    public boolean mediumTermEnabled = false;
    public String algorithm = "";
    public boolean cpuBusy = false;          // true if a process is running on the CPU

    // binary semaphore used as a mutex lock for the queues and the memory
    // (acquire() = wait(), release() = signal())
    public Semaphore mutex = new Semaphore(1);

    // ordering semaphores: main thread releases xxxStart, the thread works, then releases xxxDone
    public Semaphore longTermStart = new Semaphore(0);
    public Semaphore longTermDone = new Semaphore(0);
    public Semaphore mediumTermStart = new Semaphore(0);
    public Semaphore mediumTermDone = new Semaphore(0);

    // the methods below are called only while holding the mutex

    public void allocateMemory(int mb) {
        usedMemory += mb;
    }

    public void freeMemory(int mb) {
        usedMemory -= mb;
    }

    public int getUsedMemory() {
        return usedMemory;
    }

    public int getFreeMemory() {
        return TOTAL_MEMORY - usedMemory;
    }

    // memory utilization in percent
    public double getUtilization() {
        return (usedMemory * 100.0) / TOTAL_MEMORY;
    }

    // adds a process to the ready queue and sets N for starvation
    public void addToReadyQueue(PCB p) {
        p.setState(PCB.READY);
        readyQueue.addLast(p);
        p.setTimeInReadyQueue(0);
        p.setStarvationN(readyQueue.size());
        p.setAgingCounter(0);
    }
}
