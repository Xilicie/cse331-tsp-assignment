import java.io.*;

public class XQL662Test {
    
    public static void main(String[] args) {
        System.out.println("=== XQL662 VLSI Dataset Test ===");
        System.out.println("Testing on 662-city VLSI benchmark instance");
        
        try {
            // Parse xql662.tsp file
            TSPLIBParser.TSPInstance instance = TSPLIBParser.parseFile("xql662.tsp");
            System.out.println("Successfully loaded XQL662: " + instance.dimension + " cities");
            
            // Test algorithms with timeout protection
            testOnXQL662(instance.distances);
            
        } catch (IOException e) {
            System.out.println("Error reading xql662.tsp: " + e.getMessage());
            System.out.println("Please download xql662.tsp from VLSI collection");
        }
    }
    
    private static void testOnXQL662(double[][] distances) {
        System.out.println("\n--- Testing Algorithms on XQL662 (662 cities) ---");
        
        // MST 2-Approximation
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
            
            // Test Local Search (with timeout)
            testLocalSearchXQL662(distances, mstTour, mstCost);
            
            // Test Adaptive (with timeout)
            testAdaptiveXQL662(distances, mstCost);
            
        } catch (Exception e) {
            System.out.println("Error during MST computation: " + e.getMessage());
        }
        
        // Held-Karp (definitely skip for 662 cities)
        System.out.println("\n2. Held-Karp Dynamic Programming:");
        System.out.println("SKIPPED - 662 cities impossible for exact algorithm");
        System.out.println("Estimated memory needed: ~2^662 * 662^2 (astronomically impossible)");
    }
    
    private static void testLocalSearchXQL662(double[][] distances, int[] mstTour, double mstCost) {
        System.out.println("\n3. Local Search (from MST initialization):");
        
        try {
            long startTime = System.currentTimeMillis();
            
            LocalSearchTSP localSearch = new LocalSearchTSP(distances);
            LocalSearchTSP.TSPResult localResult = localSearch.solveWithLocalSearch(mstTour);
            
            long localTime = System.currentTimeMillis() - startTime;
            System.out.println("Local Search Cost: " + localResult.cost);
            System.out.println("Local Search Runtime: " + localTime + " ms");
            
            double localImprovement = ((mstCost - localResult.cost) / mstCost) * 100;
            System.out.println("Improvement over MST: " + String.format("%.1f%%", localImprovement));
            
            // Compare with known optimal (XQL662 optimal is 2513)
            double knownOptimal = 2513.0;
            double localGap = ((localResult.cost - knownOptimal) / knownOptimal) * 100;
            System.out.println("Gap from optimal: " + String.format("%.1f%%", localGap));
            
        } catch (Exception e) {
            System.out.println("Local Search failed: " + e.getMessage());
        }
    }
    
    private static void testAdaptiveXQL662(double[][] distances, double mstCost) {
        System.out.println("\n4. Adaptive Multi-Strategy:");
        
        try {
            long startTime = System.currentTimeMillis();
            
            AdaptiveMultiStrategyTSP adaptive = new AdaptiveMultiStrategyTSP(distances);
            AdaptiveMultiStrategyTSP.TSPResult adaptiveResult = adaptive.solve();
            
            long adaptiveTime = System.currentTimeMillis() - startTime;
            System.out.println("Adaptive Cost: " + adaptiveResult.cost);
            System.out.println("Adaptive Runtime: " + adaptiveTime + " ms");
            
            double adaptiveImprovement = ((mstCost - adaptiveResult.cost) / mstCost) * 100;
            System.out.println("Improvement over MST: " + String.format("%.1f%%", adaptiveImprovement));
            
            // Compare with known optimal
            double knownOptimal = 2513.0;
            double adaptiveGap = ((adaptiveResult.cost - knownOptimal) / knownOptimal) * 100;
            System.out.println("Gap from optimal: " + String.format("%.1f%%", adaptiveGap));
            
        } catch (Exception e) {
            System.out.println("Adaptive algorithm failed: " + e.getMessage());
        }
    }
}
