package org.nczeroshift.xyz2obj;

import com.sun.org.apache.xpath.internal.SourceTree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        if(args.length != 8)
            return;

        String strElevationFilename = args[0];
        String strScale = args[1];
        String strMargin = args[2];
        String strLatitude = args[3];
        String strLongitude = args[4];
        String strSubdivisionDetail = args[5];
        String strOutputFolder = args[6];
        String strSaveTextureTiles = args[7];

        final Integer baseLevel = 17;
        final File outputFolder = new File(strOutputFolder);
        final Double scale = Double.parseDouble(strScale);
        final Coordinate targetPosition = new Coordinate(Double.parseDouble(strLongitude), Double.parseDouble(strLatitude), 0.0);
        final Coordinate targetTileC = targetPosition.toTileCoordinate(baseLevel);
        final Double size = Utils.getTileSizeAtLatitude(.0,baseLevel);
        final Integer marginTiles = Integer.parseInt(strMargin);
        final Integer subdivision = Integer.parseInt(strSubdivisionDetail);

        System.out.println(String.format("Tile side : %.2fm",size));

        File f = new File(strElevationFilename);
        List<Coordinate> coords = readCoordinates(f);
        System.out.println("Total points: "+coords.size());

        Tile t = new Tile(baseLevel);

        for(Coordinate c : coords) {
            Coordinate tC = c.toTileCoordinate(baseLevel);
            t.AddCoordinate(tC);
        }

        if(!outputFolder.exists())
            outputFolder.mkdir();

        System.out.println("Generating obj ...");
        t.saveOBJ(new File(outputFolder,"out.obj"),targetTileC,scale,marginTiles,subdivision);

        if(strSaveTextureTiles.equals("true")) {
            System.out.println("Generating texture ...");
            t.savePNG(outputFolder, targetTileC, marginTiles);
        }

        System.out.println("Done");
    }


    public static List<Coordinate> readCoordinates(File f){
        ArrayList<Coordinate> ret = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            for (String line; (line = br.readLine()) != null; ) {
                String tokens[] = line.split(" ");
                String strX = tokens[0];
                String strY = tokens[1];
                String strZ = tokens[2];

                Double x = Double.parseDouble(strX);
                Double y = Double.parseDouble(strY);
                Double z = Double.parseDouble(strZ);

                Coordinate c = new Coordinate(x, y, z);
                ret.add(c);
            }
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }
}
