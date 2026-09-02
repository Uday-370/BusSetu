import express from "express";
import { addStop, getStopsByRoute, getAllStops, deleteStop } from "../controllers/stopController.js";
import { verifyToken, authorizeRole } from "../middleware/authMiddleware.js";

const router = express.Router();

router.get("/", getAllStops);
router.get("/route/:routeId", getStopsByRoute);
router.post("/", verifyToken, authorizeRole("admin"), addStop);
router.delete("/:id", verifyToken, authorizeRole("admin"), deleteStop);

export default router;
