import { useState, useEffect, useRef } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { io } from 'socket.io-client';
import { getStopsByRoute, getActiveTrips, getRouteById } from '../services/api';
import MapView from '../components/MapView';
import Navbar from '../components/Navbar';
import { ArrowLeft, Bus, Clock, MapPin, RefreshCw, Route, ChevronRight, AlertCircle, CheckCircle2, Timer } from 'lucide-react';

const CitizenTrackPage = () => {
  const [searchParams] = useSearchParams();
  const routeId = searchParams.get('routeId');
  const sourceId = searchParams.get('source');
  const destId = searchParams.get('dest');

  const [stops, setStops] = useState([]);
  const [buses, setBuses] = useState([]);
  const [connected, setConnected] = useState(false);
  const [lastUpdate, setLastUpdate] = useState(null);
  const [loading, setLoading] = useState(true);
  const socketRef = useRef(null);

  const [selectedStop, setSelectedStop] = useState(null);

  useEffect(() => {
    if (!routeId) return;
    setLoading(true);
    Promise.all([getRouteById(routeId), getActiveTrips()])
      .then(([routeRes, tripsRes]) => {
        const routeData = routeRes.data;
        const fetchedStops = routeData.stops || [];
        
        // If stops exist in DB, use them directly — first stop = start, last stop = end.
        // Only inject route-level start/end as fallback when no stops exist (legacy routes).
        let processedStops;
        if (fetchedStops.length > 0) {
          processedStops = fetchedStops;
        } else {
          const fallback = [];
          if (routeData.start_point_name && routeData.start_latitude) {
            fallback.push({
              id: 'start',
              stop_name: routeData.start_point_name,
              latitude: routeData.start_latitude,
              longitude: routeData.start_longitude
            });
          }
          if (routeData.end_point_name && routeData.end_latitude) {
            fallback.push({
              id: 'end',
              stop_name: routeData.end_point_name,
              latitude: routeData.end_latitude,
              longitude: routeData.end_longitude
            });
          }
          processedStops = fallback;
        }
        
        // Fetch road distances between sequential stops using OSRM
        if (processedStops.length >= 2) {
          const coords = processedStops.map(s => `${s.longitude},${s.latitude}`).join(';');
          fetch(`https://router.project-osrm.org/route/v1/driving/${coords}?overview=false`)
            .then(res => res.json())
            .then(data => {
              if (data.code === 'Ok' && data.routes?.[0]) {
                const legs = data.routes[0].legs;
                const stopsWithDistance = processedStops.map((stop, i) => ({
                  ...stop,
                  // legs[i-1] is the distance from stop i-1 to stop i
                  roadDistFromPrev: i === 0 ? 0 : (legs[i - 1]?.distance / 1000) || 0
                }));
                setStops(stopsWithDistance);
              } else {
                setStops(processedStops);
              }
            })
            .catch(err => {
              console.error('OSRM Distance Error:', err);
              setStops(processedStops);
            });
        } else {
          setStops(processedStops);
        }

        setBuses(tripsRes.data.filter(t => t.route_id === parseInt(routeId)));
      })
      .catch(console.error)
      .finally(() => setLoading(false));

    const socket = io({ transports: ['websocket'] });
    socketRef.current = socket;
    socket.on('connect', () => { setConnected(true); socket.emit('join_route', routeId); });
    socket.on('disconnect', () => setConnected(false));
    socket.on('bus_position', (data) => {
      setLastUpdate(new Date());
      setBuses(prev => prev.map(bus => bus.id === data.trip_id ? { ...bus, current_lat: data.latitude, current_lng: data.longitude, speed: data.speed, etas: data.etas } : bus));
    });

    const poll = setInterval(() => {
      getActiveTrips().then(res => {
        // Merge polling results with existing etas from socket updates.
        // getActiveTrips() does not include etas, so we must preserve them
        // from the previous bus state to avoid wiping live ETA data.
        setBuses(prev => {
          const filtered = res.data.filter(t => t.route_id === parseInt(routeId));
          return filtered.map(newBus => {
            const existing = prev.find(b => b.id === newBus.id);
            return existing?.etas ? { ...newBus, etas: existing.etas } : newBus;
          });
        });
        setLastUpdate(new Date());
      }).catch(console.error);
    }, 10000);

    return () => { socket.disconnect(); clearInterval(poll); };
  }, [routeId]);

  const targetStop = selectedStop || stops.find(s => s.id === parseInt(destId)) || stops[stops.length - 1];
  const activeBus = buses.find(b => b.current_lat);

  const closestStopIndex = (() => {
    if (!activeBus?.current_lat || stops.length === 0) return 0;
    // We can rely on etas to find closest stop: the first stop with non-null eta is the closest/current
    if (activeBus.etas) {
        const firstUpcomingIdx = stops.findIndex(s => activeBus.etas[String(s.id)] !== null && activeBus.etas[String(s.id)] !== undefined);
        return firstUpcomingIdx !== -1 ? firstUpcomingIdx : stops.length - 1;
    }
    return 0;
  })();

  const calculateETA = (bus, targetStop) => {
    if (!bus || !targetStop || !bus.etas) return null;
    
    const timeMin = bus.etas[String(targetStop.id)];
    if (timeMin === null) return "Passed";
    if (timeMin === undefined) return null;
    
    return `~${timeMin} min`;
  };

  const eta = calculateETA(activeBus, targetStop);

  if (!routeId) {
    return (
      <div className="min-h-screen bg-slate-50 flex flex-col">
        <Navbar />
        <div className="flex-1 flex flex-col items-center justify-center gap-4 px-6">
          <div className="w-20 h-20 bg-slate-100 rounded-2xl flex items-center justify-center">
            <Route className="w-10 h-10 text-slate-400" />
          </div>
          <h2 className="text-xl font-bold text-slate-700">No Route Selected</h2>
          <p className="text-slate-500 text-sm text-center">Please select a source & destination to track a bus.</p>
          <Link to="/citizen" className="bg-blue-600 hover:bg-blue-700 text-white font-semibold px-6 py-3 rounded-xl flex items-center gap-2 transition-all shadow-lg shadow-blue-600/20">
            <ArrowLeft className="w-4 h-4" />Go Back
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      <Navbar />

      {/* Page header */}
      <div className="bg-white border-b border-slate-100 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 lg:px-8 py-4 flex items-center gap-4">
          <Link to="/citizen" className="w-9 h-9 rounded-xl bg-slate-100 hover:bg-slate-200 flex items-center justify-center transition-colors flex-shrink-0">
            <ArrowLeft className="w-4 h-4 text-slate-600" />
          </Link>
          <div className="flex-1">
            <h1 className="font-bold text-slate-800 text-lg">Live Bus Tracking</h1>
            <div className="flex items-center gap-2 flex-wrap">
              <div className={`flex items-center gap-1.5 ${connected ? 'text-emerald-600' : 'text-slate-400'}`}>
                <span className={`w-2 h-2 rounded-full ${connected ? 'bg-emerald-400 animate-pulse' : 'bg-slate-300'}`} />
                <span className="text-xs font-medium">{connected ? 'Live' : 'Connecting...'}</span>
              </div>
              {lastUpdate && <span className="text-slate-400 text-xs">· Updated {lastUpdate.toLocaleTimeString()}</span>}
            </div>
          </div>
          <button onClick={() => window.location.reload()} className="flex items-center gap-2 text-slate-500 hover:text-blue-600 text-sm font-medium transition-colors px-3 py-2 rounded-lg hover:bg-blue-50">
            <RefreshCw className="w-4 h-4" />Refresh
          </button>
        </div>
      </div>

      {/* MAIN LAYOUT */}
      <div className="flex-1 max-w-7xl mx-auto w-full px-4 lg:px-8 py-6">
        <div className="flex flex-col lg:flex-row gap-8 h-full">

          {/* LEFT SIDEBAR: INFO PANELS */}
          <div className="lg:w-[350px] xl:w-[400px] flex-shrink-0 space-y-6 order-2 lg:order-1">
            
            {/* ETA Card / Route Status */}
            {activeBus && eta ? (
              <div className="relative overflow-hidden bg-gradient-to-br from-blue-600 via-blue-700 to-indigo-800 rounded-3xl p-6 text-white shadow-xl shadow-blue-600/30">
                {/* decorative circle */}
                <div className="absolute -top-6 -right-6 w-32 h-32 bg-white/5 rounded-full" />
                <div className="absolute -bottom-8 -right-2 w-24 h-24 bg-white/5 rounded-full" />
                <p className="text-white/60 text-[11px] uppercase tracking-[0.15em] font-bold mb-3 relative">
                  Arrives at destination
                </p>
                <div className="flex items-end justify-between relative">
                  <div>
                    <p className="text-5xl font-black tracking-tight leading-none">{eta}</p>
                    <div className="flex items-center gap-2 mt-2">
                      <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
                      <p className="text-blue-200 text-sm font-medium">{activeBus.bus_number} · {activeBus.speed || 0} km/h</p>
                    </div>
                  </div>
                  <div className="w-14 h-14 bg-white/10 rounded-2xl flex items-center justify-center border border-white/15 backdrop-blur-sm">
                    <Timer className="w-7 h-7 text-white" />
                  </div>
                </div>
              </div>
            ) : buses.length > 0 ? (
              <div className="bg-white rounded-3xl border border-slate-100 p-6 flex items-center gap-4 shadow-sm">
                <div className="w-14 h-14 bg-blue-50 rounded-2xl flex items-center justify-center flex-shrink-0">
                  <span className="animate-pulse text-xl">🚌</span>
                </div>
                <div>
                  <p className="font-bold text-slate-800 text-lg">Trip In Progress</p>
                  <p className="text-slate-500 text-sm mt-0.5">Waiting for GPS signal...</p>
                </div>
              </div>
            ) : (
              <div className="bg-white rounded-3xl border border-slate-100 p-6 flex items-center gap-4 shadow-sm">
                <div className="w-14 h-14 bg-amber-50 rounded-2xl flex items-center justify-center flex-shrink-0">
                  <Bus className="w-6 h-6 text-amber-500" />
                </div>
                <div>
                  <p className="font-bold text-slate-800 text-lg">No Active Bus</p>
                  <p className="text-slate-500 text-sm mt-0.5">No buses running currently</p>
                </div>
              </div>
            )}

            {/* Active buses list */}
            {buses.length > 0 && (
              <div className="bg-white rounded-3xl border border-slate-100 overflow-hidden shadow-sm">
                <div className="px-6 py-5 border-b border-slate-50 flex items-center justify-between bg-slate-50/50">
                  <h3 className="font-bold text-slate-800">Active Buses</h3>
                  <span className="bg-emerald-100 text-emerald-700 text-xs font-bold px-3 py-1 rounded-full">{buses.length} running</span>
                </div>
                <div className="divide-y divide-slate-50">
                  {buses.map(bus => (
                    <div key={bus.id} className="px-6 py-5 flex items-center gap-4">
                      <div className="w-10 h-10 bg-blue-50 rounded-2xl flex items-center justify-center text-lg flex-shrink-0">🚌</div>
                      <div className="flex-1 min-w-0">
                        <p className="font-bold text-slate-800">{bus.bus_number}</p>
                        <p className="text-slate-500 text-xs truncate mt-0.5">{bus.driver_name}</p>
                      </div>
                      <div className="text-right flex-shrink-0">
                        {bus.current_lat ? (
                          <>
                            <p className="text-blue-600 font-bold mb-1">{bus.speed || 0} km/h</p>
                            <span className="bg-emerald-100 text-emerald-700 text-[10px] font-bold px-2 py-0.5 rounded uppercase tracking-wider">Live</span>
                          </>
                        ) : (
                          <span className="bg-amber-100 text-amber-700 text-[10px] font-bold px-2 py-0.5 rounded uppercase tracking-wider">No GPS</span>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* RIGHT SIDE: TIMELINE & MAP */}
          <div className="flex-1 order-1 lg:order-2 flex flex-col gap-8 w-full">

            {/* Trip Progress Timeline */}
            <div className="bg-white rounded-3xl border border-slate-100 p-6 lg:p-8 shadow-sm">
              <div className="flex items-center justify-between mb-8">
                <h2 className="text-xl font-bold text-slate-800 tracking-tight">Trip Progress</h2>
                {activeBus && (
                  <span className="flex items-center gap-1.5 text-xs font-semibold text-emerald-600 bg-emerald-50 px-3 py-1.5 rounded-full">
                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                    Live
                  </span>
                )}
              </div>

              <div className="relative">
                {stops.map((stop, index) => {
                  const isCurrent = activeBus && index === closestStopIndex;
                  const isPassed  = activeBus && index < closestStopIndex;
                  const isUpcoming = !activeBus || index > closestStopIndex;

                  let etaMins = 0;
                  if (activeBus && activeBus.etas) {
                    const tMin = activeBus.etas[String(stop.id)];
                    etaMins = tMin !== null && tMin !== undefined ? tMin : 0;
                  }

                  // Delay threshold: anything > 5 min is "delayed" for visual purposes
                  const isDelayed = isUpcoming && activeBus && etaMins > 5;

                  return (
                    <div key={stop.id} className="relative flex gap-5 pb-0 group cursor-pointer" onClick={() => setSelectedStop(stop)}>

                      {/* Left column: dot + line */}
                      <div className="flex flex-col items-center flex-shrink-0 w-8">
                        {/* Dot / bus icon */}
                        <div className="relative z-10 flex-shrink-0">
                          {isCurrent ? (
                            <div className="relative w-8 h-8 flex items-center justify-center">
                              <span className="absolute inset-0 rounded-full bg-blue-400/30 animate-ping" />
                              <div className="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center shadow-lg shadow-blue-400/40">
                                <Bus className="w-4 h-4 text-white" />
                              </div>
                            </div>
                          ) : isPassed ? (
                            <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center">
                              <CheckCircle2 className="w-4.5 h-4.5 text-blue-500" style={{width:'18px',height:'18px'}} />
                            </div>
                          ) : (
                            <div className="w-8 h-8 rounded-full border-2 border-slate-200 bg-white flex items-center justify-center">
                              <div className="w-2.5 h-2.5 rounded-full bg-slate-300" />
                            </div>
                          )}
                        </div>

                        {/* Connector line */}
                        {index !== stops.length - 1 && (
                          <div className={`w-0.5 flex-1 min-h-[40px] mt-1 mb-1 rounded-full ${
                            isPassed ? 'bg-blue-300' : isCurrent ? 'bg-gradient-to-b from-blue-300 to-slate-200' : isDelayed ? 'bg-red-200' : 'bg-slate-200'
                          }`} />
                        )}
                      </div>

                      {/* Right column: content */}
                      <div className={`flex-1 pb-6 ${index === stops.length - 1 ? 'pb-0' : ''}`}>
                        {isCurrent ? (
                          /* ── CURRENT STOP ── */
                          <div className="bg-gradient-to-r from-blue-600 to-indigo-600 rounded-2xl p-4 shadow-lg shadow-blue-500/25 mb-1">
                            <p className="text-blue-200 text-[10px] font-bold uppercase tracking-[0.12em] mb-1">🚌 Bus is here</p>
                            <div className="flex items-center justify-between gap-3">
                              <p className="text-white font-bold text-lg leading-tight">{stop.stop_name}</p>
                              <span className="bg-white/20 backdrop-blur-sm text-white text-xs font-bold px-3 py-1.5 rounded-xl whitespace-nowrap border border-white/20">
                                {etaMins <= 1 ? 'Arriving' : `~${etaMins} min`}
                              </span>
                            </div>
                          </div>
                        ) : (
                          /* ── PASSED / UPCOMING STOP ── */
                          <div className={`flex items-center justify-between gap-3 py-1 rounded-xl transition-colors group-hover:bg-slate-50 px-2 -mx-2 ${isPassed ? 'opacity-60' : ''}`}>
                            <div className="min-w-0">
                              <p className={`font-semibold text-[15px] leading-snug ${isPassed ? 'text-slate-400 line-through decoration-slate-300' : 'text-slate-800'}`}>
                                {stop.stop_name}
                              </p>
                              {isDelayed && (
                                <div className="flex items-center gap-1 mt-0.5">
                                  <AlertCircle className="w-3 h-3 text-red-500" />
                                  <span className="text-[11px] font-semibold text-red-500">Running late</span>
                                </div>
                              )}
                            </div>

                            {/* Right badge */}
                            {isPassed ? (
                              <span className="text-[11px] font-semibold text-slate-400 bg-slate-100 px-2.5 py-1 rounded-lg whitespace-nowrap flex-shrink-0">
                                Passed
                              </span>
                            ) : isUpcoming && activeBus && etaMins > 0 ? (
                              <span className={`text-[12px] font-bold px-3 py-1.5 rounded-xl whitespace-nowrap flex-shrink-0 flex items-center gap-1 ${
                                isDelayed
                                  ? 'text-red-600 bg-red-50 border border-red-100'
                                  : 'text-blue-600 bg-blue-50 border border-blue-100'
                              }`}>
                                {isDelayed && <span className="text-red-500 font-black">+</span>}
                                {etaMins} min
                                {isDelayed && <span className="text-[10px] font-semibold text-red-400 ml-0.5">delayed</span>}
                              </span>
                            ) : isUpcoming && activeBus ? (
                              <span className="text-[11px] font-medium text-slate-400 whitespace-nowrap flex-shrink-0">
                                Upcoming
                              </span>
                            ) : null}
                          </div>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
            
            {/* BELOW: MAP VIEW */}
            <div className="bg-white rounded-3xl border border-slate-100 shadow-sm overflow-hidden flex-1 flex flex-col min-h-[500px] relative z-0">
              {loading ? (
                <div className="h-full flex items-center justify-center" style={{ minHeight: '500px' }}>
                  <div className="flex flex-col items-center gap-3">
                    <div className="w-10 h-10 border-4 border-blue-600 border-t-transparent rounded-full animate-spin" />
                    <p className="text-slate-500 text-sm font-medium">Loading map...</p>
                  </div>
                </div>
              ) : (
                <div style={{ flex: 1, position: 'relative' }}>
                  <div style={{ position: 'absolute', top: 0, left: 0, right: 0, bottom: 0 }}>
                    <MapView stops={stops} buses={buses} sourceId={sourceId} destId={destId} selectedStopId={targetStop?.id} onStopSelect={setSelectedStop} height="100%" />
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CitizenTrackPage;
