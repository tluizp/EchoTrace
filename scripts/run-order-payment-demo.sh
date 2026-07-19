#!/bin/sh
set -eu

COLLECTOR_URL="${COLLECTOR_URL:-http://localhost:8080}"
DEMO_V1_URL="${DEMO_V1_URL:-http://localhost:8081}"
DEMO_V2_URL="${DEMO_V2_URL:-http://localhost:8082}"

wait_for() {
  name="$1"
  url="$2"
  attempt=1
  while ! curl --fail --silent "$url" >/dev/null 2>&1; do
    if [ "$attempt" -ge 60 ]; then
      echo "Timed out waiting for $name at $url" >&2
      exit 1
    fi
    attempt=$((attempt + 1))
    sleep 2
  done
}

wait_for "collector" "$COLLECTOR_URL/api/health"
wait_for "demo-v1" "$DEMO_V1_URL/api/demo/health"
wait_for "demo-v2" "$DEMO_V2_URL/api/demo/health"

START="$(date -u '+%Y-%m-%dT%H:%M:%SZ')"

echo "Generating healthy cohort on demo-v1..."
curl --fail --silent --show-error -X POST \
  "$DEMO_V1_URL/api/demo/order-to-payment?orders=100&failurePercentage=5"
echo

sleep 2

echo "Generating degraded cohort on demo-v2..."
curl --fail --silent --show-error -X POST \
  "$DEMO_V2_URL/api/demo/order-to-payment?orders=100&failurePercentage=30"
echo

sleep 5
END="$(date -u '+%Y-%m-%dT%H:%M:%SZ')"

echo "Business SLO evaluation:"
curl --fail --silent --show-error --get \
  --data-urlencode "end=$END" \
  "$COLLECTOR_URL/api/slos/evaluations"
echo

echo "Order checkout funnel:"
curl --fail --silent --show-error --get \
  --data-urlencode "start=$START" \
  --data-urlencode "end=$END" \
  "$COLLECTOR_URL/api/journeys/types/order.checkout/funnel"
echo

echo "Impact correlated with demo-v2:"
curl --fail --silent --show-error --get \
  --data-urlencode "serviceName=order-payment-demo" \
  --data-urlencode "completionStage=confirmed" \
  --data-urlencode "start=$START" \
  --data-urlencode "end=$END" \
  "$COLLECTOR_URL/api/journeys/types/order.checkout/deployments/demo-v2/impact"
echo
