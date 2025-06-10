import java.util.concurrent.*;

public class LargeScaleExperiment {
    
    public static void main(String[] args) {
        System.out.println("=== Large-Scale TSP Experiment ===\n");
        
        // Test on progressively larger synthetic datasets
        testScalability();
    }
    
    public static void testScalability() {
        int[] sizes = {10, 15, 20, 25, 30}; // Start with manageable sizes
        
        for (int n : sizes) {
            System.out.println("Testing " + n + "-city instance:");
            double[][] distances = generateRandomDistanceMatrix(n);
            
            // Test with timeout
            runWithTimeout(distances, n);
            System.out.println("---");
        }
    }
    
    private static void runWithTimeout(double[][] distances, int n) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        // MST Test
        try {
            Future<String> mstFuture = executor.submit(() -> testMST(distances));
            String mstResult = mstFuture.get(10, TimeUnit.SECONDS);
            System.out.println("MST: " + mstResult);
        } catch (TimeoutException e) {
            System.out.println("MST: TIMEOUT (>10s)");
        } catch (Exception e) {
            System.out.println("MST: ERROR");
        }
        
        // Held-Karp Test (with shorter timeout for larger instances)
        if (n <= 15) {
            try {
                Future<String> hkFuture = executor.submit(() -> testHeldKarp(distances));
                String hkResult = hkFuture.get(30, TimeUnit.SECONDS);
                System.out.println("Held-Karp: " + hkResult);
            } catch (TimeoutException e) {
                System.out.println("Held-Karp: TIMEOUT (>30s)");
            } catch (Exception e) {
                System.out.println("Held-Karp: ERROR");
            }
        } else {
            System.out.println("Held-Karp: SKIPPED (n > 15)");
        }

        
        // Local Search Test
        try {
            Future<String> localFuture = executor.submit(() -> testLocalSearch(distances));
            String localResult = localFuture.get(60, TimeUnit.SECONDS);
            System.out.println("Local Search: " + localResult);
        } catch (TimeoutException e) {
            System.out.println("Local Search: TIMEOUT (>60s)");
        } catch (Exception e) {
            System.out.println("Local Search: ERROR");
        }

        
        // Adaptive Test
        try {
            Future<String> adaptiveFuture = executor.submit(() -> testAdaptive(distances));
            String adaptiveResult = adaptiveFuture.get(60, TimeUnit.SECONDS);
            System.out.println("Adaptive: " + adaptiveResult);
        } catch (TimeoutException e) {
            System.out.println("Adaptive: TIMEOUT (>60s)");
        } catch (Exception e) {
            System.out.println("Adaptive: ERROR");
        }
        
        executor.shutdown();
    }
    
    private static String testMST(double[][] distances) {
        long start = System.currentTimeMillis();
        TSP_MST mst = new TSP_MST(distances);
        int[] parent = mst.buildMST();
        int[] oddVertices = mst.findOddDegreeVertices(parent);
        int[][] matching = mst.findMinimumMatching(oddVertices);
        int[] tour = mst.createTSPTour(parent, matching);
        double cost = mst.calculateTourCost(tour);
        long time = System.currentTimeMillis() - start;
        return String.format("Cost=%.2f, Time=%dms", cost, time);
    }
    
    private static String testHeldKarp(double[][] distances) {
        long start = System.currentTimeMillis();
        HeldKarp hk = new HeldKarp(distances);
        HeldKarp.TSPResult result = hk.solveTSP();
        long time = System.currentTimeMillis() - start;
        return String.format("Cost=%.2f, Time=%dms", result.cost, time);
    }

    private static String testLocalSearch(double[][] distances) {
        // Get MST tour as starting point
        TSP_MST mst = new TSP_MST(distances);
        int[] parent = mst.buildMST();
        int[] oddVertices = mst.findOddDegreeVertices(parent);
        int[][] matching = mst.findMinimumMatching(oddVertices);
        int[] mstTour = mst.createTSPTour(parent, matching);
        
        long start = System.currentTimeMillis();
        LocalSearchTSP localSearch = new LocalSearchTSP(distances);
        LocalSearchTSP.TSPResult result = localSearch.solveWithLocalSearch(mstTour);
        long time = System.currentTimeMillis() - start;
        return String.format("Cost=%.2f, Time=%dms", result.cost, time);
    }

    
    private static String testAdaptive(double[][] distances) {
        long start = System.currentTimeMillis();
        AdaptiveMultiStrategyTSP adaptive = new AdaptiveMultiStrategyTSP(distances);
        AdaptiveMultiStrategyTSP.TSPResult result = adaptive.solve();
        long time = System.currentTimeMillis() - start;
        return String.format("Cost=%.2f, Time=%dms", result.cost, time);
    }
    
    private static double[][] generateRandomDistanceMatrix(int n) {
        double[][] distances = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    distances[i][j] = 0;
                } else if (i < j) {
                    distances[i][j] = 1 + Math.random() * 99; // Random 1-100
                    distances[j][i] = distances[i][j];
                }
            }
        }
        
        return distances;
    }
}
