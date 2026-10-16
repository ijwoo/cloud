import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, type Concert } from '../api/client'
import { concertDate } from '../components/format'

export default function ConcertListPage() {
  const [concerts, setConcerts] = useState<Concert[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api.concerts().then(setConcerts).catch((e: Error) => setError(e.message))
  }, [])

  if (error) return <p className="text-accent">{error}</p>
  if (!concerts) return <p className="text-muted">불러오는 중…</p>

  return (
    <section>
      <h1 className="mb-6 text-2xl font-bold">지금 예매 중</h1>
      <ul className="grid gap-4 sm:grid-cols-2">
        {concerts.map((c) => {
          const soldRatio = 1 - c.availableSeats / c.totalSeats
          return (
            <li key={c.id}>
              <Link
                to={`/concerts/${c.id}`}
                className="group block overflow-hidden rounded-2xl border border-line bg-panel transition hover:border-accent/60"
              >
                <div className="relative h-40 bg-gradient-to-br from-accent via-fuchsia-600 to-indigo-700">
                  <div className="absolute inset-0 bg-[radial-gradient(circle_at_30%_20%,rgba(255,255,255,.25),transparent_50%)]" />
                  <p className="absolute bottom-4 left-4 text-3xl font-black tracking-tight drop-shadow">
                    {c.title}
                  </p>
                </div>
                <div className="space-y-3 p-4">
                  <div>
                    <p className="font-semibold">{c.artist}</p>
                    <p className="text-sm text-muted">
                      {c.venue} · {concertDate(c.startsAt)}
                    </p>
                  </div>
                  <div>
                    <div className="mb-1 flex justify-between text-xs text-muted">
                      <span>잔여 {c.availableSeats}석</span>
                      <span>{Math.round(soldRatio * 100)}% 판매</span>
                    </div>
                    <div className="h-1.5 overflow-hidden rounded-full bg-line">
                      <div
                        className="h-full rounded-full bg-accent transition-all"
                        style={{ width: `${soldRatio * 100}%` }}
                      />
                    </div>
                  </div>
                </div>
              </Link>
            </li>
          )
        })}
      </ul>
    </section>
  )
}
