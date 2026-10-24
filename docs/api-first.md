# API First — inner-donation-api 

Référentiel des bonnes pratiques API First, état des lieux du service et plan d'action.

- Date de l'audit : 2026-09-26 (branche `develop`, commit `23375bd`)
- Périmètre : endpoints REST (`/api/v1/donors`, `/api/v1/donations`, `/v1/geo/villes`), gestion des erreurs, documentation OpenAPI, sécurité, événements outbox, tests, CI.

## 1. Bonnes pratiques API First

| # | Pratique | Description |
|---|---|---|
| P1 | Le contrat d'abord | Un fichier `openapi.yaml` versionné dans le dépôt est la source de vérité. Il est relu et validé avant d'écrire le code. |
| P2 | Le code est dérivé du contrat | Interfaces et DTO générés (openapi-generator), ou à défaut vérification en CI que le code correspond au contrat. |
| P3 | Contrat vérifié automatiquement (lint) | Spectral ou Redocly avec un guide de style : nommage, codes HTTP, descriptions, exemples. |
| P4 | Contrôle des changements cassants | openapi-diff en CI contre la version publiée : un changement cassant bloque le build ou impose une nouvelle version majeure. |
| P5 | Design orienté ressources | Noms au pluriel, sous-ressources (`/donors/{id}/donations`), pas de verbes, une seule convention de nommage (camelCase), une seule langue. |
| P6 | Sémantique HTTP correcte | 201 avec en-tête `Location`, 204, 404, 409, 422, 415, 400 pour une entrée mal formée (jamais 500). |
| P7 | Erreurs standardisées (RFC 9457) | `application/problem+json`, `type` stable, code d'erreur métier, `traceId`, aucun détail interne ni donnée personnelle exposés. |
| P8 | Pagination, filtre et tri uniformes | Sur toutes les collections, avec un format de page stable (pas l'objet `PageImpl` de Spring). |
| P9 | Idempotence | En-tête `Idempotency-Key` sur les POST sensibles (un don est un flux financier). |
| P10 | Gestion de la concurrence | `ETag` / `If-Match` avec un champ `@Version` pour éviter l'écrasement silencieux lors des mises à jour. |
| P11 | Versioning et dépréciation | Version dans l'URI, version du contrat alignée sur celle de l'artefact, en-têtes `Deprecation` / `Sunset`. |
| P12 | Sécurité déclarée dans le contrat | Scopes OAuth2 par opération ; la sécurité réellement appliquée correspond au contrat. |
| P13 | Contrat documenté | Descriptions, exemples, contraintes (min, max, format, enum), toutes les réponses d'erreur possibles. |
| P14 | Contrat publié | `openapi.yaml` publié comme artefact (Nexus) ou dans un catalogue, pour que les consommateurs génèrent leur SDK ou un mock. |
| P15 | Tests de conformité au contrat | Les réponses réelles sont validées contre le schéma OpenAPI (swagger-request-validator dans MockMvc). |
| P16 | Mock à partir du contrat | Prism permet aux consommateurs de travailler avant que l'implémentation existe. |
| P17 | Événements sous contrat | Contrat AsyncAPI pour les événements publiés via l'outbox (`DonationCreated`). |

## 2. État des lieux

### 2.1 Ce qui existe

- Version dans l'URI (`/api/v1/...`), identifiants UUID, `PATCH` pour les mises à jour partielles, 201 / 204 sur donors et donations.
- Documentation générée depuis le code par springdoc 3.0.3 (désactivée en preprod et prod), schéma de sécurité OAuth2 déclaré.
- Modèle d'erreur proche de la RFC 7807 (`ApiError` : type, title, status, detail, instance, traceId, errors[]).
- Validation Bean Validation sur les DTO d'entrée.
- Pagination sur `GET /api/v1/donors`.
- Enveloppe d'événement versionnée (`eventType`, `eventVersion`) écrite dans `outbox_event`.

### 2.2 Ce qui est partiel ou incorrect

| # | Constat | Où | Pratique |
|---|---|---|---|
| E1 | Approche code-first : aucun contrat versionné, le Swagger est déduit du code | tout le projet | P1, P2 |
| E2 | **Faille de sécurité** : le scope `inner:donation` n'est exigé que sur `/v1/**` alors que les endpoints sont en `/api/v1/**`. Donors et donations demandent seulement un token valide, sans vérification du scope | `config/security/SecurityConfig.java` | P12 |
| E3 | Préfixe en double derrière nginx (`/api/donation/` + `/api/v1/...`) | controllers + `myc.context` | P5 |
| E4 | `VilleAPI` : chemin `/v1/geo/villes` sans `/api`, français, paramètre snake_case (`pays_code`), POST qui renvoie 200, méthode POST nommée `getAllVilles` | `api/VilleAPI.java` | P5, P6 |
| E5 | `/donations/by-donor/{donorId}` n'est pas orienté ressource | `api/DonationController.java` | P5 |
| E6 | `GET /donations` non paginé (`findAll`) ; `GET /donors` renvoie le `PageImpl` de Spring (format instable) | controllers | P8 |
| E7 | Aucun en-tête `Location` sur les réponses 201 | POST donors et donations | P6 |
| E8 | **Un UUID invalide dans l'URL renvoie 500** (`MethodArgumentTypeMismatchException` interceptée comme `RuntimeException`) | `config/ErrorHandlingAdvice.java` | P6, P7 |
| E9 | **Un email déjà utilisé lors d'un PATCH renvoie 500** (`IllegalArgumentException`) ; une violation de contrainte en base aussi | `service/DonorServiceImpl.java` | P6 |
| E10 | Erreurs servies en `application/json` et non `application/problem+json` ; le `detail` expose des messages internes (Jackson, `MethodArgumentNotValidException`) ; le 409 révèle l'email (donnée personnelle) | `ErrorHandlingAdvice`, `DonorServiceImpl` | P7 |
| E11 | `BusinessException` renvoie 400 au lieu de 422 | `ErrorHandlingAdvice` | P6 |
| E12 | Champ `type: boolean` sans signification claire (enum `ONE_SHOT` / `RECURRING` attendue), champ `timestamp` ambigu | DTO donation | P5, P13 |
| E13 | La création d'une donation ne vérifie pas l'existence du donateur (`findDonor` jamais appelé) ; le client envoie lui-même un snapshot du donateur | `service/DonationServiceImpl.java` | P6 |
| E14 | Pas de descriptions ni d'exemples, aucune `@ApiResponse` sur donors et donations | controllers, DTO | P13 |
| E15 | Version du contrat `1.0.0` codée en dur alors que le pom est en `1.1.0` | `application.yml` (`myc.docs.version`) | P11 |
| E16 | Flux OAuth `clientCredentials` déclaré avec une `authorizationUrl` inutile ; aucun scope par opération | `config/OpenApiConfig.java` | P12 |
| E17 | Aucun test de controller pour donors et donations (seul `VilleApiTest` existe) | `src/test` | P15 |

### 2.3 Ce qui est absent

| Pratique | Manque |
|---|---|
| P3 | Lint du contrat |
| P4 | Détection des changements cassants (openapi-diff) |
| P9 | `Idempotency-Key` sur `POST /donations` |
| P10 | `@Version` + `ETag` / `If-Match` |
| P11 | Politique de dépréciation (`Deprecation` / `Sunset`) |
| P14 | Publication du contrat |
| P15 | Tests de conformité au contrat |
| P16 | Mock à partir du contrat |
| P17 | Contrat AsyncAPI des événements |

## 4. Décisions à prendre

| # | Question | Options | Décision |
|---|---|---|---|
| D1 | Mode API First | (a) Contract-first avec génération de code (recommandé) ; (b) contrat écrit à la main et vérifié contre le code | **(a)** — openapi-generator en `interfaceOnly` : le yaml génère les interfaces (`DonorsApi`, `DonationsApi`) et les DTO, les controllers les implémentent. Validé le 2026-09-26 |
| D2 | Changements cassants | (a) Corriger directement en v1 (API pas encore en production) ; (b) préserver l'existant et ouvrir une v2 | **(a)** — corrections directement en v1, pas de consommateur externe à préserver. Validé le 2026-09-26 |
| D3 | `VilleAPI` | (a) L'aligner sur les autres ressources ; (b) la supprimer (reste du squelette initial) | **(b)** — suppression du controller, du service, du repository, des DTO, des entités (`City`, `Country`) et des tests associés (intégrée au lot L0). Validé le 2026-09-26 |

## Plan d'action et suivi

Lots L0 à L8 et leur avancement : [platform-handbook, roadmap/api-first.md](https://github.com/myc-public/platform-handbook/blob/develop/roadmap/api-first.md).
