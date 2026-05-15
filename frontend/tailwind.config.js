/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: '#0069b4',
        'primary-strong': '#005daa',
        'primary-soft': '#e9f2ff',
        surface: '#f7f9fc',
        'surface-low': '#f2f4f7',
        'surface-card': '#ffffff',
        ink: '#191c1e',
        muted: '#64748b',
        outline: '#d8e0ea',
      },
      fontFamily: {
        headline: ['Manrope', 'Inter', 'system-ui', 'sans-serif'],
        body: ['Public Sans', 'Inter', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        ambient: '0 18px 45px rgba(0, 95, 174, 0.08)',
      },
    },
  },
  plugins: [],
};
