import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useParams } from 'react-router-dom'
import { api, type Concert, type Seat } from '../api/client'
import { getUserId } from '../api/user'
import { concertDate, won } from '../components/format'

// 실시간 반영은 이후 단계에서 SSE로 바꾼다. 지금은 2초마다 다시 읽는다.
const POLL_MS = 2000

type Notice = { kind: 'ok' | 'error'; text: string }

export default function SeatMapPage() {
  const concertId = Number(useParams().concertId)
  const [concert, setConcert] = useState<Concert | null>(null)
  const [seats, setSeats] = useState<Seat[]>([])
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [notice, setNotice] = useState<Notice | null>(null)
  const selectedRef = useRef<number | null>(null)

  function select(id: number | null) {
    selectedRef.current = id
    setSelectedId(id)
  }

  const load = useCallback(async () => {
    const [c, s] = await Promise.all([api.concert(concertId), api.seats(concertId)])
    setConcert(c)
    setSeats(s)
    // 고른 좌석을 다른 사람이 먼저 가져가면 선택을 풀어준다.
    const taken = s.find((seat) => seat.id === selectedRef.current && seat.status !== 'AVAILABLE')
    if (taken) {
      select(null)
      setNotice({ kind: 'error', text: `${taken.rowLabel}열 ${taken.number}번은 방금 다른 분이 예매했어요.` })
    }
  }, [concertId])

  useEffect(() => {
    load().catch((e: Error) => setNotice({ kind: 'error', text: e.message }))
    const timer = setInterval(() => load().catch(() => {}), POLL_MS)
    return () => clearInterval(timer)
  }, [load])

  const rows = useMemo(() => {
    const byRow = new Map<string, Seat[]>()
    for (const seat of seats) byRow.set(seat.rowLabel, [...(byRow.get(seat.rowLabel) ?? []), seat])
    return [...byRow.entries()]
  }, [seats])

  const selected = seats.find((s) => s.id === selectedId) ?? null

  async function reserve() {
    if (!selected) return
    setSubmitting(true)
    try {
      await api.reserve(selected.id, getUserId())
      select(null)
      await load()
      setNotice({ kind: 'ok', text: `${selected.rowLabel}열 ${selected.number}번 예매 완료!` })
    } catch (e) {
      setNotice({ kind: 'error', text: (e as Error).message })
    } finally {
      setSubmitting(false)
    }
  }

  if (!concert) return <p className="text-muted">{notice?.text ?? '불러오는 중…'}</p>

  return (
    <section className="pb-32">
      <div className="mb-8">
        <p className="text-sm text-accent">{concert.artist}</p>
        <h1 className="text-3xl font-black tracking-tight">{concert.title}</h1>
        <p className="mt-1 text-sm text-muted">
          {concert.venue} · {concertDate(concert.startsAt)}
        </p>
      </div>

      <div className="rounded-2xl border border-line bg-panel p-4 sm:p-6">
        <div className="mx-auto mb-8 w-2/3 rounded-b-[50%] border-t-4 border-accent/80 pt-2 text-center text-xs tracking-[.4em] text-muted shadow-[0_-12px_40px_-8px] shadow-accent/40">
          STAGE
        </div>

        <div className="overflow-x-auto">
          <div className="mx-auto w-max space-y-1.5">
            {rows.map(([row, rowSeats]) => (
              <div key={row} className="flex items-center gap-1.5">
                <span className="w-5 text-center text-xs text-muted">{row}</span>
                {rowSeats.map((seat, i) => (
                  <SeatButton
                    key={seat.id}
                    seat={seat}
                    selected={seat.id === selectedId}
                    gap={i === rowSeats.length / 2 - 1}
                    onSelect={() => select(seat.id === selectedId ? null : seat.id)}
                  />
                ))}
              </div>
            ))}
          </div>
        </div>

        <div className="mt-6 flex flex-wrap justify-center gap-4 text-xs text-muted">
          <Legend className="bg-vip/80" label="VIP 165,000원" />
          <Legend className="bg-seat" label="R 132,000원" />
          <Legend className="bg-accent" label="선택" />
          <Legend className="border border-line" label="예매 완료" />
        </div>
      </div>

      {notice && (
        <p
          role="status"
          className={`mt-4 rounded-xl px-4 py-3 text-sm ${
            notice.kind === 'ok' ? 'bg-emerald-500/15 text-emerald-300' : 'bg-accent/15 text-accent'
          }`}
        >
          {notice.text}
        </p>
      )}

      <div className="fixed inset-x-0 bottom-0 border-t border-line bg-ink/90 backdrop-blur">
        <div className="mx-auto flex max-w-5xl items-center justify-between gap-4 px-4 py-4">
          <div className="text-sm">
            {selected ? (
              <>
                <p className="font-semibold">
                  {selected.section} · {selected.rowLabel}열 {selected.number}번
                </p>
                <p className="text-muted">{won(selected.price)}</p>
              </>
            ) : (
              <p className="text-muted">
                좌석을 선택해 주세요 · 잔여 {concert.availableSeats}/{concert.totalSeats}
              </p>
            )}
          </div>
          <button
            type="button"
            disabled={!selected || submitting}
            onClick={reserve}
            className="rounded-xl bg-accent px-6 py-3 font-semibold text-white transition enabled:hover:brightness-110 disabled:opacity-40"
          >
            {submitting ? '예매 중…' : '예매하기'}
          </button>
        </div>
      </div>
    </section>
  )
}

function SeatButton({
  seat,
  selected,
  gap,
  onSelect,
}: {
  seat: Seat
  selected: boolean
  gap: boolean
  onSelect: () => void
}) {
  const reserved = seat.status !== 'AVAILABLE'
  const color = reserved
    ? 'border border-line bg-transparent cursor-not-allowed'
    : selected
      ? 'bg-accent scale-110 shadow-lg shadow-accent/40'
      : seat.section === 'VIP'
        ? 'bg-vip/80 hover:bg-vip'
        : 'bg-seat hover:bg-zinc-400'

  return (
    <button
      type="button"
      disabled={reserved}
      onClick={onSelect}
      aria-label={`${seat.rowLabel}열 ${seat.number}번 ${reserved ? '예매 완료' : seat.section}`}
      aria-pressed={selected}
      className={`size-6 rounded-t-lg rounded-b-sm transition ${color} ${gap ? 'mr-4' : ''}`}
    />
  )
}

function Legend({ className, label }: { className: string; label: string }) {
  return (
    <span className="flex items-center gap-1.5">
      <span className={`size-3 rounded-sm ${className}`} />
      {label}
    </span>
  )
}
