import pool from "../config/db.js";

export const startTrip = async (req, res) => {
    try {
        const { bus_id, route_id } = req.body;
        const driver_id = req.user.id;

        if (!bus_id || !route_id) {
            return res.status(400).json({ message: "bus_id and route_id are required" });
        }

        // Check if driver already has an active trip
        const existing = await pool.query(
            "SELECT id FROM trips WHERE driver_id=$1 AND status='active'",
            [driver_id]
        );
        if (existing.rows.length > 0) {
            return res.status(400).json({ message: "You already have an active trip. End it first." });
        }

        // Mark bus as active
        await pool.query("UPDATE buses SET status='active' WHERE id=$1", [bus_id]);

        const result = await pool.query(
            `INSERT INTO trips (driver_id, bus_id, route_id, status)
             VALUES ($1, $2, $3, 'active') RETURNING *`,
            [driver_id, bus_id, route_id]
        );

        const trip = result.rows[0];

        // Get route + bus info for response
        const info = await pool.query(
            `SELECT t.*, b.bus_number, r.route_name, u.name as driver_name
             FROM trips t
             JOIN buses b ON t.bus_id = b.id
             JOIN routes r ON t.route_id = r.id
             JOIN users u ON t.driver_id = u.id
             WHERE t.id=$1`,
            [trip.id]
        );

        res.status(201).json(info.rows[0]);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

export const endTrip = async (req, res) => {
    try {
        const { id } = req.params;
        const driver_id = req.user.id;

        const tripResult = await pool.query(
            "SELECT * FROM trips WHERE id=$1 AND driver_id=$2",
            [id, driver_id]
        );
        if (!tripResult.rows.length) {
            return res.status(404).json({ message: "Trip not found or not yours" });
        }

        const trip = tripResult.rows[0];

        // Mark bus as inactive
        await pool.query("UPDATE buses SET status='inactive' WHERE id=$1", [trip.bus_id]);

        const result = await pool.query(
            "UPDATE trips SET status='completed', ended_at=NOW() WHERE id=$1 RETURNING *",
            [id]
        );
        res.json(result.rows[0]);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

export const getActiveTrips = async (req, res) => {
    try {
        const result = await pool.query(
            `SELECT t.*, b.bus_number, r.route_name, u.name as driver_name,
             l.latitude as current_lat, l.longitude as current_lng, l.speed,
             l.timestamp as last_update
             FROM trips t
             JOIN buses b ON t.bus_id = b.id
             JOIN routes r ON t.route_id = r.id
             JOIN users u ON t.driver_id = u.id
             LEFT JOIN LATERAL (
                 SELECT latitude, longitude, speed, timestamp
                 FROM locations WHERE trip_id = t.id
                 ORDER BY timestamp DESC LIMIT 1
             ) l ON true
             WHERE t.status='active'
             ORDER BY t.started_at DESC`
        );
        res.json(result.rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

export const getTripsByRoute = async (req, res) => {
    try {
        const { routeId } = req.params;
        const result = await pool.query(
            `SELECT t.*, b.bus_number, u.name as driver_name,
             l.latitude as current_lat, l.longitude as current_lng, l.speed
             FROM trips t
             JOIN buses b ON t.bus_id = b.id
             JOIN users u ON t.driver_id = u.id
             LEFT JOIN LATERAL (
                 SELECT latitude, longitude, speed
                 FROM locations WHERE trip_id = t.id
                 ORDER BY timestamp DESC LIMIT 1
             ) l ON true
             WHERE t.route_id=$1 AND t.status='active'`,
            [routeId]
        );
        res.json(result.rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

export const getMyActiveTrip = async (req, res) => {
    try {
        const driver_id = req.user.id;
        const result = await pool.query(
            `SELECT t.*, b.bus_number, r.route_name
             FROM trips t
             JOIN buses b ON t.bus_id = b.id
             JOIN routes r ON t.route_id = r.id
             WHERE t.driver_id=$1 AND t.status='active'
             LIMIT 1`,
            [driver_id]
        );
        res.json(result.rows[0] || null);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
