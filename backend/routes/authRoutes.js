import express from "express";
import { register, login, getMe, getDrivers } from "../controllers/authController.js";
import { verifyToken, authorizeRole } from "../middleware/authMiddleware.js";

const router = express.Router();

router.post("/register", register);
router.post("/login", login);
router.get("/me", verifyToken, getMe);
router.get("/drivers", verifyToken, authorizeRole("admin"), getDrivers);

export default router;
