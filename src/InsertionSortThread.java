public class InsertionSortThread extends Thread {

    private final SortPanel panel;
    private final int       delayMs;

    public InsertionSortThread(SortPanel panel, int delayMs) {
        this.panel   = panel;
        this.delayMs = delayMs;
        setDaemon(true);
    }

    @Override
    public void run() {
        panel.setRunning(true);
        int[] arr = panel.getArray();
        int n = arr.length;

        panel.markSortedIndex(0);

        for (int i = 1; i < n; i++) {
            if (isInterrupted()) break;

            int key = arr[i];
            int j = i - 1;

            panel.highlight(i, j < 0 ? i : j);
            if (!sleep()) break;

            while (j >= 0 && arr[j] > key) {
                if (isInterrupted()) return;
                panel.incrementComparisons();
                panel.highlight(j + 1, j);
                arr[j + 1] = arr[j];
                panel.incrementSwaps();
                if (!sleep()) return;
                j--;
            }
            panel.incrementComparisons();

            arr[j + 1] = key;
            panel.markSortedIndex(j + 1);
            if (!sleep()) break;
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
