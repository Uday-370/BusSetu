import pool from "../config/db.js";
import { calculateETAs } from "../services/etaService.js";

export const updateLocation = async (req, res) => {
    try {
        const { trip_id, latitude, longitude, speed } = req.body;
        if (!trip_id || !latitude || !longitude) {
            return res.status(400).json({ message: "trip_id, latitude, longitude required" });
        }

        const result = await pool.query(
            "INSERT INTO locations (trip_id, latitude, longitude, speed) VALUES ($1,$2,$3,$4) RETURNING *",
            [trip_id, latitude, longitude, speed || 0]
        );

        // Fetch route_id to notify route watchers
        const tripRes = await pool.query("SELECT route_id FROM trips WHERE id=$1", [trip_id]);
        const route_id = tripRes.rows[0]?.route_id;

        // Emit via socket.io (attached to app)
        const io = req.app.get("io");
        if (io) {
            let etas = {};
            if (route_id) {
                etas = await calculateETAs(route_id, latitude, longitude, speed || 0);
            }

            const updateData = {
                trip_id,
                latitude,
                longitude,
                speed: speed || 0,
                timestamp: result.rows[0].timestamp,
                etas
            };

            // Notify trip watchers
            io.to(`trip_${trip_id}`).emit("location_update", updateData);

            // Notify route watchers (citizens)
            if (route_id) {
                io.to(`route_${route_id}`).emit("bus_position", updateData);
            }
        }

        res.json(result.rows[0]);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

export const getLatestLocation = async (req, res) => {
    try {
        const { tripId } = req.params;
        const result = await pool.query(
            "SELECT * FROM locations WHERE trip_id=$1 ORDER BY timestamp DESC LIMIT 1",
            [tripId]
        );
        res.json(result.rows[0] || null);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

export const getLocationHistory = async (req, res) => {
    try {
        const { tripId } = req.params;
        const result = await pool.query(
            "SELECT * FROM locations WHERE trip_id=$1 ORDER BY timestamp ASC",
            [tripId]
        );
        res.json(result.rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
