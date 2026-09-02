import { useState, useEffect } from 'react';
import AdminSidebar from '../../components/AdminSidebar';
import { getRoutes, getStopsByRoute, addStop, deleteStop } from '../../services/api';
import { Plus, Trash2, MapPin, Menu, AlertCircle, CheckCircle } from 'lucide-react';

const AdminStops = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [routes, setRoutes] = useState([]);
  const [stops, setStops] = useState([]);
  const [selectedRoute, setSelectedRoute] = useState('');
  const [form, setForm] = useState({ stop_name: '', latitude: '', longitude: '', stop_order: '' });
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState(null);

  const showToast = (msg, type = 'success') => { setToast({ msg, type }); setTimeout(() => setToast(null), 3000); };

  useEffect(() => { getRoutes().then(res => setRoutes(res.data)).catch(console.error); }, []);
  useEffect(() => {
    if (selectedRoute) getStopsByRoute(selectedRoute).then(res => setStops(res.data)).catch(console.error);
    else setStops([]);
  }, [selectedRoute]);

  const handleAdd = async (e) => {
    e.preventDefault();
    if (!selectedRoute) { showToast('Select a route first', 'error'); return; }
    setLoading(true);
    try { await addStop({ ...form, route_id: selectedRoute, stop_order: parseInt(form.stop_order) }); setForm({ stop_name: '', latitude: '', longitude: '', stop_order: '' }); getStopsByRoute(selectedRoute).then(res => setStops(res.data)); showToast('Stop added!'); }
    catch (err) { showToast(err.response?.data?.message || 'Failed', 'error'); }
    finally { setLoading(false); }
  };

  const handleDelete = async (id) => {
    try { await deleteStop(id); setStops(prev => prev.filter(s => s.id !== id)); showToast('Stop removed'); }
    catch { showToast('Failed to delete', 'error'); }
  };

  const nextOrder = stops.length > 0 ? Math.max(...stops.map(s => s.stop_order)) + 1 : 1;

  return (
    <div className="min-h-screen bg-slate-50 flex">
      <AdminSidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <div className="bg-white border-b border-slate-100 px-8 py-5 flex items-center gap-4 sticky top-0 z-30">
          <button onClick={() => setSidebarOpen(true)} className="md:hidden p-2 rounded-lg hover:bg-slate-100">
            <Menu className="w-5 h-5 text-slate-600" />
          </button>
          <div className="w-9 h-9 bg-teal-50 rounded-xl flex items-center justify-center">
            <MapPin className="w-5 h-5 text-teal-600" />
          </div>
          <div>
            <h1 className="text-xl font-bold text-slate-800">Stop Management</h1>
            <p className="text-slate-400 text-xs">Configure stops on bus routes</p>
          </div>
        </div>

        <div className="flex-1 overflow-y-auto p-8">
          <div className="max-w-7xl mx-auto space-y-5">
            {toast && (
              <div className={`flex items-center gap-2.5 px-4 py-3 rounded-xl text-sm font-medium ${toast.type === 'error' ? 'bg-red-50 text-red-600 border border-red-100' : 'bg-emerald-50 text-emerald-700 border border-emerald-100'}`}>
                {toast.type === 'error' ? <AlertCircle className="w-4 h-4" /> : <CheckCircle className="w-4 h-4" />}{toast.msg}
              </div>
            )}

            {/* Route selector */}
            <div className="bg-white rounded-2xl border border-slate-100 p-5">
              <label className="block text-sm font-bold text-slate-700 mb-2">Select Route</label>
              <select id="stop-route-select"
                className="w-full sm:w-80 px-4 py-3 border border-slate-200 rounded-xl bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 text-slate-800 text-sm"
                value={selectedRoute} onChange={(e) => setSelectedRoute(e.target.value)}>
                <option value="">Choose a route to manage stops...</option>
                {routes.map(r => <option key={r.id} value={r.id}>{r.route_name}</option>)}
              </select>
            </div>

            {selectedRoute && (
              <div className="grid grid-cols-1 xl:grid-cols-5 gap-6">
                {/* Add form */}
                <div className="xl:col-span-2">
                  <div className="bg-white rounded-2xl border border-slate-100 p-6">
                    <h2 className="font-bold text-slate-800 mb-5 flex items-center gap-2 text-lg">
                      <Plus className="w-5 h-5 text-teal-600" />Add Stop
                    </h2>
                    <form onSubmit={handleAdd} className="space-y-4">
                      <div>
                        <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wide mb-1.5">Stop Name</label>
                        <input id="stop-name" type="text" placeholder="e.g. City Centre Bus Stop"
                          className="w-full px-4 py-3 border border-slate-200 rounded-xl bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 text-slate-800 text-sm"
                          value={form.stop_name} onChange={(e) => setForm({ ...form, stop_name: e.target.value })} required />
                      </div>
                      <div className="grid grid-cols-2 gap-3">
                        <div>
                          <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wide mb-1.5">Latitude</label>
                          <input id="stop-lat" type="number" step="any" placeholder="18.5204"
                            className="w-full px-4 py-3 border border-slate-200 rounded-xl bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 text-slate-800 text-sm"
                            value={form.latitude} onChange={(e) => setForm({ ...form, latitude: e.target.value })} required />
                        </div>
                        <div>
                          <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wide mb-1.5">Longitude</label>
                          <input id="stop-lng" type="number" step="any" placeholder="73.8567"
                            className="w-full px-4 py-3 border border-slate-200 rounded-xl bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 text-slate-800 text-sm"
                            value={form.longitude} onChange={(e) => setForm({ ...form, longitude: e.target.value })} required />
                        </div>
                      </div>
                      <div>
                        <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wide mb-1.5">Stop Order</label>
                        <input id="stop-order" type="number" placeholder={`Next: ${nextOrder}`}
                          className="w-full px-4 py-3 border border-slate-200 rounded-xl bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 text-slate-800 text-sm"
                          value={form.stop_order} onChange={(e) => setForm({ ...form, stop_order: e.target.value })} required />
                      </div>
                      <button id="add-stop-btn" type="submit" disabled={loading}
                        className="w-full bg-teal-600 hover:bg-teal-700 text-white font-semibold py-3 rounded-xl flex items-center justify-center gap-2 transition-all shadow-lg shadow-teal-600/15 disabled:opacity-60">
                        {loading ? <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" /> : <Plus className="w-4 h-4" />}
                        Add Stop
                      </button>
                    </form>
                  </div>
                </div>

                {/* Stops list */}
                <div className="xl:col-span-3">
                  <div className="bg-white rounded-2xl border border-slate-100 overflow-hidden">
                    <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
                      <h2 className="font-bold text-slate-800 flex items-center gap-2">
                        <MapPin className="w-4 h-4 text-teal-500" />Route Stops
                      </h2>
                      <span className="bg-teal-50 text-teal-700 text-xs font-semibold px-2.5 py-1 rounded-full">{stops.length} stops</span>
                    </div>
                    {stops.length === 0 ? (
                      <div className="py-12 text-center">
                        <MapPin className="w-10 h-10 text-slate-200 mx-auto mb-3" />
                        <p className="text-slate-500 font-medium">No stops added yet</p>
                      </div>
                    ) : (
                      <div className="divide-y divide-slate-50">
                        {stops.map((stop) => (
                          <div key={stop.id} className="flex items-center gap-4 px-6 py-4 hover:bg-slate-50 transition-colors group">
                            <div className="w-8 h-8 bg-teal-100 text-teal-700 rounded-full flex items-center justify-center text-sm font-bold flex-shrink-0">
                              {stop.stop_order}
                            </div>
                            <div className="flex-1 min-w-0">
                              <p className="font-semibold text-slate-800 text-sm">{stop.stop_name}</p>
                              <p className="text-slate-400 text-xs font-mono">{parseFloat(stop.latitude).toFixed(5)}, {parseFloat(stop.longitude).toFixed(5)}</p>
                            </div>
                            <button onClick={() => handleDelete(stop.id)}
                              className="p-2 rounded-lg text-slate-300 hover:text-red-500 hover:bg-red-50 transition-all opacity-0 group-hover:opacity-100">
                              <Trash2 className="w-4 h-4" />
                            </button>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminStops;
