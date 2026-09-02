import pool from "../config/db.js";

export const addBus = async (req, res) => {
    try {
        const { bus_number, status } = req.body;
        if (!bus_number) return res.status(400).json({ message: "Bus number is required" });

        const result = await pool.query(
            "INSERT INTO buses (bus_number, status) VALUES ($1, $2) RETURNING *",
            [bus_number.trim(), status || "inactive"]
        );
        res.status(201).json(result.rows[0]);
    } catch (err) {
        if (err.code === "23505") return res.status(400).json({ message: "Bus number already exists" });
        res.status(500).json({ error: err.message });
    }
};

export const getBuses = async (req, res) => {
    try {
        const result = await pool.query(
            `SELECT b.*,
             t.id as active_trip_id,
             u.name as driver_name
             FROM buses b
             LEFT JOIN trips t ON t.bus_id = b.id AND t.status = 'active'
             LEFT JOIN users u ON t.driver_id = u.id
             ORDER BY b.id ASC`
        );
        res.json(result.rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

export const updateBusStatus = async (req, res) => {
    try {
        const { id } = req.params;
        const { status } = req.body;
        const result = await pool.query(
            "UPDATE buses SET status=$1 WHERE id=$2 RETURNING *",
            [status, id]
        );
        if (!result.rows.length) return res.status(404).json({ message: "Bus not found" });
        res.json(result.rows[0]);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

export const deleteBus = async (req, res) => {
    try {
        const { id } = req.params;
        await pool.query("DELETE FROM buses WHERE id=$1", [id]);
        res.json({ message: "Bus deleted" });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
