import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import './App.css'

type Due = {
  id: number
  apartmentId: number
  period: string
  amount: number
  dueDate: string
  status: 'UNPAID' | 'PARTIAL' | 'PAID'
}

type Summary = {
  totalDueAmount: number
  totalCollected: number
  totalOutstanding: number
  dueCount: number
  paidCount: number
  unpaidCount: number
}

type Payment = {
  id: number
  dueId: number
  amount: number
  method: 'CASH' | 'BANK_TRANSFER' | 'CREDIT_CARD'
  note: string
  paidAt: string
}

const API = 'http://127.0.0.1:8080/api/v1'

function App() {
  const [apartmentId] = useState(1)
  const [dues, setDues] = useState<Due[]>([])
  const [summary, setSummary] = useState<Summary | null>(null)
  const [payments, setPayments] = useState<Payment[]>([])
  const [loading, setLoading] = useState(false)

  const [email, setEmail] = useState('ihsan@example.com')
  const [password, setPassword] = useState('123456')
  const [token, setToken] = useState(localStorage.getItem('aidatflow_token') || '')

  const authHeaders: Record<string, string> = token ? { Authorization: `Bearer ${token}` } : {}

  const [period, setPeriod] = useState('2026-03')
  const [amount, setAmount] = useState('1200')
  const [dueDate, setDueDate] = useState('2026-03-10')

  const [paymentDueId, setPaymentDueId] = useState<number | null>(null)
  const [paymentAmount, setPaymentAmount] = useState('')
  const [paymentMethod, setPaymentMethod] = useState<'CASH' | 'BANK_TRANSFER' | 'CREDIT_CARD'>('BANK_TRANSFER')

  async function loadDuesAndSummary(id = apartmentId) {
    setLoading(true)
    try {
      const [duesRes, summaryRes] = await Promise.all([
        fetch(`${API}/dues/apartment/${id}`, { headers: authHeaders }),
        fetch(`${API}/dashboard/summary?apartmentId=${id}`, { headers: authHeaders }),
      ])
      if (!duesRes.ok) throw new Error('Aidatlar alınamadı')
      if (!summaryRes.ok) throw new Error('Özet alınamadı')
      const dueData: Due[] = await duesRes.json()
      setDues(dueData)
      setSummary(await summaryRes.json())
      if (dueData.length > 0) {
        const firstDueId = dueData[0].id
        setPaymentDueId((p: number | null) => p ?? firstDueId)
        await loadPayments((p: number | null) => p ?? firstDueId, dueData)
      }
    } finally {
      setLoading(false)
    }
  }

  async function loadPayments(dueIdOrFn?: any, list = dues) {
    const chosen = typeof dueIdOrFn === 'function' ? dueIdOrFn(paymentDueId) : dueIdOrFn
    const dueId = chosen ?? list[0]?.id
    if (!dueId) {
      setPayments([])
      return
    }
    const res = await fetch(`${API}/dues/${dueId}/payments`, { headers: authHeaders })
    if (!res.ok) return
    setPayments(await res.json())
  }

  useEffect(() => {
    loadDuesAndSummary(apartmentId)
  }, [])

  async function onRegister(e: FormEvent) {
    e.preventDefault()
    const res = await fetch(`${API}/auth/register`, {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ fullName: 'Ihsan', email, password }),
    })
    if (!res.ok) {
      alert('Register başarısız (muhtemelen kullanıcı var). Login dene.')
      return
    }
    const data = await res.json()
    localStorage.setItem('aidatflow_token', data.accessToken)
    setToken(data.accessToken)
  }

  async function onLogin(e: FormEvent) {
    e.preventDefault()
    const res = await fetch(`${API}/auth/login`, {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    })
    if (!res.ok) {
      alert('Login başarısız')
      return
    }
    const data = await res.json()
    localStorage.setItem('aidatflow_token', data.accessToken)
    setToken(data.accessToken)
  }

  async function onCreateDue(e: FormEvent) {
    e.preventDefault()
    const r = await fetch(`${API}/dues`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...authHeaders },
      body: JSON.stringify({ apartmentId, period, amount: Number(amount), dueDate }),
    })
    if (!r.ok) return alert('Aidat oluşturulamadı')
    await loadDuesAndSummary(apartmentId)
  }

  async function onCreatePayment(e: FormEvent) {
    e.preventDefault()
    if (!paymentDueId) return
    const r = await fetch(`${API}/dues/${paymentDueId}/payments`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...authHeaders },
      body: JSON.stringify({ amount: Number(paymentAmount), method: paymentMethod, note: 'UI ödeme' }),
    })
    if (!r.ok) return alert('Ödeme kaydedilemedi')
    setPaymentAmount('')
    await loadDuesAndSummary(apartmentId)
    await loadPayments(paymentDueId)
  }

  const dueOptions = useMemo(() => dues.filter(d => d.status !== 'PAID'), [dues])

  function statusColor(status: Due['status']) {
    if (status === 'PAID') return '#16a34a'
    if (status === 'PARTIAL') return '#ca8a04'
    return '#dc2626'
  }

  return (
    <main style={{ maxWidth: 1050, margin: '30px auto', fontFamily: 'Inter, system-ui, sans-serif' }}>
      <h1>AidatFlow • Demo</h1>

      <section style={{ border: '1px solid #ddd', borderRadius: 8, padding: 12, marginBottom: 12 }}>
        <h3>Auth (JWT Demo)</h3>
        <form onSubmit={onLogin} style={{ display: 'grid', gridTemplateColumns: '2fr 1fr 1fr 1fr', gap: 8 }}>
          <input value={email} onChange={e => setEmail(e.target.value)} placeholder='email' />
          <input value={password} onChange={e => setPassword(e.target.value)} placeholder='password' type='password' />
          <button type='button' onClick={(e) => onRegister(e as any)}>Register</button>
          <button type='submit'>Login</button>
        </form>
        <small>Token: {token ? `${token.slice(0, 25)}...` : 'yok'}</small>
      </section>

      {!token && <div style={{padding:10,background:'#fff3cd',border:'1px solid #ffe69c',borderRadius:8,marginBottom:12}}>Önce login olmalısın. API endpointleri artık JWT korumalı.</div>}

      <section style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 12, marginBottom: 18 }}>
        <Card title='Toplam Borç' value={summary?.totalDueAmount ?? 0} />
        <Card title='Toplam Tahsilat' value={summary?.totalCollected ?? 0} />
        <Card title='Kalan' value={summary?.totalOutstanding ?? 0} />
      </section>

      <section style={{ border: '1px solid #ddd', borderRadius: 8, padding: 16, marginBottom: 16 }}>
        <h3>Yeni Aidat Oluştur</h3>
        <form onSubmit={onCreateDue} style={{ display: 'grid', gap: 10, gridTemplateColumns: 'repeat(4, 1fr)' }}>
          <input value={period} onChange={e => setPeriod(e.target.value)} placeholder='Dönem (2026-03)' />
          <input value={amount} onChange={e => setAmount(e.target.value)} placeholder='Tutar' />
          <input value={dueDate} onChange={e => setDueDate(e.target.value)} type='date' />
          <button type='submit'>Kaydet</button>
        </form>
      </section>

      <section style={{ border: '1px solid #ddd', borderRadius: 8, padding: 16, marginBottom: 16 }}>
        <h3>Ödeme Ekle</h3>
        <form onSubmit={onCreatePayment} style={{ display: 'grid', gap: 10, gridTemplateColumns: '2fr 1fr 1fr 1fr' }}>
          <select value={paymentDueId ?? ''} onChange={e => { const id = Number(e.target.value); setPaymentDueId(id); loadPayments(id) }}>
            <option value=''>Aidat seç</option>
            {dueOptions.map(d => <option key={d.id} value={d.id}>#{d.id} • {d.period} • {d.status}</option>)}
          </select>
          <input value={paymentAmount} onChange={e => setPaymentAmount(e.target.value)} placeholder='Ödeme tutarı' />
          <select value={paymentMethod} onChange={e => setPaymentMethod(e.target.value as any)}>
            <option value='BANK_TRANSFER'>Havale</option>
            <option value='CASH'>Nakit</option>
            <option value='CREDIT_CARD'>Kart</option>
          </select>
          <button type='submit'>Ödeme Kaydet</button>
        </form>
      </section>

      <section style={{ border: '1px solid #ddd', borderRadius: 8, padding: 16, marginBottom: 16 }}>
        <h3>Ödeme Geçmişi {paymentDueId ? `(Aidat #${paymentDueId})` : ''}</h3>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead><tr><th>ID</th><th>Tutar</th><th>Yöntem</th><th>Tarih</th></tr></thead>
          <tbody>
            {payments.map(p => <tr key={p.id}><td>{p.id}</td><td>{p.amount}</td><td>{p.method}</td><td>{p.paidAt}</td></tr>)}
            {payments.length === 0 && <tr><td colSpan={4}>Ödeme yok</td></tr>}
          </tbody>
        </table>
      </section>

      <section style={{ border: '1px solid #ddd', borderRadius: 8, padding: 16 }}>
        <h3>Aidat Listesi (Apartment #{apartmentId})</h3>
        <button onClick={() => loadDuesAndSummary(apartmentId)} disabled={loading}>{loading ? 'Yükleniyor...' : 'Yenile'}</button>
        <table style={{ width: '100%', marginTop: 12, borderCollapse: 'collapse' }}>
          <thead><tr><th>ID</th><th>Dönem</th><th>Tutar</th><th>Vade</th><th>Durum</th></tr></thead>
          <tbody>
            {dues.map(d => <tr key={d.id}><td>{d.id}</td><td>{d.period}</td><td>{d.amount}</td><td>{d.dueDate}</td><td style={{ color: statusColor(d.status), fontWeight: 700 }}>{d.status}</td></tr>)}
            {dues.length === 0 && <tr><td colSpan={5}>Kayıt yok</td></tr>}
          </tbody>
        </table>
      </section>
    </main>
  )
}

function Card({ title, value }: { title: string; value: number }) {
  return <div style={{ border: '1px solid #ddd', borderRadius: 8, padding: 12 }}><div style={{ fontSize: 13, opacity: .7 }}>{title}</div><div style={{ fontSize: 28, fontWeight: 700 }}>{value.toLocaleString('tr-TR')} ₺</div></div>
}

export default App
