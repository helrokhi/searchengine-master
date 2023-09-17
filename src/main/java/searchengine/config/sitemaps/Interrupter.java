package searchengine.config.sitemaps;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Interrupter implements Runnable {
    private final Processor processor;

    @Override
    public void run() {
        processor.interrupt();
        processor.getForkJoinPool().shutdown();
        System.out.println("Interrupter run" +
                " site: " + processor.getSite().getUrl() + " stop! " +
                "");
    }
}
