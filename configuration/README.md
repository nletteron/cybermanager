# Configuration CyberManager

| Répertoire | Contenu |
| --- | --- |
| [`dev/`](dev/README.md) | PostgreSQL local, exemple d'environnement, dumps et scripts de lancement Linux. |
| [`prod/`](prod/README.md) | Quatre Dockerfiles, Compose de production, configuration Nginx et variables de livraison. |
| [`backends/`](backends/README.md) | YAML Spring partagés, migrations Flyway externes et catalogue d'erreurs de l'API métier. |
| [`database/`](database/README.md) | Configuration PostgreSQL et initialisation communes aux images dev/prod. |

Les migrations restent dans `backends/auth/db/migration` et `backends/cybermanager/db/migration` : une seule copie par backend, indépendante de l'environnement. Les YAML et migrations sont montés en lecture seule ; les secrets de production sont fournis hors Git.

Les commandes de chaque guide partent de la racine du dépôt. Le développement lance les API et Angular sur l'hôte ; la production lance les quatre composants dans Docker.
