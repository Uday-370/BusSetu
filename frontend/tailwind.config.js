/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50:  '#eff6ff',
          100: '#dbeafe',
          200: '#bfdbfe',
          300: '#93c5fd',
          400: '#60a5fa',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8',
          800: '#1e40af',
          900: '#1e3a8a',
        },
        teal: {
          50:  '#f0fdfa',
          100: '#ccfbf1',
          400: '#2dd4bf',
          500: '#14b8a6',
          600: '#0d9488',
          700: '#0f766e',
        },
        bus: {
          blue:  '#2563EB',
          teal:  '#0D9488',
          dark:  '#1E3A5F',
          light: '#F0F4FF',
        }
      },
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui'],
      },
      animation: {
        'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'bounce-slow': 'bounce 2s infinite',
        'spin-slow': 'spin 3s linear infinite',
      },
      backgroundImage: {
        'bus-gradient': 'linear-gradient(135deg, #2563EB 0%, #1d4ed8 50%, #0d9488 100%)',
        'card-gradient': 'linear-gradient(135deg, rgba(37,99,235,0.1) 0%, rgba(13,148,136,0.1) 100%)',
      },
    },
  },
  plugins: [],
}
