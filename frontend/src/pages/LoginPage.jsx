import { useState } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { login } from '../services/api';
import { Bus, Eye, EyeOff, Lock, Mail, ArrowLeft, AlertCircle, Shield, CheckCircle } from 'lucide-react';

const LoginPage = () => {
  const [form, setForm] = useState({ email: '', password: '' });
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const { loginUser } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const role = searchParams.get('role') || 'driver';
  const isDriver = role === 'driver';

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const res = await login(form);
      loginUser(res.data.token, res.data.user);
      if (res.data.user.role === 'admin') navigate('/admin');
      else if (res.data.user.role === 'driver') navigate('/driver');
      else navigate('/citizen');
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  const leftGradient = isDriver
    ? 'linear-gradient(135deg, #1d4ed8 0%, #2563eb 100%)'
    : 'linear-gradient(135deg, #0f766e 0%, #0d9488 100%)';

  const btnBg = isDriver ? '#2563eb' : '#0d9488';
  const btnHoverBg = isDriver ? '#1d4ed8' : '#0f766e';

  const features = isDriver
    ? ['Start and manage trips', 'Broadcast GPS location live', 'View your route map', 'Access trip history']
    : ['Manage all bus routes', 'Monitor active trips live', 'Register drivers & buses', 'Upload route via Excel'];

  return (
    <div style={{ minHeight: '100vh', background: '#f8fafc' }} className="flex flex-col lg:flex-row">

      {/* LEFT BRANDING PANEL — desktop only */}
      <div style={{
        background: leftGradient,
        minHeight: '100vh',
        padding: '48px',
        position: 'relative',
        overflow: 'hidden',
      }} className="hidden lg:flex w-full lg:w-[45%] flex-col justify-between">
        {/* BG decoration */}
        <div style={{ position: 'absolute', top: '-80px', right: '-80px', width: '300px', height: '300px', background: 'rgba(255,255,255,0.05)', borderRadius: '50%' }} />
        <div style={{ position: 'absolute', bottom: '-60px', left: '-60px', width: '250px', height: '250px', background: 'rgba(0,0,0,0.08)', borderRadius: '50%' }} />

        <div style={{ position: 'relative' }}>
          {/* Logo */}
          <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: '12px', textDecoration: 'none', marginBottom: '64px' }}>
            <div style={{ width: '40px', height: '40px', background: 'rgba(255,255,255,0.2)', border: '1px solid rgba(255,255,255,0.3)', borderRadius: '12px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <Bus style={{ width: '22px', height: '22px', color: 'white' }} />
            </div>
            <span style={{ color: 'white', fontSize: '20px', fontWeight: 700 }}>BusSetu</span>
          </Link>

          {/* Portal icon */}
          <div style={{ width: '64px', height: '64px', background: 'rgba(255,255,255,0.2)', border: '1px solid rgba(255,255,255,0.3)', borderRadius: '20px', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '24px' }}>
            {isDriver ? <Bus style={{ width: '32px', height: '32px', color: 'white' }} /> : <Shield style={{ width: '32px', height: '32px', color: 'white' }} />}
          </div>

          <h1 style={{ color: 'white', fontSize: '48px', fontWeight: 800, lineHeight: 1.1, marginBottom: '16px' }}>
            {isDriver ? 'Driver' : 'Admin'}<br />Portal
          </h1>
          <p style={{ color: 'rgba(255,255,255,0.7)', fontSize: '17px', lineHeight: 1.6, marginBottom: '36px', maxWidth: '340px' }}>
            {isDriver
              ? 'Manage your trips, broadcast live location, and serve commuters better.'
              : 'Full control over routes, buses, drivers, and real-time monitoring.'}
          </p>

          {/* Features list */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {features.map((f) => (
              <div key={f} style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <div style={{ width: '20px', height: '20px', background: 'rgba(255,255,255,0.25)', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                  <CheckCircle style={{ width: '12px', height: '12px', color: 'white' }} />
                </div>
                <span style={{ color: 'rgba(255,255,255,0.8)', fontSize: '14px' }}>{f}</span>
              </div>
            ))}
          </div>
        </div>

        <p style={{ color: 'rgba(255,255,255,0.35)', fontSize: '12px', position: 'relative' }}>© 2026 KMT BusSetu</p>
      </div>

      {/* RIGHT FORM PANEL */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
        {/* Mobile header */}
        <div className="lg:hidden" style={{ background: leftGradient, padding: '40px 24px', textAlign: 'center' }}>
          <Link to="/" style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', color: 'rgba(255,255,255,0.6)', fontSize: '14px', textDecoration: 'none', marginBottom: '24px' }}>
            <ArrowLeft style={{ width: '16px', height: '16px' }} />Back
          </Link>
          <div style={{ width: '56px', height: '56px', background: 'rgba(255,255,255,0.2)', borderRadius: '18px', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 16px', border: '1px solid rgba(255,255,255,0.3)' }}>
            {isDriver ? <Bus style={{ width: '28px', height: '28px', color: 'white' }} /> : <Shield style={{ width: '28px', height: '28px', color: 'white' }} />}
          </div>
          <h1 style={{ color: 'white', fontSize: '24px', fontWeight: 700 }}>Sign In</h1>
          <p style={{ color: 'rgba(255,255,255,0.6)', fontSize: '14px', marginTop: '4px', textTransform: 'capitalize' }}>{role} access portal</p>
        </div>

        {/* Form container — centered vertically and horizontally */}
        <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '48px 24px' }}>
          <div style={{ width: '100%', maxWidth: '440px' }}>
            {/* Desktop back link */}
            <Link to="/" className="hidden lg:inline-flex" style={{ alignItems: 'center', gap: '8px', color: '#94a3b8', fontSize: '14px', textDecoration: 'none', marginBottom: '32px', display: 'none' }}>
              <ArrowLeft style={{ width: '16px', height: '16px' }} />
              Back to Home
            </Link>
            <a href="/" className="hidden lg:inline-flex" style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#94a3b8', fontSize: '14px', textDecoration: 'none', marginBottom: '32px' }}>
              <ArrowLeft style={{ width: '16px', height: '16px' }} />
              Back to Home
            </a>

            <h2 style={{ fontSize: '28px', fontWeight: 800, color: '#1e293b', marginBottom: '6px' }}>
              Welcome {isDriver ? 'Captain' : 'Admin'}! 👋
            </h2>
            <p style={{ color: '#64748b', fontSize: '15px', marginBottom: '28px' }}>Enter your credentials to access the portal</p>

            {error && (
              <div style={{ display: 'flex', alignItems: 'flex-start', gap: '12px', background: '#fef2f2', color: '#dc2626', padding: '14px 16px', borderRadius: '12px', marginBottom: '20px', border: '1px solid #fecaca' }}>
                <AlertCircle style={{ width: '18px', height: '18px', flexShrink: 0, marginTop: '1px' }} />
                <p style={{ fontSize: '14px' }}>{error}</p>
              </div>
            )}

            <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#475569', marginBottom: '8px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Email Address</label>
                <div style={{ position: 'relative' }}>
                  <Mail style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)', width: '16px', height: '16px', color: '#94a3b8' }} />
                  <input
                    id="login-email"
                    name="email"
                    type="email"
                    placeholder="your@email.com"
                    value={form.email}
                    onChange={handleChange}
                    required
                    style={{ width: '100%', paddingLeft: '44px', paddingRight: '16px', paddingTop: '14px', paddingBottom: '14px', border: '1px solid #e2e8f0', borderRadius: '12px', background: 'white', color: '#1e293b', fontSize: '14px', outline: 'none', boxSizing: 'border-box', fontFamily: 'Inter, sans-serif', transition: 'border-color 0.2s' }}
                    onFocus={e => e.target.style.borderColor = '#2563eb'}
                    onBlur={e => e.target.style.borderColor = '#e2e8f0'}
                  />
                </div>
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#475569', marginBottom: '8px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Password</label>
                <div style={{ position: 'relative' }}>
                  <Lock style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)', width: '16px', height: '16px', color: '#94a3b8' }} />
                  <input
                    id="login-password"
                    name="password"
                    type={showPassword ? 'text' : 'password'}
                    placeholder="Enter your password"
                    value={form.password}
                    onChange={handleChange}
                    required
                    style={{ width: '100%', paddingLeft: '44px', paddingRight: '48px', paddingTop: '14px', paddingBottom: '14px', border: '1px solid #e2e8f0', borderRadius: '12px', background: 'white', color: '#1e293b', fontSize: '14px', outline: 'none', boxSizing: 'border-box', fontFamily: 'Inter, sans-serif', transition: 'border-color 0.2s' }}
                    onFocus={e => e.target.style.borderColor = '#2563eb'}
                    onBlur={e => e.target.style.borderColor = '#e2e8f0'}
                  />
                  <button type="button" onClick={() => setShowPassword(!showPassword)}
                    style={{ position: 'absolute', right: '14px', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', color: '#94a3b8', display: 'flex' }}>
                    {showPassword ? <EyeOff style={{ width: '16px', height: '16px' }} /> : <Eye style={{ width: '16px', height: '16px' }} />}
                  </button>
                </div>
              </div>

              <button id="login-submit" type="submit" disabled={loading}
                style={{ background: btnBg, color: 'white', fontWeight: 700, padding: '15px', borderRadius: '12px', border: 'none', cursor: loading ? 'not-allowed' : 'pointer', fontSize: '14px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', opacity: loading ? 0.7 : 1, boxShadow: '0 8px 25px rgba(37,99,235,0.3)', transition: 'all 0.2s', letterSpacing: '0.05em' }}>
                {loading ? <div style={{ width: '20px', height: '20px', border: '2px solid rgba(255,255,255,0.3)', borderTopColor: 'white', borderRadius: '50%', animation: 'spin 1s linear infinite' }} /> : 'LOGIN →'}
              </button>
            </form>

            {/* Demo credentials */}
            <div style={{ marginTop: '24px', padding: '16px', background: '#f8fafc', borderRadius: '14px', border: '1px solid #f1f5f9' }}>
              <p style={{ fontSize: '11px', fontWeight: 700, color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '0.08em', marginBottom: '12px' }}>Demo Credentials</p>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                {[{ role: 'Admin', email: 'admin@bussetu.com', pass: 'admin123' }, { role: 'Driver', email: 'driver@bussetu.com', pass: 'driver123' }].map(cred => (
                  <div key={cred.role} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '10px 14px', background: 'white', borderRadius: '10px', border: '1px solid #f1f5f9' }}>
                    <span style={{ fontWeight: 600, color: '#64748b', fontSize: '13px' }}>{cred.role}</span>
                    <span style={{ fontFamily: 'monospace', fontSize: '12px', color: '#475569' }}>{cred.email} / {cred.pass}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
