import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ThreadPool {

    private BlockingQueue<Runnable> waiting_queue = null;
    private List<WorkerThread> workerThreadList = new ArrayList<>();
    private boolean isStopped = false;

    public ThreadPool(int nThreads, int nMaxTasksWaiting) {
        waiting_queue = new ArrayBlockingQueue<>(nMaxTasksWaiting);

        for (int i = 0; i < nThreads; i++) {
            WorkerThread workerThread =
                    new WorkerThread(waiting_queue);

            workerThreadList.add(new WorkerThread(waiting_queue));
        }
        for (WorkerThread runnable : workerThreadList) {
            new Thread(runnable).start();
        }
    }

    public synchronized void execute(Runnable task) throws Exception {
        if (this.isStopped) throw
                new IllegalStateException("ThreadPool is stopped");

        this.waiting_queue.offer(task);
    }

    public synchronized void stop() {
        this.isStopped = true;
        for (WorkerThread runnable : workerThreadList) {
            runnable.doStop();
        }
    }

    public synchronized void waitUntilAllTasksFinished() {
        while (this.waiting_queue.size() > 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}