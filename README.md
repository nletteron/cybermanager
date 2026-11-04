# CyberManager

Application de gestion d'un cybercafé : Angular, deux API Java 21 / Spring Boot et PostgreSQL 16.

| Service | Module | Adresse locale |
| --- | --- | --- |
| Authentification | `backend/auth` | http://localhost:8081/api |
| API métier | `backend/cybermanager` | http://localhost:8082/api |
| Frontend | `frontend` | http://localhost:4200 |

- [Prérequis de développement](#prérequis-de-développement)
- [Démarrage Windows et Linux](#démarrage-windows-et-linux)
- [Options de démarrage](#options-de-démarrage)
- [Production : quatre images Docker](#production--quatre-images-docker)

Organisation des fichiers : [configuration dev/prod](configuration/README.md).

## Prérequis de développement

- JDK 21 et Maven 3.9+, accessibles dans le `PATH`.
- Node.js 22.12+ (branche 22) ou 24.x et npm 11.9.0 (`frontend/package.json`).
- Docker Engine avec Compose v2 sous Linux, ou Docker Desktop en mode conteneurs Linux sous Windows.
- Trois terminaux PowerShell ou Bash pour les deux API et le frontend.

Les commandes suivantes partent de la **racine du dépôt**, sauf indication contraire. Les valeurs PostgreSQL et JWT versionnées sont réservées au développement.

## Démarrage Windows et Linux

### 1. PostgreSQL

Les mêmes commandes fonctionnent sous PowerShell et Bash :

```sh
docker compose -f configuration/dev/docker-compose.yml up -d --build
docker compose -f configuration/dev/docker-compose.yml ps
```

Attendre l'état `healthy`. Connexion locale : `localhost:5432`, base/utilisateur/mot de passe `cybermanager`, schéma `cybermanager`.

Les données sont conservées dans le volume Compose `cybermanager-postgres-data`. Les dumps sont stockés dans `configuration/dev/dumps` ; `CM_DATABASE_DUMP_HOST_DIR` permet de choisir un autre dossier hôte. Voir la [configuration PostgreSQL](configuration/database/README.md).

### 2. Compiler les deux backends

```sh
mvn -f backend/cybermanager-parent/pom.xml "-Dmaven.test.skip=true" clean install
```

Attendre `BUILD SUCCESS`. Cette commande produit les deux JAR et installe leurs dépendances internes dans le dépôt Maven local. La relancer après une modification backend. `-Dmaven.test.skip=true` ignore la compilation et l'exécution des tests pour ce lancement local ; la validation complète figure plus bas.

### 3. Préparer les deux terminaux backend

Dans **chacun** des deux terminaux, exécuter le bloc adapté au système. Choisir `auth` dans le premier et `cybermanager` dans le second.

**Windows — PowerShell :**

```powershell
$backend = "auth" # Remplacer par "cybermanager" dans le second terminal
$configDir = (Resolve-Path "configuration/backends/$backend").Path
$env:CM_DB_MODE = "flyway"
$env:SPRING_CONFIG_LOCATION = ([uri]($configDir + "/")).AbsoluteUri
$env:CM_FLYWAY_LOCATIONS = "filesystem:" + $configDir.Replace('\', '/') + "/db/migration"
$env:CM_ERROR_CATALOG_FILE = ([uri](Resolve-Path "configuration/backends/cybermanager/error.json").Path).AbsoluteUri
```

**Linux — Bash :**

```bash
backend=auth # Remplacer par cybermanager dans le second terminal
config_dir="$(pwd)/configuration/backends/$backend"
export CM_DB_MODE=flyway
export SPRING_CONFIG_LOCATION="file:$config_dir/"
export CM_FLYWAY_LOCATIONS="filesystem:$config_dir/db/migration"
export CM_ERROR_CATALOG_FILE="file:$(pwd)/configuration/backends/cybermanager/error.json"
```

`CM_ERROR_CATALOG_FILE` est utilisé uniquement par l'API métier. Chaque API charge ses propres YAML et migrations, externes aux JAR. Les variables d'un terminal ne configurent pas les autres.

### 4. Lancer les API

Les commandes sont identiques sous Windows et Linux. Sur une base neuve, démarrer Auth, attendre la fin de ses migrations, puis appliquer l'[initialisation du second historique Flyway](#initialiser-le-second-historique-flyway) au premier lancement de CyberManager.

Terminal Auth :

```sh
java -jar backend/auth/cm-auth-api/target/cm-auth-api-1.0.0-SNAPSHOT.jar
```

Terminal CyberManager :

```sh
java -jar backend/cybermanager/cm-api/target/cm-api-1.0.0-SNAPSHOT.jar
```

### 5. Lancer le frontend

Dans le troisième terminal, sous Windows ou Linux :

```sh
cd frontend
npm ci
npm start
```

Ouvrir http://localhost:4200. Aux lancements suivants, `npm start` suffit si les dépendances n'ont pas changé. Les adresses des API sont définies dans `frontend/public/runtime-config.js` via `window.__CM_CONFIG__` (`authApiUrl` et `usersApiUrl`).

### Compte administrateur de développement

| Email | Mot de passe | Rôle |
| --- | --- | --- |
| `admin@cybermanager.local` | `admin123` | `ADMIN` |

Les migrations V2 créent ce compte de façon idempotente en mode `flyway`. Le mode `hibernate` ne le crée pas. Remplacer ce mot de passe avant une mise en production ; les migrations actuelles créent aussi ce compte sur une base de production neuve.

### Arrêt

Arrêter chaque serveur avec `Ctrl+C`, puis PostgreSQL :

```sh
docker compose -f configuration/dev/docker-compose.yml down
```

Les données sont conservées. Ajouter `-v` supprime le volume de données PostgreSQL ; le dossier hôte des dumps reste présent.

## Options de démarrage

### Backends : JAR, Maven ou IDE

Le lancement des JAR ci-dessus nécessite Java et la configuration externe. Pour développer avec Maven, conserver les mêmes variables et remplacer `java -jar` par la commande du backend concerné :

```sh
# Terminal Auth
mvn -f backend/auth/cm-auth-api/pom.xml "-Dmaven.test.skip=true" spring-boot:run
# Terminal CyberManager
mvn -f backend/cybermanager/cm-api/pom.xml "-Dmaven.test.skip=true" spring-boot:run
```

Les [scripts Linux de développement](configuration/dev/README.md#scripts-linux) sont également disponibles. Depuis un IDE, lancer `CmAuthApiApplication` et `CmApiApplication`, avec les mêmes variables et leurs chemins absolus.

### Modes et configuration externe

| Option | Effet |
| --- | --- |
| `CM_DB_MODE=flyway` | Migrations actives, Hibernate en `validate`, données conservées. Mode des exemples et de production. |
| `CM_DB_MODE=hibernate` | Valeur par défaut des YAML : `create-drop`, Flyway désactivé, tables recréées puis supprimées à l'arrêt. Réserver à une base de développement jetable. |
| `SPRING_CONFIG_LOCATION=file:/chemin/absolu/` | Répertoire YAML propre à chaque API ; conserver le `/` final. Peut aussi désigner directement `application-flyway.yml` ou `application-hibernate.yml`. |
| `CM_FLYWAY_LOCATIONS=filesystem:/chemin/absolu/db/migration` | Migrations du backend concerné, requis en mode Flyway. |
| `CM_DB_URL`, `CM_DB_USERNAME`, `CM_DB_PASSWORD` | Remplacent la connexion PostgreSQL locale. |
| `CM_JWT_SECRET` | Secret identique pour les deux API ; valeur locale `01234567890123456789012345678901`. |
| `CM_ERROR_CATALOG_FILE` | URI `file:` du catalogue `error.json` pour l'API métier. |

Les options Spring peuvent aussi être passées après le JAR, par exemple `--server.port=8083`, `--spring.profiles.active=flyway` ou `--spring.flyway.locations=filesystem:/chemin/absolu/db/migration`. Avec Maven, utiliser `"-Dspring-boot.run.arguments=--server.port=8083"` ; adapter également les URL du frontend si les ports changent. Détails : [configuration externe des backends](configuration/backends/README.md).

### Initialiser le second historique Flyway

Les API partagent un schéma mais utilisent deux historiques : `flyway_auth_schema_history` et `flyway_cybermanager_schema_history`. Si le second démarrage échoue avec `Found non-empty schema(s) ... but no schema history table`, vérifier la base ciblée puis ajouter **une seule fois** à la commande du JAR concerné :

```text
--spring.flyway.baseline-on-migrate=true --spring.flyway.baseline-version=0
```

La version `0` conserve l'exécution de V1 et des migrations suivantes. Avec Maven, passer ces deux options dans `spring-boot.run.arguments`. Revenir ensuite au lancement normal. Cette étape s'applique aussi à la première installation en production.

Si Flyway indique un historique à jour mais qu'Hibernate signale des tables absentes, arrêter les API et restaurer une base cohérente avec les migrations. Cela peut suivre un lancement `hibernate` sur la même base ; une baseline ne répare pas ce cas.

### Frontend : développement ou aperçu du build

`npm start` lance le serveur de développement. Pour vérifier le build de production, depuis la racine du dépôt :

```sh
npm --prefix frontend run build
npx serve dist/cybermanager-frontend/browser -s
```

La sortie réelle configurée dans `frontend/angular.json` est `dist/cybermanager-frontend/browser` à la racine du dépôt. `npx serve` installe l'outil d'aperçu si nécessaire. En production, ces fichiers sont servis par l'image frontend décrite ci-dessous.

## Production : quatre images Docker

Les Dockerfiles et le Compose sont dans [`configuration/prod`](configuration/prod/README.md) :

| Image | Dockerfile | Port interne |
| --- | --- | --- |
| Frontend Angular / Nginx | `configuration/prod/frontend/Dockerfile` | `80` |
| Auth | `configuration/prod/auth/Dockerfile` | `8081` |
| API métier | `configuration/prod/cybermanager/Dockerfile` | `8082` |
| PostgreSQL 16 | `configuration/prod/database/Dockerfile` | `5432` |

Copier `configuration/prod/.env.example` vers un fichier d'environnement protégé, remplacer les secrets, créer le dossier de dumps et renseigner le groupe du socket Docker. Le [guide de production](configuration/prod/README.md) détaille la préparation Windows/Linux, toutes les variables, les volumes et la livraison via un registre.

```sh
docker compose --env-file configuration/prod/.env -f configuration/prod/docker-compose.yml up -d --build --wait --wait-timeout 240
```

Seul le frontend publie un port (`8080` par défaut). PostgreSQL utilise un volume de données distinct du dev et un dossier persistant de dumps. Les YAML, migrations et configuration Nginx sont montés en lecture seule ; l'API métier accède au socket Docker pour les sauvegardes. Les URL du frontend sont générées au démarrage depuis les variables d'environnement.

## Vérifications de développement

Depuis la racine du dépôt :

```sh
mvn -f backend/cybermanager-parent/pom.xml clean verify
npm --prefix frontend test -- --watch=false
npm --prefix frontend run build
```

Documentation complémentaire : [fonctionnalités](documentations/features/README.md), [architecture backend](documentations/architectures/backend-architecture.md), [configuration Spring](configuration/backends/README.md), [PostgreSQL et sauvegardes](configuration/database/README.md).
