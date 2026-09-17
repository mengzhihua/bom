#!/usr/bin/env bash
set -euo pipefail
BASE="${BASE_URL:-${BASE:-http://localhost:8082}}"
OPEN_KEY="${BOM_OPEN_API_KEY:-}"
RUN_ID="$(date +%s)"
P1="9${RUN_ID: -9}"
P2="8${RUN_ID: -9}"
P3="7${RUN_ID: -9}"
json(){ curl -sS "$@"; }
api(){ json -H "Authorization: Bearer $TOKEN" "$@"; }
assert_ok(){ jq -e '.code == 0' >/dev/null; }
step(){ echo "[smoke] $1"; }
step "login"
TOKEN=$(json -X POST "$BASE/api/auth/login" -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin123"}' | jq -r '.data.token')
test -n "$TOKEN" -a "$TOKEN" != null
DEMO_ECN=$(api "$BASE/api/ecns" | jq -r '.data[] | select(.ecnNo == "ECN-DEMO-001") | .id' | head -1)
if test -n "$DEMO_ECN"; then
    DEMO_STATUS=$(api "$BASE/api/ecns/$DEMO_ECN" | jq -r '.data.status')
    if test "$DEMO_STATUS" = "DRAFT"; then
        step "submit and approve demo ECN"
        api -X POST "$BASE/api/ecns/$DEMO_ECN/submit" | assert_ok
        api -X POST "$BASE/api/ecns/$DEMO_ECN/approve" | assert_ok
        DEMO_STATUS=APPROVED
    fi
    if test "$DEMO_STATUS" = "APPROVED"; then
        step "implement demo ECN"
        api -X POST "$BASE/api/ecns/$DEMO_ECN/implement" | assert_ok
    elif test "$DEMO_STATUS" != "IMPLEMENTED"; then
        false
    fi
fi
step "create part"
PART_PAYLOAD="{\"partNo\":\"$P1\",\"revision\":\"A\",\"partName\":\"Smoke Part\",\"partType\":\"PART\",\"category\":\"BODY\",\"uom\":\"EA\",\"makeBuy\":\"MAKE\",\"unitCost\":12,\"weightKg\":1}"
PART=$(api -X POST "$BASE/api/parts" -H 'Content-Type: application/json' -d "$PART_PAYLOAD" | tee /tmp/bom-part.json)
PID=$(jq -r '.data.id' <<<"$PART"); test "$PID" != null
api -X POST "$BASE/api/parts/$PID/submit" | assert_ok
step "submit part"
api -X POST "$BASE/api/parts/$PID/release" | assert_ok
step "release part"
step "create root part"
ROOT_PAYLOAD="{\"partNo\":\"$P2\",\"revision\":\"A\",\"partName\":\"Smoke Root\",\"partType\":\"ASSEMBLY\",\"category\":\"BODY\",\"uom\":\"EA\",\"makeBuy\":\"MAKE\",\"lifecycle\":\"RELEASED\"}"
ROOT=$(api -X POST "$BASE/api/parts" -H 'Content-Type: application/json' -d "$ROOT_PAYLOAD" | jq -r '.data.id')
BOM=$(api -X POST "$BASE/api/boms" -H 'Content-Type: application/json' -d "{\"bomType\":\"EBOM\",\"rootPartId\":$ROOT,\"version\":1,\"status\":\"DRAFT\"}")
BID=$(jq -r '.data.id' <<<"$BOM")
step "create EBOM"
assert_ok <<<"$BOM"
step "add first BOM level"
FIRST=$(api -X POST "$BASE/api/boms/$BID/items" -H 'Content-Type: application/json' -d "{\"childPartId\":$PID,\"qty\":2,\"uom\":\"EA\",\"usageCondition\":\"TRANS=MT\"}")
assert_ok <<<"$FIRST"
FIRST_ITEM=$(jq -r '.data.id' <<<"$FIRST")
FIRST_ROW=$(jq -c '.data' <<<"$FIRST")
api -X PUT "$BASE/api/boms/$BID/items/$FIRST_ITEM" \
  -H 'Content-Type: application/json' \
  -d "$(jq '.stationCode = "ST-01"' <<<"$FIRST_ROW")" | assert_ok
CHILD_PAYLOAD="{\"partNo\":\"$P3\",\"revision\":\"A\",\"partName\":\"Smoke Child\",\"partType\":\"PART\",\"category\":\"BODY\",\"uom\":\"EA\",\"makeBuy\":\"MAKE\",\"lifecycle\":\"RELEASED\"}"
CHILD=$(api -X POST "$BASE/api/parts" -H 'Content-Type: application/json' -d "$CHILD_PAYLOAD" | jq -r '.data.id')
step "add second BOM level"
api -X POST "$BASE/api/boms/$BID/items" -H 'Content-Type: application/json' -d "{\"parentPartId\":$PID,\"childPartId\":$CHILD,\"qty\":3,\"uom\":\"EA\"}" | assert_ok
P4_PAYLOAD="{\"partNo\":\"${P1}-SHARED\",\"revision\":\"A\",\"partName\":\"Shared Parent\",\"partType\":\"ASSEMBLY\",\"category\":\"BODY\",\"uom\":\"EA\",\"makeBuy\":\"MAKE\",\"lifecycle\":\"RELEASED\"}"
P4=$(api -X POST "$BASE/api/parts" -H 'Content-Type: application/json' -d "$P4_PAYLOAD" | jq -r '.data.id')
api -X POST "$BASE/api/boms/$BID/items" -H 'Content-Type: application/json' -d "{\"parentPartId\":$ROOT,\"childPartId\":$P4,\"qty\":1,\"uom\":\"EA\"}" | assert_ok
api -X POST "$BASE/api/boms/$BID/items" -H 'Content-Type: application/json' -d "{\"parentPartId\":$P4,\"childPartId\":$CHILD,\"qty\":1,\"uom\":\"EA\"}" | assert_ok
step "where-used shared child paths"
api "$BASE/api/parts/$CHILD/where-used?recursive=true" | jq -e '.code == 0 and ([.data[].path] | unique | length) >= 2' >/dev/null
step "add conditional BOM rows"
CONDITIONAL=$(api -X POST "$BASE/api/boms/$BID/items" -H 'Content-Type: application/json' -d "{\"parentPartId\":$ROOT,\"childPartId\":$PID,\"qty\":1,\"uom\":\"EA\",\"usageCondition\":\"ENGINE=1.5T\"}")
assert_ok <<<"$CONDITIONAL"
CLEAR_ITEM=$(jq -r '.data.id' <<<"$CONDITIONAL")
api -X POST "$BASE/api/boms/$BID/items" -H 'Content-Type: application/json' -d "{\"parentPartId\":$ROOT,\"childPartId\":$PID,\"qty\":2,\"uom\":\"EA\",\"usageCondition\":\"ENGINE=2.0T\"}" | assert_ok
step "clear BOM item text fields"
api -X PUT "$BASE/api/boms/$BID/items/$CLEAR_ITEM" \
  -H 'Content-Type: application/json' \
  -d "{\"parentPartId\":$ROOT,\"childPartId\":$PID,\"qty\":1,\"uom\":\"EA\",\"usageType\":\"NORMAL\",\"usageCondition\":\"\",\"stationCode\":\"\",\"alternateGroup\":\"\",\"positionDesc\":\"\",\"findNo\":null}" \
  | assert_ok
api "$BASE/api/boms/$BID/items" | jq -e --argjson itemId "$CLEAR_ITEM" \
  '.data[] | select(.id == $itemId and .usageCondition == null)' >/dev/null
step "preserve BOM row fields on full edit"
ROW=$(api "$BASE/api/boms/$BID/tree" | jq -c --argjson itemId "$CLEAR_ITEM" \
  '.. | objects | select(.itemId == $itemId)')
api -X PUT "$BASE/api/boms/$BID/items/$CLEAR_ITEM" \
  -H 'Content-Type: application/json' \
  -d "$(jq '.operationSeq = 20 | .remark = "retained" | .positionDesc = "position-retained"' <<<"$ROW")" \
  | assert_ok
ROW=$(api "$BASE/api/boms/$BID/tree" | jq -c --argjson itemId "$CLEAR_ITEM" \
  '.. | objects | select(.itemId == $itemId)')
api -X PUT "$BASE/api/boms/$BID/items/$CLEAR_ITEM" \
  -H 'Content-Type: application/json' \
  -d "$(jq '.qty = 3' <<<"$ROW")" \
  | assert_ok
api "$BASE/api/boms/$BID/items" | jq -e --argjson itemId "$CLEAR_ITEM" \
  '.data[] | select(.id == $itemId and .qty == 3 and .operationSeq == 20 and .remark == "retained" and .positionDesc == "position-retained")' >/dev/null
step "reject BOM cycle"
set +e
CY=$(api -X POST "$BASE/api/boms/$BID/items" -H 'Content-Type: application/json' -d "{\"parentPartId\":$CHILD,\"childPartId\":$ROOT,\"qty\":1,\"uom\":\"EA\"}")
set -e
jq -e '.code != 0' <<<"$CY" >/dev/null
step "read BOM tree"
api "$BASE/api/boms/$BID/tree" | jq -e '.code == 0 and (.data|length) >= 2' >/dev/null
step "explode BOM"
api "$BASE/api/boms/$BID/explode" | assert_ok
step "summarize BOM"
api "$BASE/api/boms/$BID/summarized" | assert_ok
step "roll up BOM cost and weight"
api "$BASE/api/boms/$BID/rollup" | jq -e '.code == 0 and .data.totalCost != null' >/dev/null
step "where-used lookup"
api "$BASE/api/parts/$PID/where-used?recursive=true" | assert_ok
step "configure BOM for 1.5T"
CFG1=$(api -X POST "$BASE/api/boms/$BID/configure" -H 'Content-Type: application/json' -d '{"selections":{"ENGINE":"1.5T","TRANS":"AT"}}' | jq -r '.data.totalCost')
step "configure BOM for 2.0T"
CFG2=$(api -X POST "$BASE/api/boms/$BID/configure" -H 'Content-Type: application/json' -d '{"selections":{"ENGINE":"2.0T","TRANS":"AT"}}' | jq -r '.data.totalCost')
test "$CFG1" != "$CFG2"
step "release EBOM"
api -X POST "$BASE/api/boms/$BID/release" | assert_ok
step "derive MBOM"
MBOM=$(api -X POST "$BASE/api/boms/$BID/derive-mbom" -H 'Content-Type: application/json' -d '{"description":"Smoke MBOM"}')
MBID=$(jq -r '.data.id' <<<"$MBOM"); test "$MBID" != null
step "compare BOMs"
api "$BASE/api/boms/compare?leftId=$BID&rightId=$MBID" | assert_ok
step "create ECR"
ECR=$(api -X POST "$BASE/api/ecrs" -H 'Content-Type: application/json' -d '{"title":"Smoke ECR","reason":"QUALITY","priority":"HIGH"}')
ECRID=$(jq -r '.data.id' <<<"$ECR")
step "submit ECR"
api -X POST "$BASE/api/ecrs/$ECRID/submit" | assert_ok
step "approve ECR"
api -X POST "$BASE/api/ecrs/$ECRID/approve" | assert_ok
step "convert ECR to ECN"
ECN=$(api -X POST "$BASE/api/ecrs/$ECRID/to-ecn")
ECNID=$(jq -r '.data.id' <<<"$ECN")
step "set ECN target BOM"
api -X PUT "$BASE/api/ecns/$ECNID" -H 'Content-Type: application/json' -d "{\"bomId\":$BID,\"title\":\"Smoke ECN\"}" | assert_ok
step "add ECN replace item"
step "add ECN MODIFY clear station item"
ECN_MODIFY_PAYLOAD="{\"action\":\"MODIFY\",\"parentPartId\":$ROOT,\"oldChildPartId\":$PID,\"oldQty\":2,\"findNo\":10,\"oldUsageCondition\":\"TRANS=MT\",\"clearStationCode\":true}"
api -X POST "$BASE/api/ecns/$ECNID/items" -H 'Content-Type: application/json' -d "$ECN_MODIFY_PAYLOAD" | assert_ok
ECN_ITEM_PAYLOAD="{\"action\":\"REPLACE\",\"parentPartId\":$ROOT,\"oldChildPartId\":$PID,\"newChildPartId\":$CHILD,\"oldQty\":1,\"newQty\":1,\"findNo\":10,\"oldUsageCondition\":\"TRANS=MT\"}"
api -X POST "$BASE/api/ecns/$ECNID/items" -H 'Content-Type: application/json' -d "$ECN_ITEM_PAYLOAD" | assert_ok
step "submit ECN"
api -X POST "$BASE/api/ecns/$ECNID/submit" | assert_ok
step "approve ECN"
api -X POST "$BASE/api/ecns/$ECNID/approve" | assert_ok
step "implement ECN"
IMPLEMENTED=$(api -X POST "$BASE/api/ecns/$ECNID/implement")
NEWBOMID=$(jq -r '.data.implementedBomId' <<<"$IMPLEMENTED"); test "$NEWBOMID" != null
step "verify implemented BOM version"
OLD_BOM_NO=$(api "$BASE/api/boms/$BID" | jq -r '.data.bomNo')
api "$BASE/api/boms/$NEWBOMID" | jq -e --arg bomNo "$OLD_BOM_NO" '.code == 0 and .data.version == 2 and .data.status == "RELEASED" and .data.bomNo == $bomNo' >/dev/null
api "$BASE/api/boms/$BID" | jq -e '.code == 0 and .data.status == "OBSOLETE"' >/dev/null
api "$BASE/api/boms/$NEWBOMID/items" | jq -e --argjson childId "$CHILD" \
  '.data[] | select(.childPartId == $childId and .findNo == 10 and .stationCode == null)' >/dev/null
step "sync BOM to SAP"
api -X POST "$BASE/api/boms/$NEWBOMID/sync-sap" | jq -e '.code == 0 and .data.sapBomNo != null' >/dev/null
NEW_NO=$(api "$BASE/api/boms/$NEWBOMID" | jq -r '.data.bomNo')
step "open API with key"
api "$BASE/api/open/boms/$NEW_NO/explode" -H "X-Api-Key: $OPEN_KEY" | assert_ok
step "open API without key"
set +e
BAD=$(curl -sS -o /dev/null -w '%{http_code}' "$BASE/api/open/boms/$NEW_NO/explode")
set -e
test "$BAD" = 400 -o "$BAD" = 401
echo "[smoke] all checks passed"
