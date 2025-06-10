import java.io.*;
import java.util.*;

public class TSPLIBParser {
    
    public static TSPInstance parseFile(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        
        int dimension = 0;
        String edgeWeightType = "";
        List<double[]> coordinates = new ArrayList<>();
        
        // Parse header
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            
            if (line.startsWith("DIMENSION")) {
                dimension = Integer.parseInt(line.split(":")[1].trim());
            } else if (line.startsWith("EDGE_WEIGHT_TYPE")) {
                edgeWeightType = line.split(":")[1].trim();
            } else if (line.equals("NODE_COORD_SECTION")) {
                break;
            }
        }
        
        // Parse coordinates
        for (int i = 0; i < dimension; i++) {
            line = reader.readLine().trim();
            String[] parts = line.split("\\s+");
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            coordinates.add(new double[]{x, y});
        }
        
        reader.close();
        
        // Calculate distance matrix
        double[][] distances = calculateDistanceMatrix(coordinates, edgeWeightType);
        
        return new TSPInstance(dimension, distances, coordinates);
    }
    
    private static double[][] calculateDistanceMatrix(List<double[]> coords, String weightType) {
        int n = coords.size();
        double[][] distances = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    distances[i][j] = 0;
                } else {
                    distances[i][j] = calculateDistance(coords.get(i), coords.get(j), weightType);
                }
            }
        }
        
        return distances;
    }
    
    private static double calculateDistance(double[] coord1, double[] coord2, String weightType) {
        double dx = coord1[0] - coord2[0];
        double dy = coord1[1] - coord2[1];
        
        if (weightType.equals("EUC_2D")) {
            return Math.sqrt(dx * dx + dy * dy);
        } else {
            // Default Euclidean distance
            return Math.sqrt(dx * dx + dy * dy);
        }
    }
    
    public static class TSPInstance {
        public int dimension;
        public double[][] distances;
        public List<double[]> coordinates;
        
        public TSPInstance(int dimension, double[][] distances, List<double[]> coordinates) {
            this.dimension = dimension;
            this.distances = distances;
            this.coordinates = coordinates;
        }
    }
}
