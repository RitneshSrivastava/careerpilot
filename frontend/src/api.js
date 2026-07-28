const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

function getToken() {
  return localStorage.getItem('careerpilot_token')
}

function setToken(token) {
  localStorage.setItem('careerpilot_token', token)
}

function clearToken() {
  localStorage.removeItem('careerpilot_token')
}

async function request(path, options = {}) {
  const headers = { ...(options.headers || {}) }
  const token = getToken()
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  const res = await fetch(`${BASE_URL}${path}`, { ...options, headers })

  if (res.status === 204) {
    return null
  }

  const contentType = res.headers.get('content-type') || ''
  const isJson = contentType.includes('application/json')
  const body = isJson ? await res.json() : await res.text()

  if (!res.ok) {
    const message = isJson ? (body.message || JSON.stringify(body)) : body
    throw new Error(message || `Request failed with status ${res.status}`)
  }

  return body
}

export const api = {
  register(fullName, email, password) {
    return request('/users/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ fullName, email, password }),
    })
  },

  verifyOtp(email, code) {
    return request('/users/verify', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, code }),
    })
  },

  resendOtp(email) {
    return request('/users/resend-otp', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email }),
    })
  },

  async login(email, password) {
    const data = await request('/users/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    })
    setToken(data.token)
    return data
  },

  logout() {
    clearToken()
  },

  isLoggedIn() {
    return !!getToken()
  },

  listResumes(page = 0, size = 10) {
    return request(`/resume?page=${page}&size=${size}&sort=uploadedAt,desc`, {
      method: 'GET',
    })
  },

  uploadResume(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request('/resume/upload', {
      method: 'POST',
      body: formData,
    })
  },

  deleteResume(id) {
    return request(`/resume/${id}`, { method: 'DELETE' })
  },

  downloadResumeUrl(id) {
    // Direct link; browser will send request without our JWT header if opened
    // in a new tab, so we fetch it as a blob instead - see Dashboard.jsx
    return `${BASE_URL}/resume/${id}/download`
  },

  async downloadResumeBlob(id) {
    const token = getToken()
    const res = await fetch(`${BASE_URL}/resume/${id}/download`, {
      headers: { Authorization: `Bearer ${token}` },
    })
    if (!res.ok) throw new Error('Download failed.')
    return res.blob()
  },

  triggerAnalysis(id) {
    return request(`/resume/${id}/analyze`, { method: 'POST' })
  },

  getAnalysis(id) {
    return request(`/resume/${id}/analysis`, { method: 'GET' })
  },

  getJobMatches(id) {
    return request(`/resume/${id}/job-matches`, { method: 'GET' })
  },
}
