import java.util.Random;

public class LocalSearchTSP {
    private double[][] distances;
    private int n;
    private Random random;
    
    public LocalSearchTSP(double[][] distances) {
        this.distances = distances;
        this.n = distances.length;
        this.random = new Random();
    }
    
    // Main local search algorithm - takes any initial tour and improves it
    public TSPResult solveWithLocalSearch(int[] initialTour) {
        int[] current = initialTour.clone();
        boolean improved = true;
        int iteration = 0;
        int maxIterations = 100;
        
        System.out.println("Starting local search from initial cost: " + calculateTourCost(current));
        
        while (improved && iteration < maxIterations) {
            improved = false;
            double currentCost = calculateTourCost(current);
            
            // Try 2-opt improvements
            int[] twoOptResult = apply2Opt(current);
            double twoOptCost = calculateTourCost(twoOptResult);
            
            if (twoOptCost < currentCost) {
                current = twoOptResult;
                improved = true;
                System.out.println("2-opt improvement: " + currentCost + " -> " + twoOptCost);
            }
            
            // If 2-opt didn't improve, try random swaps
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
        return new TSPResult(current, calculateTourCost(current));
    }
    
    private int[] apply2Opt(int[] tour) {
        int[] bestTour = tour.clone();
        double bestCost = calculateTourCost(bestTour);
        
        // LIMIT SEARCH SPACE FOR LARGE INSTANCES
        int maxI = (n > 5000) ? Math.min(n/20, 250) : n-1;  // Only check subset for large n
        int maxJ = (n > 5000) ? Math.min(n/10, 500) : n;    // Limit j range
        
        for (int i = 1; i < maxI; i++) {
            int jLimit = Math.min(i + maxJ, n);
            for (int j = i + 1; j < jLimit; j++) {
                int[] newTour = twoOptSwap(bestTour, i, j);  // Use bestTour for incremental improvements
                double newCost = calculateTourCost(newTour);
                
                if (newCost < bestCost) {
                    bestTour = newTour;
                    bestCost = newCost;
                }
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
            int i = 1 + random.nextInt(n - 2); // Random index (not 0)
            int j = 1 + random.nextInt(n - 2); // Random index (not 0)
            
            if (i != j) {
                // Swap cities i and j
                int temp = newTour[i];
                newTour[i] = newTour[j];
                newTour[j] = temp;
            }
        }
        
        return newTour;
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
    
    // Result class
    public static class TSPResult {
        public int[] tour;
        public double cost;
        
        public TSPResult(int[] tour, double cost) {
            this.tour = tour;
            this.cost = cost;
        }
    }
}
