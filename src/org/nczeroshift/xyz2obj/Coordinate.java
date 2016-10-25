
package org.nczeroshift.xyz2obj;

public class Coordinate {
    public Coordinate(){
        x = 0.0;
        y = 0.0;
        z = 0.0;
    }

    public Coordinate(Double _x, Double _y, Double _z){
        x = _x;
        y = _y;
        z = _z;
    }

    Double getX(){
        return x;
    }
    Double getY(){
        return y;
    }
    Double getZ(){
        return z;
    }

    Double x;
    Double y;
    Double z;

    public Coordinate toTileCoordinate(int zoom){
        Double lat_deg = getY();
        Double lng_deg = getX();

        Double lat_rad = lat_deg/180 * Math.PI;
        Double n = Math.pow(2.0 ,zoom);

        Double rx = ((lng_deg + 180.0) / 360.0 * n);
        Double ry = n * (1.0 - Math.log(Math.tan(lat_rad) + (1 / Math.cos(lat_rad))) / Math.PI) / 2.0 ;

        return new Coordinate(rx, ry, getZ());
    }

    public Coordinate fromTileCoordinate(int zoom){
        Double n = Math.pow(2.0 ,zoom);
        Double lon_deg = x / n * 360.0 - 180.0;
        Double lat_rad = Math.atan(Math.sinh(Math.PI * (1 - 2 * y / n)));
        Double lat_deg = lat_rad * 180.0 / Math.PI;
        return new Coordinate(lon_deg, lat_deg, getZ());
    }
}
