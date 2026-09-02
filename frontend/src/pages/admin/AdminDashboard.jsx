import { useState, useEffect } from 'react';
import AdminSidebar from '../../components/AdminSidebar';
import { getRoutes, getBuses, getActiveTrips, getDrivers } from '../../services/api';
import {
  Bus, Route, MapPin, Users, TrendingUp,
  Activity, Menu, RefreshCw, Clock,
} from 'lucide-react';
import { Link } from 'react-router-dom';

/* ── Stat Card ─────────────────────────────────────── */
const StatCard = ({ icon: Icon, label, value, color, subtext, to }) => (
  <Link
    to={to}
    className="flex flex-col gap-4 bg-white rounded-2xl border border-slate-100 p-6 hover:shadow-md hover:border-slate-200 transition-all group"
  >
    <div className="flex items-center justify-between">
      <div className={`w-11 h-11 ${color} rounded-xl flex items-center justify-center flex-shrink-0`}>
        <Icon className="w-5 h-5 text-white" />
      </div>
      <span className="text-slate-300 text-sm group-hover:text-blue-400 transition-colors">→</span>
    </div>
    <div className="min-w-0 pt-2">
      <p className="text-3xl font-extrabold text-slate-800 leading-tight mb-1">{value}</p>
      <p className="text-sm font-medium text-slate-500 mb-1">{label}</p>
      {subtext && <p className="text-xs text-slate-400">{subtext}</p>}
    </div>
  </Link>
);

/* ── Quick Action ──────────────────────────────────── */
const QuickAction = ({ label, to, icon: Icon, iconColor, bg }) => (
  <Link
    to={to}
    className={`flex items-center gap-4 px-5 py-3.5 rounded-xl ${bg} transition-colors group`}
  >
    <Icon className={`w-5 h-5 flex-shrink-0 ${iconColor}`} />
    <span className="flex-1 text-sm font-semibold text-slate-700 group-hover:text-slate-900 truncate">{label}</span>
    <span className="text-slate-400 group-hover:text-slate-600 text-sm flex-shrink-0">→</span>
  </Link>
);

/* ── Dashboard ─────────────────────────────────────── */
const AdminDashboard = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [stats, setStats] = useState({ routes: 0, buses: 0, trips: 0, drivers: 0 });
  const [activeTrips, setActiveTrips] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchData = () => {
    setLoading(true);
    Promise.all([getRoutes(), getBuses(), getActiveTrips(), getDrivers()])
      .then(([r, b, t, d]) => {
        setStats({ routes: r.data.length, buses: b.data.length, trips: t.data.length, drivers: d.data.length });
        setActiveTrips(t.data);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchData(); }, []);

  const QUICK_ACTIONS = [
    { label: 'Add New Route', to: '/admin/routes', icon: Route, iconColor: 'text-blue-600', bg: 'bg-blue-50 hover:bg-blue-100' },
    { label: 'Add Bus Stop', to: '/admin/stops', icon: MapPin, iconColor: 'text-teal-600', bg: 'bg-teal-50 hover:bg-teal-100' },
    { label: 'Register Bus', to: '/admin/buses', icon: Bus, iconColor: 'text-indigo-600', bg: 'bg-indigo-50 hover:bg-indigo-100' },
    { label: 'Add Driver', to: '/admin/drivers', icon: Users, iconColor: 'text-purple-600', bg: 'bg-purple-50 hover:bg-purple-100' },
  ];

  return (
    <div className="min-h-screen bg-slate-50 flex ">
      <AdminSidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      {/* Main area */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">

        {/* ── Top bar ── */}
        <header className="bg-white border-b border-slate-100 sticky top-0 z-30">
          <div className="flex items-center justify-between px-8 py-5">
            <div className="flex items-center gap-4 ">
              <button
                onClick={() => setSidebarOpen(true)}
                className="md:hidden p-2 rounded-xl hover:bg-slate-100 transition-colors"
              >
                <Menu className="w-5 h-5 text-slate-600" />
              </button>
              <div className="flex flex-col justify-center gap-1">
                <h1 className="text-2xl font-bold text-slate-800 leading-tight">Dashboard</h1>
                <p className="text-sm text-slate-500 leading-tight">KMT BusSetu System Overview</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <div className="flex items-center gap-2 bg-emerald-50 border border-emerald-100 px-3 py-2 rounded-lg">
                <span className="relative flex h-2 w-2">
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75" />
                  <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500" />
                </span>
                <span className="text-emerald-700 text-xs font-semibold">System Live</span>
              </div>
              <button onClick={fetchData} className="p-2 rounded-xl hover:bg-slate-100 transition-colors" title="Refresh">
                <RefreshCw className="w-4 h-4 text-slate-500" />
              </button>
            </div>
          </div>
        </header>

        {/* ── Page content ── */}
        <main className="flex-1 overflow-y-auto p-8">
          <div className="max-w-7xl mx-auto space-y-6">

            {/* Stat cards */}
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-5">
              <StatCard icon={Route} label="Total Routes" value={stats.routes} color="bg-blue-600" subtext="Bus routes" to="/admin/routes" />
              <StatCard icon={Bus} label="Total Buses" value={stats.buses} color="bg-teal-600" subtext="Fleet size" to="/admin/buses" />
              <StatCard icon={Activity} label="Active Trips" value={stats.trips} color="bg-indigo-600" subtext="Right now" to="/admin/routes" />
              <StatCard icon={Users} label="Drivers" value={stats.drivers} color="bg-purple-600" subtext="Registered" to="/admin/drivers" />
            </div>

            {/* Bottom row */}
            <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">

              {/* Live trips */}
              <div className="xl:col-span-2 bg-white rounded-2xl border border-slate-100 overflow-hidden">
                <div className="flex items-center justify-between px-6 py-5 border-b border-slate-100">
                  <h2 className="text-lg font-bold text-slate-800 flex items-center gap-3">
                    <Activity className="w-5 h-5 text-emerald-500" />
                    Live Active Trips
                  </h2>
                  <span className="bg-emerald-100 text-emerald-700 text-xs font-semibold px-3 py-1 rounded-full">
                    {stats.trips} running
                  </span>
                </div>

                {loading ? (
                  <div className="p-6 space-y-3">
                    {[1, 2, 3].map(i => <div key={i} className="h-16 bg-slate-100 rounded-xl animate-pulse" />)}
                  </div>
                ) : activeTrips.length === 0 ? (
                  <div className="flex flex-col items-center justify-center py-20 px-6 text-center">
                    <div className="w-14 h-14 bg-slate-100 rounded-2xl flex items-center justify-center mb-4">
                      <Bus className="w-7 h-7 text-slate-300" />
                    </div>
                    <p className="text-lg font-semibold text-slate-500 mt-2 mb-1">No active trips</p>
                    <p className="text-slate-400 text-sm">Trips appear here when drivers start them</p>
                  </div>
                ) : (
                  <ul className="divide-y divide-slate-50">
                    {activeTrips.map(trip => (
                      <li key={trip.id} className="flex items-center gap-5 px-6 py-4 hover:bg-slate-50 transition-colors">
                        <div className="w-12 h-12 bg-blue-50 rounded-xl flex items-center justify-center text-2xl flex-shrink-0">🚌</div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-3 mb-1">
                            <p className="font-semibold text-slate-800 text-base truncate">{trip.bus_number}</p>
                            <span className="bg-emerald-100 text-emerald-700 text-xs font-bold px-2.5 py-0.5 rounded-full flex-shrink-0">Live</span>
                          </div>
                          <p className="text-slate-500 text-sm truncate">{trip.route_name} · {trip.driver_name}</p>
                        </div>
                        <div className="text-right flex-shrink-0">
                          <p className="text-blue-600 text-base font-bold mb-1">{trip.speed || 0} km/h</p>
                          <p className="text-slate-500 text-xs flex items-center gap-1.5 justify-end">
                            <Clock className="w-3.5 h-3.5" />
                            {new Date(trip.started_at).toLocaleTimeString()}
                          </p>
                        </div>
                      </li>
                    ))}
                  </ul>
                )}
              </div>

              {/* Right column */}
              <div className="flex flex-col gap-5">
                {/* Quick Actions */}
                <div className="bg-white rounded-2xl border border-slate-100 p-6">
                  <h2 className="text-lg font-bold text-slate-800 mb-5 flex items-center gap-3">
                    <TrendingUp className="w-5 h-5 text-blue-500" />
                    Quick Actions
                  </h2>
                  <div className="flex flex-col gap-2">
                    {QUICK_ACTIONS.map(qa => <QuickAction key={qa.to} {...qa} />)}
                  </div>
                </div>

                {/* Fleet Overview */}
                <div className="bg-gradient-to-br from-blue-600 to-blue-700 rounded-2xl p-6 text-white">
                  <h3 className="text-sm font-bold uppercase tracking-widest text-blue-200 mb-6">
                    Fleet Overview
                  </h3>
                  <div className="grid grid-cols-2 gap-3">
                    {[
                      { label: 'Routes', val: stats.routes },
                      { label: 'Buses', val: stats.buses },
                      { label: 'Live', val: stats.trips },
                      { label: 'Drivers', val: stats.drivers },
                    ].map(({ label, val }) => (
                      <div key={label} className="bg-white/10 rounded-xl px-4 py-5 text-center">
                        <p className="text-3xl font-extrabold leading-tight mb-2">{val}</p>
                        <p className="text-blue-200 text-sm font-medium">{label}</p>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>

          </div>
        </main>
      </div>
    </div>
  );
};

export default AdminDashboard;
