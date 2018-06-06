package ca.ubc.cs.cpsc210.translink.ui;

import android.content.Context;
import ca.ubc.cs.cpsc210.translink.BusesAreUs;
import ca.ubc.cs.cpsc210.translink.model.Route;
import ca.ubc.cs.cpsc210.translink.model.RoutePattern;
import ca.ubc.cs.cpsc210.translink.model.Stop;
import ca.ubc.cs.cpsc210.translink.model.StopManager;
import ca.ubc.cs.cpsc210.translink.util.Geometry;
import ca.ubc.cs.cpsc210.translink.util.LatLon;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// A bus route drawer
public class BusRouteDrawer extends MapViewOverlay {
    /** overlay used to display bus route legend text on a layer above the map */
    private BusRouteLegendOverlay busRouteLegendOverlay;
    /** overlays used to plot bus routes */
    private List<Polyline> busRouteOverlays;

    /**
     * Constructor
     * @param context   the application context
     * @param mapView   the map view
     */
    public BusRouteDrawer(Context context, MapView mapView) {
        super(context, mapView);
        busRouteLegendOverlay = createBusRouteLegendOverlay();
        busRouteOverlays = new ArrayList<>();
    }

    /**
     * Plot each visible segment of each route pattern of each route going through the selected stop.
     */
    public void plotRoutes(int zoomLevel) {
        updateVisibleArea();
        Stop selectedStop = StopManager.getInstance().getSelected();

        List<GeoPoint> geoList = new ArrayList<>();

        if (!(selectedStop == null)) {
            for (Route route : selectedStop.getRoutes()) {
                busRouteLegendOverlay.add(route.getNumber());
                for (RoutePattern routePattern : route.getPatterns()) {
                    LatLon prevLatLon = null;
                    for (LatLon latLon : routePattern.getPath()) {
                        if (!(prevLatLon == null) && Geometry.rectangleIntersectsLine(northWest, southEast, prevLatLon, latLon)) {
                            geoList.add(new GeoPoint(prevLatLon.getLatitude(), prevLatLon.getLongitude()));
                            geoList.add(new GeoPoint(latLon.getLatitude(), latLon.getLongitude()));

                            Polyline polyLine = new Polyline(mapView.getContext());
                            int polyLineColor = busRouteLegendOverlay.getColor(route.getNumber());
                            float polyLineWidth = getLineWidth(zoomLevel);
                            setPolyline(polyLine, polyLineWidth, polyLineColor, geoList);
                            busRouteOverlays.add(polyLine);

                            geoList.clear();
                            prevLatLon = latLon;

                        } else {
                            prevLatLon = latLon;
                        }

                    }
                }
            }
        } else {
            //do nothing
        }
    }

    //EFFECTS: Sets Polyline with specific color, width, and List of GeoPoint
    private static void setPolyline(Polyline polyline, float width, int color, List<GeoPoint> geoList) {
        polyline.setColor(color);
        polyline.setPoints(geoList);
        polyline.setWidth(width);
        polyline.setVisible(true);
    }



    public List<Polyline> getBusRouteOverlays() {
        return Collections.unmodifiableList(busRouteOverlays);
    }

    public BusRouteLegendOverlay getBusRouteLegendOverlay() {
        return busRouteLegendOverlay;
    }


    /**
     * Create text overlay to display bus route colours
     */
    private BusRouteLegendOverlay createBusRouteLegendOverlay() {
        ResourceProxy rp = new DefaultResourceProxyImpl(context);
        return new BusRouteLegendOverlay(rp, BusesAreUs.dpiFactor());
    }

    /**
     * Get width of line used to plot bus route based on zoom level
     * @param zoomLevel   the zoom level of the map
     * @return            width of line used to plot bus route
     */
    private float getLineWidth(int zoomLevel) {
        if(zoomLevel > 14)
            return 7.0f * BusesAreUs.dpiFactor();
        else if(zoomLevel > 10)
            return 5.0f * BusesAreUs.dpiFactor();
        else
            return 2.0f * BusesAreUs.dpiFactor();
    }
}
