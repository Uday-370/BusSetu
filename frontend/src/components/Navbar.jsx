import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Bus, LogOut, User, LayoutDashboard } from 'lucide-react';

const Navbar = () => {
  const { user, logoutUser } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => { logoutUser(); navigate('/'); };

  return (
    <nav className="bg-white border-b border-slate-100 shadow-sm sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 lg:px-8 h-16 flex items-center justify-between">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-2.5 group">
          <div className="w-9 h-9 bg-blue-600 rounded-xl flex items-center justify-center shadow-md shadow-blue-600/20 group-hover:scale-105 transition-transform">
            <Bus className="w-5 h-5 text-white" />
          </div>
          <span className="text-lg font-bold text-slate-800 tracking-tight">BusSetu</span>
        </Link>

        {/* Nav links on desktop */}
        <div className="hidden md:flex items-center gap-1">
          <Link to="/citizen" className="px-4 py-2 rounded-lg text-sm font-medium text-slate-600 hover:text-blue-600 hover:bg-blue-50 transition-all">
            Track Bus
          </Link>
          {user?.role === 'admin' && (
            <Link to="/admin" className="px-4 py-2 rounded-lg text-sm font-medium text-slate-600 hover:text-blue-600 hover:bg-blue-50 transition-all flex items-center gap-1.5">
              <LayoutDashboard className="w-4 h-4" />Dashboard
            </Link>
          )}
          {user?.role === 'driver' && (
            <Link to="/driver" className="px-4 py-2 rounded-lg text-sm font-medium text-slate-600 hover:text-blue-600 hover:bg-blue-50 transition-all flex items-center gap-1.5">
              <LayoutDashboard className="w-4 h-4" />My Trips
            </Link>
          )}
        </div>

        {/* Right actions */}
        <div className="flex items-center gap-2 lg:gap-3">
          {user ? (
            <>
              <div className="hidden sm:flex items-center gap-2 bg-slate-50 border border-slate-200 rounded-xl px-3 py-2">
                <div className="w-7 h-7 bg-blue-600 rounded-lg flex items-center justify-center">
                  <User className="w-4 h-4 text-white" />
                </div>
                <div className="leading-tight">
                  <p className="text-slate-800 text-sm font-semibold">{user.name}</p>
                  <p className="text-slate-400 text-xs capitalize">{user.role}</p>
                </div>
              </div>
              <button
                onClick={handleLogout}
                className="flex items-center gap-1.5 px-3 py-2 rounded-xl text-slate-500 hover:text-red-600 hover:bg-red-50 text-sm font-medium transition-all border border-transparent hover:border-red-100"
              >
                <LogOut className="w-4 h-4" />
                <span className="hidden sm:block">Logout</span>
              </button>
            </>
          ) : (
            <Link
              to="/login"
              className="bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold px-4 py-2.5 rounded-xl transition-all shadow-md shadow-blue-600/20 hover:scale-[1.01]"
            >
              Login
            </Link>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
