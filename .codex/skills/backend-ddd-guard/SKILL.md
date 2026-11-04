---
name: backend-ddd-guard
description: CyberManager backend DDD and hexagonal architecture guard. Use after backend Java changes, module creation, package restructuring, persistence/API work, or architecture reviews to verify the local backend follows documentations/architectures/backend-architecture.md, including the CyberManager API layer extension.
---

# Backend DDD Guard

## Purpose

Controler le respect de l'architecture backend CyberManager. La reference locale est `documentations/architectures/backend-architecture.md`, adaptee du document d'architecture hexagonale source fourni par l'utilisateur.

## Architecture Source

Appliquer la structure du document source pour tout le code hors API :

- `*-domain` : `businessobjects`, `valueobjects`, `aggregates`, `exceptions`, `commands`, `queries`, `ports`, `abstracts`.
- `*-application` : `ports`, `services`, `usecases`, `views`, `abstracts`.
- `*-infrastructure` : `abstracts`, `actuators`, `configurations`, `errors`, `in`, `out`.
- `*-architecture-validation` : regles ArchUnit et verification automatique quand le module existe.

CyberManager ajoute une couche API explicite, conservee comme extension locale :

- `*-api` : `controllers`, `dtos`, `mappers`, `errors`, `configurations`, `abstracts`.

## Dependency Rules

- `domain` ne depend d'aucune couche technique.
- `application` depend de `domain`, jamais de `api`.
- `infrastructure` depend de `application` et `domain`.
- `api` depend de `application` et expose REST JSON.
- Les dependances pointent vers le metier : `api/infrastructure -> application -> domain`.
- Aucun module ne cree de dependance circulaire.

## Layer Checks

### Domain

- Contient les business objects, value objects, aggregates, exceptions metier, commands, queries, ports et abstractions.
- Ne contient aucune dependance Spring, JPA, HTTP, MapStruct ou infrastructure.
- N'expose pas de donnees mutables directement.
- Les ports expriment une intention metier, pas une technologie.

### Application

- Contient les use cases, services applicatifs, ports applicatifs, views applicatives et abstractions.
- Orchestre les cas d'usage sans logique d'infrastructure.
- Utilise des commands ou queries en entree et retourne des vues ou objets applicatifs.
- N'appelle pas directement les repositories Spring Data, controllers, DTO API ou entites JPA.
- Les use cases sont des implementations concretes de cas d'usage, pas des interfaces.
- Une classe de use case porte une seule intention metier precise et expose exactement une methode `execute`.
- Ne pas regrouper plusieurs intentions dans une classe `*UseCaseSupport`; extraire un use case concret par command/query.
- Eviter les commandes/use cases `upsert`; preferer des intentions explicites `create`, `update`, `delete`, `read/get` ou `search`.
- Un service applicatif ne doit jamais implementer un use case.
- Un service applicatif peut agreger ou orchestrer des use cases par composition explicite.
- Les use cases exposent une methode `execute` et deleguent si besoin vers les services applicatifs.

### Infrastructure

- Contient les implementations techniques dans `in` et `out`.
- `in` contient les adapters entrants techniques hors API separee : listeners, batch, jobs.
- `out` contient les adapters sortants : persistence, API externes, messaging, providers.
- Les implementations de ports sortants vivent sous `out.adapters.<tech>.<bounded-context>`, par exemple `out.adapters.persistences.users`.
- Les adapters de persistence vivent sous `out.adapters.persistences.<bounded-context>` ou directement sous `out.adapters.persistences` pour un adapter transversal.
- Les repositories Spring Data vivent sous `out.adapters.persistences.repositories.<bounded-context>`.
- Les entites JPA vivent sous `out.entities.<bounded-context>`.
- Les autres familles suivent la norme du projet de reference `private-prescription` quand elles existent : `out.dto`, `out.mappers`, `out.views`, `out.validators`.
- `configurations`, `errors`, `actuators` et `abstracts` restent dans infrastructure.
- Les adapters implementent des ports definis par `domain` ou `application`.
- Ne pas utiliser `out.persistence`, `out.database`, `out.query`, `out.security`, `infrastructure.entities`, `infrastructure.repositories.persistence`, `infrastructure.adapters.persistence` ou `entities.persistence`.

### API

- Contient les controllers REST, DTO API, mappers API, erreurs HTTP, configurations API et abstractions API transverses.
- Aucun controller n'injecte directement un repository.
- Aucun endpoint ne retourne d'entite domain ou JPA.
- Le mapping HTTP suit le flux `DTO -> Command/Query -> UseCase -> View/DTO`.
- La traduction HTTP des erreurs reste dans la couche API.

## Review Workflow

1. Lire `documentations/architectures/backend-architecture.md`.
2. Identifier les modules backend touches.
3. Verifier la structure de dossiers et packages par couche.
4. Verifier les dependances Maven entre modules.
5. Verifier les imports Java pour detecter les fuites de Spring/JPA/API vers domain ou application.
6. Verifier les flux REST et persistence : pas d'acces repository depuis API, pas d'entite JPA exposee.
7. Signaler les ecarts par fichier avec correction recommandee.

## Output Format

Fournir :

- conformite globale : OK / KO
- modules controles
- violations detectees
- fichiers concernes
- correctifs recommandes
- points de vigilance
