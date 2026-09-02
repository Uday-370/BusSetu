import { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { MapContainer, TileLayer, Marker, Polyline, Popup, useMapEvents, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import AdminSidebar from '../../components/AdminSidebar';
import { createRouteWithStops } from '../../services/api';
import {
  MapPin, Plus, Trash2, GripVertical, ArrowUp, ArrowDown,
  Route, Menu, AlertCircle, CheckCircle, MousePointer, Hand,
  ArrowLeft, Loader2, Sparkles, Navigation
} from 'lucide-react';

// ── Marker icon factories ──────────────────────────────────

const makeStationIcon = (type, index) => {
  const colors = {
    start: { bg: '#10b981', ring: 'rgba(16,185,129,0.3)', label: 'S' },
    end: { bg: '#ef4444', ring: 'rgba(239,68,68,0.3)', label: 'E' },
    mid: { bg: '#3b82f6', ring: 'rgba(59,130,246,0.25)', label: index },
  };
  const c = colors[type] || colors.mid;

  return L.divIcon({
    className: 'route-builder-marker',
    html: `
      <div style="
        position:relative; width:36px; height:36px;
        display:flex; align-items:center; justify-content:center;
      ">
        <div style="
          position:absolute; inset:-6px;
          background:${c.ring}; border-radius:50%;
          animation: marker-ring-pulse 2s ease-in-out infinite;
        "></div>
        <div style="
          width:32px; height:32px; border-radius:50%;
          background:${c.bg}; color:white;
          font-weight:800; font-size:13px; font-family:'Inter',sans-serif;
          display:flex; align-items:center; justify-content:center;
          border:3px solid white;
          box-shadow:0 2px 10px rgba(0,0,0,0.25);
          position:relative; z-index:2;
          cursor: grab;
        ">${c.label}</div>
      </div>
    `,
    iconSize: [36, 36],
    iconAnchor: [18, 18],
  });
};

// ── Reverse geocode helper ─────────────────────────────────

const reverseGeocode = async (lat, lng) => {
  try {
    const res = await fetch(
      `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json&addressdetails=1&zoom=18`,
      { headers: { 'Accept-Language': 'en' } }
    );
    const data = await res.json();
    // Try to get a meaningful short name
    const addr = data.address || {};
    return (
      addr.bus_stop || addr.station || addr.amenity || addr.building ||
      addr.road || addr.neighbourhood || addr.suburb ||
      data.display_name?.split(',')[0] || `Station`
    );
  } catch {
    return `Station`;
  }
};

// ── Map click handler component ────────────────────────────

const MapClickHandler = ({ mode, onMapClick }) => {
  useMapEvents({
    click(e) {
      if (mode === 'add') {
        onMapClick(e.latlng);
      }
    },
  });
  return null;
};

// ── Auto-fit bounds component ──────────────────────────────

const FitBounds = ({ stations }) => {
  const map = useMap();
  useEffect(() => {
    if (stations.length === 0) return;
    const bounds = L.latLngBounds(stations.map(s => [s.lat, s.lng]));
    map.fitBounds(bounds.pad(0.15), { maxZoom: 15 });
  }, [stations, map]);
  return null;
};

// ── Toast component ────────────────────────────────────────

const Toast = ({ toast }) =>
  toast ? (
    <div
      className={`flex items-center gap-2.5 px-4 py-3 rounded-xl text-sm font-medium animate-fade-in ${
        toast.type === 'error'
          ? 'bg-red-50 text-red-600 border border-red-100'
          : 'bg-emerald-50 text-emerald-700 border border-emerald-100'
      }`}
    >
      {toast.type === 'error' ? (
        <AlertCircle className="w-4 h-4 flex-shrink-0" />
      ) : (
        <CheckCircle className="w-4 h-4 flex-shrink-0" />
      )}
      {toast.msg}
    </div>
  ) : null;

// ══════════════════════════════════════════════════════════
// ██  MAIN COMPONENT
// ══════════════════════════════════════════════════════════

const AdminRouteBuilder = () => {
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  // Route metadata
  const [routeName, setRouteName] = useState('');
  const [description, setDescription] = useState('');

  // Stations (ordered list)
  const [stations, setStations] = useState([]);

  // Map mode: 'add' or 'pan'
  const [mode, setMode] = useState('add');

  // OSRM road polyline
  const [routePoints, setRoutePoints] = useState([]);

  // Loading / toast
  const [loading, setLoading] = useState(false);
  const [geocoding, setGeocoding] = useState(false);
  const [toast, setToast] = useState(null);

  const showToast = (msg, type = 'success') => {
    setToast({ msg, type });
    setTimeout(() => setToast(null), 4000);
  };

  // ── Add station on map click ───────────────────────────

  const handleMapClick = useCallback(async (latlng) => {
    const { lat, lng } = latlng;
    const tempId = Date.now();
    const order = stations.length + 1;

    // Immediately add marker with placeholder name
    const newStation = { id: tempId, name: `Station ${order}`, lat, lng, order };
    setStations(prev => [...prev, newStation]);

    // Reverse geocode to get a real name
    setGeocoding(true);
    const name = await reverseGeocode(lat, lng);
    setStations(prev =>
      prev.map(s => (s.id === tempId ? { ...s, name } : s))
    );
    setGeocoding(false);
  }, [stations.length]);

  // ── Drag marker to reposition ──────────────────────────

  const handleDragEnd = useCallback((id, newLatLng) => {
    setStations(prev =>
      prev.map(s => (s.id === id ? { ...s, lat: newLatLng.lat, lng: newLatLng.lng } : s))
    );
    // Re-geocode for new position
    reverseGeocode(newLatLng.lat, newLatLng.lng).then(name => {
      setStations(prev =>
        prev.map(s => (s.id === id ? { ...s, name } : s))
      );
    });
  }, []);

  // ── Delete station ─────────────────────────────────────

  const deleteStation = useCallback((id) => {
    setStations(prev => {
      const filtered = prev.filter(s => s.id !== id);
      return filtered.map((s, i) => ({ ...s, order: i + 1 }));
    });
  }, []);

  // ── Reorder station ────────────────────────────────────

  const moveStation = useCallback((index, direction) => {
    setStations(prev => {
      const arr = [...prev];
      const newIndex = index + direction;
      if (newIndex < 0 || newIndex >= arr.length) return arr;
      [arr[index], arr[newIndex]] = [arr[newIndex], arr[index]];
      return arr.map((s, i) => ({ ...s, order: i + 1 }));
    });
  }, []);

  // ── Rename station ─────────────────────────────────────

  const renameStation = useCallback((id, newName) => {
    setStations(prev =>
      prev.map(s => (s.id === id ? { ...s, name: newName } : s))
    );
  }, []);

  // ── Fetch OSRM road polyline when stations change ──────

  useEffect(() => {
    if (stations.length < 2) {
      setRoutePoints(stations.map(s => [s.lat, s.lng]));
      return;
    }

    const timer = setTimeout(() => {
      const coords = stations.map(s => `${s.lng},${s.lat}`).join(';');
      fetch(`https://router.project-osrm.org/route/v1/driving/${coords}?overview=full&geometries=geojson`)
        .then(r => r.json())
        .then(data => {
          if (data.code === 'Ok' && data.routes?.[0]) {
            setRoutePoints(data.routes[0].geometry.coordinates.map(c => [c[1], c[0]]));
          } else {
            setRoutePoints(stations.map(s => [s.lat, s.lng]));
          }
        })
        .catch(() => setRoutePoints(stations.map(s => [s.lat, s.lng])));
    }, 400);

    return () => clearTimeout(timer);
  }, [stations]);

  // ── Submit ─────────────────────────────────────────────

  const handleSubmit = async () => {
    if (!routeName.trim()) return showToast('Please enter a route name', 'error');
    if (stations.length < 2) return showToast('Place at least 2 stations on the map', 'error');

    setLoading(true);
    try {
      await createRouteWithStops({
        route_name: routeName,
        description,
        stations: stations.map(s => ({
          name: s.name,
          lat: s.lat,
          lng: s.lng,
          order: s.order,
        })),
      });
      showToast(`Route "${routeName}" created with ${stations.length} stops!`);
      setTimeout(() => navigate('/admin/routes'), 1200);
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to create route', 'error');
    } finally {
      setLoading(false);
    }
  };

  // ── Determine station types ────────────────────────────

  const getStationType = (index) => {
    if (stations.length === 0) return 'mid';
    if (index === 0) return 'start';
    if (index === stations.length - 1) return 'end';
    return 'mid';
  };

  // ══════════════════════════════════════════════════════════
  // ██  RENDER
  // ══════════════════════════════════════════════════════════

  return (
    <div className="min-h-screen bg-slate-50 flex">
      <AdminSidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* ── Top Bar ──────────────────────────────────── */}
        <div className="bg-white border-b border-slate-100 px-4 sm:px-8 py-4 flex items-center gap-3 sticky top-0 z-30">
          <button onClick={() => setSidebarOpen(true)} className="md:hidden p-2 rounded-lg hover:bg-slate-100">
            <Menu className="w-5 h-5 text-slate-600" />
          </button>
          <button onClick={() => navigate('/admin/routes')}
            className="w-9 h-9 rounded-xl bg-slate-100 hover:bg-slate-200 flex items-center justify-center transition-colors flex-shrink-0">
            <ArrowLeft className="w-4 h-4 text-slate-600" />
          </button>
          <div className="w-9 h-9 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-xl flex items-center justify-center flex-shrink-0">
            <Sparkles className="w-4 h-4 text-white" />
          </div>
          <div className="flex-1 min-w-0">
            <h1 className="text-lg sm:text-xl font-bold text-slate-800">Route Builder</h1>
            <p className="text-slate-400 text-xs">Click on the map to place stations</p>
          </div>
          {geocoding && (
            <div className="flex items-center gap-2 text-blue-600 text-xs font-medium bg-blue-50 px-3 py-1.5 rounded-full">
              <Loader2 className="w-3 h-3 animate-spin" />
              Geocoding...
            </div>
          )}
        </div>

        {/* ── Main Content ─────────────────────────────── */}
        <div className="flex-1 flex flex-col lg:flex-row overflow-hidden">
          {/* ── LEFT PANEL: Controls ───────────────────── */}
          <div className="lg:w-[380px] xl:w-[420px] flex-shrink-0 overflow-y-auto border-r border-slate-100 bg-white">
            <div className="p-5 sm:p-6 space-y-5">
              {toast && <Toast toast={toast} />}

              {/* Route Info */}
              <div className="space-y-3">
                <h2 className="text-sm font-bold text-slate-600 uppercase tracking-wider flex items-center gap-2">
                  <Route className="w-4 h-4 text-blue-500" />
                  Route Details
                </h2>
                <input
                  id="builder-route-name"
                  type="text"
                  placeholder="Route name (e.g. KMT Route 42)"
                  className="w-full px-4 py-3 border border-slate-200 rounded-xl bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 text-slate-800 text-sm placeholder-slate-400 font-medium"
                  value={routeName}
                  onChange={e => setRouteName(e.target.value)}
                />
                <textarea
                  placeholder="Description (optional)"
                  className="w-full px-4 py-2.5 border border-slate-200 rounded-xl bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 text-slate-800 text-sm resize-none h-16 placeholder-slate-400"
                  value={description}
                  onChange={e => setDescription(e.target.value)}
                />
              </div>

              {/* Mode Toggle */}
              <div className="flex gap-1.5 p-1 bg-slate-100 rounded-xl">
                <button
                  onClick={() => setMode('add')}
                  className={`flex-1 flex items-center justify-center gap-2 px-3 py-2.5 rounded-lg text-sm font-semibold transition-all ${
                    mode === 'add'
                      ? 'bg-white text-blue-600 shadow-sm ring-1 ring-black/5'
                      : 'text-slate-500 hover:text-slate-700'
                  }`}
                >
                  <MousePointer className="w-4 h-4" />
                  Add Station
                </button>
                <button
                  onClick={() => setMode('pan')}
                  className={`flex-1 flex items-center justify-center gap-2 px-3 py-2.5 rounded-lg text-sm font-semibold transition-all ${
                    mode === 'pan'
                      ? 'bg-white text-blue-600 shadow-sm ring-1 ring-black/5'
                      : 'text-slate-500 hover:text-slate-700'
                  }`}
                >
                  <Hand className="w-4 h-4" />
                  Pan / Drag
                </button>
              </div>

              {/* Station instructions */}
              {stations.length === 0 && (
                <div className="bg-blue-50 border border-blue-100 rounded-xl p-4 text-center">
                  <Navigation className="w-8 h-8 text-blue-400 mx-auto mb-2" />
                  <p className="text-blue-700 text-sm font-semibold">Click on the map to start</p>
                  <p className="text-blue-500 text-xs mt-1">Each click adds a station. First = start, last = end.</p>
                </div>
              )}

              {/* Station List */}
              {stations.length > 0 && (
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <h2 className="text-sm font-bold text-slate-600 uppercase tracking-wider flex items-center gap-2">
                      <MapPin className="w-4 h-4 text-teal-500" />
                      Stations ({stations.length})
                    </h2>
                    <button
                      onClick={() => { setStations([]); setRoutePoints([]); }}
                      className="text-xs text-red-500 hover:text-red-600 font-medium hover:underline"
                    >
                      Clear all
                    </button>
                  </div>

                  <div className="space-y-1.5 max-h-[45vh] overflow-y-auto pr-1 route-builder-station-list">
                    {stations.map((station, i) => {
                      const type = getStationType(i);
                      const colorClass =
                        type === 'start' ? 'border-emerald-200 bg-emerald-50/50'
                        : type === 'end' ? 'border-red-200 bg-red-50/50'
                        : 'border-slate-150 bg-white';
                      const dotColor =
                        type === 'start' ? 'bg-emerald-500' : type === 'end' ? 'bg-red-500' : 'bg-blue-500';
                      const label =
                        type === 'start' ? 'START' : type === 'end' ? 'END' : `STOP ${i}`;

                      return (
                        <div
                          key={station.id}
                          className={`flex items-center gap-2 p-3 rounded-xl border ${colorClass} transition-all group hover:shadow-sm`}
                        >
                          {/* Dot */}
                          <div className={`w-3 h-3 rounded-full ${dotColor} flex-shrink-0 ring-2 ring-white shadow-sm`} />

                          {/* Info */}
                          <div className="flex-1 min-w-0">
                            <input
                              type="text"
                              value={station.name}
                              onChange={e => renameStation(station.id, e.target.value)}
                              className="w-full text-sm font-semibold text-slate-800 bg-transparent border-none outline-none focus:bg-white focus:ring-1 focus:ring-blue-400 rounded px-1 py-0.5 -ml-1 transition-all"
                            />
                            <div className="flex items-center gap-2 mt-0.5 pl-1">
                              <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">{label}</span>
                              <span className="text-[10px] text-slate-400 font-mono">
                                {station.lat.toFixed(4)}, {station.lng.toFixed(4)}
                              </span>
                            </div>
                          </div>

                          {/* Actions */}
                          <div className="flex items-center gap-0.5 opacity-0 group-hover:opacity-100 transition-opacity flex-shrink-0">
                            <button
                              onClick={() => moveStation(i, -1)}
                              disabled={i === 0}
                              className="p-1.5 rounded-lg text-slate-400 hover:text-blue-600 hover:bg-blue-50 disabled:opacity-30 disabled:cursor-not-allowed transition-all"
                              title="Move up"
                            >
                              <ArrowUp className="w-3.5 h-3.5" />
                            </button>
                            <button
                              onClick={() => moveStation(i, 1)}
                              disabled={i === stations.length - 1}
                              className="p-1.5 rounded-lg text-slate-400 hover:text-blue-600 hover:bg-blue-50 disabled:opacity-30 disabled:cursor-not-allowed transition-all"
                              title="Move down"
                            >
                              <ArrowDown className="w-3.5 h-3.5" />
                            </button>
                            <button
                              onClick={() => deleteStation(station.id)}
                              className="p-1.5 rounded-lg text-slate-400 hover:text-red-500 hover:bg-red-50 transition-all"
                              title="Remove station"
                            >
                              <Trash2 className="w-3.5 h-3.5" />
                            </button>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}

              {/* Submit Button */}
              <button
                id="create-route-with-stops-btn"
                onClick={handleSubmit}
                disabled={loading || stations.length < 2 || !routeName.trim()}
                className="w-full bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-bold py-3.5 rounded-xl flex items-center justify-center gap-2.5 transition-all shadow-lg shadow-blue-600/20 disabled:opacity-50 disabled:cursor-not-allowed disabled:shadow-none text-sm"
              >
                {loading ? (
                  <Loader2 className="w-4 h-4 animate-spin" />
                ) : (
                  <Sparkles className="w-4 h-4" />
                )}
                {loading
                  ? 'Creating...'
                  : stations.length < 2
                    ? 'Place at least 2 stations'
                    : `Create Route with ${stations.length} Stops`}
              </button>
            </div>
          </div>

          {/* ── RIGHT: MAP ─────────────────────────────── */}
          <div className="flex-1 relative min-h-[400px] lg:min-h-0">
            {/* Mode indicator overlay */}
            <div className="absolute top-4 left-4 z-[1000] pointer-events-none">
              <div className={`flex items-center gap-2 px-3 py-2 rounded-xl text-xs font-bold shadow-lg border backdrop-blur-sm pointer-events-auto ${
                mode === 'add'
                  ? 'bg-blue-600/90 text-white border-blue-500/50'
                  : 'bg-white/90 text-slate-600 border-slate-200'
              }`}>
                {mode === 'add' ? (
                  <>
                    <MousePointer className="w-3.5 h-3.5" />
                    Click to place station
                  </>
                ) : (
                  <>
                    <Hand className="w-3.5 h-3.5" />
                    Drag map or markers
                  </>
                )}
              </div>
            </div>

            {/* Station count badge */}
            {stations.length > 0 && (
              <div className="absolute top-4 right-4 z-[1000] pointer-events-none">
                <div className="bg-white/90 backdrop-blur-sm px-3 py-2 rounded-xl shadow-lg border border-slate-200 text-xs font-bold text-slate-700 flex items-center gap-2">
                  <MapPin className="w-3.5 h-3.5 text-blue-500" />
                  {stations.length} station{stations.length !== 1 ? 's' : ''}
                </div>
              </div>
            )}

            <MapContainer
              center={[18.5204, 73.8567]}
              zoom={13}
              style={{ height: '100%', width: '100%' }}
              zoomControl={false}
              className={mode === 'add' ? 'route-builder-crosshair' : ''}
            >
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />

              <MapClickHandler mode={mode} onMapClick={handleMapClick} />

              {stations.length > 0 && <FitBounds stations={stations} />}

              {/* Road polyline */}
              {routePoints.length > 1 && (
                <>
                  {/* Shadow polyline for depth */}
                  <Polyline
                    positions={routePoints}
                    pathOptions={{ color: '#1e40af', weight: 8, opacity: 0.2 }}
                  />
                  {/* Main polyline */}
                  <Polyline
                    positions={routePoints}
                    pathOptions={{ color: '#3b82f6', weight: 5, opacity: 0.85 }}
                  />
                </>
              )}

              {/* Station markers */}
              {stations.map((station, i) => (
                <Marker
                  key={station.id}
                  position={[station.lat, station.lng]}
                  icon={makeStationIcon(getStationType(i), i + 1)}
                  draggable={true}
                  eventHandlers={{
                    dragend: (e) => {
                      const { lat, lng } = e.target.getLatLng();
                      handleDragEnd(station.id, { lat, lng });
                    },
                  }}
                >
                  <Popup>
                    <div className="text-center min-w-28 py-1">
                      <p className="font-bold text-slate-800 text-sm">{station.name}</p>
                      <p className="text-slate-500 text-[11px] mt-0.5">
                        {getStationType(i) === 'start' ? '🟢 Start Point' : getStationType(i) === 'end' ? '🔴 End Point' : `⚪ Stop #${i + 1}`}
                      </p>
                      <p className="text-slate-400 text-[10px] font-mono mt-1">{station.lat.toFixed(5)}, {station.lng.toFixed(5)}</p>
                    </div>
                  </Popup>
                </Marker>
              ))}
            </MapContainer>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminRouteBuilder;
