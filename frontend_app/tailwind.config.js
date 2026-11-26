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
          DEFAULT: '#7B1235',
          dark: '#5c0d28',
          light: '#f8f0f3',
        },
        gold: {
          DEFAULT: '#cba258',
          light: '#f5eddc',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        card: '0 1px 3px 0 rgba(0,0,0,0.06), 0 1px 2px -1px rgba(0,0,0,0.04)',
        'card-hover': '0 10px 40px -10px rgba(123, 18, 53, 0.15)',
      },
    },
  },
  plugins: [],
};
