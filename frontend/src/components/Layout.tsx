import { Link, NavLink, Outlet } from 'react-router-dom'

const navClass = ({ isActive }: { isActive: boolean }) =>
  `rounded-lg px-3 py-1.5 text-sm transition ${isActive ? 'bg-line text-zinc-100' : 'text-muted hover:text-zinc-100'}`

export default function Layout() {
  return (
    <div className="min-h-dvh">
      <header className="sticky top-0 z-10 border-b border-line/60 bg-ink/80 backdrop-blur">
        <div className="mx-auto flex h-14 max-w-5xl items-center justify-between px-4">
          <Link to="/" className="text-lg font-black tracking-tight">
            RUSH<span className="text-accent">.</span>
          </Link>
          <nav className="flex gap-1">
            <NavLink to="/" end className={navClass}>
              예매
            </NavLink>
            <NavLink to="/lab" className={navClass}>
              실험실
            </NavLink>
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-5xl px-4 py-8">
        <Outlet />
      </main>
    </div>
  )
}
