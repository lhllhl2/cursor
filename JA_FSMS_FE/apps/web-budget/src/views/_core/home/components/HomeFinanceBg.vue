<script setup lang="ts"></script>

<template>
  <div class="finance-bg-svg" aria-hidden="true">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 1200 600"
      class="finance-svg"
    >
      <defs>
        <linearGradient id="financeGrad1" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#1890ff" stop-opacity="0.015" />
          <stop offset="100%" stop-color="#52c41a" stop-opacity="0.01" />
        </linearGradient>
      </defs>
      <rect width="100%" height="100%" fill="url(#financeGrad1)" />
      <g class="chart-bars">
        <rect x="100" y="400" width="40" height="80" rx="4" fill="#1890ff" opacity="0.08" class="bar bar-1" />
        <rect x="180" y="350" width="40" height="130" rx="4" fill="#52c41a" opacity="0.08" class="bar bar-2" />
        <rect x="260" y="300" width="40" height="180" rx="4" fill="#1890ff" opacity="0.08" class="bar bar-3" />
        <rect x="340" y="250" width="40" height="230" rx="4" fill="#52c41a" opacity="0.08" class="bar bar-4" />
        <rect x="420" y="200" width="40" height="280" rx="4" fill="#1890ff" opacity="0.08" class="bar bar-5" />
        <rect x="900" y="380" width="40" height="100" rx="4" fill="#52c41a" opacity="0.06" class="bar bar-6" />
        <rect x="980" y="320" width="40" height="160" rx="4" fill="#1890ff" opacity="0.06" class="bar bar-7" />
        <rect x="1060" y="280" width="40" height="200" rx="4" fill="#52c41a" opacity="0.06" class="bar bar-8" />
      </g>
      <g class="floating-coins">
        <circle cx="150" cy="150" r="30" fill="#1890ff" opacity="0.04" class="coin coin-1" />
        <circle cx="1050" cy="120" r="25" fill="#52c41a" opacity="0.04" class="coin coin-2" />
        <circle cx="600" cy="80" r="20" fill="#1890ff" opacity="0.03" class="coin coin-3" />
        <circle cx="300" cy="500" r="35" fill="#52c41a" opacity="0.03" class="coin coin-4" />
        <circle cx="950" cy="450" r="28" fill="#1890ff" opacity="0.035" class="coin coin-5" />
      </g>
      <path
        d="M 50 450 Q 200 400 350 350 T 550 250 T 750 200 T 950 180 T 1150 150"
        fill="none"
        stroke="#1890ff"
        stroke-width="2"
        stroke-opacity="0.05"
        stroke-dasharray="8 12"
        class="trend-line"
      />
      <path
        d="M 80 500 Q 250 420 450 380 T 700 320 T 1000 280 T 1180 250"
        fill="none"
        stroke="#52c41a"
        stroke-width="2"
        stroke-opacity="0.04"
        stroke-dasharray="6 10"
        class="trend-line-2"
      />
      <text x="200" y="200" class="percent-symbol symbol-1">%</text>
      <text x="1000" y="350" class="percent-symbol symbol-2">%</text>
    </svg>
  </div>
</template>

<style scoped lang="scss">
.finance-bg-svg {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;

  .finance-svg {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .bar {
    transform-origin: bottom;
    animation: barPulse 3s ease-in-out infinite;

    &.bar-1 { animation-delay: 0s; }
    &.bar-2 { animation-delay: 0.2s; }
    &.bar-3 { animation-delay: 0.4s; }
    &.bar-4 { animation-delay: 0.6s; }
    &.bar-5 { animation-delay: 0.8s; }
    &.bar-6 { animation-delay: 0.3s; }
    &.bar-7 { animation-delay: 0.5s; }
    &.bar-8 { animation-delay: 0.7s; }
  }

  .coin {
    animation: floatCoin 6s ease-in-out infinite;

    &.coin-1 { animation-delay: 0s; }
    &.coin-2 { animation-delay: 1s; }
    &.coin-3 { animation-delay: 2s; }
    &.coin-4 { animation-delay: 0.5s; }
    &.coin-5 { animation-delay: 1.5s; }
  }

  .trend-line {
    animation: dashFlow 8s linear infinite;
  }

  .trend-line-2 {
    animation: dashFlow 10s linear infinite reverse;
  }

  .percent-symbol {
    font-size: 48px;
    font-weight: 300;
    fill: #1890ff;
    opacity: 0.03;
    animation: symbolPulse 4s ease-in-out infinite;
  }

  .symbol-2 {
    fill: #52c41a;
    animation-delay: 2s;
  }
}

@keyframes barPulse {
  0%, 100% { transform: scaleY(1); }
  50% { transform: scaleY(1.06); }
}

@keyframes floatCoin {
  0%, 100% { transform: translate(0, 0); }
  25% { transform: translate(8px, -12px); }
  50% { transform: translate(-5px, -8px); }
  75% { transform: translate(10px, -15px); }
}

@keyframes dashFlow {
  0% { stroke-dashoffset: 0; }
  100% { stroke-dashoffset: -20; }
}

@keyframes symbolPulse {
  0%, 100% { opacity: 0.02; }
  50% { opacity: 0.05; }
}
</style>
