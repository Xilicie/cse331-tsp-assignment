import java.io.*;

public class A280Test {
    
    public static void main(String[] args) {
        System.out.println("=== A280 TSPLIB Dataset Test ===");
        System.out.println("Testing on 280-city benchmark instance");
        
        try {
            // Parse a280.tsp file
            TSPLIBParser.TSPInstance instance = TSPLIBParser.parseFile("a280.tsp");
            System.out.println("Successfully loaded a280: " + instance.dimension + " cities");
            
            // Test algorithms
            testOnA280(instance.distances);
            
        } catch (IOException e) {
            System.out.println("Error reading a280.tsp: " + e.getMessage());
            System.out.println("Make sure a280.tsp is in the current directory");
        }
    }

    private static void testLocalSearchOnA280(double[][] distances, double mstCost) {
        System.out.println("\n4. Local Search (from MST initialization):");
        
        // Get MST tour as starting point
        TSP_MST mst = new TSP_MST(distances);
        int[] parent = mst.buildMST();
        int[] oddVertices = mst.findOddDegreeVertices(parent);
        int[][] matching = mst.findMinimumMatching(oddVertices);
        int[] mstTour = mst.createTSPTour(parent, matching);
        
        long startTime = System.currentTimeMillis();
        
        LocalSearchTSP localSearch = new LocalSearchTSP(distances);
        LocalSearchTSP.TSPResult localResult = localSearch.solveWithLocalSearch(mstTour);
        
        long localTime = System.currentTimeMillis() - startTime;
        System.out.println("Local Search Cost: " + localResult.cost);
        System.out.println("Local Search Runtime: " + localTime + " ms");
        
        // Analysis
        double localImprovement = ((mstCost - localResult.cost) / mstCost) * 100;
        System.out.println("Improvement over MST: " + String.format("%.1f%%", localImprovement));
        
        // Compare with known optimal
        double knownOptimal = 2579.0;
        double localGap = ((localResult.cost - knownOptimal) / knownOptimal) * 100;
        System.out.println("Gap from optimal: " + String.format("%.1f%%", localGap));
    }

    
    private static void testOnA280(double[][] distances) {
        System.out.println("\n--- Testing Algorithms on A280 ---");
        
        // MST 2-Approximation
        System.out.println("\n1. MST 2-Approximation:");
        long startTime = System.currentTimeMillis();
        
        TSP_MST mst = new TSP_MST(distances);
        int[] parent = mst.buildMST();
        int[] oddVertices = mst.findOddDegreeVertices(parent);
        int[][] matching = mst.findMinimumMatching(oddVertices);
        int[] mstTour = mst.createTSPTour(parent, matching);
        double mstCost = mst.calculateTourCost(mstTour);
        
        long mstTime = System.currentTimeMillis() - startTime;
        System.out.println("MST Cost: " + mstCost);
        System.out.println("MST Runtime: " + mstTime + " ms");
        
        // Held-Karp (skip for 280 cities - too large)
        System.out.println("\n2. Held-Karp Dynamic Programming:");
        System.out.println("SKIPPED - 280 cities too large for exact algorithm");
        System.out.println("Estimated memory needed: ~2^280 * 280^2 (impossible)");
        

        testLocalSearchOnA280(distances, mstCost);
        
        // Adaptive Multi-Strategy
        System.out.println("\n3. Adaptive Multi-Strategy:");
        startTime = System.currentTimeMillis();
        
        AdaptiveMultiStrategyTSP adaptive = new AdaptiveMultiStrategyTSP(distances);
        AdaptiveMultiStrategyTSP.TSPResult adaptiveResult = adaptive.solve();
        
        long adaptiveTime = System.currentTimeMillis() - startTime;
        System.out.println("Adaptive Cost: " + adaptiveResult.cost);
        System.out.println("Adaptive Runtime: " + adaptiveTime + " ms");
        
        // Analysis
        System.out.println("\n--- Performance Analysis ---");
        double improvement = ((mstCost - adaptiveResult.cost) / mstCost) * 100;
        System.out.println("Improvement over MST: " + String.format("%.1f%%", improvement));
        System.out.println("Runtime ratio (Adaptive/MST): " + String.format("%.1fx", (double)adaptiveTime / mstTime));
        
        // Compare with known optimal (a280 optimal is 2579)
        double knownOptimal = 2579.0;
        double adaptiveGap = ((adaptiveResult.cost - knownOptimal) / knownOptimal) * 100;
        double mstGap = ((mstCost - knownOptimal) / knownOptimal) * 100;
        
        System.out.println("\n--- Comparison with Known Optimal (2579) ---");
        System.out.println("MST gap from optimal: " + String.format("%.1f%%", mstGap));
        System.out.println("Adaptive gap from optimal: " + String.format("%.1f%%", adaptiveGap));
    }
}
