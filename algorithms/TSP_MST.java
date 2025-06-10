public class TSP_MST {
    private double[][] distances;
    private int n;

    public TSP_MST(double[][] distances) {
        this.distances = distances;
        this.n = distances.length;
    }

    // Prim's algorithm
    public int[] buildMST() {
        int[] parent = new int[n];
        double[] key = new double[n];
        boolean[] mstSet = new boolean[n];

        // initialize all keys as infinite
        for (int i = 0; i < n; i++) {
            key[i] = Double.MAX_VALUE;
            mstSet[i] = false;
        }

        // start from vertex 0
        key[0] = 0.0;
        parent[0] = -1;

        for (int count = 0; count < n - 1; count++) {
            // find minimum key vertex not yet in MST
            int u = -1;
            double minKey = Double.MAX_VALUE;
            for (int v = 0; v < n; v++) {
                if (!mstSet[v] && key[v] < minKey) {
                    minKey = key[v];
                    u = v;
                }
            }

            mstSet[u] = true;

            // update key values of adjacent vertices
            for(int v = 0; v < n; v++) {
                if (distances[u][v] != 0 &&
                    !mstSet[v] &&
                    distances[u][v] < key[v]) {
                        parent[v] = u;
                        key[v] = distances[u][v];
                }
            }
        }

        return parent;
    }


    // find vertices with odd degree in MST
    public int[] findOddDegreeVertices(int[] parent) {
        int[] degree = new int[n];

        // count degree of each vertex
        for (int i = 1; i < n; i++) {
            degree[i]++;
            degree[parent[i]]++;
        }

        // count odd degree vertices
        int oddCount = 0;
        for (int i = 0; i < n; i++) {
            if (degree[i] % 2 == 1) {
                oddCount++;
            }
        }

        // store odd degree vertices
        int[] oddVertices = new int[oddCount];
        int index = 0;
        for (int i = 0; i < n; i++) {
            if (degree[i] % 2 == 1) {
                oddVertices[index++] = i;
            }
        }

        return oddVertices;
    }


    // simple minimum matching for odd vertices (greedy approach)
    public int[][] findMinimumMatching(int[] oddVertices) {
        int numOdd = oddVertices.length;
        int numPairs = numOdd / 2;
        int[][] matching = new int[numPairs][2];
        boolean[] used = new boolean[numOdd];

        int pairIndex = 0;

        // pair each unused vertex with its nearest unused neighbor
        for (int i = 0; i < numOdd; i++) {
            if (used[i]) continue;

            int vertex1 = oddVertices[i];
            double minDist = Double.MAX_VALUE;
            int bestMatch = -1;

            // find nearest unused odd vertex;
            for (int j = i + 1; j < numOdd; j++) {
                if (used[j]) continue;

                int vertex2 = oddVertices[j];
                if (distances[vertex1][vertex2] < minDist) {
                    minDist = distances[vertex1][vertex2];
                    bestMatch = j;
                }
            }

            // create the matching pair
            if (bestMatch != -1) {
                matching[pairIndex][0] = vertex1;
                matching[pairIndex][1] = oddVertices[bestMatch];
                used[i] = true;
                used[bestMatch] = true;
                pairIndex++;
            }
        }

        return matching;
    }

    // create TSP tour from MST + matching
    public int[] createTSPTour(int[] parent, int[][] matching) {
        // build adjacency list from MST edges
        boolean[][] adjMatrix = new boolean[n][n];

        // add MST edges
        for (int i = 0; i < n; i++) {
            int p = parent[i];
            if (p >= 0) {
                adjMatrix[i][p] = true;
                adjMatrix[p][i] = true;
            }
        }

        // add matching edges
        for (int i = 0; i < matching.length; i++) {
            int u = matching[i][0];
            int v = matching[i][1];
            adjMatrix[u][v] = true;
            adjMatrix[v][u] = true;
        }

        // DFS traversal to create tour (shortcut repeated vertices)
        boolean[] visited = new boolean[n];
        int[] tour = new int[n];
        int tourIndex = 0; 

        tourIndex = dfsTraversal(0, adjMatrix, visited, tour, tourIndex);

        return tour;
    }

    // DFS helper method
    public int dfsTraversal(int vertex, boolean[][] adjMatrix, boolean[] visited, int[] tour, int index) {
        visited[vertex] = true; 
        tour[index++] = vertex;

        for (int i = 0; i < n; i++) {
            if (adjMatrix[vertex][i] && !visited[i]) {
                index = dfsTraversal(i, adjMatrix, visited, tour, index);
            }
        }

        return index;
    }


    // calculate total tour cost
    public double calculateTourCost(int[] tour) {
        double totalCost = 0.0;

        for (int i = 0; i < tour.length - 1; i++) {
            totalCost += distances[tour[i]][tour[i + 1]];
        }

        // add cost to return to starting vertex
        totalCost += distances[tour[tour.length - 1]][tour[0]];

        return totalCost;
    }


    // test method
    public static void main(String[] args) {
        // test case 1: 
        double[][] testDistances = {
            {0, 2, 9, 10},
            {2, 0, 6, 4},
            {9, 6, 0, 5},
            {10, 4, 5, 0}
        };

        TSP_MST tsp = new TSP_MST(testDistances);
        int[] mst = tsp.buildMST();
        
        System.out.println("Test Case 1 - MST Parent Array:");
        for (int i = 0; i < mst.length; i++) {
            System.out.println("City " + i + " parent: " + mst[i]);
        }

        // Expected output should show connections forming a tree
        System.out.println("\nExpected MST edges:");
        System.out.println("0-1 (cost 2), 1-3 (cost 4), 3-2 (cost 5)");
        System.out.println("Total MST cost should be: 11");

        int[] oddVertices = tsp.findOddDegreeVertices(mst);

        System.out.println("Odd degree vertices:");
        for (int i = 0; i < oddVertices.length; i++) {
            System.out.println("City " + oddVertices[i]);
        }
        System.out.println("Total odd vertices: " + oddVertices.length);


        int[][] matching = tsp.findMinimumMatching(oddVertices);

        System.out.println("\nMatching pairs: ");
        for (int i = 0; i < matching.length; i++) {
            System.out.println("Pair " + i + " (" + matching[i][0] + "-" + matching[i][1] + ")");
        }
        System.out.println("Total minimum matching pairs: " + matching.length);


        int[] tour = tsp.createTSPTour(mst, matching);

        System.out.println("\nTSP Tour:");
        for (int i = 0; i < tour.length; i++) {
            System.out.print(tour[i] + " ");
        }
        System.out.println();

        double tourCost = tsp.calculateTourCost(tour);
        System.out.println("Total tour cost: " + tourCost);

    }
}
