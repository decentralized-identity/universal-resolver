# DNS ALIAS Setup for ALB (Route53)

## Why this is needed (concise)

Kubernetes Ingress on AWS provisions an **Application Load Balancer (ALB)** with its own DNS name (e.g. `…elb.amazonaws.com`).
To make public domains like `dev.uniresolver.io` and `resolver.identity.foundation` resolve to that ALB, create **Route53 ALIAS A records** that point those names to the ALB. Without these ALIAS records, DNS lookups return nothing and browsers show **“Server Not Found.”**

---

## Get Hosted Zone IDs (Route53)

Use these commands to locate the correct public hosted zones (copy the `HostedZones[0].Id` value and strip the `/hostedzone/` prefix):

```bash
aws route53 list-hosted-zones-by-name --dns-name dev.uniresolver.io
aws route53 list-hosted-zones-by-name --dns-name resolver.identity.foundation
```

> If a subdomain has its **own** hosted zone, ensure the parent zone delegates to it (NS records present in the parent). Otherwise, public DNS will remain blank even after updates.

---

## Verify DNS with `dig`

After creating or updating ALIAS records, verify resolution to the ALB:

```bash
# A/ALIAS resolution
dig +short dev.uniresolver.io
dig +short resolver.identity.foundation

# Optional: trace—useful to debug delegation problems
dig +trace dev.uniresolver.io
dig +trace resolver.identity.foundation

# Optional: IPv6 if an AAAA ALIAS to dualstack ALB was created
dig AAAA +short dev.uniresolver.io
dig AAAA +short resolver.identity.foundation
```

---

## Run the scripts

Two executable scripts exist—one per domain. They **UPSERT** an ALIAS A record to the ALB and wait for Route53 **INSYNC**:

```bash
# From the repository directory where the scripts live
./set-alias-dev.uniresolver.io.sh
./set-alias-resolver.identity.foundation.sh
```

Re‑check DNS after they finish:

```bash
dig +short dev.uniresolver.io
dig +short resolver.identity.foundation
```

> If `dig` returns blank after INSYNC, re-check **delegation** (parent zone must contain `NS` records for the child zone).

---

## Notes

* Current setup listens on **HTTP (80)**. HTTPS requires adding an ACM certificate and a 443 listener via Ingress annotations.
* The scripts auto-detect the ALB’s **CanonicalHostedZoneId** via `elbv2` to build a correct ALIAS record.
