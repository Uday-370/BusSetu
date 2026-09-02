import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { MapPin, ArrowUpDown, Search, Clock, Bus, ChevronRight, Navigation, Route } from 'lucide-react';
import { getAllStops, getRoutes } from '../services/api';
import Navbar from '../components/Navbar';

const CitizenHomePage = () => {
  const [stops, setStops] = useState([]);
  const [routes, setRoutes] = useState([]);
  const [source, setSource] = useState('');
  const [destination, setDestination] = useState('');
  const [activeTab, setActiveTab] = useState('route');
  const [busNo, setBusNo] = useState('');
  const navigate = useNavigate();

  const recentSearches = JSON.parse(localStorage.getItem('recentSearches') || '[]');

  useEffect(() => {
    Promise.all([getAllStops(), getRoutes()])
      .then(([s, r]) => { setStops(s.data); setRoutes(r.data); })
      .catch(console.error);
  }, []); 

  const swapStops = () => { const t = source; setSource(destination); setDestination(t); };

  const handleSearch = () => {
    if (activeTab === 'route' && (!source || !destination)) return;
    if (activeTab === 'bus' && !busNo) return;
    if (activeTab === 'route') {
      const sourceStop = stops.find(s => s.id === parseInt(source));
      const destStop = stops.find(s => s.id === parseInt(destination));
      if (!sourceStop || !destStop) return;
      const search = { type: 'route', source: sourceStop.stop_name, dest: destStop.stop_name, routeId: sourceStop.route_id };
      const updated = [search, ...recentSearches.filter(s => s.source !== search.source || s.dest !== search.dest)].slice(0, 5);
      localStorage.setItem('recentSearches', JSON.stringify(updated));
      navigate(`/citizen/track?source=${source}&dest=${destination}&routeId=${sourceStop.route_id}`);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      <Navbar />

      {/* HERO SEARCH BANNER */}
      <div className="bg-gradient-to-br from-blue-700 via-blue-600 to-teal-600">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 md:py-12">
          <h1 className="text-white text-2xl md:text-3xl font-black mb-2 tracking-tight">Find Your Bus 🚌</h1>
          <p className="text-white/80 text-sm md:text-base mb-6 md:mb-8 text-semibold">Track live buses across all KMT routes</p>

          {/* Tab + Search card */}
          <div style={{ background: 'rgba(255,255,255,0.12)', backdropFilter: 'blur(12px)', border: '1px solid rgba(255,255,255,0.2)', borderRadius: '20px', padding: '20px 24px' }}>
            {/* Tab switcher */}
            <div style={{ display: 'flex', gap: '4px', marginBottom: '18px', padding: '4px', background: 'rgba(0,0,0,0.15)', borderRadius: '14px', width: 'fit-content' }}>
              {['route', 'bus'].map((tab) => (
                <button key={tab} onClick={() => setActiveTab(tab)}
                  style={{ padding: '8px 22px', borderRadius: '11px', fontSize: '13px', fontWeight: 700, border: 'none', cursor: 'pointer', transition: 'all 0.2s', background: activeTab === tab ? 'white' : 'transparent', color: activeTab === tab ? '#2563eb' : 'rgba(255,255,255,0.8)' }}>
                  By {tab === 'route' ? 'Route' : 'Bus No.'}
                </button>
              ))}
            </div>

            {activeTab === 'route' ? (
              <div style={{display:'flex', flexWrap:'wrap', gap:'12px', alignItems:'center'}}>
                <div style={{flex:'1', minWidth:'200px', position:'relative'}}>
                  <MapPin style={{position:'absolute', left:'12px', top:'50%', transform:'translateY(-50%)', width:'16px', height:'16px', color:'#3b82f6'}} />
                  <select id="source-stop"
                    style={{width:'100%', paddingLeft:'40px', paddingRight:'16px', paddingTop:'12px', paddingBottom:'12px', border:'none', borderRadius:'12px', background:'white', color:'#1e293b', fontSize:'14px', outline:'none'}}
                    value={source} onChange={(e) => setSource(e.target.value)}>
                    <option value="">Select Start Location</option>
                    {stops.map(stop => <option key={stop.id} value={stop.id}>{stop.stop_name}</option>)}
                  </select>
                </div>
                <button onClick={swapStops}
                  style={{width:'40px', height:'40px', background:'rgba(255,255,255,0.2)', border:'1px solid rgba(255,255,255,0.3)', borderRadius:'12px', display:'flex', alignItems:'center', justifyContent:'center', cursor:'pointer', flexShrink:0}}>
                  <ArrowUpDown style={{width:'16px', height:'16px', color:'white'}} />
                </button>
                <div style={{flex:'1', minWidth:'200px', position:'relative'}}>
                  <Navigation style={{position:'absolute', left:'12px', top:'50%', transform:'translateY(-50%)', width:'16px', height:'16px', color:'#14b8a6'}} />
                  <select id="dest-stop"
                    style={{width:'100%', paddingLeft:'40px', paddingRight:'16px', paddingTop:'12px', paddingBottom:'12px', border:'none', borderRadius:'12px', background:'white', color:'#1e293b', fontSize:'14px', outline:'none'}}
                    value={destination} onChange={(e) => setDestination(e.target.value)}>
                    <option value="">Select End Destination</option>
                    {stops.filter(s => s.id !== parseInt(source)).map(stop => <option key={stop.id} value={stop.id}>{stop.stop_name}</option>)}
                  </select>
                </div>
                <button id="search-routes-btn" onClick={handleSearch}
                  style={{background:'white', color:'#2563eb', fontWeight:700, padding:'12px 28px', borderRadius:'12px', border:'none', display:'flex', alignItems:'center', gap:'8px', cursor:'pointer', fontSize:'14px', flexShrink:0, boxShadow:'0 4px 15px rgba(0,0,0,0.1)'}}>
                  <Search style={{width:'16px', height:'16px'}} />
                  Search Routes
                </button>
              </div>
            ) : (
              <div style={{display:'flex', gap:'12px', flexWrap:'wrap'}}>
                <div style={{flex:'1', minWidth:'200px', position:'relative'}}>
                  <Bus style={{position:'absolute', left:'12px', top:'50%', transform:'translateY(-50%)', width:'16px', height:'16px', color:'#3b82f6'}} />
                  <input id="bus-number" type="text" placeholder="Enter Bus Number (e.g. KMT-24)"
                    style={{width:'100%', paddingLeft:'40px', paddingRight:'16px', paddingTop:'12px', paddingBottom:'12px', border:'none', borderRadius:'12px', background:'white', color:'#1e293b', fontSize:'14px', outline:'none'}}
                    value={busNo} onChange={(e) => setBusNo(e.target.value)} />
                </div>
                <button id="search-bus-btn"
                  style={{background:'white', color:'#2563eb', fontWeight:700, padding:'12px 28px', borderRadius:'12px', border:'none', display:'flex', alignItems:'center', gap:'8px', cursor:'pointer', fontSize:'14px'}}>
                  <Search style={{width:'16px', height:'16px'}} />Search
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* MAIN CONTENT */}
      <div className="flex-1 max-w-7xl mx-auto w-full px-4 sm:px-6 lg:px-8 py-8">
        <div style={{display:'grid', gridTemplateColumns:'repeat(auto-fit, minmax(300px, 1fr))', gap:'24px'}}>

          {/* LEFT: Routes list */}
          <div style={{gridColumn:'span 2', minWidth:0}}>
            {routes.length > 0 && (
              <div>
                <h2 className="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
                  <Route className="w-5 h-5 text-blue-600" />
                  Available Routes
                  <span className="ml-1 text-sm font-normal text-slate-400">({routes.length} routes)</span>
                </h2>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  {routes.map(route => (
                    <button
                      key={route.id}
                      onClick={() => navigate(`/citizen/track?routeId=${route.id}`)}
                      className="group bg-white rounded-2xl border border-slate-100 p-5 hover:border-blue-200 hover:shadow-lg hover:shadow-blue-50 transition-all text-left flex items-center gap-4"
                    >
                      <div className="w-12 h-12 bg-blue-50 group-hover:bg-blue-100 rounded-2xl flex items-center justify-center flex-shrink-0 transition-colors">
                        <Bus className="w-6 h-6 text-blue-600" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="font-semibold text-slate-800 truncate group-hover:text-blue-700 transition-colors">{route.route_name}</p>
                        <p className="text-slate-400 text-sm flex items-center gap-1 mt-0.5">
                          <MapPin className="w-3 h-3" />
                          {route.stop_count || 0} stops
                        </p>
                      </div>
                      <ChevronRight className="w-4 h-4 text-slate-300 group-hover:text-blue-500 group-hover:translate-x-0.5 transition-all flex-shrink-0" />
                    </button>
                  ))}
                </div>
              </div>
            )}

            {routes.length === 0 && (
              <div className="bg-white rounded-2xl border border-slate-100 p-12 text-center">
                <Bus className="w-12 h-12 text-slate-300 mx-auto mb-4" />
                <h3 className="font-semibold text-slate-600 mb-1">No Routes Available</h3>
                <p className="text-slate-400 text-sm">Routes will appear here once the admin adds them</p>
              </div>
            )}
          </div>

          {/* RIGHT: Sidebar */}
          <div style={{display:'flex', flexDirection:'column', gap:'20px'}}>
            {/* Recent searches */}
            {recentSearches.length > 0 && (
              <div className="bg-white rounded-2xl border border-slate-100 p-5">
                <h3 className="font-bold text-slate-700 mb-4 flex items-center gap-2 text-sm uppercase tracking-wide">
                  <Clock className="w-4 h-4 text-blue-500" />Recent Searches
                </h3>
                <div className="space-y-2">
                  {recentSearches.map((s, i) => (
                    <button
                      key={i}
                      onClick={() => navigate(`/citizen/track?routeId=${s.routeId}`)}
                      className="w-full flex items-center gap-3 p-3 bg-slate-50 hover:bg-blue-50 rounded-xl border border-transparent hover:border-blue-100 transition-all text-left group"
                    >
                      <div className="w-8 h-8 bg-slate-100 group-hover:bg-blue-100 rounded-lg flex items-center justify-center flex-shrink-0 transition-colors">
                        <Bus className="w-4 h-4 text-slate-500 group-hover:text-blue-600 transition-colors" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-slate-700 text-sm font-medium truncate">{s.source}</p>
                        <p className="text-slate-400 text-xs truncate">→ {s.dest}</p>
                      </div>
                    </button>
                  ))}
                </div>
              </div>
            )}

            {/* How it works */}
            <div className="bg-gradient-to-br from-blue-600 to-blue-700 rounded-2xl p-5 text-white">
              <h3 className="font-bold mb-4">How it works</h3>
              <div className="space-y-3">
                {[
                  { step: '1', text: 'Select your start and end stop' },
                  { step: '2', text: 'View buses live on the map' },
                  { step: '3', text: 'Check ETA and route path' },
                ].map(({ step, text }) => (
                  <div key={step} className="flex items-start gap-3">
                    <div className="w-6 h-6 bg-white/20 rounded-full flex items-center justify-center flex-shrink-0 text-xs font-bold">{step}</div>
                    <p className="text-blue-100 text-sm">{text}</p>
                  </div>
                ))}
              </div>
            </div>

            {/* Quick info */}
            <div className="bg-white rounded-2xl border border-slate-100 p-5">
              <h3 className="font-bold text-slate-700 mb-3 text-sm">System Stats</h3>
              <div className="grid grid-cols-2 gap-3">
                <div className="bg-blue-50 rounded-xl p-3 text-center">
                  <p className="text-2xl font-extrabold text-blue-600">{routes.length}</p>
                  <p className="text-blue-600/70 text-xs font-medium">Routes</p>
                </div>
                <div className="bg-teal-50 rounded-xl p-3 text-center">
                  <p className="text-2xl font-extrabold text-teal-600">{stops.length}</p>
                  <p className="text-teal-600/70 text-xs font-medium">Stops</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CitizenHomePage;
