import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import ChatBot from './components/ChatBot';

// Pages
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import CitizenHomePage from './pages/CitizenHomePage';
import CitizenTrackPage from './pages/CitizenTrackPage';
import DriverDashboard from './pages/DriverDashboard';
import AdminDashboard from './pages/admin/AdminDashboard';
import AdminRoutes from './pages/admin/AdminRoutes';
import AdminStops from './pages/admin/AdminStops';
import AdminRouteBuilder from './pages/admin/AdminRouteBuilder';
import AdminBuses from './pages/admin/AdminBuses';
import AdminDrivers from './pages/admin/AdminDrivers';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Public */}
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/citizen" element={<CitizenHomePage />} />
          <Route path="/citizen/track" element={<CitizenTrackPage />} />

          {/* Driver routes */}
          <Route path="/driver" element={
            <ProtectedRoute role="driver">
              <DriverDashboard />
            </ProtectedRoute>
          } />

          {/* Admin routes */}
          <Route path="/admin" element={
            <ProtectedRoute role="admin">
              <AdminDashboard />
            </ProtectedRoute>
          } />
          <Route path="/admin/routes" element={
            <ProtectedRoute role="admin">
              <AdminRoutes />
            </ProtectedRoute>
          } />
          <Route path="/admin/route-builder" element={
            <ProtectedRoute role="admin">
              <AdminRouteBuilder />
            </ProtectedRoute>
          } />
          <Route path="/admin/stops" element={
            <ProtectedRoute role="admin">
              <AdminStops />
            </ProtectedRoute>
          } />
          <Route path="/admin/buses" element={
            <ProtectedRoute role="admin">
              <AdminBuses />
            </ProtectedRoute>
          } />
          <Route path="/admin/drivers" element={
            <ProtectedRoute role="admin">
              <AdminDrivers />
            </ProtectedRoute>
          } />
        </Routes>
        <ChatBot />
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
