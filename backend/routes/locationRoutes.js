import express from "express";
import { updateLocation, getLatestLocation, getLocationHistory } from "../controllers/locationController.js";
import { verifyToken, authorizeRole } from "../middleware/authMiddleware.js";

const router = express.Router();

router.post("/update", verifyToken, authorizeRole("driver"), updateLocation);
router.get("/trip/:tripId/latest", getLatestLocation);
router.get("/trip/:tripId/history", getLocationHistory);

export default router;
