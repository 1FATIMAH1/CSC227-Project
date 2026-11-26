// Thread 1: reads job.txt into the job queue. Line format: ID:Burst:Priority;Memory (e.g. 1:25:4;500)
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class JobReaderThread extends Thread {

    private SharedSystem system;
    private String fileName;

    public JobReaderThread(SharedSystem system, String fileName) {
        super("Thread-1 (Job Reader)");
        this.system = system;
        this.fileName = fileName;
    }

    public void run() {
        int count = 0;
        int lineNumber = 0;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line = reader.readLine();

            while (line != null) {
                lineNumber++;
                line = line.trim();

                // skip empty lines and the [Begin]/[End] lines
                if (line.length() == 0 || line.startsWith("[")) {
                    line = reader.readLine();
                    continue;
                }

                try {
                    String[] parts = line.split(";");
                    String[] info = parts[0].split(":");

                    int id = Integer.parseInt(info[0].trim());
                    int burst = Integer.parseInt(info[1].trim());
                    int priority = Integer.parseInt(info[2].trim());
                    int memory = Integer.parseInt(parts[1].trim());

                        if (burst <= 0) {
                        System.out.println("Line " + lineNumber + ": burst time must be positive, process skipped.");
                    } else if (priority < 1 || priority > 30) {
                        System.out.println("Line " + lineNumber + ": priority must be between 1 and 30, process skipped.");
                    } else if (memory <= 0 || memory > SharedSystem.TOTAL_MEMORY) {
                        System.out.println("Line " + lineNumber + ": memory must be between 1 and "
                                + SharedSystem.TOTAL_MEMORY + " MB, process skipped.");
                    } else {
                        count++;
                        PCB pcb = new PCB(id, burst, priority, memory, count);
                        addToJobQueue(pcb);
                    }
                } catch (Exception e) {
                    System.out.println("Line " + lineNumber + " has a wrong format: \"" + line + "\" (skipped)");
                }

                line = reader.readLine();
            }
            reader.close();

        } catch (IOException e) {
            System.out.println("Error: could not read the file " + fileName);
        }

        System.out.println("Thread 1: " + count + " processes were read from " + fileName
                + " and added to the job queue.");
    }

    // the job queue is shared with Thread 2, so this is a critical section
    private void addToJobQueue(PCB pcb) {
        try {
            system.mutex.acquire();
        } catch (InterruptedException e) {
            return;
        }
        system.jobQueue.add(pcb);
        system.allProcesses.add(pcb);
        system.mutex.release();
    }
}
