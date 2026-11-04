# Skill: backend-feature

## Purpose

Implementer une fonctionnalite backend CyberManager dans une architecture DDD.

## When to use

Utiliser cette skill lorsqu'une feature impacte uniquement ou principalement le backend :
- nouveau cas d'usage metier
- nouvel endpoint REST
- nouvelle persistance
- nouveau bounded context
- evolution d'un aggregate ou d'un repository

## Required inputs

- le fichier de feature dans documentations/features
- le bounded context concerne
- les regles de AGENTS.md
- la strategie de journalisation attendue si la feature modifie des flux critiques ou des erreurs metier

## Steps

1. Lire la feature et identifier :
    - objectif metier
    - acteurs
    - donnees manipulees
    - regles metier
    - cas d'usage
    - endpoints attendus
    - points de journalisation utiles sur les entrees API, erreurs et traitements complexes

2. Identifier le bounded context concerne.

3. Creer ou completer la couche domain :
    - entities
    - value objects
    - services de domaine
    - interfaces de repositories
    - packages attendus : `com.cybermanager.domain.businessobjects.<bounded-context>`, `com.cybermanager.domain.valueobjects.<bounded-context>`, `com.cybermanager.domain.ports.<bounded-context>`

4. Creer ou completer la couche application :
    - use cases
    - services applicatifs
    - DTO applicatifs
    - ports
    - packages attendus : `com.cybermanager.application.views.<bounded-context>`, `usecases`, `services`, `ports`
    - les commands et queries vivent dans le module domain sous `domain.commands.<bounded-context>` et `domain.queries.<bounded-context>`

5. Creer ou completer la couche infrastructure :
    - entities JPA
    - repositories Spring Data
    - adapters
    - implementations des ports
    - packages attendus : `com.cybermanager.infrastructure.out.adapters.persistences.<bounded-context>`, `com.cybermanager.infrastructure.out.entities.<bounded-context>`, `com.cybermanager.infrastructure.out.adapters.persistences.repositories.<bounded-context>`
    - les adapters portent l'intention d'implementation de port ; les entites JPA vivent sous `out.entities` et les repositories Spring Data sous `out.adapters.persistences.repositories`
    - pour les autres familles sortantes, suivre la norme infrastructure du projet de reference : `out.adapters.<tech>`, `out.dto`, `out.mappers`, `out.views`, `out.validators`

6. Creer ou completer la couche API :
    - controllers
    - request DTO
    - response DTO
    - mappers
    - packages attendus : `com.cybermanager.api.controllers.<bounded-context>`, `com.cybermanager.api.dtos.<bounded-context>`, `com.cybermanager.api.mappers.<bounded-context>`
    - ajouter une journalisation `info` / `debug` utile sur les requetes entrantes, sans fuite de donnees sensibles

7. Standardiser les erreurs metier backend :
    - ne pas renvoyer les erreurs metier attendues via `IllegalArgumentException` si elles doivent etre comprises par le frontend
    - preferer une exception metier explicite avec un `code` stable, un `message` lisible et un type parmi `VALIDATION`, `FORBIDDEN`, `NOT_FOUND`, `CONFLICT`
    - centraliser la traduction HTTP dans `ApiExceptionHandler`
    - retourner un payload d'erreur coherent : `code`, `message`, `status`, `timestamp`
    - reserver les erreurs techniques non prevues au fallback `500 INTERNAL_ERROR`
    - s'assurer que les erreurs de domaine apparaissent en log avec le contexte fonctionnel utile
    - journaliser aussi les erreurs techniques inattendues avec leur cause et leur point d'entree

8. Ajouter la journalisation backend quand elle apporte un vrai diagnostic :
    - logger les appels API entrants importants avec methode, route, identifiants fonctionnels, parametres utiles et eventuel correlation id
    - logger les entrees et sorties des use cases critiques quand cela aide a comprendre le flux
    - logger en `debug` quelques traitements complexes du domaine, de l'application ou des adapters
    - conserver une ligne de log exploitable pour les anomalies de persistance ou d'appel externe
    - ne jamais logger mot de passe, token, secret, donnees bancaires ou charge utile sensible complete

9. Ajouter les tests obligatoires avant livraison :
    - aucun changement backend ne peut etre livre sans TU et/ou TI pertinents
    - ajouter des TU dedies pour chaque classe concrete domain et application creee ou modifiee
    - ajouter des TI dedies pour chaque repository Spring Data, entite JPA et adapter de persistence cree ou modifie
    - appliquer la skill `backend-tu` pour l'inventaire, la granularite des classes de test et les commandes Maven
    - si un changement backend ne necessite exceptionnellement aucun test executable, documenter explicitement la raison technique dans le resume de livraison

10. Verifier :
    - aucun controller ne manipule une entite JPA
    - aucune dependance Spring dans le domain
    - la logique metier principale est dans domain / application
    - le bounded context est imbrique dans le package de couche et non l'inverse
    - les erreurs metier attendues sont converties en reponses HTTP stables et testables
    - les requetes API entrantes significatives sont observables en log
    - les erreurs de domaine et erreurs techniques sont visibles en log
    - aucune donnee sensible n'est exposee dans les traces
    - tous les TU et/ou TI pertinents ajoutes ou impactes passent
    - aucune classe backend concrete creee ou modifiee ne reste sans test dedie lorsque `backend-tu` l'exige

## Output format

Toujours terminer par :
- resume metier
- modules impactes
- fichiers crees / modifies
- points de vigilance
