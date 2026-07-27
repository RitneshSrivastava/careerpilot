function scoreColor(score) {
  if (score >= 75) return '#4FBF87'
  if (score >= 50) return '#E8A33D'
  return '#E2555A'
}

export default function JobMatchList({ matches }) {
  if (!matches || matches.length === 0) {
    return <div className="empty-state">No job listings to compare against yet.</div>
  }

  return (
    <div className="match-list">
      {matches.map((job) => (
        <div className="match-card" key={job.jobId}>
          <div className="match-card-top">
            <div>
              <div className="match-title">{job.title}</div>
              <div className="match-company">{job.company} · {job.location}</div>
            </div>
            <div className="match-percent" style={{ color: scoreColor(job.matchScore) }}>
              {job.matchScore}%
            </div>
          </div>

          <div className="match-bar-track">
            <div
              className="match-bar-fill"
              style={{
                width: `${job.matchScore}%`,
                background: scoreColor(job.matchScore),
              }}
            />
          </div>

          {job.matchedSkills?.length > 0 && (
            <div className="tag-row" style={{ marginTop: 10 }}>
              {job.matchedSkills.map((s) => (
                <span className="tag tag-match" key={`m-${s}`}>{s}</span>
              ))}
            </div>
          )}

          {job.missingSkills?.length > 0 && (
            <div className="tag-row" style={{ marginTop: 6 }}>
              {job.missingSkills.map((s) => (
                <span className="tag tag-missing" key={`g-${s}`}>{s}</span>
              ))}
            </div>
          )}
        </div>
      ))}
    </div>
  )
}
