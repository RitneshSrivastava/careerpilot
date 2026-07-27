export default function ScoreGauge({ score }) {
  const size = 108
  const stroke = 8
  const radius = (size - stroke) / 2
  const circumference = 2 * Math.PI * radius
  const pct = Math.max(0, Math.min(100, score ?? 0))
  const offset = circumference - (pct / 100) * circumference

  let color = '#4FBF87'
  if (pct < 50) color = '#E2555A'
  else if (pct < 75) color = '#E8A33D'

  return (
    <div className="gauge-wrap">
      <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke="#2A303C"
          strokeWidth={stroke}
        />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke={color}
          strokeWidth={stroke}
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={offset}
          transform={`rotate(-90 ${size / 2} ${size / 2})`}
          style={{ transition: 'stroke-dashoffset 0.6s ease' }}
        />
        <text
          x="50%"
          y="47%"
          textAnchor="middle"
          dominantBaseline="middle"
          fill="#E8ECF2"
          fontFamily="'JetBrains Mono', monospace"
          fontSize="26"
          fontWeight="700"
        >
          {score ?? '—'}
        </text>
        <text
          x="50%"
          y="66%"
          textAnchor="middle"
          dominantBaseline="middle"
          fill="#8B94A6"
          fontFamily="'JetBrains Mono', monospace"
          fontSize="10"
          letterSpacing="1"
        >
          / 100
        </text>
      </svg>
      <div className="gauge-label">ATS Score</div>
    </div>
  )
}
