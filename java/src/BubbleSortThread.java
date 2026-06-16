public class BubbleSortThread extends Thread {

    private final SortPanel panel;
    private final int       delayMs;

    public BubbleSortThread(SortPanel panel, int delayMs) {
        this.panel   = panel;
        this.delayMs = delayMs;
        setDaemon(true);
    }

    @Override
    public void run() {
        panel.setRunning(true);
        int[] arr = panel.getArray();
        int n = arr.length;

        outer:
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (isInterrupted()) break outer;

                panel.highlight(j, j + 1);
                panel.incrementComparisons();
                if (!sleep()) break outer;

                if (arr[j] > arr[j + 1]) {
                    int tmp  = arr[j];
                    arr[j]   = arr[j + 1];
                    arr[j+1] = tmp;
                    panel.incrementSwaps();
                    if (!sleep()) break outer;
                }
            }
            if (isInterrupted()) break;
            panel.markSortedFrom(n - 1 - i);
        }

        if (!isInterrupted()) panel.setDone();
    }

    private boolean sleep() {
        try {
            Thread.sleep(delayMs);
            return true;
        } catch (InterruptedException e) {
            interrupt();
            return false;
        }
    }
}
