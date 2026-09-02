import express from "express";
import { addBus, getBuses, updateBusStatus, deleteBus } from "../controllers/busController.js";
import { verifyToken, authorizeRole } from "../middleware/authMiddleware.js";

const router = express.Router();

router.get("/", getBuses);
router.post("/", verifyToken, authorizeRole("admin"), addBus);
router.put("/:id/status", verifyToken, authorizeRole("admin"), updateBusStatus);
router.delete("/:id", verifyToken, authorizeRole("admin"), deleteBus);

export default router;
