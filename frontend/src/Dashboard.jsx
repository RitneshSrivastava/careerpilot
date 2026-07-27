import { useEffect, useRef, useState } from 'react'
import { api } from './api'
import ScoreGauge from './ScoreGauge'
import JobMatchList from './JobMatchList'

function formatBytes(bytes) {
  if (!bytes) return '0 KB'
  return `${Math.round(bytes / 1024)} KB`
}

function formatDate(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  return d.toLocaleString(undefined, {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export default function Dashboard() {
  const [resumes, setResumes] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [uploading, setUploading] = useState(false)
  const [dragging, setDragging] = useState(false)
  const [expandedId, setExpandedId] = useState(null)
  const [analyses, setAnalyses] = useState({}) // id -> analysis response
  const [jobMatches, setJobMatches] = useState({}) // id -> matches array
  const [matchesLoading, setMatchesLoading] = useState({}) // id -> bool
  const [showMatches, setShowMatches] = useState({}) // id -> bool
  const fileInputRef = useRef(null)
  const pollRefs = useRef({})

  useEffect(() => {
    loadResumes()
    return () => {
      Object.values(pollRefs.current).forEach(clearInterval)
    }
  }, [])

  async function loadResumes() {
    setLoading(true)
    setError('')
    try {
      const data = await api.listResumes()
      setResumes(data.content || [])
    } catch (err) {
      setError(err.message || 'Could not load your resumes.')
    } finally {
      setLoading(false)
    }
  }

  async function handleFile(file) {
    if (!file) return
    if (file.type !== 'application/pdf') {
      setError('Only PDF files are accepted.')
      return
    }
    setUploading(true)
    setError('')
    try {
      await api.uploadResume(file)
      await loadResumes()
    } catch (err) {
      setError(err.message || 'Upload failed.')
    } finally {
      setUploading(false)
    }
  }

  function handleDrop(e) {
    e.preventDefault()
    setDragging(false)
    handleFile(e.dataTransfer.files?.[0])
  }

  async function handleDelete(id) {
    try {
      await api.deleteResume(id)
      await loadResumes()
    } catch (err) {
      setError(err.message || 'Delete failed.')
    }
  }

  async function handleDownload(id, name) {
    try {
      const blob = await api.downloadResumeBlob(id)
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = name
      a.click()
      window.URL.revokeObjectURL(url)
    } catch (err) {
      setError(err.message || 'Download failed.')
    }
  }

  function pollAnalysis(id) {
    if (pollRefs.current[id]) clearInterval(pollRefs.current[id])
    pollRefs.current[id] = setInterval(async () => {
      try {
        const result = await api.getAnalysis(id)
        setAnalyses((prev) => ({ ...prev, [id]: result }))
        if (result.status !== 'PENDING') {
          clearInterval(pollRefs.current[id])
          delete pollRefs.current[id]
        }
      } catch (err) {
        clearInterval(pollRefs.current[id])
      }
    }, 3000)
  }

  async function handleAnalyze(id) {
    setExpandedId(id)
    try {
      const result = await api.triggerAnalysis(id)
      setAnalyses((prev) => ({ ...prev, [id]: result }))
      if (result.status === 'PENDING') {
        pollAnalysis(id)
      }
    } catch (err) {
      setError(err.message || 'Could not start analysis.')
    }
  }

  async function toggleExpand(id) {
    if (expandedId === id) {
      setExpandedId(null)
      return
    }
    setExpandedId(id)
    if (!analyses[id]) {
      try {
        const result = await api.getAnalysis(id)
        setAnalyses((prev) => ({ ...prev, [id]: result }))
        if (result.status === 'PENDING') pollAnalysis(id)
      } catch {
        // no analysis requested yet - fine, user can trigger one
      }
    }
  }

  async function handleToggleMatches(id) {
    const currentlyShowing = showMatches[id]
    setShowMatches((prev) => ({ ...prev, [id]: !currentlyShowing }))

    if (!currentlyShowing && !jobMatches[id]) {
      setMatchesLoading((prev) => ({ ...prev, [id]: true }))
      try {
        const result = await api.getJobMatches(id)
        setJobMatches((prev) => ({ ...prev, [id]: result }))
      } catch (err) {
        setError(err.message || 'Could not load job matches. Run AI analysis first.')
        setShowMatches((prev) => ({ ...prev, [id]: false }))
      } finally {
        setMatchesLoading((prev) => ({ ...prev, [id]: false }))
      }
    }
  }

  return (
    <div className="main">
      <div className="section-heading">
        <h2>Upload</h2>
      </div>

      <div
        className={`upload-zone ${dragging ? 'dragging' : ''}`}
        onDragOver={(e) => {
          e.preventDefault()
          setDragging(true)
        }}
        onDragLeave={() => setDragging(false)}
        onDrop={handleDrop}
      >
        <input
          ref={fileInputRef}
          type="file"
          accept="application/pdf"
          id="file-upload"
          onChange={(e) => handleFile(e.target.files?.[0])}
        />
        <label htmlFor="file-upload" className="upload-label">
          {uploading ? 'Uploading…' : 'Choose a PDF resume'}
        </label>
        <div className="upload-hint">or drag and drop it here — max 5MB</div>
      </div>

      {error && <div className="error-banner">{error}</div>}

      <div className="section-heading">
        <h2>Flight Log</h2>
      </div>

      {loading && <div className="empty-state">Loading your resumes…</div>}

      {!loading && resumes.length === 0 && (
        <div className="empty-state">
          No resumes logged yet. Upload one above to get started.
        </div>
      )}

      {resumes.map((resume) => {
        const analysis = analyses[resume.id]
        const isExpanded = expandedId === resume.id

        return (
          <div className="log-entry" key={resume.id}>
            <div className="log-entry-row">
              <div>
                <div className="log-entry-name">{resume.originalFileName}</div>
                <div className="log-entry-meta">
                  {formatBytes(resume.fileSizeBytes)} · uploaded {formatDate(resume.uploadedAt)}
                </div>
              </div>
              <div className="log-entry-actions">
                <button
                  className="btn-ghost"
                  onClick={() => handleDownload(resume.id, resume.originalFileName)}
                >
                  Download
                </button>
                <button
                  className="btn-ghost"
                  onClick={() => toggleExpand(resume.id)}
                >
                  {isExpanded ? 'Hide analysis' : 'View analysis'}
                </button>
                <button
                  className="btn-ghost"
                  onClick={() => handleToggleMatches(resume.id)}
                >
                  {showMatches[resume.id] ? 'Hide job matches' : 'Job matches'}
                </button>
                <button
                  className="btn-ghost danger"
                  onClick={() => handleDelete(resume.id)}
                >
                  Delete
                </button>
              </div>
            </div>

            {showMatches[resume.id] && (
              <div className="analysis-panel">
                {matchesLoading[resume.id] && (
                  <span className="status-pill">
                    <span className="status-dot pending"></span>
                    Scanning listings…
                  </span>
                )}
                {!matchesLoading[resume.id] && jobMatches[resume.id] && (
                  <JobMatchList matches={jobMatches[resume.id]} />
                )}
              </div>
            )}

            {isExpanded && (
              <div>
                {!analysis && (
                  <div className="analysis-panel">
                    <button className="btn-ghost" onClick={() => handleAnalyze(resume.id)}>
                      Run AI analysis
                    </button>
                  </div>
                )}

                {analysis && analysis.status === 'PENDING' && (
                  <div className="analysis-panel">
                    <span className="status-pill">
                      <span className="status-dot pending"></span>
                      Analyzing…
                    </span>
                  </div>
                )}

                {analysis && analysis.status === 'FAILED' && (
                  <div className="analysis-panel">
                    <div className="error-banner" style={{ margin: 0 }}>
                      {analysis.failureReason || 'Analysis failed.'}
                    </div>
                    <button className="btn-ghost" onClick={() => handleAnalyze(resume.id)}>
                      Retry
                    </button>
                  </div>
                )}

                {analysis && analysis.status === 'COMPLETED' && (
                  <div className="analysis-panel">
                    <ScoreGauge score={analysis.atsScore} />
                    <div className="analysis-body">
                      <p className="analysis-summary">{analysis.summary}</p>

                      {analysis.extractedSkills?.length > 0 && (
                        <div className="tag-row">
                          {analysis.extractedSkills.map((skill) => (
                            <span className="tag" key={skill}>{skill}</span>
                          ))}
                        </div>
                      )}

                      {analysis.suggestions?.length > 0 && (
                        <ul className="suggestion-list">
                          {analysis.suggestions.map((s, i) => (
                            <li key={i}>{s}</li>
                          ))}
                        </ul>
                      )}

                      <div style={{ marginTop: 14 }}>
                        <button className="btn-ghost" onClick={() => handleAnalyze(resume.id)}>
                          Re-run analysis
                        </button>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        )
      })}
    </div>
  )
}
