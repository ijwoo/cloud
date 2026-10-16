import { useEffect, useState } from 'react'
import { api, type ExperimentResult, type LockStrategy, type Seat } from '../api/client'

const STRATEGIES: { key: LockStrategy; name: string; desc: string }[] = [
  { key: 'NONE', name: '락 없음', desc: '조회 → 확인 → 저장. 0단계 출발점' },
  { key: 'PESSIMISTIC', name: '비관적 락', desc: 'SELECT … FOR UPDATE로 행을 잠근다' },
  { key: 'OPTIMISTIC', name: '낙관적 락', desc: '읽은 버전과 같을 때만 UPDATE' },
  { key: 'CONDITIONAL_UPDATE', name: '조건부 UPDATE', desc: "WHERE status = 'AVAILABLE' 한 문장" },
]

const strategyName = (key: LockStrategy) => STRATEGIES.find((s) => s.key === key)?.name ?? key

export default function LabPage() {
  const [seat, setSeat] = useState<Seat | null>(null)
  const [strategy, setStrategy] = useState<LockStrategy>('NONE')
  const [users, setUsers] = useState(100)
  const [running, setRunning] = useState(false)
  const [result, setResult] = useState<ExperimentResult | null>(null)
  const [history, setHistory] = useState<ExperimentResult[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api
      .seats(1)
      .then((seats) => setSeat(seats[seats.length - 1]))
      .catch((e: Error) => setError(e.message))
  }, [])

  async function run() {
    if (!seat) return
    setRunning(true)
    setError(null)
    setResult(null)
    try {
      const r = await api.runExperiment(seat.id, strategy, users)
      setResult(r)
      setHistory((h) => [r, ...h].slice(0, 12))
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setRunning(false)
    }
  }

  return (
    <section className="space-y-6">
      <div>
        <p className="text-sm text-accent">1단계 · DB만으로 막기</p>
        <h1 className="text-3xl font-black tracking-tight">실험실</h1>
        <p className="mt-2 text-sm text-muted">
          좌석 하나{seat && ` (${seat.rowLabel}열 ${seat.number}번)`}에 여러 명이 <em>정확히 같은 순간</em> 예매를
          누르면 어떻게 되는지 서버에서 재현합니다. 정상이라면 예약은 1건이어야 합니다.
        </p>
      </div>

      <div className="grid gap-4 rounded-2xl border border-line bg-panel p-4 sm:p-6">
        <div className="grid gap-2 sm:grid-cols-4">
          {STRATEGIES.map((s) => (
            <button
              key={s.key}
              type="button"
              onClick={() => setStrategy(s.key)}
              aria-pressed={strategy === s.key}
              className={`rounded-xl border p-3 text-left transition ${
                strategy === s.key ? 'border-accent bg-accent/10' : 'border-line hover:border-zinc-500'
              }`}
            >
              <p className="font-semibold">{s.name}</p>
              <p className="mt-1 text-xs text-muted">{s.desc}</p>
            </button>
          ))}
        </div>

        <div className="flex flex-wrap items-center gap-4">
          <label className="flex flex-1 items-center gap-3 text-sm">
            <span className="shrink-0 text-muted">동시 접속</span>
            <input
              type="range"
              min={10}
              max={500}
              step={10}
              value={users}
              onChange={(e) => setUsers(Number(e.target.value))}
              className="w-full accent-accent"
            />
            <span className="w-14 shrink-0 text-right font-semibold tabular-nums">{users}명</span>
          </label>
          <button
            type="button"
            onClick={run}
            disabled={!seat || running}
            className="rounded-xl bg-accent px-6 py-3 font-semibold text-white transition enabled:hover:brightness-110 disabled:opacity-40"
          >
            {running ? '실험 중…' : `${users}명 동시 예매`}
          </button>
        </div>
      </div>

      {error && <p className="rounded-xl bg-accent/15 px-4 py-3 text-sm text-accent">{error}</p>}

      {result && <ResultPanel result={result} />}

      {history.length > 0 && (
        <div className="overflow-x-auto rounded-2xl border border-line bg-panel">
          <table className="w-full text-sm">
            <thead className="text-left text-xs text-muted">
              <tr className="border-b border-line">
                <th className="px-4 py-3 font-normal">전략</th>
                <th className="px-4 py-3 text-right font-normal">동시 접속</th>
                <th className="px-4 py-3 text-right font-normal">성공</th>
                <th className="px-4 py-3 text-right font-normal">거절</th>
                <th className="px-4 py-3 text-right font-normal">오류</th>
                <th className="px-4 py-3 text-right font-normal">저장된 예약</th>
                <th className="px-4 py-3 text-right font-normal">소요</th>
              </tr>
            </thead>
            <tbody className="tabular-nums">
              {history.map((r, i) => (
                <tr key={i} className="border-b border-line/50 last:border-0">
                  <td className="px-4 py-2.5">{strategyName(r.strategy)}</td>
                  <td className="px-4 py-2.5 text-right">{r.users}</td>
                  <td className="px-4 py-2.5 text-right">{r.succeeded}</td>
                  <td className="px-4 py-2.5 text-right text-muted">{r.rejected}</td>
                  <td className="px-4 py-2.5 text-right text-muted">{r.failed}</td>
                  <td
                    className={`px-4 py-2.5 text-right font-semibold ${r.reservations > 1 ? 'text-accent' : 'text-emerald-400'}`}
                  >
                    {r.reservations}건
                  </td>
                  <td className="px-4 py-2.5 text-right text-muted">{r.elapsedMillis}ms</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}

function ResultPanel({ result }: { result: ExperimentResult }) {
  const overbooked = result.reservations > 1
  const dots = [
    ...Array<string>(result.succeeded).fill('bg-accent'),
    ...Array<string>(result.failed).fill('bg-vip'),
    ...Array<string>(result.rejected).fill('bg-line'),
  ]

  return (
    <div className="grid gap-6 rounded-2xl border border-line bg-panel p-4 sm:grid-cols-[220px_1fr] sm:p-6">
      <div
        className={`flex flex-col justify-center rounded-xl p-5 ${overbooked ? 'bg-accent/15' : 'bg-emerald-500/10'}`}
      >
        <p className="text-xs text-muted">좌석 1개에 저장된 예약</p>
        <p className={`text-6xl font-black tabular-nums ${overbooked ? 'text-accent' : 'text-emerald-400'}`}>
          {result.reservations}
          <span className="text-2xl">건</span>
        </p>
        <p className="mt-2 text-sm">
          {overbooked ? `${result.reservations - 1}명에게 같은 좌석이 중복 판매됨` : '정상 — 한 명만 성공'}
        </p>
      </div>

      <div className="space-y-4">
        <div className="flex flex-wrap gap-x-5 gap-y-1 text-sm">
          <Stat color="bg-accent" label="성공" value={result.succeeded} />
          <Stat color="bg-line" label="거절" value={result.rejected} />
          <Stat color="bg-vip" label="오류" value={result.failed} />
          <span className="text-muted">소요 {result.elapsedMillis}ms</span>
        </div>
        <div className="flex flex-wrap gap-1" aria-label={`사용자 ${result.users}명 결과`}>
          {dots.map((color, i) => (
            <span
              key={i}
              className={`size-3 rounded-full ${color} animate-[pop_.35s_ease-out_both]`}
              style={{ animationDelay: `${Math.min(i * 6, 900)}ms` }}
            />
          ))}
        </div>
        <p className="text-xs text-muted">점 하나가 사용자 한 명입니다.</p>
      </div>
    </div>
  )
}

function Stat({ color, label, value }: { color: string; label: string; value: number }) {
  return (
    <span className="flex items-center gap-1.5">
      <span className={`size-2.5 rounded-full ${color}`} />
      {label} <b className="tabular-nums">{value}</b>
    </span>
  )
}
