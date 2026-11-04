# Configuration externe des backends

Chaque backend possede son propre repertoire de configuration Spring Boot :

```text
configuration/backends/
|- auth/
|  |- application.yml
|  |- application-common.yml
|  |- application-hibernate.yml
|  |- application-flyway.yml
|  `- db/migration/          # V1 et V2 Auth
`- cybermanager/
   |- application.yml
   |- application-common.yml
   |- application-hibernate.yml
   |- application-flyway.yml
   `- db/migration/          # migrations versionnees CyberManager
```

Les fichiers YAML et les migrations SQL sont externes aux modules Java et aux JAR. Chaque backend possede ses propres fichiers de migration ; les deux V2 administrateur restent presentes dans leurs historiques respectifs.

## Role des fichiers

| Fichier | Role |
| --- | --- |
| `application.yml` | Importe la configuration commune et selectionne le profil avec `CM_DB_MODE` ; `hibernate` reste la valeur par defaut. |
| `application-common.yml` | Definit la connexion PostgreSQL, le port HTTP, JPA, JWT, les logs et les proprietes propres au backend. |
| `application-hibernate.yml` | Importe le fichier commun, active `create-drop` et desactive Flyway. |
| `application-flyway.yml` | Importe le fichier commun, active Flyway avec les scripts externes et configure Hibernate en `validate`. |
| `db/migration/` | Contient les migrations SQL versionnees du backend. |

Les imports `application-common.yml` sont relatifs au fichier qui les declare. Copier le repertoire complet permet de conserver ces imports a un autre emplacement, y compris hors du depot.

En mode Flyway, `CM_FLYWAY_LOCATIONS` fournit le dossier des scripts avec le prefixe `filesystem:` et un chemin absolu. Flyway resout les chemins relatifs depuis le repertoire de travail du processus, independamment du repertoire YAML. `fail-on-missing-locations: true` fait echouer le demarrage si le dossier fourni n'existe pas. Voir les [emplacements Flyway](https://documentation.red-gate.com/flyway/reference/configuration/flyway-namespace/flyway-locations-setting).

Cette variable n'est pas necessaire en mode Hibernate, qui n'execute pas les migrations. Pour un deploiement, `--spring.flyway.locations=filesystem:/chemin/absolu/db/migration` peut remplacer la variable.

Les deux API utilisent le schema PostgreSQL `cybermanager` et des historiques distincts : `flyway_auth_schema_history` pour Auth, `flyway_cybermanager_schema_history` pour CyberManager. Auth utilise le port `8081`, CyberManager le port `8082`.

## Lancement local avec Maven

Installez d'abord les modules Maven depuis la racine du depot, puis ouvrez deux terminaux PowerShell :

```powershell
mvn -f backend/cybermanager-parent/pom.xml install
```

Repetez cette installation apres modification des modules dependants si l'IDE ou `spring-boot:run` les charge depuis le cache `.m2` ; `mvn test` seul ne remplace pas ces JAR.

Auth :

```powershell
$env:SPRING_CONFIG_LOCATION = ([uri]((Resolve-Path "configuration/backends/auth").Path + "/")).AbsoluteUri
$env:CM_FLYWAY_LOCATIONS = "filesystem:" + (Resolve-Path "configuration/backends/auth/db/migration").Path.Replace('\', '/')
$env:CM_DB_MODE = "flyway"
mvn -f backend/auth/cm-auth-api/pom.xml "-Dmaven.test.skip=true" spring-boot:run
```

CyberManager :

```powershell
$env:SPRING_CONFIG_LOCATION = ([uri]((Resolve-Path "configuration/backends/cybermanager").Path + "/")).AbsoluteUri
$env:CM_FLYWAY_LOCATIONS = "filesystem:" + (Resolve-Path "configuration/backends/cybermanager/db/migration").Path.Replace('\', '/')
$env:CM_DB_MODE = "flyway"
$env:CM_ERROR_CATALOG_FILE = "file:" + (Join-Path $PWD.Path "configuration/backends/cybermanager/error.json")
mvn -f backend/cybermanager/cm-api/pom.xml "-Dmaven.test.skip=true" spring-boot:run
```

L'URI de configuration et le chemin de migrations absolus restent valables lorsque Maven lance l'API depuis le repertoire du module. Le `/` final de `SPRING_CONFIG_LOCATION` indique a Spring Boot qu'il doit charger un repertoire de configuration. Definissez ces variables dans le terminal de chaque backend.

Les exemples versionnes contiennent les valeurs de developpement local. Les variables `CM_DB_URL`, `CM_DB_USERNAME`, `CM_DB_PASSWORD` et `CM_JWT_SECRET` permettent de les remplacer. Les deux API doivent partager le meme secret JWT.

Le catalogue `error.json` se trouve dans `configuration/backends/cybermanager`, avec les YAML et migrations de l'API metier. Son emplacement est fourni via `CM_ERROR_CATALOG_FILE` et peut aussi etre deplace en production.

Pour un premier lancement sur le schema partage, voir [Initialiser le second historique Flyway](../../README.md#initialiser-le-second-historique-flyway).

## Fournir directement un fichier de mode

Chaque fichier de mode importe lui-meme le fichier commun. Il peut donc etre fourni directement, sans passer par `application.yml` :

```powershell
$env:SPRING_CONFIG_LOCATION = ([uri](Resolve-Path "configuration/backends/auth/application-flyway.yml").Path).AbsoluteUri
$env:CM_FLYWAY_LOCATIONS = "filesystem:" + (Resolve-Path "configuration/backends/auth/db/migration").Path.Replace('\', '/')
mvn -f backend/auth/cm-auth-api/pom.xml "-Dmaven.test.skip=true" spring-boot:run
```

Pour le mode Hibernate, fournissez `application-hibernate.yml`. Ce mode recree les tables et efface les donnees a l'arret ; utilisez une base de developpement dediee.

## Deploiement avec un repertoire securise

Le build Maven produit les JAR executables `cm-auth-api-1.0.0-SNAPSHOT.jar` et `cm-api-1.0.0-SNAPSHOT.jar`. Deposez chaque ensemble de fichiers YAML et son dossier `db/migration` dans son propre repertoire externe. Renseignez les valeurs adaptees a la production, notamment la connexion PostgreSQL et le secret JWT. Les copies contenant les secrets de production restent hors du depot.

Exemple Linux, dans deux processus distincts :

```bash
java -jar cm-auth-api-1.0.0-SNAPSHOT.jar \
  --spring.config.location=file:/etc/cybermanager/auth/ \
  --spring.flyway.locations=filesystem:/etc/cybermanager/auth/db/migration \
  --spring.profiles.active=flyway
```

```bash
java -jar cm-api-1.0.0-SNAPSHOT.jar \
  --spring.config.location=file:/etc/cybermanager/cybermanager/ \
  --spring.flyway.locations=filesystem:/etc/cybermanager/cybermanager/db/migration \
  --spring.profiles.active=flyway \
  --cybermanager.errors.file=file:/etc/cybermanager/cybermanager/error.json
```

Le chemin fourni doit contenir le fichier `application.yml`, le fichier commun et les fichiers de mode necessaires. Un fichier seul peut aussi etre fourni a `spring.config.location`. Le repertoire ou fichier indique est obligatoire : les exemples n'utilisent pas le prefixe `optional:`.

Deposez les migrations correspondant a la version du JAR. Les scripts deja appliques ne doivent pas etre modifies ou renumerotes : leur deplacement seul conserve les checksums et les historiques existants. Aucun nouveau seed n'est ajoute ; les V2 du premier administrateur restent l'exception existante.

Ce chargement utilise le mecanisme standard [Externalized Configuration de Spring Boot](https://docs.spring.io/spring-boot/3.5/reference/features/external-config.html). Aucun chemin de production n'est impose dans le code Java.

## Environnements Docker

Les YAML et migrations ci-dessus sont communs aux lancements locaux et Docker. Le catalogue est range avec la configuration de l'API metier dans `cybermanager/error.json`. Le [Compose de production](../prod/README.md) monte ces repertoires en lecture seule et impose le mode `flyway`, avec des secrets externes. Le [developpement local](../dev/README.md) garde PostgreSQL dans Docker et les serveurs sur l'hote.
