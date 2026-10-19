const KEY = 'rush.userId'

// 로그인은 아직 없으니 브라우저마다 임의의 사용자 ID를 하나 쓴다.
export function getUserId(): string {
  try {
    const saved = localStorage.getItem(KEY)
    if (saved) return saved
    const id = `guest-${crypto.randomUUID().slice(0, 8)}`
    localStorage.setItem(KEY, id)
    return id
  } catch {
    return `guest-${crypto.randomUUID().slice(0, 8)}`
  }
}

// 대기열 입장 토큰은 탭을 닫으면 사라지도록 sessionStorage에 둔다.
const queueKey = (concertId: number) => `rush.queue.${concertId}`

export function getQueueToken(concertId: number): string | null {
  try {
    return sessionStorage.getItem(queueKey(concertId))
  } catch {
    return null
  }
}

export function setQueueToken(concertId: number, token: string | null) {
  try {
    if (token) sessionStorage.setItem(queueKey(concertId), token)
    else sessionStorage.removeItem(queueKey(concertId))
  } catch {
    // 저장이 안 되면 새로고침 때 다시 줄을 서게 된다.
  }
}
