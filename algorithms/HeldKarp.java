public class HeldKarp {
    private double[][] distances;
    private int n;
    
    public HeldKarp(double[][] distances) {
        this.distances = distances;
        this.n = distances.length;
    }
    
    // Held-Karp DP algorithm
    public TSPResult solveTSP() {
        // dp[mask][i] = minimum cost to visit all cities in mask, ending at city i
        double[][] dp = new double[1 << n][n];
        int[][] parent = new int[1 << n][n];
        
        // Initialize DP table
        for (int mask = 0; mask < (1 << n); mask++) {
            for (int i = 0; i < n; i++) {
                dp[mask][i] = Double.MAX_VALUE;
                parent[mask][i] = -1;
            }
        }
        
        // Base case: start at city 0
        dp[1][0] = 0; // mask=1 means only city 0 is visited
        
        // Fill DP table
        for (int mask = 1; mask < (1 << n); mask++) {
            for (int u = 0; u < n; u++) {
                if ((mask & (1 << u)) == 0) continue; // u not in current set
                if (dp[mask][u] == Double.MAX_VALUE) continue;
                
                for (int v = 0; v < n; v++) {
                    if ((mask & (1 << v)) != 0) continue; // v already visited - FIXED
                    
                    int newMask = mask | (1 << v);
                    double newCost = dp[mask][u] + distances[u][v];
                    
                    if (newCost < dp[newMask][v]) {
                        dp[newMask][v] = newCost;
                        parent[newMask][v] = u;
                    }
                }
            }
        }
        
        // Find minimum cost to return to start
        double minCost = Double.MAX_VALUE;
        int lastCity = -1;
        int finalMask = (1 << n) - 1; // all cities visited
        
        for (int i = 1; i < n; i++) {
            double cost = dp[finalMask][i] + distances[i][0];
            if (cost < minCost) {
                minCost = cost;
                lastCity = i;
            }
        }
        
        // Reconstruct path
        int[] tour = reconstructPath(parent, finalMask, lastCity);
        
        return new TSPResult(tour, minCost);
    }
    
    // Reconstruct the optimal path
    private int[] reconstructPath(int[][] parent, int mask, int lastCity) {
        int[] path = new int[n];
        int pathIndex = n - 1;
        int currentCity = lastCity;
        int currentMask = mask;
        
        while (currentCity != -1) {
            path[pathIndex--] = currentCity;
            int prevCity = parent[currentMask][currentCity];
            currentMask = currentMask ^ (1 << currentCity); // remove current city from mask
            currentCity = prevCity;
        }
        
        return path;
    }
    
    // Helper class for result
    public static class TSPResult {
        public int[] tour;
        public double cost;
        
        public TSPResult(int[] tour, double cost) {
            this.tour = tour;
            this.cost = cost;
        }
    }


    public static void main(String[] args) {
        double[][] testDistances = {
            {0, 2, 9, 10},
            {2, 0, 6, 4},
            {9, 6, 0, 5},
            {10, 4, 5, 0}
        };
        
        HeldKarp hk = new HeldKarp(testDistances);
        TSPResult result = hk.solveTSP();
        
        System.out.println("Held-Karp Optimal Tour:");
        for (int i = 0; i < result.tour.length; i++) {
            System.out.print(result.tour[i] + " ");
        }
        System.out.println();
        System.out.println("Optimal cost: " + result.cost);
        
        // Compare with MST approximation
        System.out.println("\nComparison:");
        System.out.println("MST approximation cost: 20.0");
        System.out.println("Held-Karp optimal cost: " + result.cost);
        System.out.println("Approximation ratio: " + (20.0 / result.cost));
    }

}

