import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import AdminSidebar from '../../components/AdminSidebar';
import { getRoutes, deleteRoute, uploadRouteExcel } from '../../services/api';
import { Plus, Trash2, Route, Upload, X, Menu, FileSpreadsheet, MapPin, AlertCircle, CheckCircle, ChevronRight, Map, Sparkles } from 'lucide-react';

const Toast = ({ toast }) => toast ? (
  <div className={`flex items-center gap-2.5 px-4 py-3 rounded-xl text-sm font-medium ${
    toast.type === 'error' ? 'bg-red-50 text-red-600 border border-red-100' : 'bg-emerald-50 text-emerald-700 border border-emerald-100'
  }`}>
    {toast.type === 'error' ? <AlertCircle className="w-4 h-4 flex-shrink-0" /> : <CheckCircle className="w-4 h-4 flex-shrink-0" />}
    {toast.msg}
  </div>
) : null;

const AdminRoutes = () => {
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [routes, setRoutes] = useState([]);
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState(null);

  const showToast = (msg, type = 'success') => { setToast({ msg, type }); setTimeout(() => setToast(null), 3000); };

  useEffect(() => { fetchRoutes(); }, []);
  const fetchRoutes = () => getRoutes().then(res => setRoutes(res.data)).catch(console.error);



  const handleExcelUpload = async (e) => {
    e.preventDefault();
    if (!file) return;
    setLoading(true);
    const formData = new FormData(); formData.append('file', file);
    try { const res = await uploadRouteExcel(formData); setFile(null); fetchRoutes(); showToast(`Route "${res.data.route.route_name}" uploaded with ${res.data.stops.length} stops!`); }
    catch (err) { showToast(err.response?.data?.message || 'Excel upload failed', 'error'); }
    finally { setLoading(false); }
  };

  const handleDelete = async (id, name) => {
    if (!confirm(`Delete route "${name}"?`)) return;
    try { await deleteRoute(id); fetchRoutes(); showToast('Route deleted'); }
    catch { showToast('Failed to delete', 'error'); }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex">
      <AdminSidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">

        {/* Top bar */}
        <div className="bg-white border-b border-slate-100 px-8 py-5 flex items-center gap-4 sticky top-0 z-30">
          <button onClick={() => setSidebarOpen(true)} className="md:hidden p-2 rounded-lg hover:bg-slate-100">
            <Menu className="w-5 h-5 text-slate-600" />
          </button>
          <div className="w-9 h-9 bg-blue-50 rounded-xl flex items-center justify-center flex-shrink-0">
            <Route className="w-5 h-5 text-blue-600" />
          </div>
          <div>
            <h1 className="text-xl font-bold text-slate-800">Route Management</h1>
            <p className="text-slate-400 text-xs">{routes.length} routes configured</p>
          </div>
        </div>

        <div className="flex-1 overflow-y-auto p-8">
          <div className="max-w-7xl mx-auto">
            {toast && <div className="mb-5"><Toast toast={toast} /></div>}

            <div className="grid grid-cols-1 xl:grid-cols-5 gap-6">
              {/* LEFT — Create form */}
              <div className="xl:col-span-2 space-y-5">
                {/* Route Builder CTA */}
                <div className="bg-gradient-to-br from-blue-600 via-blue-700 to-indigo-700 rounded-2xl p-6 text-white shadow-xl shadow-blue-600/20 relative overflow-hidden">
                  <div className="absolute top-0 right-0 w-32 h-32 bg-white/5 rounded-full -translate-y-8 translate-x-8"></div>
                  <div className="absolute bottom-0 left-0 w-24 h-24 bg-white/5 rounded-full translate-y-6 -translate-x-6"></div>
                  <div className="relative z-10">
                    <div className="w-12 h-12 bg-white/15 rounded-xl flex items-center justify-center mb-4 border border-white/20">
                      <Map className="w-6 h-6 text-white" />
                    </div>
                    <h2 className="font-bold text-xl mb-1.5">Visual Route Builder</h2>
                    <p className="text-blue-200 text-sm mb-5 leading-relaxed">
                      Click on the map to place stations. Drag to reposition. Auto-names via reverse geocoding.
                    </p>
                    <button
                      onClick={() => navigate('/admin/route-builder')}
                      className="w-full bg-white text-blue-700 font-bold py-3 rounded-xl flex items-center justify-center gap-2.5 transition-all hover:bg-blue-50 shadow-lg text-sm"
                    >
                      <Sparkles className="w-4 h-4" />
                      Open Route Builder
                    </button>
                  </div>
                </div>

                {/* Excel Upload Card */}
                <div className="bg-white rounded-2xl border border-slate-100 p-6">
                  <h2 className="font-bold text-slate-800 text-lg mb-5 flex items-center gap-2">
                    <FileSpreadsheet className="w-5 h-5 text-blue-600" />Upload via Excel
                  </h2>

                  <form onSubmit={handleExcelUpload} className="space-y-4">
                    <div className="p-3 bg-blue-50 rounded-xl border border-blue-100 text-sm text-blue-700">
                      <p className="font-semibold mb-1">📋 Required Excel Columns:</p>
                      <code className="text-xs bg-blue-100 px-2 py-0.5 rounded font-mono">route_name | stop_name | latitude | longitude | stop_order</code>
                    </div>
                    <label className="block border-2 border-dashed border-slate-200 hover:border-blue-300 rounded-xl p-8 text-center cursor-pointer transition-colors bg-slate-50 hover:bg-blue-50">
                      <FileSpreadsheet className="w-10 h-10 text-slate-300 mx-auto mb-3" />
                      <p className="text-sm font-semibold text-slate-600">{file ? file.name : 'Click to upload Excel file'}</p>
                      <p className="text-xs text-slate-400 mt-1">.xlsx or .xls format</p>
                      <input id="excel-upload" type="file" accept=".xlsx,.xls" className="hidden" onChange={(e) => setFile(e.target.files[0])} />
                    </label>
                    {file && (
                      <button type="button" onClick={() => setFile(null)} className="flex items-center gap-1.5 text-red-500 hover:text-red-600 text-sm font-medium">
                        <X className="w-3.5 h-3.5" />Remove file
                      </button>
                    )}
                    <button id="upload-excel-btn" type="submit" disabled={loading || !file}
                      className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 rounded-xl flex items-center justify-center gap-2 transition-all shadow-lg shadow-blue-600/15 disabled:opacity-50">
                      {loading ? <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" /> : <Upload className="w-4 h-4" />}
                      Upload & Create Route
                    </button>
                  </form>
                </div>
              </div>

              {/* RIGHT — Routes list */}
              <div className="xl:col-span-3">
                <div className="bg-white rounded-2xl border border-slate-100 overflow-hidden">
                  <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
                    <h2 className="font-bold text-slate-800 flex items-center gap-2">
                      <Route className="w-4 h-4 text-blue-500" />All Routes
                    </h2>
                    <span className="bg-blue-50 text-blue-600 text-xs font-semibold px-2.5 py-1 rounded-full">{routes.length} total</span>
                  </div>

                  {routes.length === 0 ? (
                    <div className="py-16 text-center px-6">
                      <Route className="w-12 h-12 text-slate-200 mx-auto mb-3" />
                      <p className="font-semibold text-slate-500">No routes yet</p>
                      <p className="text-slate-400 text-sm mt-1">Create your first route using the form</p>
                    </div>
                  ) : (
                    <div className="divide-y divide-slate-50">
                      {routes.map(route => (
                        <div key={route.id} className="flex items-center gap-4 px-6 py-4 hover:bg-slate-50 transition-colors group">
                          <div className="w-10 h-10 bg-blue-50 rounded-xl flex items-center justify-center flex-shrink-0">
                            <Route className="w-5 h-5 text-blue-600" />
                          </div>
                          <div className="flex-1 min-w-0">
                            <p className="font-semibold text-slate-800 truncate">{route.route_name}</p>
                            {(route.start_point_name || route.end_point_name) && (
                              <div className="flex items-center gap-2 mt-1.5 text-xs text-slate-500 font-medium">
                                <span className="text-emerald-600 truncate max-w-[40%]">{route.start_point_name}</span>
                                <ChevronRight className="w-3 h-3 text-slate-300 flex-shrink-0" />
                                <span className="text-red-600 truncate max-w-[40%]">{route.end_point_name}</span>
                              </div>
                            )}
                            <div className="flex items-center gap-3 mt-1.5 text-xs text-slate-400">
                              <span className="flex items-center gap-1 bg-slate-100 px-2 py-0.5 rounded-full"><MapPin className="w-3 h-3" />{route.stop_count || 0} stops</span>
                              {route.description && <span className="truncate max-w-48">{route.description}</span>}
                            </div>
                          </div>
                          <button onClick={() => handleDelete(route.id, route.route_name)}
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
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminRoutes;
