# Livraison Docker de production

Ce dossier livre quatre images : Angular/Nginx, Auth, API métier et PostgreSQL 16. Docker et Compose v2 suffisent sur le serveur ; Java, Maven et Node.js sont utilisés dans les étapes de construction des images.

## Images et configuration

| Image | Dockerfile | Contenu / port interne |
| --- | --- | --- |
| `${CM_IMAGE_PREFIX}-front:${CM_IMAGE_TAG}` | `frontend/Dockerfile` | Build Angular, Nginx, proxy des API ; `80`. |
| `${CM_IMAGE_PREFIX}-auth:${CM_IMAGE_TAG}` | `auth/Dockerfile` | Build Maven, runtime Java 21 ; `8081`. |
| `${CM_IMAGE_PREFIX}-api:${CM_IMAGE_TAG}` | `cybermanager/Dockerfile` | Build Maven, runtime Java 21, client Docker des sauvegardes ; `8082`. |
| `${CM_IMAGE_PREFIX}-db:${CM_IMAGE_TAG}` | `database/Dockerfile` | PostgreSQL 16, extensions, configuration et outils de dump ; `5432`. |

Auth et l'API métier tournent avec l'utilisateur `application` (UID/GID `10001`). Aucun secret, YAML ou script Flyway n'est intégré dans leurs JAR ou images. Les tests sont exécutés séparément en CI : les builds Docker backend utilisent `-Dmaven.test.skip=true`.

Les quatre services partagent un réseau Docker. Seul Nginx publie un port sur l'hôte. Les contrôles de santé imposent l'ordre PostgreSQL → Auth → API métier → frontend ; les contrôles backend vérifient l'ouverture du port HTTP après l'initialisation Spring. Voir le [fonctionnement de `service_healthy`](https://docs.docker.com/compose/how-tos/startup-order/).

## Première installation

Depuis la racine du dépôt, copier le modèle d'environnement et créer le dossier des dumps.

Linux :

```bash
cp configuration/prod/.env.example configuration/prod/.env
mkdir -p /srv/cybermanager/dumps
stat -c '%g' /var/run/docker.sock
```

Windows avec Docker Desktop en mode conteneurs Linux :

```powershell
Copy-Item configuration/prod/.env.example configuration/prod/.env
New-Item -ItemType Directory -Force D:/cybermanager/dumps
```

Modifier `.env` avant de continuer :

- remplacer `CM_DB_PASSWORD` et `CM_JWT_SECRET` (secret JWT aléatoire d'au moins 32 octets) ;
- adapter `CM_DATABASE_DUMP_HOST_DIR` au chemin absolu existant sur l'hôte Docker ;
- renseigner `CM_DOCKER_GID` avec le groupe numérique du socket sous Linux (`0` généralement sous Docker Desktop) ;
- choisir `CM_IMAGE_TAG` et, pour un registre, `CM_IMAGE_PREFIX`, par exemple `registry.example.com/cybermanager`.

Les dossiers montés doivent être accessibles aux processus des conteneurs. L'API métier utilise le socket Docker pour exécuter les sauvegardes dans le conteneur PostgreSQL ; ce montage lui donne accès au moteur Docker de l'hôte.

Les commandes suivantes fonctionnent sous PowerShell et Bash :

```sh
docker compose --env-file configuration/prod/.env -f configuration/prod/docker-compose.yml config --quiet
docker compose --env-file configuration/prod/.env -f configuration/prod/docker-compose.yml up -d --build --wait --wait-timeout 240
docker compose --env-file configuration/prod/.env -f configuration/prod/docker-compose.yml ps
```

Ouvrir http://localhost:8080 ou l'adresse du serveur avec le port configuré. Nginx sert le frontend et transmet `/auth-api/` à Auth et `/api/` à l'API métier. Son `runtime-config.js` est généré à chaque démarrage à partir des variables d'URL ; aucun rebuild Angular n'est nécessaire pour les changer.

Le proxy réévalue les adresses des API via le DNS Docker lors des recréations de conteneurs, grâce à la [résolution dynamique Nginx](https://nginx.org/en/docs/http/ngx_http_upstream_module.html#resolve).

Le Compose fourni expose HTTP. Pour HTTPS, terminer TLS sur le frontal existant du serveur ou fournir une configuration Nginx et un override Compose avec le port `443` et les certificats montés en lecture seule. Ne pas ajouter les clés privées à l'image ou au dépôt.

### Initialisation Flyway

Les deux API partagent le schéma `cybermanager`, avec un historique Flyway distinct par API. Le modèle `.env.example` active `CM_FLYWAY_BASELINE_ON_MIGRATE=true` avec une baseline à **0** pour la première installation sur une base neuve vérifiée : V1 et les migrations suivantes sont exécutées, même quand Auth a déjà rempli le schéma.

Une fois les deux API démarrées, passer cette variable à `false` puis relancer `up -d --wait` avec les mêmes options. Ne pas activer cette option pour contourner un écart sur une base existante ; restaurer un état cohérent avant migration. Le mode de production est fixé à `flyway` : Hibernate valide le schéma et ne supprime pas les tables à l'arrêt.

Les migrations V2 créent aussi le compte `admin@cybermanager.local` / `admin123` sur une base neuve. Remplacer ce mot de passe avant d'ouvrir l'accès aux utilisateurs. Aucune migration existante n'est modifiée et aucune autre donnée initiale n'est ajoutée par ce déploiement.

## Variables

Le fichier d'environnement peut être conservé hors du dépôt ; remplacer simplement le chemin fourni à `--env-file`. Les chemins relatifs des montages sont résolus depuis `configuration/prod`, pas depuis ce fichier d'environnement.

| Variable | Usage / défaut |
| --- | --- |
| `CM_IMAGE_PREFIX` | Préfixe des quatre images, `cybermanager`. |
| `CM_IMAGE_TAG` | Tag de livraison obligatoire, commun aux quatre images. |
| `CM_DB_PASSWORD` | Mot de passe obligatoire, partagé par PostgreSQL et les API. |
| `CM_JWT_SECRET` | Secret obligatoire, identique dans Auth et l'API métier. |
| `CM_DB_VOLUME_NAME` | Volume de données, `cybermanager-prod-postgres-data`. |
| `CM_DATABASE_BACKUP_CONTAINER` | Nom du conteneur PostgreSQL, `cybermanager-prod-db`. |
| `CM_DATABASE_DUMP_HOST_DIR` | Dossier hôte absolu et existant des dumps, obligatoire. |
| `CM_DOCKER_SOCKET`, `CM_DOCKER_GID` | Socket `/var/run/docker.sock` et groupe numérique autorisé, obligatoire pour le groupe. |
| `CM_AUTH_CONFIG_DIR`, `CM_API_CONFIG_DIR` | Dossiers externes complets, par défaut `../backends/auth` et `../backends/cybermanager`. |
| `CM_NGINX_CONFIG_FILE` | Configuration du serveur web, `./frontend/default.conf`. |
| `CM_FRONT_BIND`, `CM_FRONT_PORT` | Adresse/port publiés, `0.0.0.0:8080`. |
| `CM_AUTH_API_URL`, `CM_API_URL` | URL utilisées par le navigateur, `/auth-api` et `/api`. |
| `CM_FLYWAY_BASELINE_ON_MIGRATE` | `false` par défaut dans Compose ; `true` dans le modèle pour l'installation initiale uniquement. |
| `CM_JAVA_TOOL_OPTIONS` | Options JVM communes, `-XX:MaxRAMPercentage=75.0`. |
| `CM_DATABASE_DUMP_MAX_SIZE` | Limite d'import Spring, `1GB`. Pour l'augmenter, adapter aussi `client_max_body_size` dans Nginx. |
| `CM_AUTH_LOG_LEVEL`, `CM_AUTH_HTTP_LOG_LEVEL`, `CM_LOG_LEVEL`, `CM_HTTP_LOG_LEVEL` | Niveaux de logs, `INFO` en production. |

Les chemins internes Spring, Flyway et de dumps sont définis par le Compose et alignés sur les montages. La connexion des API utilise `jdbc:postgresql://cybermanager-db:5432/cybermanager`, avec la base et l'utilisateur `cybermanager`. Modifier `POSTGRES_PASSWORD` ne change pas le mot de passe d'un volume déjà initialisé.

## Volumes et fichiers montés

| Service | Source | Destination | Accès |
| --- | --- | --- | --- |
| PostgreSQL | Volume `CM_DB_VOLUME_NAME` | `/var/lib/postgresql/data` | Persistant, lecture/écriture. |
| PostgreSQL | Dossier `CM_DATABASE_DUMP_HOST_DIR` | `/var/lib/postgresql/dumps` | Persistant, lecture/écriture. |
| Auth | `CM_AUTH_CONFIG_DIR` (YAML et `db/migration`) | `/etc/cybermanager/auth` | Lecture seule. |
| API métier | `CM_API_CONFIG_DIR` (YAML, `error.json`, `db/migration`) | `/etc/cybermanager/cybermanager` | Lecture seule. |
| API métier | `CM_DOCKER_SOCKET` | `/var/run/docker.sock` | Accès au moteur Docker, groupe `CM_DOCKER_GID`. |
| Frontend | `CM_NGINX_CONFIG_FILE` | `/etc/nginx/conf.d/default.conf` | Lecture seule. |

Le frontend n'a pas de données persistantes ; ses fichiers compilés sont dans l'image, son fichier runtime est régénéré. Les dumps sont lus/écrits par `docker exec` dans PostgreSQL, sans montage partagé avec l'API. La rotation des logs Docker est limitée à trois fichiers de 10 Mo par service.

Les fichiers de migration montés doivent correspondre à la version des JAR. Conserver leur contenu et leur numérotation : déplacer un dossier ne doit pas changer les checksums Flyway.

## Livraison et exploitation

Pour construire puis publier dans le registre choisi :

```sh
docker compose --env-file configuration/prod/.env -f configuration/prod/docker-compose.yml build
docker compose --env-file configuration/prod/.env -f configuration/prod/docker-compose.yml push
```

Sur le serveur, déposer le Compose, son `.env` protégé, les dossiers backend correspondant à la livraison et `frontend/default.conf`. Adapter les chemins des montages, puis utiliser `pull` et `up -d --no-build --wait --wait-timeout 240` avec les mêmes options `--env-file` et `-f`. Aucun code source ni Dockerfile n'est nécessaire pour lancer les images publiées.

Pour les mises à jour, sauvegarder la base, livrer les YAML/migrations correspondants, changer le tag puis relancer les services. Les migrations sont appliquées au démarrage ; un retour à une ancienne image ne revient pas en arrière dans le schéma.

```sh
docker compose --env-file configuration/prod/.env -f configuration/prod/docker-compose.yml logs --tail 100
docker compose --env-file configuration/prod/.env -f configuration/prod/docker-compose.yml down
```

`down` conserve données et dumps. `down -v` supprime le volume PostgreSQL ; ce n'est pas une étape de mise à jour. Le volume dev et son conteneur sont distincts de ceux de production, ce qui permet les deux environnements sur le même hôte.
