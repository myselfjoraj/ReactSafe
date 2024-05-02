package jr.project.reactsafe.extras.misc;

import java.util.ArrayList;

import jr.project.reactsafe.extras.model.UserModel;

public class ClosestPoint {
    // Radius of the Earth in kilometers
    private static final double EARTH_RADIUS = 6371.0; // in kilometers

    // Method to calculate distance between two points using Haversine formula
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Calculate differences between latitude and longitude
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        // Calculate Haversine distance
        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS * c;

        return distance;
    }

    // Method to find the closest point to your location
    private static int findClosestPoint(double myLat, double myLon, ArrayList<Double> latitudes, ArrayList<Double> longitudes) {
        if (latitudes.size() != longitudes.size() || latitudes.isEmpty()) {
            throw new IllegalArgumentException("Invalid input array lengths");
        }

        int closestIndex = 0;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < latitudes.size(); i++) {
            double distance = calculateDistance(myLat, myLon, latitudes.get(i), longitudes.get(i));
            if (distance < minDistance) {
                minDistance = distance;
                closestIndex = i;
            }
        }

        return closestIndex;
    }

    public static int get(double myLatitude, double myLongitude, ArrayList<UserModel> models) {

        ArrayList<Double> latitudes = new ArrayList<>();
        ArrayList<Double> longitudes = new ArrayList<>();

        for (int i = 0 ; i < models.size() ; i++){
            try {
                latitudes.add(Double.parseDouble(models.get(i).getLat()));
                longitudes.add(Double.parseDouble(models.get(i).getLng()));
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        // Find the index of the closest point
        int closestIndex = findClosestPoint(myLatitude, myLongitude, latitudes, longitudes);
        System.out.println("Closest point index: " + closestIndex);
        System.out.println("Closest point coordinates: (" + latitudes.get(closestIndex) + ", " + longitudes.get(closestIndex) + ")");
        return closestIndex;
    }
}