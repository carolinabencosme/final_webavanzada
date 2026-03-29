/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#f9f2f3',
          100: '#f0dedf',
          200: '#ddc2c4',
          300: '#c49a9e',
          400: '#a86d73',
          500: '#8f4a52',
          600: '#722f37',
          700: '#5c262d',
          800: '#4a1f25',
          900: '#3a181d',
        },
        ink: {
          DEFAULT: '#121820',
          muted: '#4a5568',
          subtle: '#6b7280',
        },
        paper: {
          DEFAULT: '#f6f0e8',
          deep: '#ebe4d8',
          dark: '#dfd6c6',
        },
        gold: {
          DEFAULT: '#8b7355',
          light: '#c4a574',
        },
      },
      fontFamily: {
        serif: ['"Cormorant Garamond"', 'Georgia', 'Cambria', 'serif'],
        sans: ['"Source Sans 3"', 'system-ui', 'sans-serif'],
      },
      fontSize: {
        display: ['clamp(2.25rem,5vw,3.75rem)', { lineHeight: '1.08', letterSpacing: '-0.02em' }],
      },
      boxShadow: {
        book: '0 2px 8px rgba(18, 24, 32, 0.06), 0 12px 32px rgba(18, 24, 32, 0.08)',
        lift: '0 24px 48px rgba(18, 24, 32, 0.12)',
      },
    },
  },
  plugins: [],
}
