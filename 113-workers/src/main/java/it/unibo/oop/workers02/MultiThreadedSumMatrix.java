package it.unibo.oop.workers02;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Sums the elements in a matrix by using a multi-threaded process to make it more efficient.
 */
public final class MultiThreadedSumMatrix implements SumMatrix {

    private final int nthreads;

    /**
     * The constructor for the algorithm.
     * 
     * @param n is the number of threads to use.
     */
    public MultiThreadedSumMatrix(final int n) {
        this.nthreads = n;
    }

    @Override
    public double sum(final double[][] matrix) {

        final int elements = matrix.length * matrix[0].length;

        final List<Worker> workers = new ArrayList<>(nthreads);
        final int size = elements % nthreads + elements / nthreads;
        int jump = 0;
        final double[] temp = new double[size];
        int startRow = 0;
        int startCol = 0;
        int endRow = 0;
        int endCol = 0;
        for (int row = 0; row < matrix.length; row++) {
            for (int col = 0; col < matrix[0].length; col++) {
                if (jump == 0) {
                    startRow = row;
                    startCol = col;
                }
                temp[jump] = matrix[row][col];
                jump++;
                endRow = row;
                endCol = col;
                if (jump == size) {
                    workers.add(new Worker(temp.clone(), startRow, startCol, endRow, endCol));
                    jump = 0;
                }
            }
        }
        if (jump > 0) {
            final double[] extra = Arrays.copyOf(temp, jump);
            workers.add(new Worker(extra, startRow, startCol, endRow, endCol));
        }

        for (final Worker w: workers) {
            w.start();
        }

        double sum = 0;
        for (final Worker w: workers) {
            try {
                w.join();
                sum += w.getResult();
            } catch (final InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }

        return sum;
    }

    private static class Worker extends Thread {
        private final double[] array;
        private final int startRow;
        private final int startCol;
        private final int endRow;
        private final int endCol;
        private double res;

        /**
         * Build a new worker.
         *
         * @param array
         *            the array to sum.
         * @param startRow
         *            the starting row for this worker.
         * @param startCol
         *            the starting column for this worker.
         * @param endRow
         *            the ending row for this worker.
         * @param endCol
         *            the ending column for this worker.
         */
        Worker(final double[] array, final int startRow, final int startCol, final int endRow, final int endCol) {
            super();
            this.array = array.clone();
            this.startRow = startRow;
            this.startCol = startCol;
            this.endRow = endRow;
            this.endCol = endCol;
        }

        @Override
        @SuppressWarnings("PMD.SystemPrintln")
        public synchronized void run() {
            System.out.println("Working from index [" + startRow + "] [" + startCol + "]");
            double sum = 0;
            for (final double i : array) {
                sum += i;
            }
            this.res = sum;
            System.out.println("Ending at index [" + endRow + "] [" + endCol + "]");
        }

        /**
         * Returns the result of summing up the doubles within the matrix.
         *
         * @return the sum of every element in the array
         */
        public synchronized double getResult() {
            return this.res;
        }
    }
}
