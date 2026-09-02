import pool from "../config/db.js";
import xlsx from "xlsx";
import { clearStopsCache } from "../services/etaService.js";

// Create route manually
export const createRoute = async (req, res) => {
    try {
        const { route_name, description, start_point_name, start_latitude, start_longitude, end_point_name, end_latitude, end_longitude } = req.body;
        if (!route_name) return res.status(400).json({ message: "Route name is required" });

        const result = await pool.query(
            `INSERT INTO routes (
                route_name, description, created_by,
                start_point_name, start_latitude, start_longitude,
                end_point_name, end_latitude, end_longitude
            ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9) RETURNING *`,
            [
                route_name.trim(), description || null, req.user.id,
                start_point_name || null, start_latitude || null, start_longitude || null,
                end_point_name || null, end_latitude || null, end_longitude || null
            ]
        );
        res.status(201).json(result.rows[0]);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// Upload route + stops via Excel
export const uploadRouteExcel = async (req, res) => {
    try {
        if (!req.file) return res.status(400).json({ message: "No file uploaded" });

        const workbook = xlsx.read(req.file.buffer, { type: "buffer" });
        const sheet = workbook.Sheets[workbook.SheetNames[0]];
        const rows = xlsx.utils.sheet_to_json(sheet);

        if (!rows.length) return res.status(400).json({ message: "Excel file is empty" });

        // Expect columns: route_name, stop_name, latitude, longitude, stop_order
        const routeName = rows[0].route_name;
        if (!routeName) return res.status(400).json({ message: "route_name column required in Excel" });

        rows.sort((a, b) => (a.stop_order || 0) - (b.stop_order || 0));
        const firstStop = rows[0];
        const lastStop = rows[rows.length - 1];

        const routeResult = await pool.query(
            `INSERT INTO routes (
                route_name, created_by,
                start_point_name, start_latitude, start_longitude,
                end_point_name, end_latitude, end_longitude
            ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8) RETURNING *`,
            [
                routeName.trim(), req.user.id,
                firstStop?.stop_name || null, firstStop?.latitude || null, firstStop?.longitude || null,
                lastStop?.stop_name || null, lastStop?.latitude || null, lastStop?.longitude || null
            ]
        );
        const route = routeResult.rows[0];

        const stopsInserted = [];
        for (const row of rows) {
            if (!row.stop_name || !row.latitude || !row.longitude || !row.stop_order) continue;
            const stop = await pool.query(
                "INSERT INTO stops (stop_name, latitude, longitude, route_id, stop_order) VALUES ($1,$2,$3,$4,$5) RETURNING *",
                [row.stop_name.trim(), row.latitude, row.longitude, route.id, row.stop_order]
            );
            stopsInserted.push(stop.rows[0]);
        }

        // Invalidate the stops cache so ETA calculations use the new stop list
        clearStopsCache(route.id);
        res.status(201).json({ route, stops: stopsInserted });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// Get all routes
export const getRoutes = async (req, res) => {
    try {
        const result = await pool.query(
            `SELECT r.*, u.name as created_by_name,
             COUNT(s.id) as stop_count
             FROM routes r
             LEFT JOIN users u ON r.created_by = u.id
             LEFT JOIN stops s ON s.route_id = r.id
             GROUP BY r.id, u.name
             ORDER BY r.created_at DESC`
        );
        res.json(result.rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// Get single route with stops
export const getRouteById = async (req, res) => {
    try {
        const { id } = req.params;
        const routeResult = await pool.query("SELECT * FROM routes WHERE id=$1", [id]);
        if (!routeResult.rows.length) return res.status(404).json({ message: "Route not found" });

        const stopsResult = await pool.query(
            "SELECT * FROM stops WHERE route_id=$1 ORDER BY stop_order ASC",
            [id]
        );

        res.json({ ...routeResult.rows[0], stops: stopsResult.rows });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// Create route with stops (atomic transaction)
export const createRouteWithStops = async (req, res) => {
    const client = await pool.connect();
    try {
        const { route_name, description, stations } = req.body;
        if (!route_name) return res.status(400).json({ message: "Route name is required" });
        if (!stations || !Array.isArray(stations) || stations.length < 2) {
            return res.status(400).json({ message: "At least 2 stations are required" });
        }

        await client.query("BEGIN");

        // Sort stations by order
        const sorted = [...stations].sort((a, b) => a.order - b.order);
        const first = sorted[0];
        const last = sorted[sorted.length - 1];

        // Insert route
        const routeResult = await client.query(
            `INSERT INTO routes (
                route_name, description, created_by,
                start_point_name, start_latitude, start_longitude,
                end_point_name, end_latitude, end_longitude
            ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9) RETURNING *`,
            [
                route_name.trim(), description || null, req.user.id,
                first.name || null, first.lat, first.lng,
                last.name || null, last.lat, last.lng
            ]
        );
        const route = routeResult.rows[0];

        // Insert all stops
        const stopsInserted = [];
        for (const station of sorted) {
            const stop = await client.query(
                "INSERT INTO stops (stop_name, latitude, longitude, route_id, stop_order) VALUES ($1,$2,$3,$4,$5) RETURNING *",
                [station.name.trim(), station.lat, station.lng, route.id, station.order]
            );
            stopsInserted.push(stop.rows[0]);
        }

        await client.query("COMMIT");
        // Invalidate the stops cache so ETA calculations use the new stop list
        clearStopsCache(route.id);
        res.status(201).json({ route, stops: stopsInserted });
    } catch (err) {
        await client.query("ROLLBACK");
        res.status(500).json({ error: err.message });
    } finally {
        client.release();
    }
};

// Delete route
export const deleteRoute = async (req, res) => {
    try {
        const { id } = req.params;
        await pool.query("DELETE FROM routes WHERE id=$1", [id]);
        res.json({ message: "Route deleted" });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
