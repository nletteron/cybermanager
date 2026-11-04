# Feature ID: F-024

## Nom

Export et restauration de la base de donnees

## Contexte

Un administrateur doit pouvoir sauvegarder et restaurer les donnees PostgreSQL depuis CyberManager sans manipuler directement le serveur.

## Acteurs

- Administrateur

## Objectif

Exporter la base, importer des dumps, consulter les dumps disponibles et restaurer un dump selectionne depuis l'ecran `Restauration des donnees`.

## Bounded context

- database
- users

## Regles metier

- toutes les operations sont reservees au role `ADMIN`
- un export genere un dump PostgreSQL au format custom directement dans le repertoire configure du conteneur PostgreSQL
- un dump importe est copie dans le repertoire configure avant de devenir restaurable
- seuls les fichiers `.dump` du repertoire configure peuvent etre listes, telecharges ou restaures
- un nom de fichier recu du frontend ne doit jamais permettre de sortir du repertoire configure
- la restauration exige une confirmation explicite dans l'interface
- un echec de `pg_dump` ou `pg_restore` ne doit jamais etre presente comme un succes
- un export n'apparait dans la liste qu'apres la fin reussie de `pg_dump` et la verification d'un fichier non vide ; un echec ne laisse pas de dump partiel
- les noms des exports sont uniques, y compris pour plusieurs exports pendant la meme seconde
- aucun dump ne doit etre stocke dans Git ou dans les resources de l'application

## Backend

### Cas d'usage

- ExportDatabase
- UploadDatabaseDump
- ListDatabaseDumps
- DownloadDatabaseDump
- RestoreDatabaseDump

### Infrastructure

- PostgreSqlDatabaseBackupAdapter
- binaires `pg_dump` et `pg_restore`
- execution via `docker exec` dans le conteneur PostgreSQL configure ; la configuration locale utilise `cybermanager-db`
- creation de l'archive par `pg_dump --file` dans le conteneur ; liste, import et restauration depuis ce meme stockage
- telechargement en flux depuis le conteneur, sans copie de dump sur le disque de l'API ; diagnostics journalises sans mot de passe
- repertoire interne configurable par `CM_DATABASE_DUMP_DIR`, par defaut `/var/lib/postgresql/dumps`, dans un montage persistant dedie et distinct du volume des donnees PostgreSQL
- repertoire hote externalise par `CM_DATABASE_DUMP_HOST_DIR` : `configuration/dev/dumps` par defaut en developpement Windows, chemin absolu configurable en production Linux (par exemple `/srv/cybermanager/dumps`)
- livraison Docker de production dans `configuration/prod` : client Docker dans l'image de l'API metier, montage du socket du moteur et groupe supplementaire configurable par `CM_DOCKER_GID` ; donnees PostgreSQL et dumps conservent des stockages persistants distincts

### API

- `GET /api/database-backups`
- `POST /api/database-backups/export`
- `POST /api/database-backups/upload`
- `GET /api/database-backups/{fileName}/content`
- `POST /api/database-backups/{fileName}/restore`

## Frontend

### Ecran

- menu `Restauration des donnees`
- bouton `Exporter la base`
- bouton `Importer un dump`
- liste des dumps disponibles avec selection
- action `Restaurer le dump selectionne`

## Variables d'environnement

- `CM_DATABASE_DUMP_DIR` : chemin absolu Linux du repertoire persistant dans le conteneur (`/var/lib/postgresql/dumps` par defaut, a garder aligne sur la cible du montage Compose)
- `CM_DATABASE_DUMP_HOST_DIR` : dossier des dumps sur l'hote Docker, lu par Compose (`./dumps` relatif au fichier Compose par defaut) ; externalisable dans l'environnement ou un fichier fourni avec `--env-file`
- `CM_DATABASE_BACKUP_CONTAINER` : conteneur PostgreSQL (`cybermanager-db` par defaut). La connexion interne utilise `localhost:5432`, le nom de base et les identifiants de la datasource.
- `CM_DOCKER_PATH` : executable du client Docker accessible a l'API, ou chemin absolu si l'IDE ne dispose pas du bon `PATH`
- `CM_PG_DUMP_PATH` : executable `pg_dump` dans le conteneur
- `CM_PG_RESTORE_PATH` : executable `pg_restore` dans le conteneur

## Criteres d'acceptation

- un administrateur peut creer puis telecharger un dump
- un administrateur peut charger un fichier `.dump`
- la liste affiche le nom, la taille et la date de modification
- un dump selectionne peut etre restaure apres confirmation
- un utilisateur sans role `ADMIN` recoit une erreur 403
- aucun chemin arbitraire ne peut etre lu ou restaure

## Verification technique

Les tests de l'adapter et de l'execution Docker couvrent les chemins internes, les noms invalides, les imports vides, le streaming, les echecs de processus, les diagnostics et les delais. Le test reel Docker est optionnel : definir `CM_TEST_BACKUP_CONTAINER=cybermanager-db`, puis lancer `PostgreSqlDatabaseBackupAdapterIntegrationTest` via Maven. Il exporte la base dans un sous-repertoire de verification du conteneur, valide l'archive avec `pg_restore --list`, puis verifie liste, import et telechargement avant de nettoyer ses fichiers. Aucune restauration sur la base courante n'est executee.
