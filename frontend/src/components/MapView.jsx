import { useEffect, useRef, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Fix leaflet default icons
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

// Custom bus icon
const createBusIcon = () => L.divIcon({
  className: 'moving-bus-marker',
  html: `
    <div style="
      width: 44px; height: 44px;
      background: linear-gradient(135deg, #2563EB, #1d4ed8);
      border-radius: 50% 50% 50% 0;
      transform: rotate(-45deg);
      box-shadow: 0 4px 15px rgba(37,99,235,0.4);
      border: 3px solid white;
      display: flex; align-items: center; justify-content: center;
    ">
      <div style="transform: rotate(45deg); font-size: 18px; margin-top: 2px;">🚌</div>
    </div>
  `,
  iconSize: [44, 44],
  iconAnchor: [22, 44],
  popupAnchor: [0, -44],
});

// Stop icon
const createStopIcon = (isSource, isDest, isSelected) => L.divIcon({
  className: '',
  html: `
    <div style="
      width: ${isSelected ? '20px' : '14px'}; height: ${isSelected ? '20px' : '14px'};
      background: ${isSelected ? '#2563EB' : isSource ? '#10b981' : isDest ? '#ef4444' : '#cbd5e1'};
      border-radius: 50%;
      border: ${isSelected ? '4px' : '3px'} solid white;
      box-shadow: 0 2px 8px rgba(0,0,0,0.25);
      transition: all 0.2s ease-out;
    "></div>
  `,
  iconSize: [isSelected ? 20 : 14, isSelected ? 20 : 14],
  iconAnchor: [isSelected ? 10 : 7, isSelected ? 10 : 7],
});

// Fly to marker when position changes
const FlyToMarker = ({ position }) => {
  const map = useMap();
  const prevPosRef = useRef(null);

  useEffect(() => {
    if (position) {
      const [lat, lng] = position;
      const prev = prevPosRef.current;
      // Only fly if position actually changed significantly
      if (!prev || Math.abs(prev[0] - lat) > 0.0001 || Math.abs(prev[1] - lng) > 0.0001) {
        map.flyTo(position, 15, { duration: 1.5 });
        prevPosRef.current = position;
      }
    }
  }, [position, map]);
  return null;
};

const MapView = ({ stops = [], buses = [], sourceId, destId, selectedStopId, onStopSelect, height = '400px' }) => {
  const [routePoints, setRoutePoints] = useState([]);
  const [activeRoutePoints, setActiveRoutePoints] = useState([]);

  const activeBus = buses.find(b => b.current_lat);

  // Fetch full route geometry from OSRM
  useEffect(() => {
    if (stops.length < 2) {
      setRoutePoints(stops.map(s => [parseFloat(s.latitude), parseFloat(s.longitude)]));
      return;
    }

    const coords = stops.map(s => `${s.longitude},${s.latitude}`).join(';');
    fetch(`https://router.project-osrm.org/route/v1/driving/${coords}?overview=full&geometries=geojson`)
      .then(res => res.json())
      .then(data => {
        if (data.code === 'Ok' && data.routes?.[0]) {
          const points = data.routes[0].geometry.coordinates.map(coord => [coord[1], coord[0]]);
          setRoutePoints(points);
        }
      })
      .catch(err => {
        console.error('OSRM Route Error:', err);
        // Fallback to straight lines
        setRoutePoints(stops.map(s => [parseFloat(s.latitude), parseFloat(s.longitude)]));
      });
  }, [stops]);

  // Fetch active segment (source to dest) from OSRM
  useEffect(() => {
    if (!sourceId || !destId || stops.length < 2) {
      setActiveRoutePoints([]);
      return;
    }

    const src = stops.find(s => s.id === parseInt(sourceId));
    const dest = stops.find(s => s.id === parseInt(destId));

    if (!src || !dest) return;

    // We only want the road path between these specific stops on the defined route
    const segmentStops = [];
    const srcIdx = stops.findIndex(s => s.id === parseInt(sourceId));
    const destIdx = stops.findIndex(s => s.id === parseInt(destId));
    
    if (srcIdx === -1 || destIdx === -1) return;
    
    const start = Math.min(srcIdx, destIdx);
    const end = Math.max(srcIdx, destIdx);
    const coords = stops.slice(start, end + 1).map(s => `${s.longitude},${s.latitude}`).join(';');

    fetch(`https://router.project-osrm.org/route/v1/driving/${coords}?overview=full&geometries=geojson`)
      .then(res => res.json())
      .then(data => {
        if (data.code === 'Ok' && data.routes?.[0]) {
          const points = data.routes[0].geometry.coordinates.map(coord => [coord[1], coord[0]]);
          setActiveRoutePoints(points);
        }
      })
      .catch(console.error);
  }, [sourceId, destId, stops]);

  const defaultCenter = stops.length > 0
    ? [parseFloat(stops[0].latitude), parseFloat(stops[0].longitude)]
    : activeBus
      ? [parseFloat(activeBus.current_lat), parseFloat(activeBus.current_lng)]
      : [18.5204, 73.8567]; // Pune default

  return (
    <div style={{ height, width: '100%', borderRadius: '16px', overflow: 'hidden' }}>
      <MapContainer
        center={defaultCenter}
        zoom={13}
        style={{ height: '100%', width: '100%' }}
        zoomControl={false}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {/* Full Route polyline (Road following) */}
        {routePoints.length > 1 && (
          <Polyline
            positions={routePoints}
            pathOptions={{ color: '#3b82f6', weight: 6, opacity: 0.6 }}
          />
        )}

        {/* Active Segment polyline (bold/thick) */}
        {activeRoutePoints.length > 1 && (
          <Polyline
            positions={activeRoutePoints}
            pathOptions={{ color: '#0051ffff', weight: 9, opacity: 1 }}
          />
        )}

        {/* Stops */}
        {stops.map((stop, i) => (
          <Marker
            key={stop.id}
            position={[parseFloat(stop.latitude), parseFloat(stop.longitude)]}
            icon={createStopIcon(stop.id === parseInt(sourceId), stop.id === parseInt(destId), stop.id === selectedStopId)}
            eventHandlers={{
              click: () => onStopSelect && onStopSelect(stop)
            }}
          >
            <Popup>
              <div className="text-center min-w-24">
                <p className="font-bold text-slate-800 text-sm">{stop.stop_name}</p>
                <p className="text-slate-500 text-xs mt-0.5">Stop #{i + 1}</p>
                {stop.id === selectedStopId && <p className="text-blue-600 text-[10px] uppercase font-bold mt-1 bg-blue-50 py-0.5 rounded">Selected</p>}
              </div>
            </Popup>
          </Marker>
        ))}

        {/* Active buses */}
        {buses.map(bus => bus.current_lat && (
          <Marker
            key={bus.id}
            position={[parseFloat(bus.current_lat), parseFloat(bus.current_lng)]}
            icon={createBusIcon()}
          >
            <Popup>
              <div className="min-w-32">
                <p className="font-bold text-slate-800 text-sm">🚌 {bus.bus_number}</p>
                <p className="text-slate-600 text-xs">{bus.route_name}</p>
                <p className="text-slate-500 text-xs">Driver: {bus.driver_name}</p>
                {bus.speed && <p className="text-primary-600 text-xs font-medium">{bus.speed} km/h</p>}
              </div>
            </Popup>
          </Marker>
        ))}

        {activeBus && (
          <FlyToMarker position={[parseFloat(activeBus.current_lat), parseFloat(activeBus.current_lng)]} />
        )}
      </MapContainer>
    </div>
  );
};

export default MapView;
