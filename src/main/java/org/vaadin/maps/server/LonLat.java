package org.vaadin.maps.server;

/**
 * @author Kamil Morong
 */
public class LonLat {

    private final double lon;
    private final double lat;

    public LonLat(double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

    public LonLat add(double lon, double lat) {
        return new LonLat(this.lon + lon, this.lat + lat);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof LonLat)) {
            return false;
        }

        LonLat other = (LonLat) obj;

        return (lon == other.lon && lat == other.lat);
    }

    @Override
    public LonLat clone() {
        return new LonLat(lon, lat);
    }

    @Override
    public String toString() {
        return "lon=" + lon + ",lat=" + lat;
    }
}
