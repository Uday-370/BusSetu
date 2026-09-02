import pool from "../config/db.js";

const INTENTS = {
  GREETING: 'greeting',
  HELP: 'help',
  LIST_ROUTES: 'list_routes',
  ROUTE_DETAILS: 'route_details',
  ROUTE_STOPS: 'route_stops',
  ACTIVE_BUSES: 'active_buses',
  FIND_ROUTE_BY_STOP: 'find_route_by_stop',
  UNKNOWN: 'unknown',
};

function parseIntent(text) {
  const t = text.toLowerCase().trim();

  if (/^(hi|hello|hey|good\s?(morning|afternoon|evening)|namaste|hii+)[\s!.]*$/i.test(t))
    return { intent: INTENTS.GREETING };

  if (/^(help|commands|what can you do|menu)[\s?!]*$/i.test(t))
    return { intent: INTENTS.HELP };

  const stopsMatch = t.match(/stops?\s+(?:on|for|of|in)\s+(?:route\s+)?(.+)/i)
    || t.match(/(?:route\s+)?(.+?)\s+stops/i);
  if (stopsMatch) {
    return { intent: INTENTS.ROUTE_STOPS, query: stopsMatch[1].trim() };
  }

  const detailMatch = t.match(/(?:route|about route|details? (?:of|for) route)\s+(.+)/i);
  if (detailMatch) {
    return { intent: INTENTS.ROUTE_DETAILS, query: detailMatch[1].trim() };
  }

  if (/active|running|live|current\s+bus/i.test(t))
    return { intent: INTENTS.ACTIVE_BUSES };

  if (/routes|all routes|show routes|list routes|available routes/i.test(t))
    return { intent: INTENTS.LIST_ROUTES };

  const findMatch = t.match(/(?:which|what)\s+routes?\s+(?:goes?|pass(?:es)?|stops?)\s+(?:to|at|through|via)\s+(.+)/i)
    || t.match(/(?:find|search)\s+(?:route|bus)\s+(?:for|to|at)\s+(.+)/i)
    || t.match(/(?:bus|route)\s+(?:to|for|from)\s+(.+)/i);
  if (findMatch) {
    return { intent: INTENTS.FIND_ROUTE_BY_STOP, query: findMatch[1].trim() };
  }

  return { intent: INTENTS.UNKNOWN };
}

async function handleIntent(intentObj) {
  const { intent, query } = intentObj;
  
  switch (intent) {
    case INTENTS.GREETING:
      return {
        text: "Hello! 👋 I'm the BusSetu assistant. I can help you with bus routes, stops, and live tracking. Type **help** to see what I can do!",
        icon: 'bus',
      };

    case INTENTS.HELP:
      return {
        text: `Here's what I can help you with:\n\n🚌 **"routes"** — List all bus routes\n📍 **"stops on route [name/id]"** — See stops for a route\n🟢 **"active buses"** — See currently running buses\n🔍 **"bus to [place]"** — Find routes passing through a stop\n📋 **"route [name/id]"** — Get route details\n\nJust type your question naturally!`,
        icon: 'help',
      };

    case INTENTS.LIST_ROUTES: {
      const result = await pool.query("SELECT * FROM routes ORDER BY id ASC");
      const routes = result.rows;
      if (!routes.length) return { text: 'No routes are currently configured in the system.', icon: 'route' };
      const list = routes.map((r, i) => `${i + 1}. **${r.route_name}** (ID: ${r.id})\n   ${r.start_point_name || '?'} → ${r.end_point_name || '?'}`).join('\n');
      return { text: `🚌 **Available Routes (${routes.length}):**\n\n${list}\n\nType **stops on route [name]** for stop details.`, icon: 'route' };
    }

    case INTENTS.ROUTE_DETAILS: {
      const q = query.toLowerCase().replace(/^#/, '');
      const result = await pool.query("SELECT * FROM routes WHERE id::text = $1 OR LOWER(route_name) LIKE $2 LIMIT 1", [q, `%${q}%`]);
      if (!result.rows.length) return { text: `❌ I couldn't find a route matching "${query}". Try typing **routes** to see all available routes.`, icon: 'route' };
      const route = result.rows[0];
      
      const stopsResult = await pool.query("SELECT * FROM stops WHERE route_id = $1", [route.id]);
      const stops = stopsResult.rows;
      
      return {
        text: `📋 **Route: ${route.route_name}**\n\n📌 From: ${route.start_point_name || 'N/A'}\n📌 To: ${route.end_point_name || 'N/A'}\n${route.description ? `📝 ${route.description}\n` : ''}🔢 Stops: ${stops.length}\n\nType **stops on route ${route.id}** to see all stops.`,
        icon: 'route',
      };
    }

    case INTENTS.ROUTE_STOPS: {
      const q = query.toLowerCase().replace(/^#/, '');
      const result = await pool.query("SELECT * FROM routes WHERE id::text = $1 OR LOWER(route_name) LIKE $2 LIMIT 1", [q, `%${q}%`]);
      if (!result.rows.length) return { text: `❌ I couldn't find a route matching "${query}". Try typing **routes** to see all available routes.`, icon: 'pin' };
      const route = result.rows[0];
      
      const stopsResult = await pool.query("SELECT * FROM stops WHERE route_id = $1 ORDER BY stop_order ASC", [route.id]);
      const stops = stopsResult.rows;
      
      if (!stops.length) return { text: `Route **${route.route_name}** has no stops configured yet.`, icon: 'pin' };
      const list = stops.map((s, i) => `${i + 1}. ${s.stop_name}`).join('\n');
      return { text: `📍 **Stops on ${route.route_name}** (${stops.length} stops):\n\n${list}`, icon: 'pin' };
    }

    case INTENTS.ACTIVE_BUSES: {
      const result = await pool.query(`
        SELECT t.*, b.bus_number, r.route_name
        FROM trips t
        JOIN buses b ON t.bus_id = b.id
        JOIN routes r ON t.route_id = r.id
        WHERE t.status='active'
      `);
      const trips = result.rows;
      if (!trips.length) return { text: '🔴 No buses are currently running. Check back later!', icon: 'bus' };
      const list = trips.map((t, i) => `${i + 1}. Bus **${t.bus_number}** on route **${t.route_name}**`).join('\n');
      return { text: `🟢 **Active Buses (${trips.length}):**\n\n${list}\n\nGo to **Citizen → Track** to see them live on the map!`, icon: 'bus' };
    }

    case INTENTS.FIND_ROUTE_BY_STOP: {
      const result = await pool.query(`
        SELECT DISTINCT r.* 
        FROM routes r
        JOIN stops s ON s.route_id = r.id
        WHERE LOWER(s.stop_name) LIKE $1
      `, [`%${query.toLowerCase()}%`]);
      
      const matchedRoutes = result.rows;
      if (!matchedRoutes.length) return { text: `❌ No routes found passing through "${query}". Try a different stop name.`, icon: 'pin' };
      const list = matchedRoutes.map((r, i) => `${i + 1}. **${r.route_name}** (${r.start_point_name} → ${r.end_point_name})`).join('\n');
      return { text: `🔍 **Routes passing through "${query}":**\n\n${list}`, icon: 'route' };
    }

    default:
      return {
        text: "🤔 I didn't quite understand that. Try asking about **routes**, **stops**, or **active buses**. Type **help** for a list of commands!",
        icon: 'help',
      };
  }
}

export const processChat = async (req, res) => {
    try {
        const { message } = req.body;
        if (!message) {
            return res.status(400).json({ error: "Message is required" });
        }
        
        const intentObj = parseIntent(message);
        const response = await handleIntent(intentObj);
        
        res.json(response);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
