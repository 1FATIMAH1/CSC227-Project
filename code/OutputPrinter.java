// Prints the Gantt chart, processes table, averages and starved processes
// Written by: Fatimah Alzeer
import java.util.ArrayList;

public class OutputPrinter {

    // segment = {processID, start, end}; processID = -1 means Idle
    public static void printGanttChart(ArrayList<int[]> segments) {
        System.out.println();
        System.out.println("==================== Gantt Chart ====================");

        int perRow = 10;
        for (int i = 0; i < segments.size(); i += perRow) {
            int end = Math.min(i + perRow, segments.size());

            String border = "+";
            String names = "|";
            String times = "";

            for (int j = i; j < end; j++) {
                int[] seg = segments.get(j);
                String name;
                if (seg[0] == -1) {
                    name = "Idle";
                } else {
                    name = "P" + seg[0];
                }

                border += "--------+";
                names += center(name, 8) + "|";
                times += padRight("" + seg[1], 9);
            }
            times += segments.get(end - 1)[2];

            System.out.println(border);
            System.out.println(names);
            System.out.println(border);
            System.out.println(times);
            System.out.println();
        }

        System.out.println("CPU allocations:");
        for (int i = 0; i < segments.size(); i++) {
            int[] seg = segments.get(i);
            String name = (seg[0] == -1) ? "Idle" : "P" + seg[0];
            System.out.println("  " + name + " : from " + seg[1] + " ms to " + seg[2] + " ms");
        }
    }

    public static void printTable(ArrayList<PCB> processes) {
        System.out.println();
        System.out.println("============================ Processes Table ============================");
        System.out.println("+------------+------------+------------+------------------+------------+------------+");
        System.out.println("| Process ID | Burst Time | Start Time | Termination Time | Waiting    | Turnaround |");
        System.out.println("+------------+------------+------------+------------------+------------+------------+");

        for (int i = 0; i < processes.size(); i++) {
            PCB p = processes.get(i);
            System.out.println("| " + padRight("P" + p.getProcessID(), 11)
                    + "| " + padRight("" + p.getBurstTime(), 11)
                    + "| " + padRight("" + p.getStartTime(), 11)
                    + "| " + padRight("" + p.getTerminationTime(), 17)
                    + "| " + padRight("" + p.getWaitingTime(), 11)
                    + "| " + padRight("" + p.getTurnaroundTime(), 11) + "|");
        }
        System.out.println("+------------+------------+------------+------------------+------------+------------+");
    }

    public static void printAverages(ArrayList<PCB> processes) {
        double totalWaiting = 0;
        double totalTurnaround = 0;

        for (int i = 0; i < processes.size(); i++) {
            totalWaiting += processes.get(i).getWaitingTime();
            totalTurnaround += processes.get(i).getTurnaroundTime();
        }

        double avgWaiting = totalWaiting / processes.size();
        double avgTurnaround = totalTurnaround / processes.size();

        System.out.println();
        System.out.printf("Average Turnaround Time = %.2f ms%n", avgTurnaround);
        System.out.printf("Average Waiting Time    = %.2f ms%n", avgWaiting);
    }

    public static void printStarvedProcesses(ArrayList<PCB> processes) {
        System.out.println();
        System.out.println("Starved processes (Priority scheduling):");

        boolean found = false;
        for (int i = 0; i < processes.size(); i++) {
            PCB p = processes.get(i);
            if (p.isStarved()) {
                found = true;
                System.out.println("  - Process " + p.getProcessID() + " suffered from starvation"
                        + " (original priority = " + p.getOriginalPriority()
                        + ", priority after aging = " + p.getPriority() + ")");
            }
        }
        if (!found) {
            System.out.println("  No process suffered from starvation.");
        }
    }

    private static String padRight(String s, int width) {
        while (s.length() < width) {
            s = s + " ";
        }
        return s;
    }

    private static String center(String s, int width) {
        int left = (width - s.length()) / 2;
        String result = "";
        for (int i = 0; i < left; i++) {
            result += " ";
        }
        result += s;
        return padRight(result, width);
    }
}
