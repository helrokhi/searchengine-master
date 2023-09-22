package searchengine.dto.sitemaps;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Interrupter implements Runnable {
    private final Processor processor;

    @Override
    public void run() {
        processor.interrupt();
        processor.getForkJoinPool().shutdown();
        processor.getPoolExecutor().getQueue().clear();
        System.out.println("Interrupter run" +
                " site: " + processor.getSite().getUrl() + " stop! " +
                " queued " + processor.getPoolExecutor().getQueue().size() +
                "");
    }
}
