# Setup CI/CD con GitHub Actions

## ¿Por qué CI/CD?

**Sin CI/CD:**
```
Cambio código → Build manual → Push manual → Deploy manual
⏱️  Tiempo: 10-15 minutos cada vez
❌ Propenso a errores
😫 Aburrido y repetitivo
```

**Con CI/CD:**
```
Cambio código → git push → Automático
⏱️  Tiempo: 2-3 minutos (automático)
✅ Sin errores manuales
😊 Puedes hacer otra cosa mientras
```

## Setup Rápido

### 1. Crear Secrets en GitHub

Ve a tu repo → Settings → Secrets and variables → Actions

Agrega estos secrets:

```
OCI_USER_OCID          # Tu User OCID
OCI_TENANCY_OCID       # Tu Tenancy OCID
OCI_FINGERPRINT        # Fingerprint de tu API key
OCI_PRIVATE_KEY        # Contenido de tu ~/.oci/oci_api_key.pem
OCI_REGION             # ej: eu-madrid-1
OCI_AUTH_TOKEN         # Token de autenticación para OCI Registry
```

### 2. Configurar OCI Registry

```bash
# Crear registry en OCI Console
# Obtener namespace
oci os ns get --query 'data' --raw-output

# Configurar en .github/workflows/deploy.yml
# Cambiar: <region>.ocir.io/<tenancy-namespace>
```

### 3. Configurar Kubernetes Access

```bash
# Obtener kubeconfig
oci ce cluster create-kubeconfig --cluster-id <cluster-id> --file kubeconfig

# Agregar como secret en GitHub
# O usar OCI IAM para acceso desde GitHub Actions
```

## Workflow Simplificado (Solo Build)

Si no quieres deploy automático todavía:

```yaml
# Solo construye y sube imágenes
# Tú haces el deploy manual cuando quieras
```

## Alternativas

### Opción A: Solo Build Automático
- CI/CD construye imágenes
- Tú haces deploy manual cuando quieras

### Opción B: Build + Deploy Automático
- Todo automático
- Cada push a main → deploy automático

### Opción C: Manual (Sin CI/CD)
- Build local
- Push manual
- Deploy manual
- **Más control, más trabajo**

## Recomendación

**Para empezar:** Opción A (solo build)
- Aprende cómo funciona
- Menos riesgo
- Puedes hacer deploy cuando quieras

**Después:** Opción B (full automático)
- Una vez que confíes en el proceso

## ¿Quieres que lo configuremos?

Puedo ayudarte a:
1. Crear el workflow de GitHub Actions
2. Configurar los secrets
3. Probar el primer build

¿Empezamos?

