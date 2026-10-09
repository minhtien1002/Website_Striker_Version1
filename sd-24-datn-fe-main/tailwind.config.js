// tailwind.config.js
module.exports = {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        'primary-red': '#B91C1C', // Màu đỏ từ logo
        'secondary-gray': '#4B5563', // Màu xám đậm từ logo
        'background-light': '#F3F4F6', // Màu nền sáng
        'text-dark': '#1F2937', // Màu chữ đậm
      },
    },
  },
  plugins: [],
};
