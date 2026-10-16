import { Link, Outlet } from 'react-router-dom'

export default function Layout() {
  return (
    <div className="min-h-dvh">
      <header className="sticky top-0 z-10 border-b border-line/60 bg-ink/80 backdrop-blur">
        <div className="mx-auto flex h-14 max-w-5xl items-center justify-between px-4">
          <Link to="/" className="text-lg font-black tracking-tight">
            RUSH<span className="text-accent">.</span>
          </Link>
          <span className="text-xs text-muted">선착순 예매 실험실</span>
        </div>
      </header>
      <main className="mx-auto max-w-5xl px-4 py-8">
        <Outlet />
      </main>
    </div>
  )
}
