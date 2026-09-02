import express from "express";
import cors from "cors";
import dotenv from "dotenv";
import { createServer } from "http";
import { Server } from "socket.io";
import { calculateETAs } from "./services/etaService.js";

import pool from "./config/db.js";
import authRoutes from "./routes/authRoutes.js";
import routeRoutes from "./routes/routeRoutes.js";
import busRoutes from "./routes/busRoutes.js";
import stopRoutes from "./routes/stopRoutes.js";
import tripRoutes from "./routes/tripRoutes.js";
import locationRoutes from "./routes/locationRoutes.js";
import chatbotRoutes from "./routes/chatbotRoutes.js";

dotenv.config();

const app = express();
const httpServer = createServer(app);

// Socket.IO setup
const io = new Server(httpServer, {
    cors: {
        origin: "*",
        methods: ["GET", "POST"]
    }
});

// Make io accessible in controllers
app.set("io", io);

app.use(cors());
app.use(express.json());

// Routes
app.use("/api/auth", authRoutes);
app.use("/api/routes", routeRoutes);
app.use("/api/buses", busRoutes);
app.use("/api/stops", stopRoutes);
app.use("/api/trips", tripRoutes);
app.use("/api/locations", locationRoutes);
app.use("/api/chatbot", chatbotRoutes);

app.get("/", (req, res) => {
    res.send("KMT BusSetu Backend Running 🚍");
});

// Socket.IO events
io.on("connection", (socket) => {
    console.log("Client connected:", socket.id);

    // Driver joins their trip room
    socket.on("join_trip", (tripId) => {
        socket.join(`trip_${tripId}`);
        console.log(`Socket ${socket.id} joined trip_${tripId}`);
    });

    // Citizen joins a route room to watch buses on that route
    socket.on("join_route", (routeId) => {
        socket.join(`route_${routeId}`);
        console.log(`Socket ${socket.id} watching route_${routeId}`);
    });

    // Driver sends GPS location via socket (alternative to HTTP)
    socket.on("driver_location", async (data) => {
        const { trip_id, latitude, longitude, speed, route_id } = data;

        let etas = {};
        if (route_id) {
            etas = await calculateETAs(route_id, latitude, longitude, speed || 0);
        }

        // Broadcast to all clients watching this trip
        io.to(`trip_${trip_id}`).emit("location_update", {
            trip_id,
            latitude,
            longitude,
            speed: speed || 0,
            timestamp: new Date().toISOString(),
            etas
        });

        // Also broadcast to route watchers
        if (route_id) {
            io.to(`route_${route_id}`).emit("bus_position", {
                trip_id,
                latitude,
                longitude,
                speed: speed || 0,
                timestamp: new Date().toISOString(),
                etas
            });
        }
    });

    socket.on("disconnect", () => {
        console.log("Client disconnected:", socket.id);
    });
});

// Test DB connection
pool.connect()
    .then(() => console.log("PostgreSQL Connected ✅"))
    .catch(err => console.error("DB Connection Error:", err));

const PORT = process.env.PORT || 5000;

httpServer.listen(PORT, () => {
    console.log(`🚍 KMT BusSetu server running on port ${PORT}`);
});
