// Thread 1 -> JobReaderThread, Thread 2 -> LongTermScheduler,
// Thread 3 -> MediumTermScheduler, Main thread -> CPUScheduler
// Written by: Leen Alkhathlan
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        Scanner input = new Scanner(System.in);
        SharedSystem sys = new SharedSystem();

        System.out.println("===============================================");
        System.out.println("     Multithreaded Scheduling Simulator");
        System.out.println("===============================================");

        int choice = 0;
        while (choice < 1 || choice > 3) {
            System.out.println("Choose the CPU scheduling algorithm:");
            System.out.println("  1. Shortest Job First (SJF)");
            System.out.println("  2. Round Robin (q = 5 ms)");
            System.out.println("  3. Priority (Non-Preemptive) with aging");
            System.out.print("Your choice: ");
            if (input.hasNextInt()) {
                choice = input.nextInt();
            } else {
                input.next();
            }
            if (choice < 1 || choice > 3) {
                System.out.println("Wrong choice, try again.\n");
            }
        }

        switch (choice) {
            case 1: sys.algorithm = "SJF"; break;
            case 2: sys.algorithm = "RR"; break;
            case 3: sys.algorithm = "PRIORITY"; break;
        }

        String answer = "";
        while (!answer.equals("y") && !answer.equals("n")) {
            System.out.print("Enable the medium-term scheduler? (y/n): ");
            answer = input.next().trim().toLowerCase();
        }
        sys.mediumTermEnabled = answer.equals("y");

        System.out.println();
        System.out.println("Algorithm: " + getName(sys.algorithm));
        System.out.println("Medium-term scheduler: " + (sys.mediumTermEnabled ? "Enabled" : "Disabled"));
        System.out.println("Main memory: " + SharedSystem.TOTAL_MEMORY + " MB");
        System.out.println();

        // Thread 1: all jobs must be in the job queue before time 0
        JobReaderThread reader = new JobReaderThread(sys, "job.txt");
        reader.start();
        reader.join();

        if (sys.allProcesses.isEmpty()) {
            System.out.println("No processes to schedule. Program ended.");
            return;
        }

        LongTermScheduler longTerm = new LongTermScheduler(sys);
        MediumTermScheduler mediumTerm = new MediumTermScheduler(sys);
        longTerm.start();
        mediumTerm.start();

        System.out.println();
        System.out.println("------------------ Simulation starts ------------------");

        // main thread: short-term scheduler
        CPUScheduler cpu = new CPUScheduler(sys);
        cpu.runSimulation();

        longTerm.join();
        mediumTerm.join();

        System.out.println("------------------ Simulation ends ------------------");

        System.out.println();
        System.out.println("Results for: " + getName(sys.algorithm));
        OutputPrinter.printGanttChart(cpu.getGantt());
        OutputPrinter.printTable(sys.allProcesses);
        OutputPrinter.printAverages(sys.allProcesses);

        if (sys.algorithm.equals("PRIORITY")) {
            OutputPrinter.printStarvedProcesses(sys.allProcesses);
        }

        if (sys.mediumTermEnabled) {
            System.out.println();
            System.out.println("Number of medium-term scheduler interventions: " + mediumTerm.getInterventions());
        }

        input.close();
    }

    private static String getName(String algorithm) {
        if (algorithm.equals("SJF")) {
            return "Shortest Job First";
        } else if (algorithm.equals("RR")) {
            return "Round Robin (q = 5 ms)";
        } else {
            return "Priority (Non-Preemptive) with Aging";
        }
    }
}
