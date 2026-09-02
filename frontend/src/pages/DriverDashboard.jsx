import { useState, useEffect, useRef } from 'react';
import { io } from 'socket.io-client';
import { useAuth } from '../context/AuthContext';
import { getBuses, getRoutes, startTrip, endTrip, getMyActiveTrip, updateLocation } from '../services/api';
import Navbar from '../components/Navbar';
import MapView from '../components/MapView';
import { Bus, Play, Square, Navigation, Wifi, WifiOff, AlertCircle, Clock, Route, Map, Activity, RefreshCw } from 'lucide-react';

const DriverDashboard = () => {
  const { user } = useAuth();
  const [buses, setBuses] = useState([]);
  const [routes, setRoutes] = useState([]);
  const [selectedBus, setSelectedBus] = useState('');
  const [selectedRoute, setSelectedRoute] = useState('');
  const [activeTrip, setActiveTrip] = useState(null);
  const [gpsActive, setGpsActive] = useState(false);
  const [currentPos, setCurrentPos] = useState(null);
  const [gpsError, setGpsError] = useState('');
  const [loading, setLoading] = useState(false);
  const [startError, setStartError] = useState('');
  const [connected, setConnected] = useState(false);

  const socketRef = useRef(null);
  const watchIdRef = useRef(null);
  const intervalRef = useRef(null);
  const posRef = useRef(null);
  const tripRef = useRef(null);

  useEffect(() => {
    Promise.all([getBuses(), getRoutes(), getMyActiveTrip()])
      .then(([b, r, t]) => {
        setBuses(b.data.filter(bus => bus.status !== 'maintenance'));
        setRoutes(r.data);
        if (t.data) {
          setActiveTrip(t.data);
          tripRef.current = t.data;
          startGPS(t.data);
        }
      }).catch(console.error);

    const socket = io({ transports: ['websocket'] });
    socketRef.current = socket;
    socket.on('connect', () => setConnected(true));
    socket.on('disconnect', () => setConnected(false));
    return () => { socket.disconnect(); stopGPS(); };
  }, []);

  const startGPS = (trip) => {
    if (!navigator.geolocation) { setGpsError('Geolocation not supported'); return; }
    setGpsActive(true); setGpsError('');

    // Immediate fix
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const { latitude, longitude, speed } = pos.coords;
        const spd = speed ? parseFloat((speed * 3.6).toFixed(1)) : 0;
        const newPos = { lat: latitude, lng: longitude, speed: spd };
        setCurrentPos(newPos);
        posRef.current = newPos;
        if (socketRef.current?.connected) {
          socketRef.current.emit('driver_location', { trip_id: trip.id, latitude, longitude, speed: spd, route_id: trip.route_id });
        }
      },
      (err) => { console.warn('Initial GPS fix failed:', err.message); },
      { enableHighAccuracy: true, timeout: 5000, maximumAge: 0 }
    );

    watchIdRef.current = navigator.geolocation.watchPosition(
      (pos) => {
        const { latitude, longitude, speed } = pos.coords;
        const spd = speed ? parseFloat((speed * 3.6).toFixed(1)) : 0;
        const newPos = { lat: latitude, lng: longitude, speed: spd };
        setCurrentPos(newPos);
        posRef.current = newPos;
        if (socketRef.current?.connected) {
          socketRef.current.emit('driver_location', { trip_id: trip.id, latitude, longitude, speed: spd, route_id: trip.route_id });
        }
      },
      (err) => { setGpsError(`GPS Error: ${err.message}`); setGpsActive(false); },
      { enableHighAccuracy: true, maximumAge: 0, timeout: 10000 }
    );
    intervalRef.current = setInterval(() => {
      if (posRef.current && tripRef.current) {
        updateLocation({
          trip_id: tripRef.current.id,
          latitude: posRef.current.lat,
          longitude: posRef.current.lng,
          speed: posRef.current.speed
        }).catch(console.error);
      }
    }, 15000);
  };

  const stopGPS = () => {
    if (watchIdRef.current) navigator.geolocation.clearWatch(watchIdRef.current);
    if (intervalRef.current) clearInterval(intervalRef.current);
    setGpsActive(false); setCurrentPos(null);
  };

  const handleStartTrip = async () => {
    if (!selectedBus || !selectedRoute) { setStartError('Please select both bus and route'); return; }
    setLoading(true); setStartError('');
    try {
      const res = await startTrip({ bus_id: selectedBus, route_id: selectedRoute });
      setActiveTrip(res.data);
      tripRef.current = res.data;
      startGPS(res.data);
    } catch (err) { setStartError(err.response?.data?.message || 'Failed to start trip'); }
    finally { setLoading(false); }
  };

  const handleEndTrip = async () => {
    if (!activeTrip) return;
    setLoading(true);
    try { await endTrip(activeTrip.id); stopGPS(); setActiveTrip(null); }
    catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  return (
    <div style={{ minHeight: '100vh', background: '#f8fafc', display: 'flex', flexDirection: 'column' }}>
      <Navbar />

      {/* Header */}
      <div style={{
        background: 'rgba(255,255,255,0.85)',
        backdropFilter: 'blur(12px)',
        borderBottom: '1px solid #e2e8f0',
        position: 'sticky',
        top: '64px',
        zIndex: 30,
      }}>
        <div style={{
          maxWidth: '1280px',
          margin: '0 auto',
          padding: 'clamp(12px, 3vw, 20px) clamp(16px, 4vw, 24px)',
          display: 'flex',
          flexWrap: 'wrap',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: '12px',
        }}>
          <div>
            <h1 style={{ fontSize: '1.5rem', fontWeight: 900, color: '#0f172a', letterSpacing: '-0.02em', margin: 0 }}>
              Driver Dashboard
            </h1>
            <p style={{ color: '#64748b', fontSize: '0.875rem', marginTop: '2px' }}>
              Welcome back, <span style={{ fontWeight: 600, color: '#2563eb' }}>{user?.name}</span>
            </p>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
            {activeTrip && (
              <div style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                background: '#ecfdf5',
                border: '1px solid #d1fae5',
                padding: '8px 16px',
                borderRadius: '16px',
              }}>
                <span style={{
                  width: '10px',
                  height: '10px',
                  background: '#10b981',
                  borderRadius: '50%',
                  display: 'inline-block',
                  animation: 'pulse 2s infinite',
                }} />
                <span style={{ color: '#065f46', fontSize: '0.8rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.08em' }}>
                  Trip Active
                </span>
              </div>
            )}
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '8px 16px',
              borderRadius: '16px',
              fontSize: '0.8rem',
              fontWeight: 700,
              border: '1px solid',
              borderColor: connected ? '#bfdbfe' : '#e2e8f0',
              background: connected ? '#eff6ff' : '#f1f5f9',
              color: connected ? '#1d4ed8' : '#94a3b8',
            }}>
              {connected ? <Wifi style={{ width: 16, height: 16 }} /> : <WifiOff style={{ width: 16, height: 16 }} />}
              <span>{connected ? 'Live Sync' : 'Offline'}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Main content */}
      <div style={{
        flex: 1,
        maxWidth: '1280px',
        margin: '0 auto',
        width: '100%',
        padding: 'clamp(16px, 4vw, 32px) clamp(16px, 4vw, 24px)',
      }}>
        <div className="driver-grid" style={{
          display: 'grid',
          gridTemplateColumns: '1fr',
          gap: '28px',
          alignItems: 'start',
        }}>

          {/* LEFT COL — Controls */}
          <div className="driver-left-col" style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>

            {/* Active Trip Card */}
            {activeTrip ? (
              <div style={{
                background: 'linear-gradient(135deg, #2563eb 0%, #1d4ed8 40%, #4338ca 100%)',
                borderRadius: '24px',
                padding: '28px',
                color: 'white',
                boxShadow: '0 20px 60px rgba(37, 99, 235, 0.3)',
                position: 'relative',
                overflow: 'hidden',
              }}>
                {/* Decorative glow */}
                <div style={{
                  position: 'absolute',
                  top: '-60px',
                  right: '-60px',
                  width: '150px',
                  height: '150px',
                  background: 'rgba(255,255,255,0.08)',
                  borderRadius: '50%',
                  filter: 'blur(40px)',
                }} />
                <div style={{
                  position: 'absolute',
                  bottom: '-60px',
                  left: '-60px',
                  width: '150px',
                  height: '150px',
                  background: 'rgba(45,212,191,0.08)',
                  borderRadius: '50%',
                  filter: 'blur(40px)',
                }} />

                <div style={{ position: 'relative', zIndex: 2 }}>
                  {/* Top row: badge + bus icon */}
                  <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: '24px' }}>
                    <div style={{ minWidth: 0 }}>
                      <span style={{
                        display: 'inline-block',
                        background: 'rgba(255,255,255,0.2)',
                        backdropFilter: 'blur(8px)',
                        color: 'white',
                        fontSize: '0.6rem',
                        fontWeight: 900,
                        textTransform: 'uppercase',
                        padding: '4px 10px',
                        borderRadius: '8px',
                        border: '1px solid rgba(255,255,255,0.2)',
                        letterSpacing: '0.12em',
                        marginBottom: '8px',
                      }}>
                        Ongoing Trip
                      </span>
                      <h3 style={{
                        fontSize: 'clamp(2rem, 6vw, 3rem)',
                        fontWeight: 900,
                        letterSpacing: '-0.04em',
                        margin: '4px 0 0 0',
                        lineHeight: 1,
                      }}>
                        {activeTrip.bus_number}
                      </h3>
                      <p style={{
                        color: 'rgba(191, 219, 254, 0.9)',
                        fontSize: '0.875rem',
                        fontWeight: 600,
                        marginTop: '6px',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '6px',
                        textTransform: 'uppercase',
                        letterSpacing: '0.05em',
                      }}>
                        <Route style={{ width: 14, height: 14 }} /> {activeTrip.route_name}
                      </p>
                    </div>
                    <div style={{
                      width: '64px',
                      height: '64px',
                      background: 'rgba(255,255,255,0.12)',
                      backdropFilter: 'blur(8px)',
                      borderRadius: '20px',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      border: '1px solid rgba(255,255,255,0.15)',
                      fontSize: '2rem',
                      flexShrink: 0,
                    }}>
                      🚌
                    </div>
                  </div>

                  {/* GPS Details Glass Card */}
                  <div style={{
                    background: 'rgba(255,255,255,0.1)',
                    backdropFilter: 'blur(16px)',
                    borderRadius: '16px',
                    padding: '16px',
                    marginBottom: '20px',
                    border: '1px solid rgba(255,255,255,0.1)',
                  }}>
                    {/* GPS Status + Speed */}
                    <div style={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      marginBottom: '14px',
                      paddingBottom: '14px',
                      borderBottom: '1px solid rgba(255,255,255,0.1)',
                    }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <div style={{
                          width: '10px',
                          height: '10px',
                          borderRadius: '50%',
                          background: gpsActive ? '#2dd4bf' : 'rgba(255,255,255,0.2)',
                          boxShadow: gpsActive ? '0 0 12px rgba(45,212,191,0.8)' : 'none',
                          animation: gpsActive ? 'pulse 2s infinite' : 'none',
                        }} />
                        <span style={{ color: 'white', fontWeight: 700, fontSize: '0.8rem', letterSpacing: '0.03em' }}>
                          Live GPS {gpsActive ? 'Active' : 'Standby'}
                        </span>
                      </div>
                      {currentPos && (
                        <div style={{ textAlign: 'right' }}>
                          <p style={{ fontSize: '1.5rem', fontWeight: 900, color: 'white', lineHeight: 1, margin: 0 }}>
                            {currentPos.speed}
                          </p>
                          <p style={{ fontSize: '0.6rem', color: 'rgba(255,255,255,0.5)', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '-0.02em', margin: 0 }}>
                            km per hour
                          </p>
                        </div>
                      )}
                    </div>

                    {/* Lat/Lng Grid */}
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                      <div>
                        <p style={{ fontSize: '0.6rem', color: 'rgba(255,255,255,0.4)', fontWeight: 900, textTransform: 'uppercase', letterSpacing: '0.1em', marginBottom: '4px' }}>
                          Latitude
                        </p>
                        <p style={{ fontSize: '0.8rem', fontFamily: 'monospace', fontWeight: 500, color: 'rgba(255,255,255,0.9)', margin: 0 }}>
                          {currentPos ? currentPos.lat.toFixed(5) : 'Calculating...'}
                        </p>
                      </div>
                      <div>
                        <p style={{ fontSize: '0.6rem', color: 'rgba(255,255,255,0.4)', fontWeight: 900, textTransform: 'uppercase', letterSpacing: '0.1em', marginBottom: '4px' }}>
                          Longitude
                        </p>
                        <p style={{ fontSize: '0.8rem', fontFamily: 'monospace', fontWeight: 500, color: 'rgba(255,255,255,0.9)', margin: 0 }}>
                          {currentPos ? currentPos.lng.toFixed(5) : 'Calculating...'}
                        </p>
                      </div>
                    </div>

                    {gpsError && (
                      <p style={{ color: '#fca5a5', fontSize: '0.75rem', marginTop: '12px', display: 'flex', alignItems: 'center', gap: '6px', fontWeight: 600 }}>
                        <AlertCircle style={{ width: 14, height: 14 }} /> {gpsError}
                      </p>
                    )}
                  </div>

                  {/* Time and status badges */}
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap', marginBottom: '24px' }}>
                    <div style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '6px',
                      background: 'rgba(255,255,255,0.06)',
                      padding: '8px 12px',
                      borderRadius: '10px',
                      border: '1px solid rgba(255,255,255,0.06)',
                      fontSize: '0.75rem',
                      fontWeight: 600,
                      color: 'rgba(255,255,255,0.6)',
                    }}>
                      <Clock style={{ width: 14, height: 14, color: '#93c5fd' }} />
                      Started {new Date(activeTrip.started_at).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </div>
                    <div style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '6px',
                      background: 'rgba(255,255,255,0.06)',
                      padding: '8px 12px',
                      borderRadius: '10px',
                      border: '1px solid rgba(255,255,255,0.06)',
                      fontSize: '0.75rem',
                      fontWeight: 600,
                      color: 'rgba(255,255,255,0.6)',
                    }}>
                      <Activity style={{ width: 14, height: 14, color: '#2dd4bf' }} />
                      Trip Active
                    </div>
                  </div>

                  {/* End Trip Button */}
                  <button
                    id="end-trip-btn"
                    onClick={handleEndTrip}
                    disabled={loading}
                    style={{
                      width: '100%',
                      background: 'white',
                      color: '#dc2626',
                      fontWeight: 900,
                      padding: '16px',
                      borderRadius: '16px',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      gap: '10px',
                      border: 'none',
                      cursor: loading ? 'not-allowed' : 'pointer',
                      boxShadow: '0 8px 30px rgba(0,0,0,0.15)',
                      fontSize: '0.8rem',
                      textTransform: 'uppercase',
                      letterSpacing: '0.12em',
                      transition: 'all 0.2s',
                      opacity: loading ? 0.7 : 1,
                    }}
                  >
                    {loading ? (
                      <div style={{
                        width: 20,
                        height: 20,
                        border: '2px solid #fecaca',
                        borderTopColor: '#dc2626',
                        borderRadius: '50%',
                        animation: 'spin 0.8s linear infinite',
                      }} />
                    ) : (
                      <>
                        <Square style={{ width: 18, height: 18, fill: '#dc2626' }} />
                        End Trip Now
                      </>
                    )}
                  </button>
                </div>
              </div>
            ) : (
              /* Start Trip Form */
              <div style={{
                background: 'white',
                borderRadius: '24px',
                border: '1px solid #e2e8f0',
                padding: '28px',
                boxShadow: '0 1px 3px rgba(0,0,0,0.06)',
              }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '14px', marginBottom: '28px' }}>
                  <div style={{
                    width: '48px',
                    height: '48px',
                    background: '#eff6ff',
                    borderRadius: '14px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    flexShrink: 0,
                  }}>
                    <Play style={{ width: 22, height: 22, color: '#2563eb' }} />
                  </div>
                  <div>
                    <h2 style={{ fontSize: '1.15rem', fontWeight: 900, color: '#0f172a', letterSpacing: '-0.02em', margin: 0 }}>
                      Start New Trip
                    </h2>
                    <p style={{ color: '#64748b', fontSize: '0.8rem', margin: 0 }}>Deploy a bus to an active route</p>
                  </div>
                </div>

                {startError && (
                  <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '10px',
                    background: '#fef2f2',
                    color: '#dc2626',
                    fontSize: '0.8rem',
                    padding: '14px 18px',
                    borderRadius: '14px',
                    marginBottom: '20px',
                    border: '1px solid #fecaca',
                  }}>
                    <AlertCircle style={{ width: 18, height: 18, flexShrink: 0 }} />
                    <span style={{ fontWeight: 600 }}>{startError}</span>
                  </div>
                )}

                <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                  <div>
                    <label style={{
                      display: 'block',
                      fontSize: '0.65rem',
                      fontWeight: 900,
                      color: '#94a3b8',
                      textTransform: 'uppercase',
                      letterSpacing: '0.15em',
                      marginBottom: '10px',
                    }}>
                      Bus Assignment
                    </label>
                    <div style={{ position: 'relative' }}>
                      <Bus style={{
                        position: 'absolute',
                        left: '14px',
                        top: '50%',
                        transform: 'translateY(-50%)',
                        width: 18,
                        height: 18,
                        color: selectedBus ? '#3b82f6' : '#94a3b8',
                        transition: 'color 0.2s',
                      }} />
                      <select
                        id="select-bus"
                        style={{
                          width: '100%',
                          paddingLeft: '44px',
                          paddingRight: '16px',
                          paddingTop: '14px',
                          paddingBottom: '14px',
                          border: '1px solid #e2e8f0',
                          borderRadius: '14px',
                          background: '#f8fafc',
                          color: '#1e293b',
                          fontSize: '0.85rem',
                          fontWeight: 600,
                          appearance: 'none',
                          outline: 'none',
                          fontFamily: 'Inter, sans-serif',
                        }}
                        value={selectedBus}
                        onChange={(e) => setSelectedBus(e.target.value)}
                      >
                        <option value="">Select bus numbering...</option>
                        {buses.map(bus => <option key={bus.id} value={bus.id}>{bus.bus_number} {bus.active_trip_id ? '— Busy' : '— Idle'}</option>)}
                      </select>
                    </div>
                  </div>

                  <div>
                    <label style={{
                      display: 'block',
                      fontSize: '0.65rem',
                      fontWeight: 900,
                      color: '#94a3b8',
                      textTransform: 'uppercase',
                      letterSpacing: '0.15em',
                      marginBottom: '10px',
                    }}>
                      Target Route
                    </label>
                    <div style={{ position: 'relative' }}>
                      <Route style={{
                        position: 'absolute',
                        left: '14px',
                        top: '50%',
                        transform: 'translateY(-50%)',
                        width: 18,
                        height: 18,
                        color: selectedRoute ? '#3b82f6' : '#94a3b8',
                        transition: 'color 0.2s',
                      }} />
                      <select
                        id="select-route"
                        style={{
                          width: '100%',
                          paddingLeft: '44px',
                          paddingRight: '16px',
                          paddingTop: '14px',
                          paddingBottom: '14px',
                          border: '1px solid #e2e8f0',
                          borderRadius: '14px',
                          background: '#f8fafc',
                          color: '#1e293b',
                          fontSize: '0.85rem',
                          fontWeight: 600,
                          appearance: 'none',
                          outline: 'none',
                          fontFamily: 'Inter, sans-serif',
                        }}
                        value={selectedRoute}
                        onChange={(e) => setSelectedRoute(e.target.value)}
                      >
                        <option value="">Select bus route...</option>
                        {routes.map(route => <option key={route.id} value={route.id}>{route.route_name} ({route.stop_count} stops)</option>)}
                      </select>
                    </div>
                  </div>

                  <button
                    id="start-trip-btn"
                    onClick={handleStartTrip}
                    disabled={loading}
                    style={{
                      width: '100%',
                      background: '#2563eb',
                      color: 'white',
                      fontWeight: 900,
                      padding: '16px',
                      borderRadius: '16px',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      gap: '10px',
                      border: 'none',
                      cursor: loading ? 'not-allowed' : 'pointer',
                      boxShadow: '0 8px 30px rgba(37, 99, 235, 0.25)',
                      fontSize: '0.8rem',
                      textTransform: 'uppercase',
                      letterSpacing: '0.12em',
                      transition: 'all 0.2s',
                      opacity: loading ? 0.6 : 1,
                    }}
                  >
                    {loading ? (
                      <div style={{
                        width: 22,
                        height: 22,
                        border: '3px solid rgba(255,255,255,0.3)',
                        borderTopColor: 'white',
                        borderRadius: '50%',
                        animation: 'spin 0.8s linear infinite',
                      }} />
                    ) : (
                      <>
                        <Play style={{ width: 18, height: 18 }} />
                        Dispatch Trip
                      </>
                    )}
                  </button>
                </div>
              </div>
            )}

            {/* Telemetry Status Card */}
            <div style={{
              background: 'white',
              borderRadius: '24px',
              border: '1px solid #e2e8f0',
              padding: '24px',
              boxShadow: '0 1px 3px rgba(0,0,0,0.06)',
            }}>
              <h3 style={{
                fontWeight: 900,
                color: '#1e293b',
                fontSize: '0.7rem',
                textTransform: 'uppercase',
                letterSpacing: '0.15em',
                marginBottom: '20px',
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
              }}>
                <Activity style={{ width: 16, height: 16, color: '#10b981' }} />
                Telemetry Status
              </h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                {[
                  { label: 'Network', value: connected ? 'Stable' : 'Offline', valueColor: connected ? '#059669' : '#ef4444' },
                  { label: 'GPS State', value: gpsActive ? 'Live' : 'Standby', valueColor: gpsActive ? '#2563eb' : '#94a3b8' },
                  { label: 'Speed', value: currentPos ? `${currentPos.speed} km/h` : '0 km/h', valueColor: '#0f172a' },
                ].map(({ label, value, valueColor }) => (
                  <div key={label} style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    paddingBottom: '12px',
                    borderBottom: '1px solid #f8fafc',
                  }}>
                    <span style={{ color: '#94a3b8', fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.06em' }}>
                      {label}
                    </span>
                    <span style={{ fontSize: '0.85rem', fontWeight: 900, color: valueColor }}>
                      {value}
                    </span>
                  </div>
                ))}
              </div>
              {!gpsActive && activeTrip && (
                <button
                  onClick={() => startGPS(activeTrip)}
                  style={{
                    width: '100%',
                    marginTop: '18px',
                    background: '#eff6ff',
                    color: '#2563eb',
                    fontSize: '0.7rem',
                    fontWeight: 900,
                    padding: '12px',
                    borderRadius: '12px',
                    border: 'none',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: '8px',
                    textTransform: 'uppercase',
                    letterSpacing: '0.1em',
                    transition: 'all 0.2s',
                  }}
                >
                  <RefreshCw style={{ width: 14, height: 14 }} /> Retry Connection
                </button>
              )}
            </div>
          </div>

          {/* RIGHT COL — Map */}
          <div className="driver-right-col" style={{ display: 'flex', flexDirection: 'column' }}>
            <div style={{
              background: 'white',
              borderRadius: '24px',
              border: '1px solid #e2e8f0',
              boxShadow: '0 4px 20px rgba(0,0,0,0.06)',
              overflow: 'hidden',
              display: 'flex',
              flexDirection: 'column',
              minHeight: '520px',
            }}>
              {/* Map Header */}
              <div style={{
                padding: '18px 24px',
                borderBottom: '1px solid #f1f5f9',
                display: 'flex',
                alignItems: 'center',
                gap: '12px',
                background: 'white',
                flexShrink: 0,
              }}>
                <div style={{
                  width: '36px',
                  height: '36px',
                  background: '#eff6ff',
                  borderRadius: '10px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  flexShrink: 0,
                }}>
                  <Map style={{ width: 18, height: 18, color: '#2563eb' }} />
                </div>
                <div style={{ minWidth: 0 }}>
                  <span style={{ fontWeight: 900, color: '#1e293b', fontSize: '0.9rem', letterSpacing: '-0.01em' }}>
                    Active Operation Map
                  </span>
                  <p style={{
                    fontSize: '0.6rem',
                    color: '#94a3b8',
                    fontWeight: 700,
                    textTransform: 'uppercase',
                    letterSpacing: '0.12em',
                    margin: 0,
                  }}>
                    {gpsActive ? 'Synchronized with Live Satellites' : 'Awaiting Connection'}
                  </p>
                </div>
                {gpsActive && (
                  <div style={{
                    marginLeft: 'auto',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '6px',
                    background: '#ecfdf5',
                    color: '#065f46',
                    padding: '6px 12px',
                    borderRadius: '8px',
                    border: '1px solid #d1fae5',
                  }}>
                    <span style={{
                      width: '8px',
                      height: '8px',
                      background: '#10b981',
                      borderRadius: '50%',
                      display: 'inline-block',
                      animation: 'pulse 1.5s infinite',
                    }} />
                    <span style={{ fontSize: '0.6rem', fontWeight: 900, textTransform: 'uppercase', letterSpacing: '0.1em' }}>
                      Live
                    </span>
                  </div>
                )}
              </div>

              {/* Map */}
              <div style={{ flex: 1, position: 'relative', minHeight: '400px' }}>
                <div style={{ position: 'absolute', top: 0, left: 0, right: 0, bottom: 0 }}>
                  <MapView
                    stops={[]}
                    buses={currentPos && activeTrip ? [{
                      id: activeTrip.id,
                      current_lat: currentPos.lat,
                      current_lng: currentPos.lng,
                      bus_number: activeTrip.bus_number,
                      speed: currentPos.speed,
                      driver_name: user?.name
                    }] : []}
                    height="100%"
                  />
                </div>

                {/* Floating Map Controls Overlay */}
                <div style={{ position: 'absolute', right: '20px', top: '20px', zIndex: 10, display: 'flex', flexDirection: 'column', gap: '8px' }}>
                  <button
                    onClick={() => activeTrip && startGPS(activeTrip)}
                    style={{
                      width: '44px',
                      height: '44px',
                      background: 'white',
                      borderRadius: '14px',
                      boxShadow: '0 4px 20px rgba(0,0,0,0.1)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      border: '1px solid #f1f5f9',
                      cursor: 'pointer',
                      color: '#475569',
                      transition: 'all 0.2s',
                    }}
                  >
                    <RefreshCw style={{ width: 18, height: 18 }} />
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Responsive CSS */}
      <style>{`
        @keyframes pulse {
          0%, 100% { opacity: 1; }
          50% { opacity: 0.5; }
        }
        @keyframes spin {
          to { transform: rotate(360deg); }
        }
        
        .driver-grid {
          grid-template-columns: 1fr !important;
        }

        @media (min-width: 1024px) {
          .driver-grid {
            grid-template-columns: 4fr 8fr !important;
          }
          .driver-right-col {
            position: sticky;
            top: 160px;
          }
        }

        @media (max-width: 480px) {
          .driver-grid {
            gap: 16px !important;
          }
        }

        select:focus {
          border-color: #3b82f6 !important;
          box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15) !important;
        }

        button:hover {
          filter: brightness(0.97);
        }

        button:active {
          transform: scale(0.98);
        }
      `}</style>
    </div>
  );
};

export default DriverDashboard;
