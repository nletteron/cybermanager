# Feature ID: F-026

## Nom

Gestion backend des erreurs

## Contexte

CyberManager centralise les messages d'erreur exposes par l'API afin de ne plus coder les libelles techniques ou metier directement dans les handlers HTTP.

## Acteurs

- Administrateur technique
- Developpeur backend
- Consommateur API

## Objectif

Associer chaque code d'erreur du domaine a une entree de catalogue persistable et externalisee dans `configuration/backends/cybermanager/error.json`.

## Bounded context

- shared
- database

## Regles metier

- toute erreur metier porte un code stable via `DomainException`
- le catalogue separe les erreurs `business` et `technical`
- le fichier JSON externe est charge au demarrage
- le fichier JSON est recharge automatiquement quand il change
- les erreurs chargees sont persistees en base dans `cm_error_catalog`
- le resolver infrastructure associe le code domaine au code d'erreur du catalogue

## Backend

### Domain
- DomainException

### Application
- BusinessException

### Infrastructure
- ErrorCatalogJpaEntity
- ErrorCatalogJpaRepository
- ErrorCatalogPersistenceAdapter
- ErrorCatalogLoader
- ErrorCatalogCache
- DatabaseErrorMessageResolver
- ErrorMessageDomainResolver
- ErrorMessageInfrastructureResolver

### API
- ApiExceptionHandler

## Configuration

- `CM_ERROR_CATALOG_FILE` permet de pointer vers le fichier externe
- `CM_ERROR_CATALOG_RELOAD_DELAY_MS` controle la frequence de detection de changement
- fichier par defaut : `configuration/backends/cybermanager/error.json`

## Criteres d'acceptation

- une exception domaine retourne le code catalogue et le message resolu
- une erreur technique API retourne le code catalogue et le message resolu
- une modification du fichier externe est rechargee sans redemarrage applicatif
- le mode Flyway cree la table `cm_error_catalog`
