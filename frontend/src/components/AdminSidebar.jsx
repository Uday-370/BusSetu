import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Bus, Route, MapPin, Users,
  LayoutDashboard, LogOut, X, ChevronRight,
} from 'lucide-react';

const LINKS = [
  { to: '/admin',         icon: LayoutDashboard, label: 'Dashboard', end: true  },
  { to: '/admin/routes',  icon: Route,           label: 'Routes',    end: false },
  { to: '/admin/stops',   icon: MapPin,           label: 'Stops',     end: false },
  { to: '/admin/buses',   icon: Bus,              label: 'Buses',     end: false },
  { to: '/admin/drivers', icon: Users,            label: 'Drivers',   end: false },
];

/* Width token — change once to apply everywhere */
const SIDEBAR_W = 260;

const SidebarInner = ({ onClose, user, handleLogout }) => (
  <div className="flex flex-col h-full bg-white overflow-hidden">

    {/* Brand */}
    <div className="flex items-center gap-3 px-6 py-6 border-b border-slate-100 flex-shrink-0">
      <div className="w-10 h-10 bg-blue-600 rounded-xl flex items-center justify-center shadow-md shadow-blue-600/25 flex-shrink-0">
        <Bus className="w-5 h-5 text-white" />
      </div>
      <div className="min-w-0">
        <p className="font-bold text-slate-800 text-base leading-tight truncate">BusSetu</p>
        <p className="text-slate-400 text-xs leading-tight">Admin Panel</p>
      </div>
    </div>

    {/* Navigation — scrollable */}
    <nav className="flex-1 overflow-y-auto px-4 py-5 min-h-0">
      <p className="px-4 text-[10px] font-bold tracking-widest text-slate-400 uppercase mb-3">
        Management
      </p>
      <ul className="space-y-1">
        {LINKS.map(({ to, icon: Icon, label, end }) => (
          <li key={to}>
            <NavLink
              to={to}
              end={end}
              onClick={onClose}
              className={({ isActive }) =>
                `flex items-center gap-3 w-full px-4 py-3 rounded-xl text-sm font-semibold transition-colors duration-150 ${
                  isActive
                    ? 'bg-blue-600 text-white shadow-md shadow-blue-600/20'
                    : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
                }`
              }
            >
              {({ isActive }) => (
                <>
                  <Icon className={`w-5 h-5 flex-shrink-0 ${isActive ? 'text-white' : 'text-slate-400'}`} />
                  <span className="flex-1 truncate">{label}</span>
                  {isActive && <ChevronRight className="w-4 h-4 text-blue-200 flex-shrink-0" />}
                </>
              )}
            </NavLink>
          </li>
        ))}
      </ul>
    </nav>

    {/* Footer — never cut off */}
    <div className="flex-shrink-0 px-4 py-4 border-t border-slate-100 space-y-2">
      <div className="flex items-center gap-3 px-4 py-3 rounded-xl bg-slate-50">
        <div className="w-9 h-9 rounded-xl bg-blue-600 flex items-center justify-center flex-shrink-0">
          <span className="text-white text-sm font-bold">{user?.name?.charAt(0) || 'A'}</span>
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-sm font-semibold text-slate-800 truncate leading-tight">{user?.name || 'Admin'}</p>
          <p className="text-xs text-slate-400 truncate leading-tight">{user?.email}</p>
        </div>
      </div>
      <button
        onClick={handleLogout}
        className="flex items-center gap-3 w-full px-4 py-3 rounded-xl text-sm font-semibold text-red-500 hover:bg-red-50 hover:text-red-600 transition-colors duration-150"
      >
        <LogOut className="w-5 h-5 flex-shrink-0" />
        <span>Logout</span>
      </button>
    </div>
  </div>
);

const AdminSidebar = ({ open, onClose }) => {
  const { user, logoutUser } = useAuth();
  const navigate = useNavigate();
  const handleLogout = () => { logoutUser(); navigate('/'); };

  return (
    <>
      {/* ── Desktop sidebar ── always visible at md+ */}
      <div
        className="hidden md:flex flex-col flex-shrink-0 sticky top-0 h-screen border-r border-slate-100"
        style={{ width: `${SIDEBAR_W}px`, minWidth: `${SIDEBAR_W}px` }}
      >
        <SidebarInner onClose={undefined} user={user} handleLogout={handleLogout} />
      </div>

      {/* ── Mobile overlay ── */}
      {open && (
        <div className="md:hidden fixed inset-0 z-50 flex">
          {/* Backdrop */}
          <div
            className="absolute inset-0 bg-black/40 backdrop-blur-sm"
            onClick={onClose}
          />
          {/* Panel */}
          <div
            className="relative z-10 h-full shadow-2xl flex-shrink-0"
            style={{ width: `${SIDEBAR_W}px` }}
          >
            <SidebarInner onClose={onClose} user={user} handleLogout={handleLogout} />
            <button
              onClick={onClose}
              className="absolute top-4 right-4 w-8 h-8 bg-slate-100 rounded-lg flex items-center justify-center hover:bg-slate-200 transition-colors"
            >
              <X className="w-4 h-4 text-slate-600" />
            </button>
          </div>
        </div>
      )}
    </>
  );
};

export default AdminSidebar;
