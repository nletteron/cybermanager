# AGENTS.md

## Contexte projet

CyberManager est une application fullstack de gestion d'un cyber cafe.

Stack cible :
- Backend : Java 21 / Spring Boot
- Build : Maven multi-module
- Architecture backend : DDD / hexagonale
- Frontend : Angular
- API : REST JSON
- Mapping : MapStruct
- Base de donnees : PostgreSQL
- Tests : JUnit / Mockito / Spring Boot Test / Angular unit tests

## Priorite des regles

Les skills locales dans `.codex/skills` sont la reference operationnelle prioritaire.

En cas de conflit :
1. suivre la skill explicitement demandee par l'utilisateur
2. suivre les skills applicables a la tache
3. suivre `documentations/architectures/backend-architecture.md`
4. suivre le present `AGENTS.md`

Skills locales principales :
- `backend-ddd-guard` : controle DDD / architecture backend
- `backend-feature` : implementation backend
- `backend-rules-coding` : lisibilite, espacement Java et retour unique obligatoire
- `backend-tu` : regles TU/TI backend
- `database-feature` : PostgreSQL, Flyway, Hibernate et Docker BDD
- `frontend-feature` : implementation Angular par feature
- `frontend-design-system` : UX, composants et CSS Angular
- `fullstack-feature` : feature de bout en bout
- `pull-request-backend`, `pull-request-frontend` : preparation de PR

## Objectifs

L'application permet de :
- gerer les utilisateurs et leurs roles
- gerer les produits, categories, abonnements, clients et sessions
- suivre les ventes, dettes, vues de monitoring et historique
- sauvegarder et restaurer la base lorsque la feature le prevoit

## Architecture backend attendue

Le backend est organise selon les standards DDD / hexagonaux : `domain`, `application`, `infrastructure`, `api`.

Reference locale :
- `documentations/architectures/backend-architecture.md`

Structure Maven actuelle :

```text
backend/
|- cybermanager-parent
|- auth
|  |- cm-auth-domain
|  |- cm-auth-application
|  |- cm-auth-infrastructure
|  `- cm-auth-api
`- cybermanager
   |- cm-domain
   |- cm-application
   |- cm-infrastructure
   `- cm-api
```

## Regles DDD backend

### Convention de packaging backend

Le packaging Java est organise d'abord par couche technique, puis par bounded context a l'interieur de cette couche.

Exemples attendus :
- `com.cybermanager.domain.businessobjects.users`
- `com.cybermanager.domain.valueobjects.users`
- `com.cybermanager.domain.commands.users`
- `com.cybermanager.domain.queries.users`
- `com.cybermanager.domain.ports.users`
- `com.cybermanager.application.views.users`
- `com.cybermanager.application.usecases.users`
- `com.cybermanager.application.services.users`
- `com.cybermanager.application.ports.users`
- `com.cybermanager.api.controllers.users`
- `com.cybermanager.api.dtos.users`
- `com.cybermanager.api.mappers.users`
- `com.cybermanager.api.errors`
- `com.cybermanager.api.configurations`
- `com.cybermanager.api.abstracts`
- `com.cybermanager.infrastructure.configurations.persistence`
- `com.cybermanager.infrastructure.out.adapters.persistences.users`
- `com.cybermanager.infrastructure.out.entities.users`
- `com.cybermanager.infrastructure.out.adapters.persistences.repositories.users`
- `com.cybermanager.infrastructure.out.adapters.security.users`

Regles :
- ne pas mettre le bounded context a la racine du package avant la couche
- le bounded context apparait a l'interieur du package de couche
- ne pas utiliser `domain.model`, `domain.port`, `application.commands`, `application.queries`, `infrastructure.entities`, `infrastructure.repositories.persistence`, `infrastructure.adapters.persistence`, `infrastructure.out.persistence`, `infrastructure.out.database`, `infrastructure.out.query`, `infrastructure.out.security` ou `api.config`
- appliquer la meme logique dans `auth`, par exemple `com.cybermanager.auth.domain.businessobjects`, `com.cybermanager.auth.domain.ports`, `com.cybermanager.auth.infrastructure.out.adapters.persistences`, `com.cybermanager.auth.infrastructure.out.entities`

### Domain

Contient exclusivement :
- business objects
- value objects
- agregats
- exceptions metier
- commands
- queries
- ports entrants et sortants
- abstractions metier

Contraintes :
- aucune dependance Spring
- aucune annotation JPA
- aucune logique technique
- aucune exposition HTTP
- aucun DTO API ou objet infrastructure
- package racine attendu : `com.cybermanager.domain.*` ou `com.cybermanager.auth.domain.*`

### Application

Contient :
- use cases
- services applicatifs
- ports applicatifs
- views / read models applicatifs
- orchestrations metier
- abstractions applicatives

Contraintes :
- depend du domain, jamais de l'API
- ne contient pas de logique d'infrastructure
- n'appelle pas directement les repositories Spring Data
- annotations autorisees : `@Transactional`, `@Qualifier`, `@Service`
- les use cases prennent en entree une `Query` ou une `Command` du domain et retournent une `View`
- les use cases exposent une methode `execute`
- package racine attendu : `com.cybermanager.application.*` ou `com.cybermanager.auth.application.*`

### Infrastructure

Contient :
- implementations techniques
- entites JPA
- repositories Spring Data
- adapters entrants techniques hors API REST
- adapters sortants : persistence, providers, messaging, API externes
- clients externes
- configuration technique
- securite technique

Contraintes :
- peut dependre de `application` et `domain`
- ne doit pas contenir la logique metier centrale
- pas d'appel direct aux `JpaRepository` hors adapter
- les entites JPA vivent sous `infrastructure.out.entities.<bounded-context>`
- les repositories Spring Data vivent sous `infrastructure.out.adapters.persistences.repositories.<bounded-context>`
- les adapters de persistence vivent sous `infrastructure.out.adapters.persistences.<bounded-context>`
- les adapters transverses de persistence peuvent vivre directement sous `infrastructure.out.adapters.persistences` lorsque le bounded context n'apporte rien, comme dans le projet de reference `private-prescription`
- les packages `infrastructure.out.adapters.<tech>.<bounded-context>` portent les implementations de ports et l'intention metier ; `out.entities` porte les entites JPA, et les repositories Spring Data restent sous `out.adapters.persistences.repositories`
- package racine attendu : `com.cybermanager.infrastructure.*` ou `com.cybermanager.auth.infrastructure.*`

### API

Contient :
- controllers REST
- request / response DTO
- mappers API
- gestion des erreurs HTTP
- configurations API
- abstractions API transverses

Contraintes :
- un controller ne retourne jamais d'entite domain ou JPA
- un controller appelle les use cases / services applicatifs
- pas d'acces direct aux repositories depuis les controllers
- le flux REST suit `DTO -> Command/Query -> UseCase -> View/DTO`
- package racine attendu : `com.cybermanager.api.*` ou `com.cybermanager.auth.api.*`

## Regles de code Java backend

Appliquer `backend-rules-coding` avant livraison.

Regles obligatoires :
- une ligne vide entre les methodes et constructeurs
- une ligne vide apres l'accolade ouvrante d'une classe, record, enum ou interface avant le premier membre
- une ligne vide avant la derniere accolade d'une classe, record, enum ou interface contenant des membres
- meme regle d'espacement dans les interfaces
- aucune ligne vide entre une annotation et sa declaration
- les lignes vides restent vraiment vides, sans espaces ni tabs
- une seule instruction `return` par methode qui retourne une valeur
- eviter `return;` dans les methodes `void`, sauf absence d'alternative propre

## Regles base de donnees

Appliquer `database-feature` pour toute evolution BDD.

Regles :
- PostgreSQL est le moteur cible
- la configuration locale doit etre reproductible par Docker
- `CM_DB_MODE=hibernate` : developpement, `ddl-auto: create-drop`, Flyway desactive
- `CM_DB_MODE=flyway` : production ou verification stable, Flyway actif, Hibernate en `validate`
- `application-common.yml` contient les proprietes partagees
- `application-hibernate.yml` et `application-flyway.yml` importent `application-common.yml`
- migrations CyberManager : `configuration/backends/cybermanager/db/migration`
- migrations Auth : `configuration/backends/auth/db/migration`
- migrations SQL externes aux JAR, chargees via `CM_FLYWAY_LOCATIONS` (`filesystem:` avec un chemin absolu) ou `spring.flyway.locations`
- les tables d'historique Flyway sont distinctes pour `auth` et `cybermanager`
- ne jamais modifier une migration deja livree ; ajouter une nouvelle version
- aucune donnee par defaut, seed metier ou reference data hors migration idempotente du premier administrateur `ADMIN`
- aucun `CommandLineRunner`, `ApplicationRunner`, `data.sql` ou `import.sql` pour injecter des donnees metier
- une configuration metier absente reste absente : pas de fallback code en dur donnant l'illusion d'une donnee persistante
- verifier les index pour les recherches, filtrages, archivages et purges potentiellement volumineux

## Architecture frontend attendue

Organisation Angular par feature.

```text
frontend/src/app/
|- core/
|- shared/
|- layout/
`- features/
   |- auth/
   |- dashboard/
   |- users/
   `- reporting/
```

### core

Contient les services transverses, gardes, interceptors et infrastructure Angular commune.

### shared

Contient :
- composants reutilisables
- pipes
- directives
- modeles partages

### features

Chaque feature contient :
- `pages`
- `components`
- `services`
- `models`
- `routes` si necessaire
- etat local si necessaire

### Structuration frontend attendue

Pour toute nouvelle page Angular de gestion ou de CRUD :
- la page principale vit dans `features/<feature>/pages`
- les sous-composants reutilisables vivent dans `features/<feature>/components`
- les appels HTTP restent dans `features/<feature>/services`
- les interfaces et types restent dans `features/<feature>/models`
- les styles substantiels vivent dans un fichier `*.component.css`
- utiliser `frontend-design-system` pour les pages, panneaux, filtres, boutons, modales et CSS generiques
- les listes CRUD privilegient :
  - barre d'actions haute avec bouton `Filtres` et action principale `Creer un nouvel ...`
  - bloc `filters-grid collapsible` avec animation legere
  - champs de filtre compacts et alignes a gauche
  - liste a gauche et panneau de creation / edition a droite si le flux s'y prete
  - pas de bouton `Nouveau` duplique dans la colonne de droite si l'action de creation principale est deja visible en haut
  - panneau secondaire ferme par defaut s'il sert seulement a de l'ajout ponctuel

## Bounded contexts

### auth
Authentification et recuperation de l'utilisateur courant.

### users
Gestion des employes, administrateurs et roles applicatifs.

### product / catalog
Catalogue des produits et categories vendus au cybercafe.

### subscription
Offres d'abonnement et credit de temps associe.

### customer
Gestion des clients occasionnels et abonnes.

### sales
Vente de produits, d'abonnements et de temps de connexion.

### session
Gestion des sessions de connexion sur les postes.

### monitoring
Vue operationnelle de la journee, agregee et lisible.

### reporting
Historique journalier et consultation des journees passees.

### database
Sauvegarde et restauration de la base lorsque la feature est active.

## Regles frontend

- architecture par feature
- composants simples et fortement types
- services Angular dedies aux appels API
- pas de logique metier lourde dans les composants
- typage strict TypeScript
- toujours isoler les modeles d'entree/sortie
- ne pas stocker jeton, utilisateur courant, cache applicatif, configuration metier ou donnees metier dans `localStorage`, `sessionStorage` ou IndexedDB
- conserver l'etat ephemere dans des services Angular a base de `signals` ou un state local explicite
- pour les formulaires reactifs, ne pas attendre de reactivite d'un `computed` base directement sur `form.getRawValue()`
- preferer `signals`, `valueChanges` ou un state local explicite pour les filtres instantanes
- toute page de configuration doit normaliser les valeurs avant sauvegarde et rendre visible la confirmation d'enregistrement
- ne jamais remplacer une donnee metier absente de l'API par un fallback code en dur
- afficher un etat vide quand l'API ne retourne aucune configuration ou donnee metier
- toute modale frontend de calcul ou de simulation doit reproduire la logique metier backend de reference ou expliciter si elle n'est qu'indicative
- verifier mobile et desktop pour les refontes visuelles significatives

## Workflow attendu pour Codex

Pour toute nouvelle fonctionnalite :

1. lire la feature dans `documentations/features`
2. identifier les bounded contexts impactes
3. appliquer les skills pertinentes
4. implementer le domain
5. implementer l'application
6. implementer l'infrastructure
7. implementer l'API
8. implementer le frontend Angular si necessaire
9. ecrire ou completer les tests
10. verifier le respect DDD avec `backend-ddd-guard`
11. verifier les regles de code Java backend si du Java a ete modifie
12. mettre a jour la documentation de feature si le comportement livre a evolue
13. resumer les fichiers modifies

## Regles de livraison

Une tache est terminee si :
- le code compile
- les tests pertinents passent
- les couches DDD sont respectees
- aucun controller n'expose d'entite
- le frontend consomme correctement l'API
- la documentation fonctionnelle est a jour quand le comportement utilisateur change
- un resume clair des changements est fourni

## Interdictions

- ne pas retourner d'entity JPA dans les endpoints
- ne pas injecter de repository dans un controller
- ne pas mettre de logique metier complexe dans les adapters
- ne pas melanger DTO API, DTO applicatifs et objets domain
- ne pas creer de dependance circulaire entre modules
- ne pas reintroduire les anciens packages interdits par les skills
- ne pas inventer de fallback metier quand une donnee est absente en base

## Convention de reponse de l'agent

Quand une implementation est proposee, toujours fournir :
- les modules concernes
- les fichiers crees / modifies
- les choix d'architecture
- les points de vigilance
