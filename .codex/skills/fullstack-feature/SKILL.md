---
name: fullstack-feature
description: Implement end-to-end CyberManager features across the DDD backend and Angular frontend, including REST contracts and database migrations.
---

# Skill: fullstack-feature

## Purpose

Implementer une feature CyberManager de bout en bout, y compris son evolution de schema reproductible en developpement et en future production.

## When to use

Utiliser cette skill lorsqu'une feature necessite :
- backend et frontend
- nouveaux cas d'usage
- nouvelle API
- nouvel ecran Angular
- nouvelle entite JPA ou evolution du schema PostgreSQL

## Required inputs

- le fichier de feature
- `AGENTS.md`
- les bounded contexts impliques
- la skill `database-feature` si la persistance ou le schema evolue
- les skills `frontend-feature` et `frontend-design-system` si l'ecran Angular evolue

## Rules

- respecter les couches `domain`, `application`, `infrastructure` et `api`
- toute evolution JPA ayant un impact SQL inclut une migration Flyway versionnee dans les resources du backend API concerne
- les migrations de `cybermanager` vivent dans `configuration/backends/cybermanager/db/migration`
- les migrations de `auth` vivent dans `configuration/backends/auth/db/migration`
- les migrations SQL sont externes aux JAR et chargees via un emplacement `filesystem:` absolu fourni au demarrage
- le schema doit fonctionner en mode `hibernate` avec `ddl-auto: create-drop` pour le developpement et en mode `flyway` avec validation Hibernate pour la production
- le mode est selectionne avec `CM_DB_MODE` depuis `application.yml`
- les fichiers de mode importent `application-common.yml` et doivent fonctionner aussi lorsqu'ils sont lances directement
- aucune valeur par defaut, donnee de reference ou seed metier ne doit etre injecte ; seule la migration idempotente explicitement prevue pour le premier administrateur et son role `ADMIN` est autorisee
- toute donnee fonctionnelle doit pouvoir etre creee par un cas d'usage de l'application
- aucune donnee metier issue de l'API ne doit etre remplacee par un fallback code en dur dans le frontend ou le backend
- les donnees metier persistantes, notamment tarifs et grilles, ne doivent pas etre stockees dans `localStorage` ou `sessionStorage`
- les jetons, utilisateurs courants, reglages de grilles et caches applicatifs Angular restent uniquement en memoire
- au chargement du frontend, les anciennes cles applicatives `cm_*` de `localStorage` et `sessionStorage` doivent etre supprimees
- lorsqu'aucune configuration n'existe en base, le frontend affiche un etat vide et des formulaires sans valeurs metier prechargees

## Steps

1. Lire la feature.
2. Identifier les bounded contexts.
3. Implementer le backend :
   - domain
   - application
   - infrastructure et entites JPA
   - migration Flyway sans donnees par defaut si le schema evolue, hors administrateur initial explicitement autorise
   - API
4. Implementer le frontend :
   - pages
   - services
   - modeles
   - composants
   - directives UI/CSS de `frontend-design-system`
5. Connecter le frontend au backend.
6. Ajouter les tests utiles.
7. Verifier le mode Hibernate de developpement.
8. Verifier le mode Flyway sur base vide puis la validation Hibernate.
9. Rechercher et exclure tout bootstrap ou seed de donnees hors migration du premier administrateur.
10. Verifier le respect DDD et la coherence API/UI.
11. Mettre a jour la documentation de feature si le comportement livre evolue.

## Output format

Toujours fournir :
- decoupage backend
- decoupage frontend
- endpoints exposes
- ecrans impactes
- migrations Flyway et entites JPA impactees
- mode de construction de la base verifie
- confirmation de l'absence de donnees par defaut
- fichiers crees ou modifies
- risques ou points ouverts
