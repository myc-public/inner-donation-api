# Architecture interne

Vérifié sur le code de `develop` le 19/10/2026. Décisions transverses : ADR de
[platform-handbook](https://github.com/myc-public/platform-handbook/blob/develop/adr/README.md).

## Packages

Racine `src/main/java/ma/myc/inner/donation/` :

| Package | Contenu |
|---|---|
| `api` | Controllers REST : `DonorController`, `DonationController` (back-office), `MyDonorController`, `MyDonationController` (`/me`, donateur connecté) |
| `service` | `DonorService`, `DonationService` et implémentations, transactionnelles (`@Transactional` sur la classe) |
| `repository` | Spring Data JPA (`DonorRepository`, `DonationRepository`) |
| `domain/bo`, `domain/dto` | Entités JPA (`DonorBO`, `DonationBO`) ; DTO d'entrée et de sortie (records) |
| `domain/core/error` | Modèle d'erreur `ApiError` |
| `mapper` | MapStruct (`DonorMapper`, `DonationMapper`) |
| `events` | Enveloppe `EventEnvelope` et payload `DonationCreated` |
| `outbox` | `OutboxEventBO`, `OutboxFactory`, `OutboxEventSaved` (fait technique publié après chaque écriture) |
| `audit` | `AuditRecorder`, `AuditOutboxBO`, `AuditEvent` (K4d) |
| `config/security` | Chaîne de sécurité, conversion des claims, `CurrentParty`, `Permissions` |
| `config/observability` | Logs OpenTelemetry, métriques métier (`OutboxMetricsListener`) |
| `config/properties` | `@ConfigurationProperties` (`myc.*`, `app.outbox`) |
| `exception`, `util` | Exceptions métier et techniques ; utilitaires |

Schéma : Flyway (`src/main/resources/db/migration`, V1 à V3), Hibernate en `validate` uniquement.

## Création d'un don et outbox

`POST /api/v1/donations` (ou `/api/v1/donations/me`) -> `DonationServiceImpl.create`, une seule transaction :

1. le donateur doit exister (404 sinon), vérifié avant toute écriture ;
2. écriture de la ligne `donation` ;
3. construction de l'enveloppe `DonationCreated` (`eventId`, `eventType`, `eventVersion`, `occurredAt`,
   `aggregateType`, `aggregateId`, `producer`, payload avec un instantané du donateur lu en base) ;
4. écriture de la ligne `outbox_event`.

Les deux lignes sont commitées ensemble ou pas du tout. L'API ne connaît pas Kafka (ADR 0018) : le routage (topic,
clé = `aggregate_id`) est laissé au CDC, d'où la suppression des colonnes `topic` et `message_key` (V3). Le relais
CDC (Debezium) n'existe pas encore : voir la roadmap
[EDA](https://github.com/myc-public/platform-handbook/blob/develop/roadmap/eda.md). Les propriétés `app.outbox.*`
(intervalle, lot, tentatives, backoff) sont déclarées pour ce relais ; aucun code de l'API ne les lit.

Après le commit, `OutboxMetricsListener` lit l'événement `OutboxEventSaved` et alimente les métriques métier.

## Audit

Modification et suppression d'un don -> `AuditRecorder`, appelé dans la transaction du service : une ligne
`audit_outbox` existe si et seulement si l'action est commitée. Elle contient l'auteur (issu du token), l'action,
la ressource, l'état avant ou les changements, et le `traceId`. Schéma de l'événement :
`src/main/resources/events/audit-recorded-v1.schema.json`. Décisions : ADR 0017 (DA1-DA7) ; diffusion vers
Elasticsearch prévue en K4e.

## Sécurité

- Resource server OAuth2, deux émetteurs (un par realm), audience `donation-api` (ADR 0014 à 0016).
- `ClaimsJwtAuthenticationConverter` : `scope` -> `SCOPE_…`, claim `permissions` -> autorités ; nom = `party_id`
  (à défaut `azp`, compte de service). Aucune structure propre à Keycloak n'est lue.
- `@PreAuthorize` sur chaque opération avec les constantes de `Permissions` (modèle C, ADR 0013).
- Endpoints `/me` : l'identité vient de `CurrentParty` (`party_id`), jamais d'un paramètre de la requête.
- `/management/**` : chaîne séparée (`SecurityManagementConfig`), basic auth.
