package org.nczeroshift.xyz2obj;

import org.nczeroshift.xyz2obj.maps.BingMaps;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

public class Tile {
    private TreeMap<Double, TreeMap<Double, Coordinate>> rows = new TreeMap<>();
    private TreeMap<Double, TreeMap<Double, Coordinate>> columns = new TreeMap<>();
    private Integer level = 1;


    public Tile(Integer baseLevel) {
        level = baseLevel;
    }

    public void AddCoordinate(Coordinate c) {
        Double x = Math.floor(c.getX() * 1000) / 1000;
        Double y = Math.floor(c.getY() * 1000) / 1000;
        if (!columns.containsKey(x))
            columns.put(x, new TreeMap<>());

        columns.get(x).put(y, c);

        if (!rows.containsKey(y))
            rows.put(y, new TreeMap<>());

        rows.get(y).put(x, c);
    }

    public Double getElevation(Coordinate center){
        ArrayList<Double> columsKeys = new ArrayList<Double>(columns.keySet());
        ArrayList<Double> rowsKeys = new ArrayList<Double>(rows.keySet());

        for (int x_i = 0; x_i < columsKeys.size()-1; x_i++)
        {
            if(center.getX() >= columsKeys.get(x_i) && center.getX() < columsKeys.get(x_i+1))
            {
                Double alpha_x = (center.getX() - columsKeys.get(x_i))/(columsKeys.get(x_i+1)- columsKeys.get(x_i));
                TreeMap<Double,Coordinate> rows_1 = columns.get(columsKeys.get(x_i));
                TreeMap<Double,Coordinate> rows_2 = columns.get(columsKeys.get(x_i+1));

                for (int y_i = 0; y_i < rowsKeys.size()-1; y_i++) {
                    if(center.getY() >= rowsKeys.get(y_i) && center.getY() < rowsKeys.get(y_i+1)) {
                        Double alpha_y = (center.getY() - rowsKeys.get(y_i))/(rowsKeys.get(y_i+1)- rowsKeys.get(y_i));

                        Double z1 = rows_1.get(rowsKeys.get(y_i)).getZ();
                        Double z2 = rows_1.get(rowsKeys.get(y_i+1)).getZ();
                        Double xz1 = z1*(1-alpha_y) + z2*alpha_y;

                        Double z3 = rows_2.get(rowsKeys.get(y_i)).getZ();
                        Double z4 = rows_2.get(rowsKeys.get(y_i+1)).getZ();
                        Double xz2 = z3*(1-alpha_y) + z4*alpha_y;

                        return xz1 * (1-alpha_x) + xz2 * alpha_x;
                    }
                }
            }
        }
        return null;
    }

    public void saveOBJ(File folder, Coordinate center, Double scale, Integer span, Integer subdivision) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(folder,"out.obj")));

        Double tSize = Utils.getTileSizeAtLatitude(0.0, level);
        int cx = (int)Math.floor(center.getX());
        int cy = (int)Math.floor(center.getY());

        for(int x_i = cx - span;x_i <= cx + span; x_i++)
        {
            for(int y_i = cy - span; y_i <= cy+span; y_i++)
            {
                for(int x = 0;x<subdivision;x++)
                {
                    for(int y = 0;y<subdivision;y++)
                    {
                        double xd = x / ((double)subdivision-1) + x_i;
                        double yd = y / ((double)subdivision-1) + y_i;

                        Double _x = (xd - center.getX()) * tSize * scale;
                        Double _y = -(yd - center.getY()) * tSize * scale;

                        Double z = this.getElevation(new Coordinate(xd,yd,0.0));
                        if(z == null)
                            z = .0;
                        writer.write("v " + _x + " " + _y + " " + (z * scale) + "\n");
                    }
                }
            }
        }

        for(int x = 0;x < subdivision;x++){
            for(int y = 0;y < subdivision;y++){
                double xd = x / ((double)subdivision-1);
                double yd = 1-y / ((double)subdivision-1);
                writer.write("vt " + xd + " " + yd + "\n");
            }
        }

        HashSet<String> mtlKeys = new HashSet<String>();

        int vCount = 0;
        for(int x_i = cx - span;x_i <= cx + span; x_i++) {
            for (int y_i = cy - span; y_i <= cy + span; y_i++) {
                String key = x_i + "_" + y_i;
                mtlKeys.add(key);
                writer.write("usemtl " + key+"\n");
                for (int x = 0; x < subdivision-1; x++) {
                    for (int y = 0; y < subdivision-1; y++) {

                        int v1 = y + (x ) * (subdivision) + 1+vCount;
                        int v2 = y + (x ) * (subdivision) + 2+vCount;
                        int v3 = y + (x + 1) * (subdivision) + 1+vCount;
                        int v4 = y + (x + 1) * (subdivision) + 2+vCount;

                        int uv1 = y + (x ) * (subdivision) + 1;
                        int uv2 = y + (x ) * (subdivision) + 2;
                        int uv3 = y + (x + 1) * (subdivision) + 1;
                        int uv4 = y + (x + 1) * (subdivision) + 2;

                        writer.write("f " + v4 + "/" + uv4 + " " + v3 + "/" + uv3 + " " + v1 + "/" + uv1 + " " + v2 + "/" + uv2 + "\n");

                    }
                }

                vCount += subdivision*subdivision;
            }
        }

        writer.close();

        BufferedWriter mtlWriter = new BufferedWriter(new FileWriter(new File(folder,"out.mtl")));

        for(String keys : mtlKeys){
            mtlWriter.write("newmtl "+keys+"\n");
            mtlWriter.write("map_Kd "+keys+".png\n");
            mtlWriter.write("\n");
        }

        mtlWriter.close();
    }


    public void savePNG(File folder,Coordinate center, int span) throws Exception {
        int cx = (int)Math.floor(center.getX());
        int cy = (int)Math.floor(center.getY());

        int resolution = 32;
        for(int x_i = cx - span;x_i <= cx + span; x_i++)
        {
            for(int y_i = cy - span; y_i <= cy+span; y_i++)
            {
                String tileId = x_i+"_"+y_i;

                Coordinate c = new Coordinate((double)x_i,(double)y_i,.0);
                Coordinate lonlat = c.fromTileCoordinate(level);

                System.out.println("Downloading tile " + tileId +"...");

                final BufferedImage image = new BufferedImage(
                        1024,
                        1024,
                        BufferedImage.TYPE_INT_ARGB
                );
                final Graphics2D gc = image.createGraphics();

                final int max = 4;
                final int endLevel = level+2;
                Coordinate c4 = lonlat.toTileCoordinate(endLevel);
                for(int i = 0; i<max; i++){
                    for(int j = 0; j<max; j++){
                        Coordinate subTileCoord = new Coordinate(c4.getX()+i,c4.getY()+j,.0);

                        URL website = new URL(BingMaps.getURI(subTileCoord.getX().intValue(),
                                subTileCoord.getY().intValue(),
                                endLevel));

                        InputStream in = website.openStream();

                        BufferedImage img = ImageIO.read(in);
                        in.close();

                        Double xoff = 1024.0 *(i) / 4;
                        Double yoff = 1024.0 *(j) / 4;

                        gc.drawImage(img,xoff.intValue(),yoff.intValue(),256,256,null);
                    }
                }

                ImageIO.write(image, "png", new File(folder,tileId+".png"));
            }
        }
    }

}


