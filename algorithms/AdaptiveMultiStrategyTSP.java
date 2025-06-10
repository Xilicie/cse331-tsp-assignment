public class AdaptiveMultiStrategyTSP {
    private double[][] distances;
    private int n;
    
    public AdaptiveMultiStrategyTSP(double[][] distances) {
        this.distances = distances;
        this.n = distances.length;
    }
    
    // Main solving method
    public TSPResult solve() {
        // Phase 1: Dual initialization strategy
        int[] mstTour = getMSTApproximation();
        double mstCost = calculateTourCost(mstTour);
        
        int[] greedyTour = nearestNeighborHeuristic();
        double greedyCost = calculateTourCost(greedyTour);
        
        // Select better initialization
        int[] bestTour;
        double bestCost;
        if (mstCost < greedyCost) {
            bestTour = mstTour.clone();
            bestCost = mstCost;
            System.out.println("Selected MST initialization (cost: " + mstCost + ")");
        } else {
            bestTour = greedyTour.clone();
            bestCost = greedyCost;
            System.out.println("Selected Greedy initialization (cost: " + greedyCost + ")");
        }
        
        // Phase 2: Adaptive local search
        int[] improvedTour = adaptiveLocalSearch(bestTour);
        double finalCost = calculateTourCost(improvedTour);
        
        System.out.println("Final cost after adaptive local search: " + finalCost);
        
        return new TSPResult(improvedTour, finalCost);
    }
    

    // MST-based 2-approximation using your existing TSP_MST class
    private int[] getMSTApproximation() {
        TSP_MST mstSolver = new TSP_MST(distances);
        int[] parent = mstSolver.buildMST();
        int[] oddVertices = mstSolver.findOddDegreeVertices(parent);
        int[][] matching = mstSolver.findMinimumMatching(oddVertices);
        int[] tour = mstSolver.createTSPTour(parent, matching);
        return tour;
    }


    // Calculate tour cost
    private double calculateTourCost(int[] tour) {
        double cost = 0.0;
        for (int i = 0; i < n - 1; i++) {
            cost += distances[tour[i]][tour[i + 1]];
        }
        cost += distances[tour[n - 1]][tour[0]]; // return to start
        return cost;
    }


        // Nearest Neighbor greedy heuristic
    private int[] nearestNeighborHeuristic() {
        int[] tour = new int[n];
        boolean[] visited = new boolean[n];
        
        // Start from city 0
        tour[0] = 0;
        visited[0] = true;
        
        // Build tour by always going to nearest unvisited city
        for (int i = 1; i < n; i++) {
            int currentCity = tour[i - 1];
            double minDistance = Double.MAX_VALUE;
            int nearestCity = -1;
            
            // Find nearest unvisited city
            for (int j = 0; j < n; j++) {
                if (!visited[j] && distances[currentCity][j] < minDistance) {
                    minDistance = distances[currentCity][j];
                    nearestCity = j;
                }
            }
            
            tour[i] = nearestCity;
            visited[nearestCity] = true;
        }
        
        return tour;
    }


    // Adaptive local search with multiple improvement strategies
    private int[] adaptiveLocalSearch(int[] tour) {
        int[] current = tour.clone();
        boolean improved = true;
        int iteration = 0;
        
        int maxIterations = (n > 5000) ? 5 : 100;
        while (improved && iteration < maxIterations) { // Limit iterations to prevent infinite loops
            improved = false;
            double currentCost = calculateTourCost(current);
            
            // Strategy 1: Try 2-opt improvements
            int[] twoOptResult = apply2Opt(current);
            double twoOptCost = calculateTourCost(twoOptResult);
            
            if (twoOptCost < currentCost) {
                current = twoOptResult;
                improved = true;
                System.out.println("2-opt improvement: " + currentCost + " -> " + twoOptCost);
            }
            
            // Strategy 2: Try random swaps if 2-opt didn't improve
            if (!improved) {
                int[] swapResult = applyRandomSwaps(current);
                double swapCost = calculateTourCost(swapResult);
                
                if (swapCost < currentCost) {
                    current = swapResult;
                    improved = true;
                    System.out.println("Swap improvement: " + currentCost + " -> " + swapCost);
                }
            }
            
            iteration++;
        }
        
        System.out.println("Local search completed after " + iteration + " iterations");
        return current;
    }

    // Simple 2-opt implementation
    private int[] apply2Opt(int[] tour) {
        int[] bestTour = tour.clone();
        double bestCost = calculateTourCost(bestTour);
        
        // AGGRESSIVE LIMITS FOR VERY LARGE INSTANCES
        int maxChecks = Math.min(n * 10, 50000); // Cap total checks at 50k
        int stepSize = Math.max(1, n / 1000);     // Skip cities for large n
        
        int checks = 0;
        for (int i = 1; i < n - 1 && checks < maxChecks; i += stepSize) {
            for (int j = i + stepSize; j < n && checks < maxChecks; j += stepSize) {
                int[] newTour = twoOptSwap(bestTour, i, j);
                double newCost = calculateTourCost(newTour);
                
                if (newCost < bestCost) {
                    bestTour = newTour;
                    bestCost = newCost;
                }
                checks++;
            }
        }
        
        return bestTour;
    }

    // Perform 2-opt swap
    private int[] twoOptSwap(int[] tour, int i, int j) {
        int[] newTour = new int[n];
        
        // Copy first part: 0 to i-1
        for (int k = 0; k < i; k++) {
            newTour[k] = tour[k];
        }
        
        // Reverse middle part: i to j
        for (int k = i; k <= j; k++) {
            newTour[k] = tour[j - (k - i)];
        }
        
        // Copy last part: j+1 to end
        for (int k = j + 1; k < n; k++) {
            newTour[k] = tour[k];
        }
        
        return newTour;
    }

    // Random swap improvements
    private int[] applyRandomSwaps(int[] tour) {
        int[] newTour = tour.clone();
        
        // Try 3 random swaps
        for (int attempt = 0; attempt < 3; attempt++) {
            int i = 1 + (int)(Math.random() * (n - 2)); // Random index (not 0)
            int j = 1 + (int)(Math.random() * (n - 2)); // Random index (not 0)
            
            if (i != j) {
                // Swap cities i and j
                int temp = newTour[i];
                newTour[i] = newTour[j];
                newTour[j] = temp;
            }
        }
        
        return newTour;
    }

    
    // Result class
    public static class TSPResult {
        public int[] tour;
        public double cost;
        
        public TSPResult(int[] tour, double cost) {
            this.tour = tour;
            this.cost = cost;
        }
    }


    // Test method
    public static void main(String[] args) {
        // Same test case as before
        double[][] testDistances = {
            {0, 2, 9, 10},
            {2, 0, 6, 4},
            {9, 6, 0, 5},
            {10, 4, 5, 0}
        };
        
        AdaptiveMultiStrategyTSP solver = new AdaptiveMultiStrategyTSP(testDistances);
        TSPResult result = solver.solve();
        
        System.out.println("\nAdaptive Multi-Strategy TSP Result:");
        System.out.print("Final tour: ");
        for (int i = 0; i < result.tour.length; i++) {
            System.out.print(result.tour[i] + " ");
        }
        System.out.println();
        System.out.println("Final cost: " + result.cost);
        
        // Compare with other algorithms
        System.out.println("\nComparison:");
        System.out.println("MST approximation: 20.0");
        System.out.println("Held-Karp optimal: 20.0");
        System.out.println("Adaptive Multi-Strategy: " + result.cost);
    }

}
