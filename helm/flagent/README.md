# Flagent Helm Chart

Official Helm chart for deploying Flagent to Kubernetes.

## Prerequisites

- Kubernetes 1.19+
- Helm 3.0+
- PV provisioner support in the underlying infrastructure (for PostgreSQL persistence)

## Installing the Chart

### Add Helm Repository (future)

```bash
helm repo add flagent https://maxluxs.github.io/Flagent/helm
helm repo update
```

### Install from Local Chart

```bash
git clone https://github.com/MaxLuxs/Flagent.git
cd Flagent/flagent/helm
helm install flagent ./flagent
```

### Install with Custom Values

```bash
helm install flagent ./flagent -f custom-values.yaml
```

## Quick Start

### 1. Install with Default Values

```bash
helm install my-flagent ./flagent
```

This installs Flagent with:
- 3 replicas
- Embedded PostgreSQL (for testing)
- ClusterIP service

### 2. Port-Forward to Access

```bash
kubectl port-forward svc/my-flagent 18000:80
```

Open browser: `http://localhost:18000`

### 3. Access with Ingress

Create `production-values.yaml`:

```yaml
ingress:
  enabled: true
  className: nginx
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
  hosts:
    - host: flagent.example.com
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: flagent-tls
      hosts:
        - flagent.example.com

postgresql:
  enabled: true
  auth:
    username: flagent
    password: "YOUR_SECURE_PASSWORD"
    database: flagent
  primary:
    persistence:
      enabled: true
      size: 20Gi
```

Install:

```bash
helm install flagent ./flagent -f production-values.yaml
```

## Configuration

### Core Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `replicaCount` | Number of Flagent pods | `3` |
| `image.repository` | Flagent image repository | `ghcr.io/maxluxs/flagent` |
| `image.tag` | Flagent image tag | `latest` |
| `image.pullPolicy` | Image pull policy | `IfNotPresent` |

### Service Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `service.type` | Kubernetes service type | `ClusterIP` |
| `service.port` | Service port | `80` |
| `service.targetPort` | Container port | `18000` |

### Ingress Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `ingress.enabled` | Enable ingress | `false` |
| `ingress.className` | Ingress class name | `nginx` |
| `ingress.hosts` | Ingress hosts configuration | `[]` |
| `ingress.tls` | Ingress TLS configuration | `[]` |

### Database Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `config.database.driver` | Database driver (postgres/mysql/sqlite) | `postgres` |
| `config.database.host` | Database host | `postgres-service` |
| `config.database.port` | Database port | `5432` |
| `config.database.name` | Database name | `flagent` |
| `config.database.user` | Database user | `flagent` |
| `config.database.password` | Database password | `changeme` |

### PostgreSQL Subchart

| Parameter | Description | Default |
|-----------|-------------|---------|
| `postgresql.enabled` | Enable PostgreSQL subchart | `true` |
| `postgresql.auth.username` | PostgreSQL username | `flagent` |
| `postgresql.auth.password` | PostgreSQL password | `changeme` |
| `postgresql.auth.database` | PostgreSQL database | `flagent` |
| `postgresql.primary.persistence.size` | PVC size | `10Gi` |

### Resources

| Parameter | Description | Default |
|-----------|-------------|---------|
| `resources.requests.memory` | Memory request | `512Mi` |
| `resources.requests.cpu` | CPU request | `250m` |
| `resources.limits.memory` | Memory limit | `1Gi` |
| `resources.limits.cpu` | CPU limit | `1000m` |

### Autoscaling

| Parameter | Description | Default |
|-----------|-------------|---------|
| `autoscaling.enabled` | Enable HPA | `false` |
| `autoscaling.minReplicas` | Minimum replicas | `3` |
| `autoscaling.maxReplicas` | Maximum replicas | `10` |
| `autoscaling.targetCPUUtilizationPercentage` | Target CPU % | `80` |

## Production Deployment

### External PostgreSQL

Disable embedded PostgreSQL and use external:

```yaml
postgresql:
  enabled: false

config:
  database:
    host: "postgres.example.com"
    port: 5432
    name: "flagent_prod"
    user: "flagent"
    password: "SECURE_PASSWORD"
```

### High Availability Setup

```yaml
replicaCount: 5

podDisruptionBudget:
  enabled: true
  minAvailable: 2

autoscaling:
  enabled: true
  minReplicas: 5
  maxReplicas: 20
  targetCPUUtilizationPercentage: 70

resources:
  requests:
    memory: "1Gi"
    cpu: "500m"
  limits:
    memory: "2Gi"
    cpu: "2000m"

affinity:
  podAntiAffinity:
    preferredDuringSchedulingIgnoredDuringExecution:
    - weight: 100
      podAffinityTerm:
        labelSelector:
          matchExpressions:
          - key: app
            operator: In
            values:
            - flagent
        topologyKey: kubernetes.io/hostname
```

### Enable Monitoring

```yaml
serviceMonitor:
  enabled: true
  interval: 30s
  labels:
    prometheus: kube-prometheus
```

### Enable Redis Cache

```yaml
redis:
  enabled: true
  auth:
    enabled: true
    password: "SECURE_REDIS_PASSWORD"
  master:
    persistence:
      enabled: true
      size: 5Gi
```

## Upgrading

### Upgrade Release

```bash
helm upgrade flagent ./flagent -f production-values.yaml
```

### Rollback

```bash
helm rollback flagent 1
```

## Uninstalling

```bash
helm uninstall flagent
```

**Note**: This will delete all data. Backup your database before uninstalling.

## Examples

### Development Setup

```yaml
replicaCount: 1

resources:
  requests:
    memory: "256Mi"
    cpu: "100m"
  limits:
    memory: "512Mi"
    cpu: "500m"

postgresql:
  enabled: true
  primary:
    persistence:
      enabled: false  # Use emptyDir for dev
```

### Staging Setup

```yaml
replicaCount: 2

ingress:
  enabled: true
  hosts:
    - host: staging-flagent.example.com
      paths:
        - path: /
          pathType: Prefix

postgresql:
  enabled: true
  primary:
    persistence:
      size: 10Gi
```

### Production Setup

See "Production Deployment" section above.

## Troubleshooting

### Pods not starting

Check pod logs:

```bash
kubectl logs -l app=flagent
```

Common issues:
1. Database connection failed - check credentials
2. Image pull error - verify image repository and tag
3. Resource limits too low - increase resources

### Database connection errors

Verify database connectivity:

```bash
kubectl run -it --rm debug --image=postgres:14 --restart=Never -- \
  psql -h postgres-service -U flagent -d flagent
```

### High memory usage

Increase memory limits:

```yaml
resources:
  limits:
    memory: "2Gi"
```

### Slow evaluation

Enable Redis cache:

```yaml
redis:
  enabled: true
```

## Support

- **Documentation**: https://maxluxs.github.io/Flagent
- **GitHub Issues**: https://github.com/MaxLuxs/Flagent/issues
- **Helm Chart Issues**: https://github.com/MaxLuxs/Flagent/issues

## License

Apache 2.0
