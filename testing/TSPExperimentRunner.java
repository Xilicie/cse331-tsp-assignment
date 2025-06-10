import java.io.*;

public class TSPExperimentRunner {
    
    public static void main(String[] args) {
        // Test on a larger synthetic dataset first
        testOnSyntheticData();
        
        // Later we'll add real TSPLIB dataset testing
    }
    
    public static void testOnSyntheticData() {
        System.out.println("=== TSP Algorithm Comparison ===\n");
        
        int[] cityCounts = {8, 10, 15, 20, 25, 30}; // Multiple test sizes
        
        for (int n : cityCounts) {
            System.out.println("\n=== Testing " + n + " cities ===");
            double[][] distances = generateRandomDistanceMatrix(n);
            
            // Test all 4 algorithms
            testMSTApproximation(distances);
            testHeldKarp(distances);
            testLocalSearch(distances);
            testAdaptiveMultiStrategy(distances);
            
            System.out.println("--- " + n + " cities complete ---");
        }
        
        System.out.println("\n=== All Small-Scale Experiments Complete ===");
    }

    
    private static double[][] generateRandomDistanceMatrix(int n) {
        double[][] distances = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    distances[i][j] = 0;
                } else if (i < j) {
                    // Generate random distance between 1 and 20
                    distances[i][j] = 1 + Math.random() * 19;
                    distances[j][i] = distances[i][j]; // Symmetric
                }
            }
        }
        
        return distances;
    }
    
    private static void testMSTApproximation(double[][] distances) {
        System.out.println("\n--- MST 2-Approximation ---");
        long startTime = System.currentTimeMillis();
        
        TSP_MST mst = new TSP_MST(distances);
        int[] parent = mst.buildMST();
        int[] oddVertices = mst.findOddDegreeVertices(parent);
        int[][] matching = mst.findMinimumMatching(oddVertices);
        int[] tour = mst.createTSPTour(parent, matching);
        double cost = mst.calculateTourCost(tour);
        
        long endTime = System.currentTimeMillis();
        
        System.out.println("Tour cost: " + cost);
        System.out.println("Runtime: " + (endTime - startTime) + " ms");
    }
    
    private static void testHeldKarp(double[][] distances) {
        System.out.println("\n--- Held-Karp Dynamic Programming ---");
        
        if (distances.length > 22) {
            System.out.println("Skipped: Problem size too large for available memory");
            System.out.println("Held-Karp requires O(n*2^n) space - not feasible for n > 22");
            return;
        }
        
        try {
            long startTime = System.currentTimeMillis();
            HeldKarp hk = new HeldKarp(distances);
            HeldKarp.TSPResult result = hk.solveTSP();
            long endTime = System.currentTimeMillis();
            
            System.out.println("Optimal cost: " + result.cost);
            System.out.println("Runtime: " + (endTime - startTime) + " ms");
        } catch (OutOfMemoryError e) {
            System.out.println("Skipped: Out of memory (problem size too large)");
        }
    }

    
    private static void testAdaptiveMultiStrategy(double[][] distances) {
        System.out.println("\n--- Adaptive Multi-Strategy ---");
        long startTime = System.currentTimeMillis();
        
        AdaptiveMultiStrategyTSP adaptive = new AdaptiveMultiStrategyTSP(distances);
        AdaptiveMultiStrategyTSP.TSPResult result = adaptive.solve();
        
        long endTime = System.currentTimeMillis();
        
        System.out.println("Final cost: " + result.cost);
        System.out.println("Runtime: " + (endTime - startTime) + " ms");
    }

    private static void testLocalSearch(double[][] distances) {
        System.out.println("\n--- Local Search (from MST initialization) ---");
        
        // Get MST tour as starting point
        TSP_MST mst = new TSP_MST(distances);
        int[] parent = mst.buildMST();
        int[] oddVertices = mst.findOddDegreeVertices(parent);
        int[][] matching = mst.findMinimumMatching(oddVertices);
        int[] mstTour = mst.createTSPTour(parent, matching);
        
        long startTime = System.currentTimeMillis();
        
        LocalSearchTSP localSearch = new LocalSearchTSP(distances);
        LocalSearchTSP.TSPResult result = localSearch.solveWithLocalSearch(mstTour);
        
        long endTime = System.currentTimeMillis();
        
        System.out.println("Final cost: " + result.cost);
        System.out.println("Runtime: " + (endTime - startTime) + " ms");
    }

}
