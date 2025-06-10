import java.io.*;

public class KazakhstanTest {
    
    public static void main(String[] args) {
        System.out.println("=== Kazakhstan Geographic Dataset Test ===");
        
        try {
            // Parse Kazakhstan TSP file (if available)
            TSPLIBParser.TSPInstance instance = TSPLIBParser.parseFile("kz9976.tsp");
            System.out.println("Successfully loaded Kazakhstan: " + instance.dimension + " cities");
            
            // For large datasets, test with timeout
            if (instance.dimension > 1000) {
                testLargeKazakhstan(instance.distances, instance.dimension);
            } else {
                testMediumKazakhstan(instance.distances);
            }
            
        } catch (IOException e) {
            System.out.println("Kazakhstan dataset not found. Testing with synthetic large dataset instead.");
            testSyntheticLarge();
        }
    }
    
    private static void testSyntheticLarge() {
        System.out.println("Testing with 1000-city synthetic dataset:");
        
        double[][] distances = generateLargeRandomMatrix(1000);
        
        // Only test scalable algorithms
        testMSTOnly(distances);
        testAdaptiveOnly(distances);
    }
    
    private static void testMSTOnly(double[][] distances) {
        System.out.println("\n1. MST 2-Approximation (1000 cities):");
        long startTime = System.currentTimeMillis();
        
        try {
            TSP_MST mst = new TSP_MST(distances);
            int[] parent = mst.buildMST();
            int[] oddVertices = mst.findOddDegreeVertices(parent);
            int[][] matching = mst.findMinimumMatching(oddVertices);
            int[] mstTour = mst.createTSPTour(parent, matching);
            double mstCost = mst.calculateTourCost(mstTour);
            
            long mstTime = System.currentTimeMillis() - startTime;
            System.out.println("MST Cost: " + mstCost);
            System.out.println("MST Runtime: " + mstTime + " ms");
            
        } catch (Exception e) {
            System.out.println("MST failed: " + e.getMessage());
        }
    }
    
    private static void testAdaptiveOnly(double[][] distances) {
        System.out.println("\n2. Adaptive Multi-Strategy (1000 cities):");
        long startTime = System.currentTimeMillis();
        
        try {
            AdaptiveMultiStrategyTSP adaptive = new AdaptiveMultiStrategyTSP(distances);
            AdaptiveMultiStrategyTSP.TSPResult result = adaptive.solve();
            
            long adaptiveTime = System.currentTimeMillis() - startTime;
            System.out.println("Adaptive Cost: " + result.cost);
            System.out.println("Adaptive Runtime: " + adaptiveTime + " ms");
            
        } catch (Exception e) {
            System.out.println("Adaptive failed: " + e.getMessage());
        }
    }
    
    private static double[][] generateLargeRandomMatrix(int n) {
        double[][] distances = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    distances[i][j] = 0;
                } else if (i < j) {
                    distances[i][j] = 1 + Math.random() * 999; // Random 1-1000
                    distances[j][i] = distances[i][j];
                }
            }
        }
        
        return distances;
    }


    private static void testLargeKazakhstan(double[][] distances, int dimension) {
        System.out.println("\n1. MST 2-Approximation (" + dimension + " cities):");
        long startTime = System.currentTimeMillis();
        
        try {
            TSP_MST mst = new TSP_MST(distances);
            int[] parent = mst.buildMST();
            int[] oddVertices = mst.findOddDegreeVertices(parent);
            int[][] matching = mst.findMinimumMatching(oddVertices);
            int[] mstTour = mst.createTSPTour(parent, matching);
            double mstCost = mst.calculateTourCost(mstTour);
            
            long mstTime = System.currentTimeMillis() - startTime;
            System.out.println("MST Cost: " + mstCost);
            System.out.println("MST Runtime: " + mstTime + " ms");
            
            // Local Search (from MST initialization)
            System.out.println("\n2. Local Search (from MST initialization):");
            startTime = System.currentTimeMillis();
            LocalSearchTSP localSearch = new LocalSearchTSP(distances);
            LocalSearchTSP.TSPResult localResult = localSearch.solveWithLocalSearch(mstTour);
            long localTime = System.currentTimeMillis() - startTime;
            System.out.println("Local Search Cost: " + localResult.cost);
            System.out.println("Local Search Runtime: " + localTime + " ms");
            
            // Calculate improvement
            double localImprovement = ((mstCost - localResult.cost) / mstCost) * 100;
            System.out.println("Local Search improvement over MST: " + String.format("%.1f%%", localImprovement));
            
            // Adaptive Multi-Strategy
            System.out.println("\n3. Adaptive Multi-Strategy (" + dimension + " cities):");
            startTime = System.currentTimeMillis();
            AdaptiveMultiStrategyTSP adaptive = new AdaptiveMultiStrategyTSP(distances);
            AdaptiveMultiStrategyTSP.TSPResult result = adaptive.solve();
            long adaptiveTime = System.currentTimeMillis() - startTime;
            System.out.println("Adaptive Cost: " + result.cost);
            System.out.println("Adaptive Runtime: " + adaptiveTime + " ms");
            
            // Calculate improvement
            double adaptiveImprovement = ((mstCost - result.cost) / mstCost) * 100;
            System.out.println("Adaptive improvement over MST: " + String.format("%.1f%%", adaptiveImprovement));
            
            // Compare Local Search vs Adaptive
            System.out.println("\n--- Algorithm Comparison ---");
            if (localResult.cost < result.cost) {
                double advantage = ((result.cost - localResult.cost) / result.cost) * 100;
                System.out.println("Local Search outperforms Adaptive by: " + String.format("%.1f%%", advantage));
            } else {
                double advantage = ((localResult.cost - result.cost) / localResult.cost) * 100;
                System.out.println("Adaptive outperforms Local Search by: " + String.format("%.1f%%", advantage));
            }
            
        } catch (Exception e) {
            System.out.println("Error during computation: " + e.getMessage());
        }
    }


    private static void testMediumKazakhstan(double[][] distances) {
        System.out.println("\n1. MST 2-Approximation:");
        long startTime = System.currentTimeMillis();
        
        try {
            TSP_MST mst = new TSP_MST(distances);
            int[] parent = mst.buildMST();
            int[] oddVertices = mst.findOddDegreeVertices(parent);
            int[][] matching = mst.findMinimumMatching(oddVertices);
            int[] mstTour = mst.createTSPTour(parent, matching);
            double mstCost = mst.calculateTourCost(mstTour);
            
            long mstTime = System.currentTimeMillis() - startTime;
            System.out.println("MST Cost: " + mstCost);
            System.out.println("MST Runtime: " + mstTime + " ms");
            
            // Local Search
            System.out.println("\n2. Local Search (from MST initialization):");
            startTime = System.currentTimeMillis();
            LocalSearchTSP localSearch = new LocalSearchTSP(distances);
            LocalSearchTSP.TSPResult localResult = localSearch.solveWithLocalSearch(mstTour);
            long localTime = System.currentTimeMillis() - startTime;
            System.out.println("Local Search Cost: " + localResult.cost);
            System.out.println("Local Search Runtime: " + localTime + " ms");
            
            // Adaptive Multi-Strategy
            System.out.println("\n3. Adaptive Multi-Strategy:");
            startTime = System.currentTimeMillis();
            AdaptiveMultiStrategyTSP adaptive = new AdaptiveMultiStrategyTSP(distances);
            AdaptiveMultiStrategyTSP.TSPResult adaptiveResult = adaptive.solve();
            long adaptiveTime = System.currentTimeMillis() - startTime;
            System.out.println("Adaptive Cost: " + adaptiveResult.cost);
            System.out.println("Adaptive Runtime: " + adaptiveTime + " ms");
            
        } catch (Exception e) {
            System.out.println("Error during MST, Local Search or Adaptive computation: " + e.getMessage());
        }
    }

}
