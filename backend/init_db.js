import pkg from "pg";
import fs from "fs";
import path from "path";
import dotenv from "dotenv";
import { fileURLToPath } from "url";

dotenv.config();

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const { Pool } = pkg;

const pool = new Pool({
    user: process.env.DB_USER,
    host: process.env.DB_HOST,
    database: process.env.DB_NAME,
    password: process.env.DB_PASSWORD,
    port: process.env.DB_PORT,
});

const initializeDatabase = async () => {
    try {
        console.log("Connecting to the database...");
        const schemaPath = path.join(__dirname, "schema.sql");
        const schema = fs.readFileSync(schemaPath, "utf8");

        console.log("Executing schema.sql...");
        await pool.query(schema);

        console.log("Database initialized successfully!");
    } catch (err) {
        console.error("Error initializing database:", err.message);
    } finally {
        await pool.end();
    }
};

initializeDatabase();
