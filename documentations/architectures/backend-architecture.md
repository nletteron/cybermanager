# Architecture backend CyberManager

Source de reference : https://github.com/nletteron/parent/blob/master/documentations/architecture.md

Ce document adapte l'architecture hexagonale source au backend CyberManager. La structure source est conservee pour `domain`, `application`, `infrastructure` et `architecture-validation`. CyberManager ajoute une couche `api` separee pour l'exposition REST JSON.

## Modules Maven

Chaque ensemble backend fonctionnel suit cette organisation :

| Module | Role | Structure attendue |
| --- | --- | --- |
| `*-domain` | Coeur metier | `businessobjects`, `valueobjects`, `aggregates`, `exceptions`, `commands`, `queries`, `ports`, `abstracts` |
| `*-application` | Orchestration des cas d'usage | `ports`, `services`, `usecases`, `views`, `abstracts` |
| `*-infrastructure` | Implementations techniques | `abstracts`, `actuators`, `configurations`, `errors`, `in`, `out` |
| `*-api` | Exposition REST JSON CyberManager | `controllers`, `dtos`, `mappers`, `errors`, `configurations`, `abstracts` |
| `*-architecture-validation` | Validation automatique | regles ArchUnit |

Modules actuels :

- `backend/auth/cm-auth-domain`
- `backend/auth/cm-auth-application`
- `backend/auth/cm-auth-infrastructure`
- `backend/auth/cm-auth-api`
- `backend/cybermanager/cm-domain`
- `backend/cybermanager/cm-application`
- `backend/cybermanager/cm-infrastructure`
- `backend/cybermanager/cm-api`

## Configuration d'execution

Les fichiers Spring Boot `application*.yml` vivent hors des modules Maven, dans `configuration/backends/auth` et `configuration/backends/cybermanager`. Chaque API recoit son emplacement externe au demarrage avec `SPRING_CONFIG_LOCATION` ou `--spring.config.location`. Les imports entre fichiers YAML sont relatifs a leur repertoire pour permettre leur deploiement dans un emplacement securise sans recompilation.

Les migrations SQL sont aussi externes aux JAR, dans `configuration/backends/auth/db/migration` et `configuration/backends/cybermanager/db/migration`. Leur emplacement `filesystem:` absolu est fourni a chaque API via `CM_FLYWAY_LOCATIONS` ou `--spring.flyway.locations`. La configuration externe conserve le choix entre `hibernate` (`create-drop`, Flyway desactive) et `flyway` (`validate`, migrations actives), ainsi que les deux historiques distincts dans le schema PostgreSQL `cybermanager`.

Voir [Configuration externe des backends](../../configuration/backends/README.md) pour les commandes locales et de deploiement.

Les lancements propres aux environnements sont separes dans `configuration/dev` et `configuration/prod`. La production utilise quatre images (frontend, Auth, API metier et PostgreSQL) avec les YAML et migrations montes en lecture seule ; les migrations restent communes aux deux environnements. Voir [Organisation de la configuration](../../configuration/README.md).

## Sens des dependances

Les dependances doivent pointer vers le metier :

```text
api -> application -> domain
infrastructure -> application -> domain
architecture-validation -> domain/application/infrastructure/api
```

Regles :

- `domain` ne depend d'aucune autre couche backend.
- `application` depend du `domain`, jamais de `api`.
- `infrastructure` depend de `application` et `domain`.
- `api` depend de `application` pour appeler les use cases.
- Aucun module ne doit creer de dependance circulaire.

## Domaine

Le module `domain` contient exclusivement le coeur metier.

Structure attendue :

- `businessobjects` : objets metier avec identite, invariants et cycle de vie.
- `valueobjects` : objets immuables definis par leurs valeurs.
- `aggregates` : regroupements coherents qui protegent leurs invariants.
- `exceptions` : exceptions metier et codes d'erreur.
- `commands` : commandes autorisees par le domaine.
- `queries` : requetes autorisees par le domaine.
- `ports` : interfaces qui expriment une intention metier.
- `abstracts` : abstractions necessaires au domaine.

Contraintes :

- aucune dependance Spring
- aucune annotation JPA
- aucune dependance HTTP, API, MapStruct ou infrastructure
- pas de donnees mutables exposees directement
- les ports utilisent le langage metier, pas le vocabulaire technique

## Application

Le module `application` orchestre les intentions metier.

Structure attendue :

- `ports` : ports utilises ou exposes par l'application.
- `services` : services applicatifs.
- `usecases` : cas d'utilisation metier.
- `views` : read models applicatifs retournes par les use cases.
- `abstracts` : abstractions necessaires a l'application.

Contraintes :

- depend du `domain`, jamais de `api`
- ne contient pas de logique d'infrastructure
- n'appelle pas directement les repositories Spring Data
- prend des commands ou queries du domaine en entree
- les use cases sont des classes concretes qui implementent les cas d'usage
- un use case expose une methode `execute`
- un service applicatif ne doit jamais implementer un use case
- un service applicatif peut agreger ou orchestrer des use cases par composition explicite

## Infrastructure

Le module `infrastructure` contient les details techniques.

Structure attendue :

- `abstracts` : abstractions techniques internes.
- `actuators` : monitoring et gestion applicative.
- `configurations` : configuration Spring et configuration technique.
- `errors` : gestion technique des erreurs.
- `in` : adapters entrants hors couche API separee, par exemple listeners, batch, jobs.
- `out` : adapters sortants, par exemple persistence, messaging, API externes et providers.

Contraintes :

- implemente les ports definis par `domain` ou `application`
- ne contient pas la logique metier centrale
- isole les details JPA, SQL, clients externes et messaging
- ne laisse pas les repositories Spring Data etre appeles hors adapter
- les adapters sortants qui implementent des ports vivent sous `infrastructure.out.adapters.<tech>.<bounded-context>`
- les adapters de persistence vivent sous `infrastructure.out.adapters.persistences.<bounded-context>` ou directement sous `infrastructure.out.adapters.persistences` pour un adapter transversal
- les repositories Spring Data vivent sous `infrastructure.out.adapters.persistences.repositories.<bounded-context>`
- les entites JPA vivent sous `infrastructure.out.entities.<bounded-context>`
- ne pas utiliser `infrastructure.out.persistence`, `infrastructure.out.database`, `infrastructure.out.query`, `infrastructure.out.security`, `infrastructure.entities`, `infrastructure.repositories.persistence`, `infrastructure.adapters.persistence` ou `entities.persistence`

## API

La couche `api` est une extension CyberManager a l'architecture source. Elle isole l'exposition REST JSON au lieu de la melanger avec les adapters `in` d'infrastructure.

Structure attendue :

- `controllers` : endpoints REST.
- `dtos` : request et response DTO.
- `mappers` : mapping API vers command/query/view.
- `errors` : traduction HTTP des erreurs.
- `configurations` : configuration specifique API.
- `abstracts` : helpers et abstractions transverses API qui ne sont ni DTO, ni mapper, ni controller.

Contraintes :

- un controller appelle les use cases ou services applicatifs
- un controller n'injecte jamais de repository
- un endpoint ne retourne jamais d'entite domain ou JPA
- les DTO API restent separes des objets application et domain
- le flux REST suit `DTO -> Command/Query -> UseCase -> View/DTO`

## Ports et adapters

Un port est un contrat exprime en langage metier. Un adapter est une implementation technique.

Regles :

- creer un port pour chaque besoin externe significatif
- implementer les ports dans les adapters `in` ou `out`
- placer les implementations de ports sortants sous `out.adapters.<tech>` pour rendre visible l'intention d'adapter un port
- suivre la norme du projet de reference `private-prescription` : `out.adapters.persistences`, `out.adapters.persistences.repositories`, `out.entities`, `out.mappers`, `out.dto`, `out.views`, `out.validators` selon les besoins
- laisser le domaine et l'application manipuler les ports, pas les implementations
- eviter les noms de ports centres sur une technologie

## Validation

La validation d'architecture doit controler :

- dependances Maven entre modules
- imports Java interdits par couche
- absence de Spring/JPA/API dans `domain`
- absence de DTO API et repositories Spring Data dans `application`
- absence de repository injecte dans `api`
- absence d'entite JPA retournee par un controller
- respect des dossiers source : `domain`, `application`, `infrastructure`, `api`

Si un module `*-architecture-validation` existe, il doit porter les regles ArchUnit correspondantes.
