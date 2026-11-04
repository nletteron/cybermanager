# Développement local

Prérequis, compte admin et commandes Windows/Linux des trois serveurs : [README principal](../../README.md#démarrage-windows-et-linux).

Depuis la racine du dépôt :

```sh
docker compose -f configuration/dev/docker-compose.yml up -d --build
docker compose -f configuration/dev/docker-compose.yml ps
docker compose -f configuration/dev/docker-compose.yml down
```

Ce Compose ne lance que PostgreSQL 16 sur `127.0.0.1:5432`. Base, utilisateur et mot de passe local : `cybermanager`. Il réutilise par défaut le volume existant `cybermanager_cybermanager-postgres-data` ; le déplacement du Compose ne change pas les données.

Les dumps existants ont été déplacés dans `configuration/dev/dumps`. Recréer le conteneur avec la commande `up` ci-dessus pour prendre en compte son nouveau montage ; aucun `down -v` n'est nécessaire. Les identifiants PostgreSQL ne sont appliqués qu'à l'initialisation d'un volume vide : changer une variable ne modifie pas un mot de passe déjà enregistré.

Pour personnaliser le lancement, copier `.env.example` vers `.env`, puis fournir explicitement ce fichier :

```sh
docker compose --env-file configuration/dev/.env -f configuration/dev/docker-compose.yml up -d --build
```

| Variable Compose | Défaut |
| --- | --- |
| `CM_DB_PORT` | `5432` sur l'hôte |
| `CM_DB_PASSWORD` | `cybermanager` |
| `CM_DB_VOLUME_NAME` | `cybermanager_cybermanager-postgres-data` |
| `CM_DATABASE_DUMP_HOST_DIR` | `./dumps`, relatif à `configuration/dev` |

Si le port ou le mot de passe change, fournir aussi `CM_DB_URL` ou `CM_DB_PASSWORD` aux API sur l'hôte. Les variables Compose ne sont pas exportées vers les autres terminaux.

## Scripts Linux

Dans trois terminaux distincts :

```bash
bash configuration/dev/scripts/linux/start-auth.sh
```

```bash
bash configuration/dev/scripts/linux/start-cybermanager.sh
```

```bash
bash configuration/dev/scripts/linux/start-frontend.sh
```

Chaque script retrouve la racine du dépôt depuis son emplacement. Les scripts backend installent leurs modules Maven puis lancent l'API en mode `flyway` par défaut ; le script frontend exécute `npm ci` puis `npm start`. Les variables déjà fournies sont conservées. `CM_CONFIG_DIR` permet de choisir un autre répertoire YAML ; les arguments supplémentaires sont transmis au lancement.

Le premier lancement sur le schéma partagé peut nécessiter la [baseline Flyway à 0](../../README.md#initialiser-le-second-historique-flyway). Le mode `hibernate` reste disponible sur une base jetable, sans création du compte administrateur.
