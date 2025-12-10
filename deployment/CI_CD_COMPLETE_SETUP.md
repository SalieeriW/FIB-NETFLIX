# Setup CI/CD Completo - Paso a Paso

## 🎯 Objetivo

Configurar GitHub Actions para que automáticamente:
1. Construya imágenes Docker cuando hagas `git push`
2. Suba las imágenes a OCI Container Registry
3. Actualice Kubernetes con las nuevas imágenes

## 📋 Checklist de Setup

### Paso 1: OCI Container Registry

```bash
# 1. Crear registry en OCI Console
#    - Hamburger menu → Developer Services → Container Registry
#    - Create Repository
#    - Nombre: vidstream (o el que prefieras)

# 2. Obtener namespace
oci os ns get --query 'data' --raw-output

# 3. Crear Auth Token para el registry
#    - User Settings → Auth Tokens → Generate Token
#    - Guarda el token (solo se muestra una vez!)

# 4. Obtener región
#    Ejemplo: eu-madrid-1
```

### Paso 2: Configurar GitHub Secrets

Ve a tu repo en GitHub:
1. Settings → Secrets and variables → Actions
2. Click "New repository secret"
3. Agrega estos secrets:

```
Nombre: OCI_USER_OCID
Valor: ocid1.user.oc1..aaaaaaaazjuike4elbzoxriqkyv5rl2rxrbr3jq4gr4pq2drjfzh2qnwhdmq

Nombre: OCI_TENANCY_OCID
Valor: ocid1.tenancy.oc1..aaaaaaaahmwzr7ai5ztxmqnnq4ti45jcrspnwx5gezebozzlie7bmkqeh5ta

Nombre: OCI_FINGERPRINT
Valor: [tu fingerprint actual - obtener con: cat ~/.oci/config | grep fingerprint]

Nombre: OCI_PRIVATE_KEY
Valor: [contenido completo de tu ~/.oci/oci_api_key.pem]

Nombre: OCI_REGION
Valor: eu-madrid-1

Nombre: OCI_AUTH_TOKEN
Valor: [el token que generaste en OCI Console]

Nombre: OCI_REGISTRY_REGION
Valor: eu-madrid-1 (o tu región)

Nombre: OCI_TENANCY_NAMESPACE
Valor: [obtenido con: oci os ns get]
```

### Paso 3: Obtener Valores Necesarios

Ejecuta estos comandos para obtener los valores:

```bash
# 1. Fingerprint
cat ~/.oci/config | grep fingerprint | head -1 | cut -d'=' -f2

# 2. Private Key
cat ~/.oci/oci_api_key.pem

# 3. Namespace
oci os ns get --query 'data' --raw-output

# 4. User OCID (ya lo tienes)
# 5. Tenancy OCID (ya lo tienes)
```

### Paso 4: Actualizar Workflow

Edita `.github/workflows/deploy.yml` y actualiza:

```yaml
env:
  REGISTRY: ${{ secrets.OCI_REGISTRY_REGION }}.ocir.io/${{ secrets.OCI_TENANCY_NAMESPACE }}
```

### Paso 5: Configurar Kubernetes Access

```bash
# Opción A: Usar kubeconfig como secret
# 1. Obtener kubeconfig
oci ce cluster create-kubeconfig --cluster-id <cluster-id> --file kubeconfig

# 2. Agregar como secret en GitHub
#    Nombre: KUBECONFIG
#    Valor: contenido del archivo kubeconfig

# Opción B: Usar OCI IAM (más seguro, más complejo)
# Configurar Dynamic Group y Policy en OCI
```

### Paso 6: Probar el Workflow

```bash
# 1. Commit y push
git add .github/workflows/deploy.yml
git commit -m "Add CI/CD workflow"
git push origin main

# 2. Ver en GitHub
#    - Ve a tu repo → Actions tab
#    - Deberías ver el workflow ejecutándose
```

## 🔍 Troubleshooting

### Error: "Authentication failed"
- Verifica que los secrets estén correctos
- Revisa el fingerprint y private key

### Error: "Registry not found"
- Verifica el namespace
- Asegúrate de que el registry existe en OCI Console

### Error: "Kubernetes connection failed"
- Verifica el kubeconfig
- Asegúrate de que el cluster existe

## 📝 Notas

- El workflow se ejecuta en cada push a `main` o `master`
- También puedes ejecutarlo manualmente desde GitHub Actions
- Las imágenes se etiquetan con `latest` y el SHA del commit

## 🚀 Siguiente Paso

Una vez configurado, cada `git push` automáticamente:
1. ✅ Construye las imágenes
2. ✅ Las sube al registry
3. ✅ Actualiza Kubernetes

¡Ya no necesitas hacer nada manualmente!

