# inner-donation-api

API des dons (Spring Boot 4, Java 21, MySQL). Lire d'abord [README.md](README.md) et
[docs/architecture.md](docs/architecture.md). Règles communes du socle : `platform-handbook`.

## Commandes

- Tests : `mvn verify` (sans Docker).
- Base locale : `docker compose -f local/compose.yaml up -d` ; API : `mvn spring-boot:run` (profil `local`).
- E2E : `postman/PLAN-DE-TEST-E2E.md` (Newman, environnements `e2e-*`).

## Règles propres à ce dépôt

- Toute création de don écrit `donation` et `outbox_event` dans la même transaction ; jamais d'appel Kafka ni
  réseau dans le service (ADR 0018).
- Toute modification ou suppression de don passe par `AuditRecorder`, dans la transaction.
- Chaque opération REST porte un `@PreAuthorize` avec une constante de `Permissions` ; un nom de permission =
  un rôle du client `donation-api` dans le realm (`gitops-platform/apps/keycloak`).
- Endpoints `/me` : identité tirée du token (`CurrentParty`), jamais de la requête.
- Schéma : nouvelle migration Flyway `V<n>__*.sql`, jamais de modification d'une migration existante.
- Nouveau test de sécurité : ajouté dans `postman/PLAN-DE-TEST-E2E.md` § 5 (identifiant `SEC-*`).
- `local/.env` n'est jamais écrasé ni affiché ; nouvelle variable = ajout dans `local/.env.example`.
- `cicd/Jenkinsfile-ci` est le seul pipeline utilisé ; les autres fichiers de `cicd/` sont hérités.
