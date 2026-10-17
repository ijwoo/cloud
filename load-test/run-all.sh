#!/usr/bin/env bash
# 모든 전략 x 시나리오를 차례로 돌리고 결과를 load-test/results/ 에 남긴다.
# 사용법: ./load-test/run-all.sh   (백엔드가 localhost:8080 에 떠 있어야 함)
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p results && chmod a+w results  # k6 이미지는 root가 아닌 사용자로 돈다

for scenario in ${SCENARIOS:-HOT SPREAD}; do
  for strategy in ${STRATEGIES:-NONE PESSIMISTIC OPTIMISTIC CONDITIONAL_UPDATE UNIQUE_KEY}; do
    echo "=== $strategy / $scenario"
    docker run --rm --network host -v "$PWD:/scripts" \
      -e STRATEGY="$strategy" -e SCENARIO="$scenario" \
      -e VUS="${VUS:-200}" -e ITERATIONS="${ITERATIONS:-20000}" \
      grafana/k6:2.3.0 run --quiet \
      --summary-export "/scripts/results/${scenario}-${strategy}.json" \
      /scripts/reserve.js 2>&1 | grep -E '\[(NONE|PESSIMISTIC|OPTIMISTIC|CONDITIONAL_UPDATE|UNIQUE_KEY)/' || true
    sleep "${PAUSE:-5}"
  done
done
