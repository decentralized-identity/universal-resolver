#!/usr/bin/env bash
set -euo pipefail

# === inputs you can tweak ===
HZ_ID="Z05835472RTBILV7EH141"   # Hosted Zone ID for resolver.identity.foundation
RECORD_NAME="resolver.identity.foundation."
ALB_DNS="k8s-uniresol-uniresol-01e0b1f895-139924821.us-east-2.elb.amazonaws.com"
AWS_REGION="us-east-2"          # Region of the ALB

# === get the ALB's Canonical Hosted Zone ID (required for ALIAS) ===
ALB_ZONE_ID="$(aws elbv2 describe-load-balancers \
  --region "$AWS_REGION" \
  --query "LoadBalancers[?DNSName=='${ALB_DNS}'].CanonicalHostedZoneId | [0]" \
  --output text)"

if [[ -z "$ALB_ZONE_ID" || "$ALB_ZONE_ID" == "None" ]]; then
  echo "ERROR: Could not resolve ALB CanonicalHostedZoneId for $ALB_DNS in $AWS_REGION"
  exit 1
fi

echo "Using ALB DNS: $ALB_DNS"
echo "ALB Hosted Zone ID: $ALB_ZONE_ID"
echo "Target record: $RECORD_NAME (zone $HZ_ID)"

# === build the change batch payload ===
CHANGE_BATCH="$(cat <<JSON
{
  "Comment": "Point ${RECORD_NAME} to ALB ${ALB_DNS}",
  "Changes": [{
    "Action": "UPSERT",
    "ResourceRecordSet": {
      "Name": "${RECORD_NAME}",
      "Type": "A",
      "AliasTarget": {
        "HostedZoneId": "${ALB_ZONE_ID}",
        "DNSName": "${ALB_DNS}",
        "EvaluateTargetHealth": false
      }
    }
  }]
}
JSON
)"

# === apply the change ===
CHANGE_ID="$(aws route53 change-resource-record-sets \
  --hosted-zone-id "$HZ_ID" \
  --change-batch "$CHANGE_BATCH" \
  --query 'ChangeInfo.Id' --output text)"

echo "Submitted change: $CHANGE_ID"
echo "Waiting for Route53 to propagate..."
aws route53 wait resource-record-sets-changed --id "$CHANGE_ID"
echo "Route53 status: INSYNC"

# === quick verification ===
echo "dig result:"
dig +short "${RECORD_NAME}"

