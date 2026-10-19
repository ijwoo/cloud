import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { api, type Concert, type QueueStatus } from '../api/client'
import { getQueueToken, setQueueToken } from '../api/user'

const POLL_MS = 1000

export default function WaitingRoomPage() {
  const concertId = Number(useParams().concertId)
  const navigate = useNavigate()
  const [concert, setConcert] = useState<Concert | null>(null)
  const [status, setStatus] = useState<QueueStatus | null>(null)
  const [error, setError] = useState<string | null>(null)
  // 처음 줄 섰을 때의 앞 인원. 진행 막대의 기준이 된다.
  const [startAhead, setStartAhead] = useState<number | null>(null)

  useEffect(() => {
    let cancelled = false
    let timer: ReturnType<typeof setInterval> | undefined

    async function start() {
      setConcert(await api.concert(concertId))
      let token = getQueueToken(concertId)
      if (!token) {
        token = (await api.enterQueue(concertId)).token
        setQueueToken(concertId, token)
      }
      const poll = async () => {
        const s = await api.queueStatus(concertId, token!)
        if (cancelled) return
        setStartAhead((prev) => prev ?? Math.max(s.ahead, 1))
        setStatus(s)
        if (s.state === 'ADMITTED') {
          clearInterval(timer)
          setTimeout(() => navigate(`/concerts/${concertId}`, { replace: true }), 900)
        } else if (s.state === 'EXPIRED') {
          setQueueToken(concertId, null)
          clearInterval(timer)
        }
      }
      await poll()
      timer = setInterval(() => poll().catch(() => {}), POLL_MS)
    }

    start().catch((e: Error) => setError(e.message))
    return () => {
      cancelled = true
      clearInterval(timer)
    }
  }, [concertId, navigate])

  const progress = status && startAhead ? Math.min(1, 1 - status.ahead / startAhead) : 0
  const etaSec = status ? Math.ceil(status.ahead / Math.max(status.admitPerSecond, 1)) : 0

  return (
    <section className="mx-auto flex max-w-md flex-col items-center py-10 text-center">
      <p className="text-sm text-accent">{concert?.artist ?? ' '}</p>
      <h1 className="text-2xl font-black tracking-tight">{concert?.title ?? '대기열'}</h1>

      <div className="mt-10 w-full rounded-3xl border border-line bg-panel p-8">
        {error && <p className="text-accent">{error}</p>}

        {status?.state === 'WAITING' && (
          <>
            <p className="text-sm text-muted">내 앞에</p>
            <p className="my-2 text-7xl font-black tabular-nums tracking-tight transition-all">
              {status.ahead.toLocaleString('ko-KR')}
              <span className="ml-1 text-3xl font-bold text-muted">명</span>
            </p>
            <div className="mt-6 h-2 overflow-hidden rounded-full bg-line">
              <div
                className="h-full rounded-full bg-gradient-to-r from-accent to-fuchsia-500 transition-[width] duration-700 ease-out"
                style={{ width: `${progress * 100}%` }}
              />
            </div>
            <div className="mt-3 flex justify-between text-xs text-muted">
              <span>예상 대기 {formatEta(etaSec)}</span>
              <span>전체 대기 {status.waiting.toLocaleString('ko-KR')}명</span>
            </div>
            <p className="mt-8 text-xs leading-relaxed text-muted">
              새로고침하거나 창을 닫으면 순서가 뒤로 밀릴 수 있어요.
              <br />
              1초마다 {status.admitPerSecond}명씩 입장합니다.
            </p>
          </>
        )}

        {status?.state === 'ADMITTED' && (
          <div className="py-6">
            <p className="text-5xl">🎟️</p>
            <p className="mt-4 text-xl font-bold text-emerald-400">입장 차례입니다!</p>
            <p className="mt-1 text-sm text-muted">좌석 선택 화면으로 이동합니다.</p>
          </div>
        )}

        {status?.state === 'EXPIRED' && (
          <div className="py-6">
            <p className="font-semibold">대기 순서가 만료됐어요.</p>
            <button
              type="button"
              onClick={() => window.location.reload()}
              className="mt-4 rounded-xl bg-accent px-5 py-2.5 font-semibold text-white"
            >
              다시 줄 서기
            </button>
          </div>
        )}

        {!status && !error && <p className="py-10 text-muted">대기열에 들어가는 중…</p>}
      </div>
    </section>
  )
}

function formatEta(sec: number) {
  if (sec < 60) return `${sec}초`
  return `${Math.floor(sec / 60)}분 ${sec % 60}초`
}
