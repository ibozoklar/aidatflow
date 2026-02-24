import { useEffect, useState } from 'react'
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

const API = 'http://localhost:8080/api/v1'

function App() {
  const [apartmentId] = useState(1)
  const [dues, setDues] = useState<Due[]>([])
  const [loading, setLoading] = useState(false)

  const [period, setPeriod] = useState('2026-03')
  const [amount, setAmount] = useState('1200')
  const [dueDate, setDueDate] = useState('2026-03-10')

  async function loadDues(id = apartmentId) {
    setLoading(true)
    try {
      const r = await fetch(`${API}/dues/apartment/${id}`)
      if (!r.ok) throw new Error('Aidatlar alınamadı')
      setDues(await r.json())
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadDues(apartmentId)
  }, [])

  async function onCreateDue(e: FormEvent) {
    e.preventDefault()
    const r = await fetch(`${API}/dues`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ apartmentId, period, amount: Number(amount), dueDate }),
    })
    if (!r.ok) {
      alert('Aidat oluşturulamadı')
      return
    }
    await loadDues(apartmentId)
  }

  return (
    <main style={{ maxWidth: 980, margin: '40px auto', fontFamily: 'Inter, system-ui, sans-serif' }}>
      <h1>AidatFlow • Revizyon 1</h1>

      <section style={{ border: '1px solid #ddd', borderRadius: 8, padding: 16, marginBottom: 20 }}>
        <h3>Yeni Aidat Oluştur</h3>
        <form onSubmit={onCreateDue} style={{ display: 'grid', gap: 10, gridTemplateColumns: 'repeat(4, 1fr)' }}>
          <input value={period} onChange={e => setPeriod(e.target.value)} placeholder='Dönem (2026-03)' />
          <input value={amount} onChange={e => setAmount(e.target.value)} placeholder='Tutar' />
          <input value={dueDate} onChange={e => setDueDate(e.target.value)} type='date' />
          <button type='submit'>Kaydet</button>
        </form>
      </section>

      <section style={{ border: '1px solid #ddd', borderRadius: 8, padding: 16 }}>
        <h3>Aidat Listesi (Apartment #{apartmentId})</h3>
        <button onClick={() => loadDues(apartmentId)} disabled={loading}>
          {loading ? 'Yükleniyor...' : 'Yenile'}
        </button>
        <table style={{ width: '100%', marginTop: 12, borderCollapse: 'collapse' }}>
          <thead>
            <tr>
              <th>ID</th><th>Dönem</th><th>Tutar</th><th>Vade</th><th>Durum</th>
            </tr>
          </thead>
          <tbody>
            {dues.map(d => (
              <tr key={d.id}>
                <td>{d.id}</td>
                <td>{d.period}</td>
                <td>{d.amount}</td>
                <td>{d.dueDate}</td>
                <td>{d.status}</td>
              </tr>
            ))}
            {dues.length === 0 && (
              <tr><td colSpan={5}>Kayıt yok</td></tr>
            )}
          </tbody>
        </table>
      </section>
    </main>
  )
}

export default App
