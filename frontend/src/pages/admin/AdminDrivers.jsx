import { useState, useEffect } from 'react';
import AdminSidebar from '../../components/AdminSidebar';
import { getDrivers, register } from '../../services/api';
import { Plus, Users, Menu, CheckCircle, AlertCircle, User, Mail, Lock, UserPlus } from 'lucide-react';

const AdminDrivers = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [drivers, setDrivers] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ name: '', email: '', password: '' });
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState(null);

  const showToast = (msg, type = 'success') => { setToast({ msg, type }); setTimeout(() => setToast(null), 3000); };

  useEffect(() => { getDrivers().then(res => setDrivers(res.data)).catch(console.error); }, []);

  const handleAddDriver = async (e) => {
    e.preventDefault(); setLoading(true);
    try { await register({ ...form, role: 'driver' }); setForm({ name: '', email: '', password: '' }); setShowForm(false); getDrivers().then(res => setDrivers(res.data)); showToast('Driver registered!'); }
    catch (err) { showToast(err.response?.data?.message || 'Failed to add driver', 'error'); }
    finally { setLoading(false); }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex">
      <AdminSidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} className="" />
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <div className="bg-white border-b border-slate-100 px-8 py-5 flex items-center gap-4 sticky top-0 z-30">
          <button onClick={() => setSidebarOpen(true)} className="md:hidden p-2 rounded-lg hover:bg-slate-100">
            <Menu className="w-5 h-5 text-slate-600" />
          </button>
          <div className="w-9 h-9 bg-purple-50 rounded-xl flex items-center justify-center">
            <Users className="w-5 h-5 text-purple-600" />
          </div>
          <div className="flex-1">
            <h1 className="text-xl font-bold text-slate-800">Driver Management</h1>
            <p className="text-slate-400 text-xs">{drivers.length} registered drivers</p>
          </div>
          <button onClick={() => setShowForm(!showForm)}
            className="bg-purple-600 hover:bg-purple-700 text-white font-semibold px-4 py-2.5 rounded-xl flex items-center gap-2 transition-all shadow-lg shadow-purple-600/20">
            <UserPlus className="w-4 h-4" />
            <span className="hidden sm:block">Add Driver</span>
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-8">
          <div className="max-w-7xl mx-auto space-y-5">
            {toast && (
              <div className={`flex items-center gap-2.5 px-4 py-3 rounded-xl text-sm font-medium ${toast.type === 'error' ? 'bg-red-50 text-red-600 border border-red-100' : 'bg-emerald-50 text-emerald-700 border border-emerald-100'}`}>
                {toast.type === 'error' ? <AlertCircle className="w-4 h-4" /> : <CheckCircle className="w-4 h-4" />}{toast.msg}
              </div>
            )}

            <div className="grid grid-cols-1 xl:grid-cols-5 gap-6">
              {/* Form — left, only when open */}
              {showForm && (
                <div className="xl:col-span-2">
                  <div className="bg-white rounded-2xl border border-slate-100 p-6">
                    <h2 className="font-bold text-slate-800 mb-5 flex items-center gap-2 text-lg">
                      <UserPlus className="w-5 h-5 text-purple-600" />Register Driver
                    </h2>
                    <form onSubmit={handleAddDriver} className="space-y-4">
                      <div>
                        <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wide mb-1.5">Full Name</label>
                        <div className="relative">
                          <User className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                          <input id="driver-name" type="text" placeholder="Full Name"
                            className="w-full pl-10 pr-4 py-3 border border-slate-200 rounded-xl bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 text-slate-800 text-sm"
                            value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
                        </div>
                      </div>
                      <div>
                        <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wide mb-1.5">Email</label>
                        <div className="relative">
                          <Mail className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                          <input id="driver-email" type="email" placeholder="driver@email.com"
                            className="w-full pl-10 pr-4 py-3 border border-slate-200 rounded-xl bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 text-slate-800 text-sm"
                            value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
                        </div>
                      </div>
                      <div>
                        <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wide mb-1.5">Password</label>
                        <div className="relative">
                          <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                          <input id="driver-password" type="password" placeholder="Min 6 characters"
                            className="w-full pl-10 pr-4 py-3 border border-slate-200 rounded-xl bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 text-slate-800 text-sm"
                            value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required minLength={6} />
                        </div>
                      </div>
                      <div className="flex gap-3">
                        <button id="add-driver-btn" type="submit" disabled={loading}
                          className="flex-1 bg-purple-600 hover:bg-purple-700 text-white font-semibold py-3 rounded-xl flex items-center justify-center gap-2 transition-all shadow-lg shadow-purple-600/15 disabled:opacity-60">
                          {loading ? <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" /> : <UserPlus className="w-4 h-4" />}
                          Register
                        </button>
                        <button type="button" onClick={() => setShowForm(false)}
                          className="px-5 py-3 border border-slate-200 rounded-xl text-slate-600 hover:bg-slate-50 text-sm font-medium transition-all">
                          Cancel
                        </button>
                      </div>
                    </form>
                  </div>
                </div>
              )}

              {/* Drivers table */}
              <div className={showForm ? 'xl:col-span-3' : 'xl:col-span-5'}>
                <div className="bg-white rounded-2xl border border-slate-100 overflow-hidden">
                  <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
                    <h2 className="font-bold text-slate-800 flex items-center gap-2">
                      <Users className="w-4 h-4 text-purple-500" />All Drivers
                    </h2>
                    <span className="bg-purple-50 text-purple-700 text-xs font-semibold px-2.5 py-1 rounded-full">{drivers.length} total</span>
                  </div>

                  {drivers.length === 0 ? (
                    <div className="py-16 text-center">
                      <Users className="w-12 h-12 text-slate-200 mx-auto mb-3" />
                      <p className="font-semibold text-slate-500">No drivers registered yet</p>
                      <button onClick={() => setShowForm(true)} className="mt-4 bg-purple-600 text-white px-5 py-2.5 rounded-xl text-sm font-semibold hover:bg-purple-700 transition-all inline-flex items-center gap-2">
                        <UserPlus className="w-4 h-4" />Add First Driver
                      </button>
                    </div>
                  ) : (
                    <div className="overflow-x-auto">
                      <table className="w-full">
                        <thead>
                          <tr className="bg-slate-50 text-left">
                            <th className="px-6 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Driver</th>
                            <th className="px-6 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Email</th>
                            <th className="px-6 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Role</th>
                            <th className="px-6 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Joined</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-50">
                          {drivers.map(driver => (
                            <tr key={driver.id} className="hover:bg-slate-50 transition-colors">
                              <td className="px-6 py-4">
                                <div className="flex items-center gap-3">
                                  <div className="w-9 h-9 bg-purple-100 rounded-xl flex items-center justify-center flex-shrink-0">
                                    <span className="text-purple-700 text-sm font-bold">{driver.name?.charAt(0) || 'D'}</span>
                                  </div>
                                  <span className="font-semibold text-slate-800">{driver.name}</span>
                                </div>
                              </td>
                              <td className="px-6 py-4 text-slate-500 text-sm">{driver.email}</td>
                              <td className="px-6 py-4">
                                <span className="bg-purple-100 text-purple-700 text-xs font-semibold px-2.5 py-1 rounded-full">Driver</span>
                              </td>
                              <td className="px-6 py-4 text-slate-400 text-sm">
                                {new Date(driver.created_at).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })}
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
      </div>
    </div>
  );
};

export default AdminDrivers;
