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
            int key = arr[i];
            int j = i - 1;

            panel.highlight(i, j);
            sleep();

            while (j >= 0 && arr[j] > key) {
                panel.incrementComparisons();
                panel.highlight(j + 1, j);
                arr[j + 1] = arr[j];
                panel.incrementSwaps();
                sleep();
                j--;
                if (j >= 0) panel.highlight(j + 1, j);
            }
            panel.incrementComparisons();

            arr[j + 1] = key;
            panel.markSortedIndex(j + 1);
            sleep();
        }

        panel.setDone();
    }

    private void sleep() {
        try { Thread.sleep(delayMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
