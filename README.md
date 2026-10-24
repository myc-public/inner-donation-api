# inner-donation-api

API des dons et des donateurs, service pilote du socle **myc-public**. Spring Boot 4, Java 21, MySQL, parent Maven
`craft-parent` (dépôt `artifacts-manager`). Chaque création de don écrit, dans la même transaction, un événement
dans la table `outbox_event` ; l'API ne connaît pas Kafka (ADR 0018). Les modifications et suppressions sont
auditées dans `audit_outbox` (ADR 0017).

Place dans le socle : navigateur -> APISIX externe -> web-bff -> APISIX interne -> **inner-donation-api** -> MySQL.
Vue d'ensemble : [platform-handbook](https://github.com/myc-public/platform-handbook).

## Démarrer en local

Prérequis : Java 21, Maven avec le `settings.xml` de `artifacts-manager` (Nexus), Docker. Pour appeler l'API avec
un token : Keycloak local (`gitops-platform/local/keycloak`).

```powershell
cd D:\workspace\public\inner-donation-api
# Une seule fois (ne jamais écraser un fichier existant), puis renseigner les valeurs
if (-not (Test-Path local\.env)) { Copy-Item local\.env.example local\.env }
docker compose -f local/compose.yaml up -d      # MySQL seul (127.0.0.1:3307)
mvn spring-boot:run                             # profil local, http://localhost:8080
```

Variante conteneurisée (même Dockerfile que la CI) :
`mvn package` puis `docker compose -f local/compose.yaml --profile app up -d --build`.

Swagger UI : `http://localhost:8080/docs/swagger-ui.html` (profils `local`, `dev`, `uat` uniquement).
Santé : `http://localhost:8080/management/health`.

## Build et tests

```powershell
mvn verify     # tests unitaires et MockMvc (sécurité, erreurs, audit, outbox), sans Docker
```

Tests de bout en bout (Newman) et recette sécurité : [`postman/PLAN-DE-TEST-E2E.md`](postman/PLAN-DE-TEST-E2E.md),
environnements `e2e-dev`, `e2e-pile`, `e2e-sandbox`.

## API

| Ressource | Accès |
|---|---|
| `/api/v1/donors`, `/api/v1/donations` | back-office : une permission par opération (`donor:read`, `donation:delete`…) |
| `/api/v1/donors/me`, `/api/v1/donations/me` | donateur connecté : permissions `…:own`, uniquement ses propres données (`party_id` du token) |
| `/management/**` | sondes et métriques ; comptes basic auth (`MANAGEMENT_*`) |

Tokens de deux émetteurs (realms `myc-internal` et `myc-customers`), audience `donation-api`. Les droits viennent
du claim `permissions` ; la matrice rôle -> permissions vit dans le realm Keycloak (ADR 0013 à 0016). La sécurité
est active par défaut ; `myc.security.enabled=false` la désactive (tests locaux sans Keycloak).

## CI/CD

| Étape | Où |
|---|---|
| Job Jenkins `inner-donation-api` | [`cicd/Jenkinsfile-ci`](cicd/Jenkinsfile-ci) : build, tests, SonarCloud (Quality Gate synchrone), publication du jar dans Nexus, image (BuildConfig [`cicd/openshift/build.yaml`](cicd/openshift/build.yaml)), mise à jour du tag dans `gitops-platform` |
| Image | [`cicd/openshift/Dockerfile`](cicd/openshift/Dockerfile) : UBI 8 OpenJDK 21 runtime, tag `<version>-<sha>-bNN` |
| Déploiement | `gitops-platform/apps/donation-api`, synchronisé par Argo CD |

`cicd/Jenkinsfile-build`, `cicd/Jenkinsfile-deploy-*` et `cicd/jenkins.properties` datent de l'ancien modèle
(déploiement SSH sur VM) : aucun job ne les utilise.

## Configuration

Variables : [`local/.env.example`](local/.env.example) (mêmes noms que la ConfigMap et les Secrets OpenShift
`donation-api-config`, `donation-api-db-secret`, `donation-api-management-secret`). Valeurs réelles : `local/.env`
(ignoré par Git) en local, Secrets OpenShift hors Git sur le Sandbox.

| Variable | Rôle |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | MySQL |
| `MANAGEMENT_ADMIN_*`, `MANAGEMENT_VIEWER_*` | comptes des endpoints `/management` |
| `OIDC_INTERNAL_*`, `OIDC_CUSTOMERS_*` | émetteurs Keycloak (défaut : Keycloak local, port 8180) |
| `MYC_CONTEXT` | préfixe derrière la passerelle (défaut hors local : `/api/donation/`) |
| `OUTBOX_*` | réglages de l'outbox (`app.outbox`) |

## Documentation

| Fichier | Contenu |
|---|---|
| [docs/architecture.md](docs/architecture.md) | Packages, transactions, outbox, audit, sécurité |
| [docs/api-first.md](docs/api-first.md) | Référentiel API First, état des lieux, décisions D1-D3 |
| [postman/PLAN-DE-TEST-E2E.md](postman/PLAN-DE-TEST-E2E.md) | Plan de test E2E et recette sécurité (SEC-*) |

Roadmap API First (lots L0-L8) et décisions transverses :
[roadmap/api-first.md](https://github.com/myc-public/platform-handbook/blob/develop/roadmap/api-first.md),
[adr/](https://github.com/myc-public/platform-handbook/blob/develop/adr/README.md).
