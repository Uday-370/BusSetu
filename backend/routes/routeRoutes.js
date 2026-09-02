import express from "express";
import multer from "multer";
import { createRoute, createRouteWithStops, uploadRouteExcel, getRoutes, getRouteById, deleteRoute } from "../controllers/routeController.js";
import { verifyToken, authorizeRole } from "../middleware/authMiddleware.js";

const router = express.Router();
const upload = multer({ storage: multer.memoryStorage() });

router.get("/", getRoutes);
router.get("/:id", getRouteById);
router.post("/create", verifyToken, authorizeRole("admin"), createRoute);
router.post("/create-with-stops", verifyToken, authorizeRole("admin"), createRouteWithStops);
router.post("/upload-excel", verifyToken, authorizeRole("admin"), upload.single("file"), uploadRouteExcel);
router.delete("/:id", verifyToken, authorizeRole("admin"), deleteRoute);

export default router;
