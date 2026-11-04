# Configuration PostgreSQL partagee

PostgreSQL 16 est utilise en developpement et en production. Ce repertoire contient `postgresql.conf`, `pg_hba.conf` et `initdb/00-init-database.sh`. Le Dockerfile commun est dans [`../prod/database/Dockerfile`](../prod/database/Dockerfile) et utilise ce repertoire comme contexte de build.

Les fichiers sont integres a l'image : aucun montage de configuration PostgreSQL n'est necessaire au lancement. Le script d'initialisation ne s'execute que sur un volume vide ; il cree les extensions `uuid-ossp` et `pgcrypto`, le schema `cybermanager`, le search path et le fuseau `Europe/Paris`. Il n'insere aucune donnee metier.

| Usage | Chemin dans le conteneur |
| --- | --- |
| Donnees PostgreSQL | `/var/lib/postgresql/data` |
| Dumps | `/var/lib/postgresql/dumps` |
| Configuration serveur | `/etc/postgresql/postgresql.conf` |
| Authentification | `/etc/postgresql/pg_hba.conf` |

Les stockages des donnees et des dumps sont distincts et persistants. Leurs sources sont definies par les Compose propres a chaque environnement : [developpement](../dev/README.md), [production](../prod/README.md). Le Compose dev conserve le nom du volume historique ; celui de production utilise un autre volume.

Les API construisent le schema applicatif en mode `flyway` puis Hibernate le valide. Les migrations externes vivent dans `configuration/backends/<backend>/db/migration`. Le mode `hibernate` est reserve aux bases de developpement jetables (`create-drop`) ; il ne doit pas etre utilise sur une base de production ou une base Flyway a conserver.

Les sauvegardes de l'application executent `pg_dump` et `pg_restore` via `docker exec` dans le conteneur choisi par `CM_DATABASE_BACKUP_CONTAINER`. Le client Docker doit etre accessible a l'API metier, les outils PostgreSQL sont dans l'image de base. `CM_DATABASE_DUMP_DIR` est un chemin interne au conteneur ; `CM_DATABASE_DUMP_HOST_DIR` est le dossier hote monte par Compose. Ne pas inverser ces deux valeurs.

Pour verifier un dump local :

```sh
docker exec cybermanager-db ls -lh /var/lib/postgresql/dumps
docker exec cybermanager-db pg_restore --list /var/lib/postgresql/dumps/<nom-du-dump>.dump
```

En production, remplacer `cybermanager-db` par le conteneur configure, par defaut `cybermanager-prod-db`. Les autres variables des sauvegardes sont decrites dans [F-024](../../documentations/features/F-024-database-backup-and-restore.md).
