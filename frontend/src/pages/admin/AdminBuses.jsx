import { useState, useEffect } from 'react';
import AdminSidebar from '../../components/AdminSidebar';
import { getBuses, addBus, updateBusStatus, deleteBus } from '../../services/api';
import { Plus, Trash2, Bus, Menu, CheckCircle, AlertCircle } from 'lucide-react';

const STATUS_OPTIONS = ['active', 'inactive', 'maintenance'];
const STATUS_STYLE = {
  active: 'bg-emerald-100 text-emerald-700',
  inactive: 'bg-slate-100 text-slate-600',
  maintenance: 'bg-amber-100 text-amber-700',
};

const AdminBuses = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [buses, setBuses] = useState([]);
  const [form, setForm] = useState({ bus_number: '', status: 'inactive' });
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState(null);

  const showToast = (msg, type = 'success') => { setToast({ msg, type }); setTimeout(() => setToast(null), 3000); };

  useEffect(() => { fetchBuses(); }, []);
  const fetchBuses = () => getBuses().then(res => setBuses(res.data)).catch(console.error);

  const handleAdd = async (e) => {
    e.preventDefault();
    if (!form.bus_number.trim()) return;
    setLoading(true);
    try { await addBus(form); setForm({ bus_number: '', status: 'inactive' }); fetchBuses(); showToast('Bus added!'); }
    catch (err) { showToast(err.response?.data?.message || 'Failed to add bus', 'error'); }
    finally { setLoading(false); }
  };

  const handleStatusChange = async (id, status) => {
    try { await updateBusStatus(id, status); setBuses(prev => prev.map(b => b.id === id ? { ...b, status } : b)); showToast('Status updated'); }
    catch { showToast('Failed to update', 'error'); }
  };

  const handleDelete = async (id, num) => {
    if (!confirm(`Delete bus ${num}?`)) return;
    try { await deleteBus(id); fetchBuses(); showToast('Bus deleted'); }
    catch { showToast('Failed to delete', 'error'); }
  };

  const activeBuses = buses.filter(b => b.status === 'active').length;
  const maintenanceBuses = buses.filter(b => b.status === 'maintenance').length;

  return (
    <div className="min-h-screen bg-slate-50 flex">
      <AdminSidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <div className="bg-white border-b border-slate-100 px-8 py-5 flex items-center gap-4 sticky top-0 z-30">
          <button onClick={() => setSidebarOpen(true)} className="md:hidden p-2 rounded-lg hover:bg-slate-100">
            <Menu className="w-5 h-5 text-slate-600" />
          </button>
          <div className="w-9 h-9 bg-indigo-50 rounded-xl flex items-center justify-center">
            <Bus className="w-5 h-5 text-indigo-600" />
          </div>
          <div className="flex-1">
            <h1 className="text-xl font-bold text-slate-800">Bus Management</h1>
            <p className="text-slate-400 text-xs">{buses.length} buses · {activeBuses} active · {maintenanceBuses} maintenance</p>
          </div>
        </div>

        <div className="flex-1 overflow-y-auto p-8">
          <div className="max-w-7xl mx-auto space-y-5">
            {toast && (
              <div className={`flex items-center gap-2.5 px-4 py-3 rounded-xl text-sm font-medium ${toast.type === 'error' ? 'bg-red-50 text-red-600 border border-red-100' : 'bg-emerald-50 text-emerald-700 border border-emerald-100'}`}>
                {toast.type === 'error' ? <AlertCircle className="w-4 h-4" /> : <CheckCircle className="w-4 h-4" />}{toast.msg}
              </div>
            )}

            {/* Stats bar */}
            <div className="grid grid-cols-3 gap-4">
              {[
                { label: 'Total', val: buses.length, color: 'bg-blue-50 text-blue-700' },
                { label: 'Active', val: activeBuses, color: 'bg-emerald-50 text-emerald-700' },
                { label: 'Maintenance', val: maintenanceBuses, color: 'bg-amber-50 text-amber-700' },
              ].map(({ label, val, color }) => (
                <div key={label} className={`${color} rounded-xl p-3 md:p-4 text-center min-w-0 overflow-hidden`}>
                  <p className="text-xl md:text-2xl font-extrabold truncate w-full">{val}</p>
                  <p className="text-xs md:text-sm font-medium opacity-70 truncate w-full">{label}</p>
                </div>
              ))}
            </div>

            {/* Add bus form */}
            <div className="bg-white rounded-2xl border border-slate-100 p-6">
              <h2 className="font-bold text-slate-800 mb-4 flex items-center gap-2 text-lg">
                <Plus className="w-5 h-5 text-indigo-600" />Register New Bus
              </h2>
              <form onSubmit={handleAdd} className="flex flex-col sm:flex-row gap-3">
                <input id="bus-number-input" type="text" placeholder="Bus Number (e.g. KMT-042)"
                  className="flex-1 px-4 py-3 border border-slate-200 rounded-xl bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 text-slate-800 text-sm"
                  value={form.bus_number} onChange={(e) => setForm({ ...form, bus_number: e.target.value })} required />
                <select className="sm:w-44 px-4 py-3 border border-slate-200 rounded-xl bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 text-slate-800 text-sm"
                  value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                  {STATUS_OPTIONS.map(s => <option key={s} value={s}>{s.charAt(0).toUpperCase() + s.slice(1)}</option>)}
                </select>
                <button id="add-bus-btn" type="submit" disabled={loading}
                  className="bg-indigo-600 hover:bg-indigo-700 text-white font-semibold px-6 py-3 rounded-xl flex items-center justify-center gap-2 transition-all shadow-lg shadow-indigo-600/15 disabled:opacity-60">
                  {loading ? <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" /> : <Plus className="w-4 h-4" />}
                  Add Bus
                </button>
              </form>
            </div>

            {/* Buses table */}
            <div className="bg-white rounded-2xl border border-slate-100 overflow-hidden">
              <div className="px-6 py-4 border-b border-slate-100">
                <h2 className="font-bold text-slate-800 flex items-center gap-2">
                  <Bus className="w-4 h-4 text-indigo-500" />Fleet ({buses.length})
                </h2>
              </div>

              {buses.length === 0 ? (
                <div className="py-16 text-center">
                  <Bus className="w-12 h-12 text-slate-200 mx-auto mb-3" />
                  <p className="font-semibold text-slate-500">No buses registered</p>
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full">
                    <thead>
                      <tr className="text-left bg-slate-50">
                        <th className="px-6 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Bus</th>
                        <th className="px-6 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Status</th>
                        <th className="px-6 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Driver</th>
                        <th className="px-6 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Change Status</th>
                        <th className="px-6 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider"></th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-50">
                      {buses.map(bus => (
                        <tr key={bus.id} className="hover:bg-slate-50 transition-colors group">
                          <td className="px-6 py-4">
                            <div className="flex items-center gap-3">
                              <div className="w-9 h-9 bg-indigo-50 rounded-xl flex items-center justify-center text-base flex-shrink-0">🚌</div>
                              <span className="font-bold text-slate-800">{bus.bus_number}</span>
                            </div>
                          </td>
                          <td className="px-6 py-4">
                            <span className={`inline-flex text-xs font-semibold px-2.5 py-1 rounded-full ${STATUS_STYLE[bus.status] || 'bg-slate-100 text-slate-600'}`}>
                              {bus.status}
                            </span>
                          </td>
                          <td className="px-6 py-4 text-slate-500 text-sm">{bus.driver_name || <span className="text-slate-300">—</span>}</td>
                          <td className="px-6 py-4">
                            <select
                              className="text-sm border border-slate-200 rounded-lg px-3 py-1.5 bg-white focus:outline-none focus:ring-1 focus:ring-blue-500 text-slate-700"
                              value={bus.status}
                              onChange={(e) => handleStatusChange(bus.id, e.target.value)}
                            >
                              {STATUS_OPTIONS.map(s => <option key={s} value={s}>{s.charAt(0).toUpperCase() + s.slice(1)}</option>)}
                            </select>
                          </td>
                          <td className="px-6 py-4">
                            <button onClick={() => handleDelete(bus.id, bus.bus_number)}
                              className="p-2 rounded-lg text-slate-300 hover:text-red-500 hover:bg-red-50 transition-all opacity-0 group-hover:opacity-100">
                              <Trash2 className="w-4 h-4" />
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminBuses;
