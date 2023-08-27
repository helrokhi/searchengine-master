package searchengine.config.sitemaps;

public class Interrupter implements Runnable {
    private final Processor processor;

    public Interrupter(Processor processor) {
        this.processor = processor;
    }

    @Override
    public void run() {
        processor.interrupt();
        processor.getForkJoinPool().shutdown();
        System.out.println("Interrupter run" +
                " site: " + processor.getSite().getUrl() + " stop! " +
                "");
    }
}
