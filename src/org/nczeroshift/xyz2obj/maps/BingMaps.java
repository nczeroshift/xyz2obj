package org.nczeroshift.xyz2obj.maps;

public class BingMaps {
    static String bingURI = "https://ecn.t1.tiles.virtualearth.net/tiles/a{KEY}.jpeg?g=5239";

    // https://msdn.microsoft.com/en-us/library/bb259689.aspx
    public static String TileXYToQuadKey(int tileX, int tileY, int levelOfDetail)
    {
        String quadKey = new String();
        for (int i = levelOfDetail; i > 0; i--)
        {
            char digit = '0';
            int mask = 1 << (i - 1);
            if ((tileX & mask) != 0)
            {
                digit++;
            }
            if ((tileY & mask) != 0)
            {
                digit++;
                digit++;
            }
            quadKey += digit;
        }
        return quadKey;
    }

    public static String getURI(int x, int y, int levelOfDetail){
        String quadkey = TileXYToQuadKey(x,y,levelOfDetail);
        String uri = bingURI.replace("{KEY}",quadkey);
        return uri;
    }

}
