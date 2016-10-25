package org.nczeroshift.xyz2obj;

public class Utils {
    public static Double getTileSizeAtLatitude(Double lat, Integer zoom){
        return Math.cos(lat * Math.PI/180) * 2 * Math.PI * 6378137 / Math.pow(2,zoom);
    }
}
