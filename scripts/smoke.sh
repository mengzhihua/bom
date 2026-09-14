#!/usr/bin/env bash
set -euo pipefail
BASE="${BASE:-http://localhost:8082}"
RUN_ID="$(date +%s)"
P1="9${RUN_ID: -9}"
P2="8${RUN_ID: -9}"
P3="7${RUN_ID: -9}"
json(){ curl -fsS "$@"; }
api(){ json -H "Authorization: Bearer $TOKEN" "$@"; }
assert_ok(){ jq -e '.code == 0' >/dev/null; }
echo "[smoke] login"
TOKEN=$(json -X POST "$BASE/api/auth/login" -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin123"}' | jq -r '.data.token')
test -n "$TOKEN" -a "$TOKEN" != null
echo "[smoke] create part and release"
PART=$(api -X POST "$BASE/api/parts" -H 'Content-Type: application/json' -d "{\"partNo\":\"$P1\",\"revision\":\"A\",\"partName\":\"Smoke Part\",\"partType\":\"PART\",\"category\":\"BODY\",\"uom\":\"EA\",\"makeBuy\":\"MAKE\",\"unitCost\":12,\"weightKg\":1}" | tee /tmp/bom-part.json)
PID=$(jq -r '.data.id' <<<"$PART"); test "$PID" != null
api -X POST "$BASE/api/parts/$PID/submit" | assert_ok
api -X POST "$BASE/api/parts/$PID/release" | assert_ok
echo "[smoke] create BOM and add three levels"
ROOT=$(api -X POST "$BASE/api/parts" -H 'Content-Type: application/json' -d "{\"partNo\":\"$P2\",\"revision\":\"A\",\"partName\":\"Smoke Root\",\"partType\":\"ASSEMBLY\",\"category\":\"BODY\",\"uom\":\"EA\",\"makeBuy\":\"MAKE\",\"lifecycle\":\"RELEASED\"}" | jq -r '.data.id')
BOM=$(api -X POST "$BASE/api/boms" -H 'Content-Type: application/json' -d "{\"bomType\":\"EBOM\",\"rootPartId\":$ROOT,\"version\":1,\"status\":\"DRAFT\"}")
BID=$(jq -r '.data.id' <<<"$BOM")
api -X POST "$BASE/api/boms/$BID/items" -H 'Content-Type: application/json' -d "{\"childPartId\":$PID,\"qty\":2,\"uom\":\"EA\"}" | assert_ok
CHILD=$(api -X POST "$BASE/api/parts" -H 'Content-Type: application/json' -d "{\"partNo\":\"$P3\",\"revision\":\"A\",\"partName\":\"Smoke Child\",\"partType\":\"PART\",\"category\":\"BODY\",\"uom\":\"EA\",\"makeBuy\":\"MAKE\",\"lifecycle\":\"RELEASED\"}" | jq -r '.data.id')
api -X POST "$BASE/api/boms/$BID/items" -H 'Content-Type: application/json' -d "{\"parentPartId\":$PID,\"childPartId\":$CHILD,\"qty\":3,\"uom\":\"EA\"}" | assert_ok
api -X POST "$BASE/api/boms/$BID/items" -H 'Content-Type: application/json' -d "{\"parentPartId\":$ROOT,\"childPartId\":$PID,\"qty\":1,\"uom\":\"EA\",\"usageCondition\":\"ENGINE=1.5T\"}" | assert_ok
api -X POST "$BASE/api/boms/$BID/items" -H 'Content-Type: application/json' -d "{\"parentPartId\":$ROOT,\"childPartId\":$PID,\"qty\":2,\"uom\":\"EA\",\"usageCondition\":\"ENGINE=2.0T\"}" | assert_ok
set +e
CY=$(api -X POST "$BASE/api/boms/$BID/items" -H 'Content-Type: application/json' -d "{\"parentPartId\":$CHILD,\"childPartId\":$ROOT,\"qty\":1,\"uom\":\"EA\"}")
set -e
jq -e '.code != 0' <<<"$CY" >/dev/null
api "$BASE/api/boms/$BID/tree" | jq -e '.code == 0 and (.data|length) >= 2' >/dev/null
api "$BASE/api/boms/$BID/explode" | assert_ok
api "$BASE/api/boms/$BID/summarized" | assert_ok
api "$BASE/api/boms/$BID/rollup" | jq -e '.code == 0 and .data.totalCost != null' >/dev/null
api "$BASE/api/parts/$PID/where-used?recursive=true" | assert_ok
CFG1=$(api -X POST "$BASE/api/boms/$BID/configure" -H 'Content-Type: application/json' -d '{"selections":{"ENGINE":"1.5T","TRANS":"AT"}}' | jq -r '.data.totalCost')
CFG2=$(api -X POST "$BASE/api/boms/$BID/configure" -H 'Content-Type: application/json' -d '{"selections":{"ENGINE":"2.0T","TRANS":"AT"}}' | jq -r '.data.totalCost')
test "$CFG1" != "$CFG2"
api -X POST "$BASE/api/boms/$BID/release" | assert_ok
MBOM=$(api -X POST "$BASE/api/boms/$BID/derive-mbom" -H 'Content-Type: application/json' -d '{"description":"Smoke MBOM"}')
MBID=$(jq -r '.data.id' <<<"$MBOM"); test "$MBID" != null
api "$BASE/api/boms/compare?leftId=$BID&rightId=$MBID" | assert_ok
ECR=$(api -X POST "$BASE/api/ecrs" -H 'Content-Type: application/json' -d '{"title":"Smoke ECR","reason":"QUALITY","priority":"HIGH"}')
ECRID=$(jq -r '.data.id' <<<"$ECR")
api -X POST "$BASE/api/ecrs/$ECRID/submit" | assert_ok
api -X POST "$BASE/api/ecrs/$ECRID/approve" | assert_ok
ECN=$(api -X POST "$BASE/api/ecrs/$ECRID/to-ecn")
ECNID=$(jq -r '.data.id' <<<"$ECN")
api -X PUT "$BASE/api/ecns/$ECNID" -H 'Content-Type: application/json' -d "{\"bomId\":$BID,\"title\":\"Smoke ECN\"}" | assert_ok
api -X POST "$BASE/api/ecns/$ECNID/items" -H 'Content-Type: application/json' -d "{\"action\":\"REPLACE\",\"parentPartId\":$ROOT,\"oldChildPartId\":$PID,\"newChildPartId\":$CHILD,\"oldQty\":1,\"newQty\":1}" | assert_ok
api -X POST "$BASE/api/ecns/$ECNID/submit" | assert_ok
api -X POST "$BASE/api/ecns/$ECNID/approve" | assert_ok
IMPLEMENTED=$(api -X POST "$BASE/api/ecns/$ECNID/implement")
NEWBOMID=$(jq -r '.data.implementedBomId' <<<"$IMPLEMENTED"); test "$NEWBOMID" != null
api "$BASE/api/boms/$NEWBOMID" | jq -e '.code == 0 and .data.version == 2 and .data.status == "RELEASED"' >/dev/null
api "$BASE/api/boms/$BID" | jq -e '.code == 0 and .data.status == "OBSOLETE"' >/dev/null
api -X POST "$BASE/api/boms/$NEWBOMID/sync-sap" | jq -e '.code == 0 and .data.sapBomNo != null' >/dev/null
NEW_NO=$(api "$BASE/api/boms/$NEWBOMID" | jq -r '.data.bomNo')
api "$BASE/api/open/boms/$NEW_NO/explode" -H 'X-Api-Key: bom-open-key' | assert_ok
set +e
BAD=$(curl -sS -o /dev/null -w '%{http_code}' "$BASE/api/open/boms/$NEW_NO/explode")
set -e
test "$BAD" = 400 -o "$BAD" = 401
echo "[smoke] all checks passed"
