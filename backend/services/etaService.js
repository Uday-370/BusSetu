import pool from "../config/db.js";

// Accurate Haversine distance in km
const getDistanceInKm = (lat1, lon1, lat2, lon2) => {
    const R = 6371;
    const dLat = (lat2 - lat1) * (Math.PI / 180);
    const dLon = (lon2 - lon1) * (Math.PI / 180);
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + 
              Math.cos(lat1 * (Math.PI / 180)) * Math.cos(lat2 * (Math.PI / 180)) * 
              Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c; 
};

// Cache for route stops to avoid constant DB queries
const stopsCache = new Map();
const stopsCacheTime = new Map();
const CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes

// Invalidate the stops cache for a route (call after any stop mutation)
export const clearStopsCache = (route_id) => {
    stopsCache.delete(route_id);
    stopsCacheTime.delete(route_id);
};

export const getRouteStops = async (route_id) => {
    if (stopsCache.has(route_id)) {
        const cachedAt = stopsCacheTime.get(route_id) || 0;
        if (Date.now() - cachedAt < CACHE_TTL_MS) {
            return stopsCache.get(route_id);
        }
        // TTL expired — fall through to re-fetch from DB
    }

    const stopsResult = await pool.query(
        "SELECT * FROM stops WHERE route_id=$1 ORDER BY stop_order ASC",
        [route_id]
    );
    let stops = stopsResult.rows;

    if (stops.length === 0) {
        // Fallback to route start/end points if no stops exist
        const routeResult = await pool.query("SELECT * FROM routes WHERE id=$1", [route_id]);
        const routeData = routeResult.rows[0];
        if (routeData) {
            const fallback = [];
            if (routeData.start_point_name && routeData.start_latitude) {
                fallback.push({
                    id: 'start', stop_name: routeData.start_point_name,
                    latitude: routeData.start_latitude, longitude: routeData.start_longitude
                });
            }
            if (routeData.end_point_name && routeData.end_latitude) {
                fallback.push({
                    id: 'end', stop_name: routeData.end_point_name,
                    latitude: routeData.end_latitude, longitude: routeData.end_longitude
                });
            }
            stops = fallback;
        }
    }
    
    // Add roadDistFromPrev using Haversine
    for (let i = 0; i < stops.length; i++) {
        if (i === 0) {
            stops[i].roadDistFromPrev = 0;
        } else {
            stops[i].roadDistFromPrev = getDistanceInKm(stops[i-1].latitude, stops[i-1].longitude, stops[i].latitude, stops[i].longitude);
        }
    }
    
    stopsCache.set(route_id, stops);
    stopsCacheTime.set(route_id, Date.now());
    return stops;
};

// Calculates ETA for all stops on the route
export const calculateETAs = async (route_id, current_lat, current_lng, speed) => {
    if (!route_id || !current_lat || !current_lng) return {};

    const stops = await getRouteStops(route_id);
    if (!stops || stops.length === 0) return {};
    
    // Find closest stop
    let minIdx = 0;
    let minDist = Infinity;
    stops.forEach((stop, i) => {
        const d = getDistanceInKm(current_lat, current_lng, stop.latitude, stop.longitude);
        if (d < minDist) { minDist = d; minIdx = i; }
    });
    
    const busSpeed = parseFloat(speed) || 0;
    const effectiveSpeed = busSpeed > 5 ? busSpeed : 25; // default 25km/h
    
    const etas = {};
    let runningDistance = 0;
    
    for (let i = 0; i < stops.length; i++) {
        const stop = stops[i];
        if (i < minIdx) {
            // Passed
            etas[stop.id] = null;
        } else if (i === minIdx) {
            // Current closest
            runningDistance = getDistanceInKm(current_lat, current_lng, stop.latitude, stop.longitude);
            const timeHours = runningDistance / effectiveSpeed;
            etas[stop.id] = Math.max(1, Math.round(timeHours * 60));
        } else {
            // Upcoming
            runningDistance += stop.roadDistFromPrev || getDistanceInKm(stops[i-1].latitude, stops[i-1].longitude, stop.latitude, stop.longitude);
            const timeHours = runningDistance / effectiveSpeed;
            etas[stop.id] = Math.max(1, Math.round(timeHours * 60));
        }
    }
    
    return etas;
};
