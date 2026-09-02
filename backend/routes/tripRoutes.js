import express from "express";
import { startTrip, endTrip, getActiveTrips, getTripsByRoute, getMyActiveTrip } from "../controllers/tripController.js";
import { verifyToken, authorizeRole } from "../middleware/authMiddleware.js";

const router = express.Router();

router.get("/active", getActiveTrips);
router.get("/route/:routeId", getTripsByRoute);
router.get("/my-trip", verifyToken, authorizeRole("driver"), getMyActiveTrip);
router.post("/start", verifyToken, authorizeRole("driver"), startTrip);
router.put("/:id/end", verifyToken, authorizeRole("driver"), endTrip);

export default router;
