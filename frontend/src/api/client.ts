export type SeatStatus = 'AVAILABLE' | 'RESERVED'

export interface Concert {
  id: number
  title: string
  artist: string
  venue: string
  startsAt: string
  totalSeats: number
  availableSeats: number
}

export interface Seat {
  id: number
  section: string
  rowLabel: string
  number: number
  price: number
  status: SeatStatus
}

export interface Reservation {
  id: number
  seatId: number
  userId: string
  createdAt: string
}

export type LockStrategy = 'NONE' | 'PESSIMISTIC' | 'OPTIMISTIC' | 'CONDITIONAL_UPDATE' | 'UNIQUE_KEY'

export type Scenario = 'HOT' | 'SPREAD'

export interface ExperimentResult {
  strategy: LockStrategy
  scenario: Scenario
  users: number
  targetSeats: number
  succeeded: number
  rejected: number
  failed: number
  reservations: number
  reservedSeats: number
  overbooked: number
  elapsedMillis: number
}

export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(path, {
    ...init,
    headers: { 'Content-Type': 'application/json', ...init?.headers },
  })
  if (!res.ok) {
    const body = await res.json().catch(() => null)
    throw new ApiError(res.status, body?.detail ?? `요청 실패 (${res.status})`)
  }
  return res.status === 204 ? (undefined as T) : res.json()
}

export const api = {
  concerts: () => request<Concert[]>('/api/concerts'),
  concert: (id: number) => request<Concert>(`/api/concerts/${id}`),
  seats: (concertId: number) => request<Seat[]>(`/api/concerts/${concertId}/seats`),
  reserve: (seatId: number, userId: string) =>
    request<Reservation>('/api/reservations', {
      method: 'POST',
      body: JSON.stringify({ seatId, userId }),
    }),
  runExperiment: (seatId: number, strategy: LockStrategy, scenario: Scenario, users: number) =>
    request<ExperimentResult>('/api/admin/experiments', {
      method: 'POST',
      body: JSON.stringify({ seatId, strategy, scenario, users }),
    }),
}
