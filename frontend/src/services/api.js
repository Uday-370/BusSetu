import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

// Attach JWT token to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Auth
export const login = (data) => api.post('/auth/login', data);
export const register = (data) => api.post('/auth/register', data);
export const getMe = () => api.get('/auth/me');
export const getDrivers = () => api.get('/auth/drivers');

// Routes
export const getRoutes = () => api.get('/routes');
export const getRouteById = (id) => api.get(`/routes/${id}`);
export const createRoute = (data) => api.post('/routes/create', data);
export const deleteRoute = (id) => api.delete(`/routes/${id}`);
export const uploadRouteExcel = (formData) =>
  api.post('/routes/upload-excel', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
export const createRouteWithStops = (data) => api.post('/routes/create-with-stops', data);

// Stops
export const getAllStops = () => api.get('/stops');
export const getStopsByRoute = (routeId) => api.get(`/stops/route/${routeId}`);
export const addStop = (data) => api.post('/stops', data);
export const deleteStop = (id) => api.delete(`/stops/${id}`);

// Buses
export const getBuses = () => api.get('/buses');
export const addBus = (data) => api.post('/buses', data);
export const updateBusStatus = (id, status) => api.put(`/buses/${id}/status`, { status });
export const deleteBus = (id) => api.delete(`/buses/${id}`);

// Trips
export const startTrip = (data) => api.post('/trips/start', data);
export const endTrip = (id) => api.put(`/trips/${id}/end`);
export const getActiveTrips = () => api.get('/trips/active');
export const getTripsByRoute = (routeId) => api.get(`/trips/route/${routeId}`);
export const getMyActiveTrip = () => api.get('/trips/my-trip');

// Locations
export const updateLocation = (data) => api.post('/locations/update', data);
export const getLatestLocation = (tripId) => api.get(`/locations/trip/${tripId}/latest`);

// ChatBot
export const sendChatMessage = (data) => api.post('/chatbot', data);

export default api;
