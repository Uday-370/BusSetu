import express from "express";
import { processChat } from "../controllers/chatbotController.js";

const router = express.Router();

// Route: POST /api/chatbot
router.post("/", processChat);

export default router;
