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
