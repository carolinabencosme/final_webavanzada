/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#f0fdfa',
          100: '#ccfbf1',
          200: '#99f6e4',
          300: '#5eead4',
          400: '#2dd4bf',
          500: '#14b8a6',
          600: '#0d9488',
          700: '#0f766e',
          800: '#115e59',
          900: '#134e4a',
        },
        ink: {
          DEFAULT: '#1c1917',
          muted: '#57534e',
          subtle: '#78716c',
        },
        paper: {
          DEFAULT: '#faf7f2',
          deep: '#f0ebe3',
          dark: '#e7dfd4',
        },
        gold: {
          DEFAULT: '#ea580c',
          light: '#fb923c',
        },
        surface: {
          DEFAULT: '#ffffff',
          muted: '#f5f5f4',
        },
      },
      fontFamily: {
        serif: ['"Fraunces"', 'Georgia', 'serif'],
        sans: ['"Plus Jakarta Sans"', 'system-ui', 'sans-serif'],
      },
      fontSize: {
        display: ['clamp(2.35rem,5.5vw,3.85rem)', { lineHeight: '1.06', letterSpacing: '-0.025em' }],
      },
      boxShadow: {
        book: '0 4px 6px -1px rgb(28 25 23 / 0.06), 0 16px 40px -12px rgb(13 148 136 / 0.12)',
        lift: '0 20px 50px -12px rgb(28 25 23 / 0.18)',
        glow: '0 0 0 1px rgb(13 148 136 / 0.08), 0 12px 40px -8px rgb(13 148 136 / 0.2)',
      },
      borderRadius: {
        '2xl': '1rem',
        '3xl': '1.35rem',
      },
      backgroundImage: {
        'mesh-light':
          'radial-gradient(at 40% 20%, rgb(204 251 241 / 0.5) 0px, transparent 50%), radial-gradient(at 80% 0%, rgb(254 215 170 / 0.35) 0px, transparent 45%), radial-gradient(at 0% 50%, rgb(167 243 208 / 0.25) 0px, transparent 50%)',
        'mesh-hero':
          'linear-gradient(135deg, rgb(15 118 110) 0%, rgb(13 148 136) 40%, rgb(15 118 110) 100%)',
      },
    },
  },
  plugins: [],
}
