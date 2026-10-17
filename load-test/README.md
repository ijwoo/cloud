# 부하 테스트 (k6)

`RUSH ARENA`(5,000석)에 같은 양의 예매 요청을 보내고 전략별로 처리량·응답 시간·중복 판매를 비교합니다.

```bash
docker compose --profile monitoring up -d   # PostgreSQL + Prometheus + Grafana
cd backend && ./gradlew bootRun             # 다른 터미널에서
./load-test/run-all.sh                      # 5개 전략 x 2개 시나리오
```

- Grafana: http://localhost:3000 (로그인 없이 "RUSH — 예매 부하 관제" 대시보드가 열립니다)
- Prometheus: http://localhost:9090

| 환경 변수 | 기본값 | 설명 |
|---|---|---|
| `SCENARIOS` | `HOT SPREAD` | HOT: 맨 앞줄 50석에 몰림 / SPREAD: 5,000석 전체에 흩어짐 |
| `STRATEGIES` | 5개 전부 | `NONE PESSIMISTIC OPTIMISTIC CONDITIONAL_UPDATE UNIQUE_KEY` |
| `VUS` | `200` | 동시 가상 사용자 수 |
| `ITERATIONS` | `20000` | 총 요청 수 (VUS명이 나눠서 보냄) |

결과 요약은 `load-test/results/*.json`에 남습니다. 백엔드 커넥션 풀 크기는 `DB_POOL_SIZE`(기본 10)로 바꿀 수 있습니다.
