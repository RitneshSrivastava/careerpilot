import { useState } from 'react'
import { useNavigate, useLocation, Link } from 'react-router-dom'
import { api } from './api'

export default function Verify() {
  const location = useLocation()
  const navigate = useNavigate()
  const [email, setEmail] = useState(location.state?.email || '')
  const [code, setCode] = useState('')
  const [error, setError] = useState('')
  const [info, setInfo] = useState('')
  const [loading, setLoading] = useState(false)
  const [resending, setResending] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setInfo('')
    setLoading(true)
    try {
      await api.verifyOtp(email, code)
      navigate('/login', { state: { justVerified: true } })
    } catch (err) {
      setError(err.message || 'Verification failed.')
    } finally {
      setLoading(false)
    }
  }

  async function handleResend() {
    setError('')
    setInfo('')
    setResending(true)
    try {
      await api.resendOtp(email)
      setInfo('A new code has been sent to your email.')
    } catch (err) {
      setError(err.message || 'Could not resend code.')
    } finally {
      setResending(false)
    }
  }

  return (
    <div className="auth-wrap">
      <div className="auth-card">
        <h1 className="auth-title">Verify your email</h1>
        <p className="auth-sub">
          Enter the 6-digit code sent to your inbox to activate your account.
        </p>

        {error && <div className="error-banner">{error}</div>}
        {info && <div className="info-banner">{info}</div>}

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoComplete="email"
            />
          </div>
          <div className="field">
            <label htmlFor="code">Verification code</label>
            <input
              id="code"
              type="text"
              inputMode="numeric"
              pattern="[0-9]{6}"
              maxLength={6}
              value={code}
              onChange={(e) => setCode(e.target.value.replace(/\D/g, ''))}
              required
              placeholder="123456"
            />
          </div>
          <button className="btn-primary" type="submit" disabled={loading}>
            {loading ? 'Verifying…' : 'Verify account'}
          </button>
        </form>

        <div className="auth-switch">
          Didn't get a code?{' '}
          <button
            className="link-btn"
            onClick={handleResend}
            disabled={resending || !email}
          >
            {resending ? 'Sending…' : 'Resend code'}
          </button>
        </div>

        <div className="auth-switch">
          <Link to="/login">Back to sign in</Link>
        </div>
      </div>
    </div>
  )
}
