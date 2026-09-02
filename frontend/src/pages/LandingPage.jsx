import { useNavigate } from 'react-router-dom';
import { Bus, MapPin, Shield, Navigation, Clock, BarChart3, ChevronRight, Zap } from 'lucide-react';

/* ── 3D Bus Scene ─────────────────────────────────────────────── */
const Bus3DScene = () => {
  // Floating particles with random positions & delays
  const particles = Array.from({ length: 8 }, (_, i) => ({
    left: `${15 + Math.random() * 70}%`,
    top: `${40 + Math.random() * 40}%`,
    delay: `${i * 0.5}s`,
    size: 3 + Math.random() * 3,
  }));

  return (
    <div className="bus-3d-scene" style={{ marginTop: '20px' }}>
      {/* Glow behind bus */}
      <div className="bus-3d-glow" />

      {/* Road ring (ellipse beneath the bus) */}
      <div className="bus-3d-road" />

      {/* Orbiting map pins */}
      {[0, 1, 2].map(i => (
        <div key={i} className="bus-3d-orbit">
          <div className="bus-3d-pin">
            <div className="bus-3d-pin-dot" />
          </div>
        </div>
      ))}

      {/* 3D Rotating Bus (cuboid) */}
      <div className="bus-3d-wrapper">
        {/* Front face with windows */}
        <div className="bus-3d-face bus-3d-front">
          {/* Windshield */}
          <div className="bus-3d-window" style={{ top: '12px', left: '10px', right: '10px', height: '32px', borderRadius: '6px 6px 4px 4px' }} />
          {/* Headlights */}
          <div style={{ position: 'absolute', bottom: '12px', left: '14px', width: '20px', height: '10px', background: 'rgba(252,211,77,0.6)', borderRadius: '3px', boxShadow: '0 0 10px rgba(252,211,77,0.4)' }} />
          <div style={{ position: 'absolute', bottom: '12px', right: '14px', width: '20px', height: '10px', background: 'rgba(252,211,77,0.6)', borderRadius: '3px', boxShadow: '0 0 10px rgba(252,211,77,0.4)' }} />
          {/* Route number badge */}
          <div style={{ position: 'absolute', bottom: '30px', left: '50%', transform: 'translateX(-50%)', background: 'rgba(255,255,255,0.25)', borderRadius: '4px', padding: '2px 10px', fontSize: '11px', fontWeight: 700, color: 'white', letterSpacing: '0.05em' }}>
            KMT
          </div>
        </div>
        {/* Back face */}
        <div className="bus-3d-face bus-3d-back">
          <div className="bus-3d-window" style={{ top: '12px', left: '20px', right: '20px', height: '28px' }} />
          <div style={{ position: 'absolute', bottom: '12px', left: '14px', width: '14px', height: '14px', background: 'rgba(239,68,68,0.6)', borderRadius: '3px', boxShadow: '0 0 8px rgba(239,68,68,0.4)' }} />
          <div style={{ position: 'absolute', bottom: '12px', right: '14px', width: '14px', height: '14px', background: 'rgba(239,68,68,0.6)', borderRadius: '3px', boxShadow: '0 0 8px rgba(239,68,68,0.4)' }} />
        </div>
        {/* Side faces with windows */}
        <div className="bus-3d-face bus-3d-left">
          <div className="bus-3d-window" style={{ top: '14px', left: '8px', width: '22px', height: '24px' }} />
          <div className="bus-3d-window" style={{ top: '14px', right: '8px', width: '22px', height: '24px' }} />
          {/* Wheels */}
          <div style={{ position: 'absolute', bottom: '6px', left: '8px', width: '18px', height: '18px', background: 'rgba(0,0,0,0.4)', borderRadius: '50%', border: '2px solid rgba(255,255,255,0.2)' }} />
          <div style={{ position: 'absolute', bottom: '6px', right: '8px', width: '18px', height: '18px', background: 'rgba(0,0,0,0.4)', borderRadius: '50%', border: '2px solid rgba(255,255,255,0.2)' }} />
        </div>
        <div className="bus-3d-face bus-3d-right">
          <div className="bus-3d-window" style={{ top: '14px', left: '8px', width: '22px', height: '24px' }} />
          <div className="bus-3d-window" style={{ top: '14px', right: '8px', width: '22px', height: '24px' }} />
          <div style={{ position: 'absolute', bottom: '6px', left: '8px', width: '18px', height: '18px', background: 'rgba(0,0,0,0.4)', borderRadius: '50%', border: '2px solid rgba(255,255,255,0.2)' }} />
          <div style={{ position: 'absolute', bottom: '6px', right: '8px', width: '18px', height: '18px', background: 'rgba(0,0,0,0.4)', borderRadius: '50%', border: '2px solid rgba(255,255,255,0.2)' }} />
        </div>
        {/* Top face */}
        <div className="bus-3d-face bus-3d-top">
          {/* AC unit */}
          <div style={{ position: 'absolute', top: '12px', left: '50%', transform: 'translateX(-50%)', width: '80px', height: '16px', background: 'rgba(255,255,255,0.15)', borderRadius: '4px', border: '1px solid rgba(255,255,255,0.2)' }} />
        </div>
        {/* Bottom face */}
        <div className="bus-3d-face bus-3d-bottom" />
      </div>

      {/* Floating particles */}
      {particles.map((p, i) => (
        <div
          key={i}
          className="bus-3d-particle"
          style={{
            left: p.left,
            top: p.top,
            animationDelay: p.delay,
            width: `${p.size}px`,
            height: `${p.size}px`,
          }}
        />
      ))}
    </div>
  );
};

/* ── Landing Page ─────────────────────────────────────────────── */
const LandingPage = () => {
  const navigate = useNavigate();

  const roles = [
    {
      id: 'citizen-btn',
      title: 'Citizen',
      desc: 'Track buses & view live routes in real-time',
      badge: 'No login required',
      badgeColor: '#16a34a',
      badgeBg: '#dcfce7',
      icon: MapPin,
      iconBg: '#2563eb',
      bg: '#f0f7ff',
      border: '#bfdbfe',
      hoverBorder: '#3b82f6',
      action: () => navigate('/citizen'),
    },
    {
      id: 'driver-btn',
      title: 'Driver',
      desc: 'Start trip & broadcast live GPS location',
      badge: 'Login required',
      badgeColor: '#93c5fd',
      badgeBg: 'rgba(255,255,255,0.2)',
      icon: Bus,
      iconBg: 'rgba(255,255,255,0.2)',
      bg: 'linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%)',
      border: 'transparent',
      textWhite: true,
      action: () => navigate('/login?role=driver'),
    },
    {
      id: 'admin-btn',
      title: 'Admin',
      desc: 'Manage routes, buses & driver accounts',
      badge: 'Login required',
      badgeColor: '#0f766e',
      badgeBg: '#ccfbf1',
      icon: Shield,
      iconBg: '#0d9488',
      bg: '#f0fdf9',
      border: '#99f6e4',
      hoverBorder: '#0d9488',
      action: () => navigate('/login?role=admin'),
    },
  ];

  return (
    <div style={{ minHeight: '100vh', background: '#f8fafc' }} className="flex flex-col lg:flex-row">

      {/* LEFT HERO PANEL */}
      <div style={{
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #1d4ed8 0%, #2563eb 50%, #0d9488 100%)',
        padding: '48px',
        position: 'relative',
        overflow: 'hidden',
      }} className="hidden lg:flex w-full lg:w-1/2 flex-col justify-between">

        <div style={{ position: 'relative' }}>
          {/* Logo */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '40px' }}>
            <div style={{ width: '48px', height: '48px', background: 'rgba(255,255,255,0.2)', border: '1px solid rgba(255,255,255,0.3)', borderRadius: '14px', display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: '0 8px 20px rgba(0,0,0,0.15)' }}>
              <Bus style={{ width: '26px', height: '26px', color: 'white' }} />
            </div>
            <div>
              <span style={{ color: 'white', fontSize: '22px', fontWeight: 800, letterSpacing: '-0.02em' }}>BusSetu</span>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginTop: '2px' }}>
                <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#34d399', boxShadow: '0 0 0 3px rgba(52,211,153,0.3)', display: 'inline-block' }} />
                <span style={{ color: '#6ee7b7', fontSize: '11px', fontWeight: 600, letterSpacing: '0.1em' }}>LIVE TRACKING</span>
              </div>
            </div>
          </div>

          {/* Headline */}
          <h1 style={{ color: 'white', fontSize: '48px', fontWeight: 900, lineHeight: 1.05, letterSpacing: '-0.03em', marginBottom: '16px' }}>
            KMT Live Bus<br />
            <span style={{ background: 'linear-gradient(90deg, #67e8f9 0%, #34d399 100%)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
              Tracking System
            </span>
          </h1>
          <p style={{ color: 'rgba(255,255,255,0.75)', fontSize: '16px', lineHeight: 1.65, maxWidth: '380px', marginBottom: '24px' }}>
            Real-time bus tracking for smarter commuting. Know exactly when your bus arrives before you leave home.
          </p>

          {/* Feature pills */}
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '10px', marginBottom: '24px' }}>
            {[
              { icon: Navigation, text: 'Live GPS' },
              { icon: Clock, text: 'ETA Prediction' },
              { icon: BarChart3, text: 'Route Analytics' },
            ].map(({ icon: Icon, text }) => (
              <div key={text} style={{ display: 'flex', alignItems: 'center', gap: '8px', background: 'rgba(255,255,255,0.12)', border: '1px solid rgba(255,255,255,0.2)', borderRadius: '100px', padding: '8px 18px', backdropFilter: 'blur(10px)' }}>
                <Icon style={{ width: '14px', height: '14px', color: '#67e8f9' }} />
                <span style={{ color: 'white', fontSize: '13px', fontWeight: 600 }}>{text}</span>
              </div>
            ))}
          </div>
        </div>

        {/* ── 3D Bus Scene ── */}
        <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '320px' }}>
          <Bus3DScene />
        </div>

        {/* Stats bar */}
        <div style={{ position: 'relative', background: 'rgba(255,255,255,0.1)', border: '1px solid rgba(255,255,255,0.2)', borderRadius: '20px', padding: '24px 32px', display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '16px', backdropFilter: 'blur(10px)' }}>
          {[{ value: '50+', label: 'Active Buses' }, { value: '200+', label: 'Daily Trips' }, { value: '99%', label: 'GPS Accuracy' }].map(stat => (
            <div key={stat.label} style={{ textAlign: 'center' }}>
              <p style={{ color: 'white', fontSize: '30px', fontWeight: 900, lineHeight: 1 }}>{stat.value}</p>
              <p style={{ color: 'rgba(255,255,255,0.6)', fontSize: '12px', marginTop: '4px', fontWeight: 500 }}>{stat.label}</p>
            </div>
          ))}
        </div>
      </div>

      {/* RIGHT ROLE SELECTOR */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minHeight: '100vh', background: 'white' }}>
        {/* Mobile header */}
        <div className="lg:hidden" style={{ background: 'linear-gradient(135deg, #1d4ed8 0%, #2563eb 50%, #0d9488 100%)', padding: '40px 24px 32px', textAlign: 'center' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '10px', marginBottom: '12px' }}>
            <div style={{ width: '42px', height: '42px', background: 'rgba(255,255,255,0.2)', border: '1px solid rgba(255,255,255,0.3)', borderRadius: '12px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <Bus style={{ width: '22px', height: '22px', color: 'white' }} />
            </div>
            <span style={{ color: 'white', fontSize: '20px', fontWeight: 800 }}>BusSetu</span>
          </div>
          <p style={{ color: 'rgba(255,255,255,0.7)', fontSize: '14px' }}>KMT Live Bus Tracking System</p>
        </div>

        {/* Role cards — vertically + horizontally centered */}
        <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '40px 32px' }}>
          <div style={{ width: '100%', maxWidth: '480px' }}>
            <h2 style={{ fontSize: '26px', fontWeight: 800, color: '#1e293b', marginBottom: '6px' }}>Welcome Back</h2>
            <p style={{ color: '#64748b', fontSize: '15px', marginBottom: '28px' }}>Select your role to access the system</p>

            {/* Role cards */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', marginBottom: '32px' }}>
              {roles.map((r) => {
                const Icon = r.icon;
                return (
                  <button key={r.id} id={r.id} onClick={r.action}
                    style={{
                      background: r.bg,
                      border: `1.5px solid ${r.border || 'transparent'}`,
                      borderRadius: '18px',
                      padding: '18px 20px',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '16px',
                      cursor: 'pointer',
                      textAlign: 'left',
                      transition: 'all 0.2s',
                      width: '100%',
                    }}
                    onMouseEnter={e => { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 8px 25px rgba(37,99,235,0.15)'; }}
                    onMouseLeave={e => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = 'none'; }}>
                    <div style={{ width: '52px', height: '52px', background: r.iconBg, borderRadius: '14px', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, border: r.textWhite ? '1px solid rgba(255,255,255,0.2)' : 'none' }}>
                      <Icon style={{ width: '26px', height: '26px', color: 'white' }} />
                    </div>
                    <div style={{ flex: 1 }}>
                      <p style={{ fontSize: '17px', fontWeight: 700, color: r.textWhite ? 'white' : '#1e293b', marginBottom: '3px' }}>{r.title}</p>
                      <p style={{ fontSize: '13px', color: r.textWhite ? 'rgba(255,255,255,0.7)' : '#64748b', marginBottom: '6px' }}>{r.desc}</p>
                      <span style={{ display: 'inline-block', background: r.badgeBg, color: r.badgeColor, fontSize: '11px', fontWeight: 700, padding: '3px 10px', borderRadius: '100px' }}>{r.badge}</span>
                    </div>
                    <ChevronRight style={{ width: '18px', height: '18px', color: r.textWhite ? 'rgba(255,255,255,0.5)' : '#cbd5e1', flexShrink: 0 }} />
                  </button>
                );
              })}
            </div>

            {/* Feature mini grid */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '12px', marginBottom: '24px' }}>
              {[
                { icon: MapPin, label: 'Live Map', color: '#2563eb', bg: '#eff6ff' },
                { icon: Clock, label: 'ETA Display', color: '#0d9488', bg: '#f0fdfa' },
                { icon: Zap, label: 'Real-time', color: '#7c3aed', bg: '#f5f3ff' },
              ].map(({ icon: Icon, label, color, bg }) => (
                <div key={label} style={{ background: '#f8fafc', border: '1px solid #f1f5f9', borderRadius: '14px', padding: '16px 12px', textAlign: 'center' }}>
                  <div style={{ width: '40px', height: '40px', background: bg, borderRadius: '12px', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 8px' }}>
                    <Icon style={{ width: '18px', height: '18px', color }} />
                  </div>
                  <p style={{ fontSize: '12px', fontWeight: 600, color: '#475569' }}>{label}</p>
                </div>
              ))}
            </div>

            <p style={{ textAlign: 'center', color: '#cbd5e1', fontSize: '12px' }}>© 2026 KMT BusSetu · All rights reserved</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LandingPage;
