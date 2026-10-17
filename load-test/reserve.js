// 예매 러시 부하 테스트.
// 같은 양의 요청(ITERATIONS)을 VUS명이 나눠 보내고, 전략별로 처리량·지연·중복 판매를 비교한다.
//
//   STRATEGY   NONE | PESSIMISTIC | OPTIMISTIC | CONDITIONAL_UPDATE | UNIQUE_KEY
//   SCENARIO   HOT(맨 앞줄 50석에 몰림) | SPREAD(5,000석 전체에 흩어짐)
import http from 'k6/http'
import { check } from 'k6'
import { Counter } from 'k6/metrics'

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080'
const STRATEGY = __ENV.STRATEGY || 'NONE'
const SCENARIO = __ENV.SCENARIO || 'SPREAD'
const CONCERT_ID = __ENV.CONCERT_ID || '2'
const HOT_SEATS = 50

const reserved = new Counter('reserve_success')
const rejected = new Counter('reserve_rejected')
const failed = new Counter('reserve_failed')

export const options = {
  scenarios: {
    rush: {
      executor: 'shared-iterations',
      vus: Number(__ENV.VUS || 200),
      iterations: Number(__ENV.ITERATIONS || 20000),
      maxDuration: '5m',
    },
  },
  summaryTrendStats: ['avg', 'p(50)', 'p(95)', 'p(99)', 'max'],
}

export function setup() {
  http.post(`${BASE_URL}/api/admin/concerts/${CONCERT_ID}/reset`)
  const seats = http.get(`${BASE_URL}/api/concerts/${CONCERT_ID}/seats`).json()
  const ids = seats.map((s) => s.id)
  return { seatIds: SCENARIO === 'HOT' ? ids.slice(0, HOT_SEATS) : ids }
}

export default function ({ seatIds }) {
  const seatId = seatIds[Math.floor(Math.random() * seatIds.length)]
  const res = http.post(
    `${BASE_URL}/api/reservations?strategy=${STRATEGY}`,
    JSON.stringify({ seatId, userId: `vu-${__VU}-${__ITER}` }),
    { headers: { 'Content-Type': 'application/json' }, tags: { name: 'reserve' } },
  )
  if (res.status === 201) reserved.add(1)
  else if (res.status === 409) rejected.add(1)
  else failed.add(1)
  check(res, { '201 또는 409': (r) => r.status === 201 || r.status === 409 })
}

export function teardown() {
  const stats = http.get(`${BASE_URL}/api/admin/concerts/${CONCERT_ID}/stats`).json()
  console.log(
    `[${STRATEGY}/${SCENARIO}] 예약 ${stats.reservations}건, 예매된 좌석 ${stats.reservedSeats}석, ` +
      `중복 판매 좌석 ${stats.doubleBookedSeats}석`,
  )
}
