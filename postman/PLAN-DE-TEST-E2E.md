# Plan de test de bout en bout : donation-api securisee (lots K1 a K6)

## 1. Objectif et perimetre

Verifier de bout en bout que la chaine securisee de donation-api fonctionne et resiste aux abus. La section 5 est
l'**inventaire exhaustif des tests de securite** realises depuis K1 : chaque test y a un identifiant, la fonction
testee, la commande et le resultat attendu.

| Lot | Ce qui est verifie |
|---|---|
| K1 | Keycloak as code : deux realms en ligne, clients, flux autorises et refuses |
| K2 | L'API exige un token valide (signature, emetteur, audience) et le bon scope |
| K3 | Matrice RBAC (modele C) : chaque role n'a que ses permissions |
| K4a / K4b | Identites internes / externes, contrat de claims (`party_id`, `actor_type`, `permissions`, `team_id`) |
| K4c | ABAC donateur : endpoints `/me`, aucun acces aux donnees d'un autre |
| K4d | Audit : toute modification / suppression d'une donation produit une ligne `audit_outbox` |
| K5 | Passerelles : WAF, liste blanche, tokens falsifies, scopes, en-tetes, isolation reseau, limitation de debit |
| O1 | Deploiement OpenShift : une seule entree publique, IP reelle, secrets hors Git |
| K6 | Front + BFF : session serveur, OIDC code + PKCE, refresh, deconnexion, back-channel (spike fait, application a venir) |

Hors perimetre : contrat OpenAPI (K7), observabilite securite (K8), MFA des internes (K6b).

## 2. Environnements

| Mode | Chaine | Environnement Postman | Usage |
|---|---|---|---|
| **dev** | Postman -> API (`http://localhost:8080`) et Keycloak (`http://localhost:8180`) en direct | `e2e-dev` | Developpement : K1 a K4 |
| **pile** | Postman -> APISIX externe (`https://localhost:8443`) -> APISIX interne -> API | `e2e-pile` | **Recette** : K1 a K5, chaine complete |
| **sandbox** | Postman -> Route `donation` du Sandbox OpenShift (TLS) -> APISIX externe -> APISIX interne -> API | `e2e-sandbox` | **Recette OpenShift** : meme chaine que la pile, `mode=pile` sur l'URL publique |

Le dossier **08 (passerelles)** ne s'execute qu'en mode pile (`e2e-pile`, `e2e-sandbox` ; ignore en mode dev).
En mode pile, `/management/**` et `/docs/**` ne sont pas exposes (attendu : 404).

Variables propres a un environnement : `hstsMaxAge` (valeur HSTS exacte attendue : 0 en local, 1 an sur le Sandbox ;
vide = presence seule), `httpBaseUrl` (entree http a rediriger vers https, G15 ; vide en local, ou il n'y en a pas).

Variante **avant APISIX externe** (deploiement par etapes) : l'`iss` des tokens est deja l'URL publique, pas encore
exposee. Mettre `mode=dev`, `baseUrl` = Route de l'API, `keycloakUrl=http://localhost:18080` (`ocs port-forward
svc/keycloak 18080:8080`) et `issuerUrl` = URL publique : la collection compare l'`issuer` a `issuerUrl` et envoie le
formulaire de login a `keycloakUrl`. `issuerUrl` vide : comportement inchange.

## 3. Prerequis

- Mode dev : `gitops-platform/local/keycloak` et `inner-donation-api/local` demarres, API lancee (`mvn spring-boot:run`).
- Mode pile : `gitops-platform/local/stack/README.md` (certificat, `mvn package`, `docker compose up -d --build`),
  **mode dev arrete**.
- Environnement Postman : recopier les trois secrets depuis le `.env` du mode utilise
  (`DONATION_SERVICE_CLIENT_SECRET`, `DONATION_TESTS_CLIENT_SECRET`, `TEST_USERS_PASSWORD`).
  Ne jamais exporter un environnement rempli dans le depot.
- Mode pile : certificat auto-signe -> Postman, Settings > General > SSL certificate verification : OFF.
- Mode sandbox : connecte au Sandbox (`ocs whoami`) pour lire les secrets ; certificat valide (verification SSL active).
  Secrets : lus dans les Secrets du Sandbox et copies **dans le presse-papiers sans affichage**, un par un, puis colles
  dans la valeur courante de l'environnement `e2e-sandbox` :

  ```powershell
  function Copy-KcSecret($cle) {
    $b64 = ocs get secret keycloak-realm-secret -o jsonpath="{.data.$cle}"
    [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($b64)) | Set-Clipboard
    "$cle copie dans le presse-papiers"
  }
  Copy-KcSecret DONATION_SERVICE_CLIENT_SECRET   # -> donationServiceClientSecret
  Copy-KcSecret DONATION_TESTS_CLIENT_SECRET     # -> donationTestsClientSecret
  Copy-KcSecret TEST_USERS_PASSWORD              # -> testUsersPassword
  Set-Clipboard -Value " "                       # vider le presse-papiers a la fin
  ```

## 4. Execution

**Collection Runner de Postman** : collection `e2e-donation`, environnement `e2e-pile` (ou `e2e-dev`, `e2e-sandbox`),
toute la collection, dans l'ordre. Les tokens durent 5 minutes : relancer toute la collection plutot qu'un dossier.

**Newman** (secrets passes en variables, jamais ecrits sur disque) : commande `SEC-RUN` (section 5.0).

Puis les autres controles de la section 5. La **recette securite complete** = toute la section 5, dans l'ordre.

## 5. Securite : inventaire exhaustif

### 5.0 Mode d'emploi

**Identifiants** : `SEC-<domaine>-<numero>`, stables (un test retire garde son numero, marque « retire »).
Domaines : `API` (tests automatises), `REALM` (realm as code), `KC` (Keycloak), `AUTHZ` (authentification,
autorisation, audit), `GW` (passerelles et bordure), `PF` (plateforme OpenShift), `OBS` (observabilite),
`SECRET` (secrets et acces), `BFF` (front + BFF).

**Colonnes** : fonction testee, commande (ou requete de la collection `e2e-donation`, lancee par `SEC-RUN`), resultat
attendu, modes (`dev`, `pile`, `sandbox`), origine (lot et date de premiere validation).

**Regle** : recette securite complete a la fin de chaque lot qui touche la securite, **obligatoire a la fin de K6**
(pile puis Sandbox). Le resultat (vert, ou ecarts avec leur cause) est consigne dans le journal de la ROADMAP. Un
test « nouveau » n'a pas encore ete execute sous cette forme : il est valide a la premiere recette.

**Variables communes** (PowerShell 7 ; une fenetre par recette ; aucun secret affiche) :

```powershell
$mode = 'pile'   # dev | pile | sandbox
$ks = "$HOME\.kube\sandbox.config"; $ns = 'gregorie769-dev'
$pub = 'https://donation-gregorie769-dev.apps.rm3.7wse.p1.openshiftapps.com'
switch ($mode) {
  'dev'     { $api = 'http://localhost:8080';  $kc = 'http://localhost:8180'; $envFile = 'D:\workspace\public\gitops-platform\local\keycloak\.env' }
  'pile'    { $api = 'https://localhost:8443'; $kc = $api;                    $envFile = 'D:\workspace\public\gitops-platform\local\stack\.env' }
  'sandbox' { $api = $pub;                     $kc = $api }
}
$k = if ($mode -eq 'pile') { '-k' } else { $null }            # curl : certificat auto-signe de la pile
function Get-TestSecret([string]$cle) {                         # retourne la valeur, ne l'affiche jamais
  if ($mode -eq 'sandbox') {
    $b64 = oc --kubeconfig $ks get secret keycloak-realm-secret -n $ns -o jsonpath="{.data.$cle}"
    return [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($b64)) }
  ((Get-Content $envFile | Where-Object { $_ -match "^$cle=" }) -replace "^$cle=", '').Trim().Trim('"') }
function Get-SvcToken {                                         # donation-service, donation:read, 300 s
  (Invoke-RestMethod -Method Post "$kc/realms/myc-internal/protocol/openid-connect/token" -SkipCertificateCheck:($mode -eq 'pile') `
    -Body @{ grant_type = 'client_credentials'; client_id = 'donation-service'; client_secret = (Get-TestSecret DONATION_SERVICE_CLIENT_SECRET); scope = 'donation:read' }).access_token }
```

**SEC-RUN : collection `e2e-donation` par Newman** (toute la collection : les dossiers dependent des tokens du dossier 01) :

```powershell
cd D:\workspace\public\inner-donation-api\postman
$opt = if ($mode -eq 'pile') { '--insecure' } else { $null }
npx --yes newman@6.2.1 run e2e-donation.postman_collection.json -e "e2e-$mode.postman_environment.json" $opt `
  --env-var "donationServiceClientSecret=$(Get-TestSecret DONATION_SERVICE_CLIENT_SECRET)" `
  --env-var "donationTestsClientSecret=$(Get-TestSecret DONATION_TESTS_CLIENT_SECRET)" `
  --env-var "testUsersPassword=$(Get-TestSecret TEST_USERS_PASSWORD)"
```

Attendu : toutes les assertions vertes, G2 ignore (section 7). Ne jamais ajouter `--export-environment` (secrets
ecrits sur disque).

### 5.1 API : tests automatises (K2, K3, K4, K8)

Commande commune (dans `inner-donation-api`, aucun service requis) :

```powershell
cd D:\workspace\public\inner-donation-api
mvn -q test "-Dtest=JwtValidationTest,SecurityConfigTest,PermissionsTest,EndpointsSecuredTest,MyEndpointsTest,ClaimsJwtAuthenticationConverterTest,SecurityUtilsTest,SecurityHandlersLogLevelTest,ErrorHandlingAdviceLogLevelTest,AuditRecorderTest"
```

| ID | Fonction testee | Classe | Attendu | Origine |
|---|---|---|---|---|
| SEC-API-01 | Validation du JWT : emetteur, audience `donation-api`, expiration, signature | `JwtValidationTest` | vert | K2, 01/10 |
| SEC-API-02 | Chaine securisee par defaut : sans token 401, scope manquant 403, scope correct 200 | `SecurityConfigTest` | vert | K2 / L0, 01/10 |
| SEC-API-03 | Matrice des permissions (modele C, 22 cas) | `PermissionsTest` | vert | K3, 01/10 |
| SEC-API-04 | Garde-fou : tout endpoint porte un `@PreAuthorize` (verifie par mutation : un endpoint sans annotation rend le test rouge) | `EndpointsSecuredTest` | vert | K3, 01/10 |
| SEC-API-05 | ABAC `/me` : identite = `party_id` du token, jamais de l'URL ou du corps | `MyEndpointsTest` | vert | K4c, 03/10 |
| SEC-API-06 | Contrat de claims : `permissions`, `party_id`, `actor_type` ; aucune structure Keycloak lue | `ClaimsJwtAuthenticationConverterTest`, `SecurityUtilsTest` | vert | K4b, 02/10 |
| SEC-API-07 | Niveaux de log : 401 / 403 / 4xx en WARN, 5xx seules en ERROR | `SecurityHandlersLogLevelTest`, `ErrorHandlingAdviceLogLevelTest` | vert | K8, 10/10 |
| SEC-API-08 | Audit : auteur, action, avant / apres, sans donnee personnelle du donateur | `AuditRecorderTest` | vert | K4d, 04/10 |

### 5.2 Realm as code (K1, K4a)

| ID | Fonction testee | Commande | Attendu | Modes | Origine |
|---|---|---|---|---|---|
| SEC-REALM-01 | Regle Groupe -> Role metier -> Permission (jamais groupe -> permission ni utilisateur -> role) | `cd D:\workspace\public\gitops-platform; python tools/check-realm-rbac.py` | code de sortie 0 ; rouge sur une copie fautive du realm | tous | K4a, 02/10 |
| SEC-REALM-02 | Reimport du realm idempotent (configuration as code, pas de derive) | `cd D:\workspace\public\gitops-platform; docker compose -f local/keycloak/compose.yaml --env-file local/keycloak/.env run --rm keycloak-config; $LASTEXITCODE`, deux fois de suite | 0 les deux fois ; SEC-RUN toujours vert | dev | K1, 28/09 |

### 5.3 Keycloak : clients, flux et identites (K1, K4a, K4b)

| ID | Fonction testee | Commande | Attendu | Modes | Origine |
|---|---|---|---|---|---|
| SEC-KC-01 | Decouverte OIDC des deux realms, `issuer` public | SEC-RUN, 00 / K1a-K1b | 200, `issuer` = URL publique + realm | tous | K1, 28/09 |
| SEC-KC-02 | Client credentials : claims du contrat ; sans scope, pas de scope API | SEC-RUN, 01 / tokens `donation-service` | 200, `aud`, scope, `party_id` attendus | tous | K1, 28/09 |
| SEC-KC-03 | Code + PKCE des 6 utilisateurs de test (internes et externes) | SEC-RUN, 01 / Login 1/3 a 3/3 | 302 puis 200 ; permissions, `party_id`, `actor_type` par role | tous | K3-K4b, 01-02/10 |
| SEC-KC-04 | Console de compte : `party_id` invisible et non modifiable par l'utilisateur | SEC-RUN, 05 / M7 | API de compte 200, `party_id` absent | tous | K4a, 02/10 |
| SEC-KC-05 | Scope non autorise pour le client refuse | voir ci-dessous | 400 `invalid_scope` | tous | K1, 28/09 (nouveau sous cette forme) |
| SEC-KC-06 | Client confidentiel sans secret refuse | voir ci-dessous | 401 `invalid_client` | tous | K1, 28/09 (nouveau sous cette forme) |
| SEC-KC-07 | Grant « password » refuse (desactive sur tous les clients) | voir ci-dessous | 400 `unauthorized_client` | tous | K1, 28/09 (nouveau sous cette forme) |
| SEC-KC-08 | PKCE obligatoire : demande d'autorisation sans `code_challenge` refusee | voir ci-dessous | 302 vers le `redirect_uri` avec `error=invalid_request` (code_challenge manquant), aucun code | tous | K1, 28/09 (nouveau sous cette forme) |
| SEC-KC-09 | `redirect_uri` etrangere refusee | voir ci-dessous | 400 (« Invalid parameter: redirect_uri »), aucune redirection | tous | K1, 28/09 (nouveau sous cette forme) |

```powershell
$tok = "$kc/realms/myc-internal/protocol/openid-connect/token"
$auth = "$kc/realms/myc-internal/protocol/openid-connect/auth"
$svc = Get-TestSecret DONATION_SERVICE_CLIENT_SECRET
# SEC-KC-05 : donation-service n'a que donation:read
curl.exe -s $k -w "`n%{http_code}`n" -d grant_type=client_credentials -d client_id=donation-service -d "client_secret=$svc" -d "scope=donation:write" $tok
# SEC-KC-06 : sans secret
curl.exe -s $k -w "`n%{http_code}`n" -d grant_type=client_credentials -d client_id=donation-service $tok
# SEC-KC-07 : grant password (identifiants factices : le refus porte sur le grant)
curl.exe -s $k -w "`n%{http_code}`n" -d grant_type=password -d client_id=donation-service -d "client_secret=$svc" -d username=x -d password=x $tok
Remove-Variable svc
# SEC-KC-08 : sans code_challenge (client donation-tests, redirect_uri enregistree)
curl.exe -s $k -o NUL -w "%{http_code} %{redirect_url}`n" "$auth`?client_id=donation-tests&response_type=code&scope=openid&redirect_uri=https%3A%2F%2Foauth.pstmn.io%2Fv1%2Fcallback"
# SEC-KC-09 : redirect_uri etrangere (avec code_challenge)
curl.exe -s $k -o NUL -w "%{http_code} %{redirect_url}`n" "$auth`?client_id=donation-tests&response_type=code&scope=openid&code_challenge=E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM&code_challenge_method=S256&redirect_uri=https%3A%2F%2Fevil.example%2Fcb"
```

### 5.4 API : authentification, autorisation, ABAC, CORS, audit (K2 a K4d)

| ID | Fonction testee | Commande | Attendu | Modes | Origine |
|---|---|---|---|---|---|
| SEC-AUTHZ-01 | Sans token / token altere | SEC-RUN, 02 / A1, A5 | 401 + `WWW-Authenticate: Bearer` | tous | K2, 01/10 |
| SEC-AUTHZ-02 | Scope insuffisant / absent (audience seule) | SEC-RUN, 02 / A3, A4 | 403 | tous | K2, 01/10 |
| SEC-AUTHZ-03 | Matrice RBAC : agent, admin, superviseur, beneficiaire, donateur | SEC-RUN, 03 / R4-R19 | 2xx si la permission est presente, 403 `problem+json` sinon | tous | K3, 01/10 |
| SEC-AUTHZ-04 | CORS ferme | SEC-RUN, 04 / C1 | origine etrangere refusee | tous | K2, 01/10 |
| SEC-AUTHZ-05 | ABAC donateur : profil = `party_id`, don d'un autre invisible, sans profil 409, internes sans `:own` 403, beneficiaire 403 | SEC-RUN, 06 / C1-C12 | conforme au libelle de chaque requete | tous | K4c, 03/10 |
| SEC-AUTHZ-06 | Audit : creation non auditee, PATCH / DELETE admin audites, 403 / 404 non audites ; claims `team_id`, `preferred_username` | SEC-RUN, 07 / A1-A8 | conforme au libelle de chaque requete | tous | K4d, 05/10 |
| SEC-AUTHZ-07 | Parcours croise donateur / agent / admin (aucune fuite entre donateurs) | SEC-RUN, 09 / P1-P9 | 201, 200, 403, 200, 200 (90), 404, 204, 404 | tous | K4d, 05/10 |
| SEC-AUTHZ-08 | Lignes d'audit en base pour le parcours | requete SQL ci-dessous | `DonationUpdated` (`75.00` -> `90.00`) puis `DonationDeleted`, auteur `admin.test`, `occurredAt` a la milliseconde | tous | K4d, 05/10 |
| SEC-AUTHZ-09 | Signature croisee : token d'un realm presente avec l'`iss` de l'autre | commande ci-dessous | 401 (signature verifiee avec les cles du realm annonce) | tous | K5 spike, 07/10 (nouveau sous cette forme) |

SEC-AUTHZ-08 :

```powershell
$sql = @'
SELECT event_type, aggregate_id, JSON_UNQUOTE(JSON_EXTRACT(payload, '$.actor.username')) AS auteur,
       JSON_EXTRACT(payload, '$.changes') AS changes, JSON_UNQUOTE(JSON_EXTRACT(payload, '$.occurredAt')) AS quand
FROM audit_outbox ORDER BY occurred_at DESC LIMIT 6;
'@
# pile (mode dev : cd inner-donation-api et -f local/compose.yaml)
cd D:\workspace\public\gitops-platform\local\stack
$sql | docker compose exec -T donation-api-mysql sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" mysql -t -u "$MYSQL_USER" "$MYSQL_DATABASE"'
# sandbox (oc --kubeconfig : la fonction ocs avale le --)
$sql | oc --kubeconfig $ks exec -i deploy/donation-api-mysql -n $ns -- sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" mysql -t -u "$MYSQL_USER" "$MYSQL_DATABASE"'
```

Comparer avec `e2eDonationId` (console Postman ou sortie Newman).

SEC-AUTHZ-09 :

```powershell
function ConvertFrom-B64Url($s) { $s = $s.Replace('-', '+').Replace('_', '/'); $s += '=' * ((4 - $s.Length % 4) % 4); [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($s)) }
function ConvertTo-B64Url($t) { [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($t)).TrimEnd('=').Replace('+', '-').Replace('/', '_') }
$p = (Get-SvcToken).Split('.')
$faux = "$($p[0]).$(ConvertTo-B64Url ((ConvertFrom-B64Url $p[1]).Replace('/realms/myc-internal', '/realms/myc-customers'))).$($p[2])"
curl.exe -s $k -o NUL -w "signature croisee -> %{http_code}`n" -H "Authorization: Bearer $faux" "$api/api/v1/donors"
Remove-Variable p, faux
```

### 5.5 Passerelles et bordure (K5)

| ID | Fonction testee | Commande | Attendu | Modes | Origine |
|---|---|---|---|---|---|
| SEC-GW-01 | WAF : injection SQL en parametre, traversee de chemin | SEC-RUN, 08 / G1, G3 | 403 par le WAF (corps sans `/problem/`) | pile, sandbox | K5, 07/10 |
| SEC-GW-02 | WAF : XSS dans un corps JSON | SEC-RUN, 08 / G2 | **ignore** : corps non inspectes (section 7, dette 16) | pile, sandbox | K5, 08/10 |
| SEC-GW-03 | WAF : pas de faux positif sur des donnees legitimes (accents, apostrophe, tiret), PATCH / DELETE autorises | SEC-RUN, 08 / G4-G5 | 201 puis 204 | pile, sandbox | K5, 07-08/10 |
| SEC-GW-04 | WAF : Content-Type non supporte refuse a la bordure | collection `observabilite`, S2 cas 5 (`OBSERVABILITY-TEST.md` 1 bis) | 403 (regle CRS 920420) | pile, sandbox | 10/10 |
| SEC-GW-05 | Liste blanche : console admin Keycloak, realm master, chemin inconnu, `/management`, `/docs` | SEC-RUN, 08 / G6-G8 et 00 / S1-S4 | 404 | pile, sandbox | K5, 07/10 |
| SEC-GW-06 | APISIX interne : sans token, emetteur inconnu | SEC-RUN, 08 / G9-G10 | 401 par APISIX interne, l'API n'est pas appelee | pile, sandbox | K5, 07/10 |
| SEC-GW-07 | Token falsifie (permissions ajoutees, signature d'origine) | SEC-RUN, 08 / G11 | 401 | pile, sandbox | K5, 07/10 |
| SEC-GW-08 | Scope a la passerelle : lecture seule en ecriture | SEC-RUN, 08 / G12 | 403 par APISIX interne | pile, sandbox | K5, 07/10 |
| SEC-GW-09 | En-tetes de securite sur l'API et sur Keycloak | SEC-RUN, 08 / G13-G14 | HSTS (= `hstsMaxAge`), `nosniff`, `SAMEORIGIN`, `Referrer-Policy`, `X-Request-Id` | pile, sandbox | K5, 07/10 |
| SEC-GW-10 | http redirige vers https | SEC-RUN, 08 / G15 | 301 / 302 vers `https://` | sandbox | O1, 10/10 |
| SEC-GW-11 | Isolation : seul 8443 publie sur le poste | commande ci-dessous | `8443 OUVERT`, autres `ferme` | pile | K5, 08/10 |
| SEC-GW-12 | Limitation de debit par IP : 20 req/s, rafale 40 | commande ci-dessous | ~60-70 `HTTP 200`, le reste `HTTP 429` ; sandbox : des 429 puis `poste 200` (autre client non limite) | pile, sandbox | K5, 08/10 ; O1, 10/10 |
| SEC-GW-13 | IP reelle : un `X-Forwarded-For` fourni par le client est ignore (pas de contournement de la limite) | variante de SEC-GW-12 ci-dessous | memes 429 qu'en SEC-GW-12 malgre une IP differente par requete | sandbox | O1 spike S3, 09/10 (nouveau sous cette forme) |
| SEC-GW-14 | Journal du WAF : regle et `request_id` traces | commande ci-dessous | lignes « Coraza: Access denied » avec regle (ex. 949111) et `request_id` | pile, sandbox | K5, 08/10 |

SEC-GW-11 :

```powershell
8080, 8180, 3307, 8025, 9080, 8443 | ForEach-Object {
  $ouvert = Test-NetConnection -ComputerName localhost -Port $_ -WarningAction SilentlyContinue -InformationLevel Quiet
  "{0,-5} {1}" -f $_, $(if ($ouvert) { 'OUVERT' } else { 'ferme' }) }
```

SEC-GW-12 (120 requetes simultanees depuis le reseau interne : Postman et un poste Windows ne depassent pas
~16 req/s, insuffisant pour declencher la limite) :

```powershell
$lua = @'
local http = require("resty.http")
local codes, t = {}, {}
for i = 1, 120 do
  t[i] = ngx.thread.spawn(function()
    local r = http.new():request_uri("http://apisix-ext:9080/realms/myc-internal/.well-known/openid-configuration")
    return r and r.status or 0
  end)
end
for i = 1, 120 do local _, s = ngx.thread.wait(t[i]); codes[s] = (codes[s] or 0) + 1 end
for k, v in pairs(codes) do print("HTTP ", k, " : ", v) end
'@
# pile
cd D:\workspace\public\gitops-platform\local\stack
$lua | docker compose exec -T apisix-int sh -c 'cd /usr/local/apisix && resty -c 512 -I deps/share/lua/5.1 /dev/stdin'
# sandbox : depuis le pod apisix-internal vers l'URL publique (par le routeur, comme un client Internet), puis le poste
$luaS = $lua.Replace('http://apisix-ext:9080', $pub).Replace('openid-configuration")', 'openid-configuration", { ssl_verify = false })')
$luaS | oc --kubeconfig $ks exec -i deploy/apisix-internal -n $ns -- sh -c 'cd /usr/local/apisix && resty -c 512 -I deps/share/lua/5.1 /dev/stdin'
curl.exe -s -o NUL -w "poste %{http_code}`n" "$pub/realms/myc-internal/.well-known/openid-configuration"
```

SEC-GW-13 (sandbox ; attendre une minute apres SEC-GW-12) : chaque requete annonce une IP differente.

```powershell
$luaX = $luaS.Replace('{ ssl_verify = false })', '{ ssl_verify = false, headers = { ["X-Forwarded-For"] = "198.51.100." .. i } })')
$luaX | oc --kubeconfig $ks exec -i deploy/apisix-internal -n $ns -- sh -c 'cd /usr/local/apisix && resty -c 512 -I deps/share/lua/5.1 /dev/stdin'
```

SEC-GW-14 :

```powershell
# pile
cd D:\workspace\public\gitops-platform\local\stack
docker compose logs apisix-ext | Select-String "Coraza: Access denied" | Select-Object -Last 5
# sandbox
oc --kubeconfig $ks logs deploy/apisix-external -n $ns --since=30m | Select-String "Coraza: Access denied" | Select-Object -Last 5
```

### 5.6 Plateforme OpenShift (O1)

| ID | Fonction testee | Commande | Attendu | Modes | Origine |
|---|---|---|---|---|---|
| SEC-PF-01 | Etat du deploiement, une seule entree publique de la chaine, TLS edge | commande ci-dessous | apps `Synced` / `Healthy` ; pods 1/1 (hors builds et Job termines) ; Route `donation` en `edge`, seule Route de la chaine (`grafana`, `jenkins` hors chaine) | sandbox | O1, 10/10 |
| SEC-PF-02 | L'API n'est plus joignable directement (Route `donation-api` supprimee) | commande ci-dessous | 503 (routeur : aucune application) | sandbox | O1-5, 10/10 |
| SEC-PF-03 | NetworkPolicy declarees pour chaque composant | `oc --kubeconfig $ks get networkpolicy -n $ns` | les 7 politiques de la chaine presentes (sans effet sur le Sandbox : section 7, dette 17) | sandbox | O1-5, 10/10 |

```powershell
# SEC-PF-01
k --context minikube get applications -n argocd
oc --kubeconfig $ks get pods -n $ns
oc --kubeconfig $ks get routes -n $ns -o custom-columns=NOM:.metadata.name,HOTE:.spec.host,TLS:.spec.tls.termination
# SEC-PF-02
curl.exe -s -o NUL -w "ancienne Route de l'API -> %{http_code}`n" https://donation-api-gregorie769-dev.apps.rm3.7wse.p1.openshiftapps.com/api/v1/donors
```

### 5.7 Observabilite (exposition et fuites)

| ID | Fonction testee | Commande | Attendu | Modes | Origine |
|---|---|---|---|---|---|
| SEC-OBS-01 | Grafana refuse l'anonyme | `OBSERVABILITY-TEST.md` section 5 | 401 | sandbox | 26/09, revalide 10/10 |
| SEC-OBS-02 | Reception OTLP non exposee publiquement | `OBSERVABILITY-TEST.md` section 5 | 302 (login Grafana), pas d'ingestion | sandbox | 26/09, revalide 10/10 |
| SEC-OBS-03 | Aucun token dans les logs des composants de la chaine | commande ci-dessous (apres SEC-RUN) | `0` pour chaque composant | pile, sandbox | nouveau |

```powershell
$motif = 'eyJ[A-Za-z0-9_-]{10,}\.eyJ|Bearer [A-Za-z0-9._-]{20,}'
# pile
cd D:\workspace\public\gitops-platform\local\stack
'apisix-ext', 'apisix-int', 'donation-api', 'keycloak' | ForEach-Object {
  "{0,-15} {1}" -f $_, (docker compose logs --no-color $_ | Select-String -Pattern $motif | Measure-Object).Count }
# sandbox
'apisix-external', 'apisix-internal', 'donation-api', 'keycloak' | ForEach-Object {
  "{0,-15} {1}" -f $_, (oc --kubeconfig $ks logs deploy/$_ -n $ns --since=2h | Select-String -Pattern $motif | Measure-Object).Count }
```

### 5.8 Secrets et acces

| ID | Fonction testee | Commande | Attendu | Modes | Origine |
|---|---|---|---|---|---|
| SEC-SECRET-01 | Aucun fichier secret suivi par Git (7 depots) | commande ci-dessous | aucune ligne | poste | O7, 24/09 (nouveau sous cette forme) |
| SEC-SECRET-02 | Environnements Postman versionnes sans secret | commande ci-dessous | aucune ligne | poste | K5, 08/10 (nouveau sous cette forme) |
| SEC-SECRET-03 | Tokens OpenShift : aucun token expose encore actif | commande ci-dessous | `actif : False` pour chaque token trouve en clair ; `ocs logout` sinon | sandbox | dette 1, 10/10 |
| SEC-SECRET-04 | Sauvegarde des fichiers locaux chiffree, en-tetes compris | commande ci-dessous | la liste sans mot de passe echoue ; `7z t` avec mot de passe : `Everything is Ok` | poste | backup v3, 10/10 |

```powershell
# SEC-SECRET-01
$motif = '(^|/)\.env$|\.dev\.yaml$|\.tfvars$|\.tfstate|postman_test_run|\.key$|\.pem$|motpass'
foreach ($d in 'craft-platform', 'gen-ai-api', 'gitops-platform', 'inner-donation-api', 'inner-order-api', 'jenkins-on-openshift-manifests', 'jenkins-on-openshift-with-terraform') {
  git -C "D:\workspace\public\$d" ls-files | Select-String -Pattern $motif | ForEach-Object { "$d : $_" } }
# SEC-SECRET-02 : valeur non vide pour une cle de secret
Get-ChildItem D:\workspace\public\inner-donation-api\postman\*.postman_environment.json | ForEach-Object {
  $f = $_.Name; (Get-Content $_ -Raw | ConvertFrom-Json).values |
    Where-Object { $_.key -match 'Secret|Password|token' -and $_.value } | ForEach-Object { "$f : $($_.key) renseigne" } }
# SEC-SECRET-03 : $t = token trouve en clair (jamais affiche) ; nom de l'objet = sha256~ + empreinte base64url
function Get-OAuthTokenName([string]$t) {
  $h = [Security.Cryptography.SHA256]::HashData([Text.Encoding]::UTF8.GetBytes($t -replace '^sha256~', ''))
  'sha256~' + [Convert]::ToBase64String($h).TrimEnd('=').Replace('+', '-').Replace('/', '_') }
$actifs = ocs get useroauthaccesstokens -o jsonpath='{.items[*].metadata.name}'
"actif : $($actifs -split ' ' -contains (Get-OAuthTokenName $t))"
# SEC-SECRET-04
& 'C:\Program Files\7-Zip\7z.exe' l D:\backup\socle-sandbox-v3-20261010\fichiers-locaux.7z -p"faux"   # attendu : erreur
& 'C:\Program Files\7-Zip\7z.exe' t D:\backup\socle-sandbox-v3-20261010\fichiers-locaux.7z            # mot de passe demande
```

### 5.9 Front + BFF (K6)

Valides par le spike du 12/10 (serveur Node jetable, Keycloak local, Redis `noeviction`). A rejouer sur
l'application `donation-web` des qu'elle existe, puis a automatiser (Vitest, Playwright : decision D6-14). Les
commandes Redis passent par `REDISCLI_AUTH` (mot de passe jamais affiche) ; les identifiants de session sont masques.

| ID | Fonction testee | Commande / procedure | Attendu | Modes | Origine |
|---|---|---|---|---|---|
| SEC-BFF-01 | Decouverte OIDC par l'URL interne avec un `iss` public | logs du BFF au demarrage | decouverte sur l'emetteur public avec reecriture interne : `authorization` et `end_session` publics, `token` et `jwks` internes ; sans parade : refus (emetteur different) | dev, pile | spike K6a SP1, 12/10 |
| SEC-BFF-02 | Connexion code + PKCE, cookies `__Host-` | navigateur : Connexion ; F12 > Application > Cookies | `__Host-session` HttpOnly, Secure, SameSite=Lax ; `__Host-auth` supprime apres le callback ; aucun token dans le navigateur | dev, pile | SP2, 12/10 |
| SEC-BFF-03 | Session serveur dans Redis | `redis-cli --scan` | `sess:<id>` (TTL glissant 30 min) et index `sid:<sid>` (TTL 10 h) | dev, pile | SP2, 12/10 |
| SEC-BFF-04 | Callback rejoue ; cookie de pre-authentification absent | rejouer l'URL du callback (onglet Reseau) ; supprimer `__Host-auth` avant de valider le login | 400, aucune session creee | dev, pile | SP2b, 12/10 |
| SEC-BFF-05 | Pas de session orpheline a la reconnexion | se reconnecter deux fois dans le meme navigateur ; compter `sess:*` | l'ancienne session est detruite (nombre inchange) | dev, pile | SP2b, 12/10 (defaut corrige) |
| SEC-BFF-06 | Token limite a l'API, claims du contrat | claims affiches par le BFF (jamais le token) | `aud` = `donation-api`, `donation:read donation:write`, `party_id`, permissions `*:own` | dev, pile | SP3, 12/10 |
| SEC-BFF-07 | Refresh concurrent avec verrou | 5 requetes simultanees avec un access token expire | un seul appel au token endpoint, meme access token pour les 5 | dev, pile | SP4, 12/10 |
| SEC-BFF-08 | Rejeu d'un refresh token consomme | rejouer l'ancien refresh token | `invalid_grant` ; chaine de refresh coupee ; session BFF detruite ; reconnexion sans mot de passe (SSO conserve) | dev, pile | SP4, 12/10 |
| SEC-BFF-09 | Deconnexion RP-Initiated | Deconnexion, puis Connexion | session Redis et index supprimes, cookie efface, retour sur `/` ; la connexion suivante redemande le mot de passe | dev, pile | SP5, 12/10 |
| SEC-BFF-10 | Back-channel logout cible | deux sessions (fenetre normale, fenetre privee) ; deconnecter l'autre appareil depuis la console de compte | seule la session visee est supprimee (orphelines du meme `sid` comprises) ; l'URL de back-channel est interne | dev, pile | SP6, 12/10 |
| SEC-BFF-11 | Rejeu d'un `logout_token` | renvoyer le meme `logout_token` en moins de 2 min | 400 « rejeu (jti deja vu) » | dev, pile | SP6, 12/10 |
| SEC-BFF-12 | Redis sature (`noeviction`) | remplir Redis jusqu'a `maxmemory`, puis se connecter | aucune session existante evincee ; nouvelle connexion 503 sans session partielle ; un nouvel essai reussit apres liberation | dev, pile | SP7, 12/10 |

A ajouter pendant K6 (decisions D6-5 a D6-14) : CSRF (`Origin` absente ou etrangere, mauvais `Content-Type` -> 403),
route ou methode BFF non listee -> 404, API interne indisponible -> 503, liste blanche APISIX des routes Next.js,
back-channel et sondes non routes publiquement, `/api/v1` seulement sur l'hote machine, CSP avec nonce, expiration
absolue de la session.

## 6. Criteres d'acceptation

- Recette securite complete : chaque test de la section 5 au resultat attendu, dans les modes indiques ; un ecart est
  explique (cause, correction ou dette) dans le journal de la ROADMAP.
- SEC-RUN : toutes les assertions vertes (G2 ignore, section 7).
- Aucun secret dans le depot (SEC-SECRET-01 et 02) ; aucun token dans les logs (SEC-OBS-03).

## 7. Limites et tests non couverts

| Limite | Effet | Suite |
|---|---|---|
| WAF Coraza dans APISIX : corps de requete non inspectes (`wasm_process_req_body` non demande par `coraza-proxy-wasm`) | Une injection dans un corps JSON ou un formulaire n'est pas bloquee a la bordure (SEC-GW-02) ; l'API reste protegee par la validation, les requetes parametrees et le token. Limite acceptee (decision K5 du 2026-10-08, option A) | WAF dedie qui inspecte les corps (option B) : dette 16 |
| NetworkPolicy sans effet sur le Sandbox (politiques imposees `allow-same-namespace`) | Pas de cloisonnement interne sur le Sandbox (SEC-PF-03 ne prouve que leur presence) ; cloisonnement prouve en local (reseaux Docker, SEC-GW-11) | Dette 17 ; a verifier sur un vrai cluster |
| Limitation de debit non testable depuis Postman | Le Runner envoie les requetes une par une (~16 req/s sous Windows) | SEC-GW-12 ; test de charge en CI (K9) |
| MFA des internes non active | Connexion des internes par mot de passe seul | K6b |
| Tests du BFF manuels | SEC-BFF-* rejoues a la main tant que Vitest / Playwright ne sont pas en place | D6-14 (K6a) |
| Certificat auto-signe en local | Verification SSL a desactiver dans Postman (`--insecure` pour Newman, `-k` pour curl) | Route TLS sur OpenShift |
