---
name: database-feature
description: Configure CyberManager PostgreSQL, Docker database setup, JPA mappings, Hibernate/Flyway modes and external SQL migrations. Use for database and persistence changes.
---

# Skill: database-feature

## Purpose

Implementer ou faire evoluer la couche base de donnees de CyberManager en respectant PostgreSQL, la separation DDD, JPA/Hibernate pour le developpement et Flyway pour la future production.

## When to use

Utiliser cette skill lorsqu'une demande concerne :
- une entite JPA ou une evolution de schema
- une configuration PostgreSQL
- un Dockerfile ou un compose de base de donnees
- un script Flyway
- un schema, un index, une contrainte ou un role SQL
- une optimisation de performance sur les recherches, filtrages, archivages ou purges

## Required inputs

- `AGENTS.md`
- le bounded context concerne
- la feature ou le besoin technique
- les entites JPA impactees
- les variables d'environnement attendues par le backend

## Rules

- la base reste une implementation technique, sans logique metier centrale deplacee depuis le domain
- PostgreSQL est le moteur cible
- les objets SQL sont nommes de facon lisible et stable
- toute configuration locale est reproductible par Docker
- le mode de construction du schema est selectionnable dans `application.yml` avec `CM_DB_MODE`
- les proprietes partagees vivent dans `application-common.yml`, importe par `application.yml`, `application-hibernate.yml` et `application-flyway.yml`
- chaque fichier de mode doit rester utilisable aussi bien comme profil que comme fichier fourni directement avec `spring.config.location`
- le profil `hibernate` est destine au developpement : Hibernate utilise `ddl-auto: create-drop`, Flyway est desactive et aucune donnee ne survit a un redemarrage normal
- le profil `flyway` est destine a la production : Flyway migre le schema et Hibernate utilise `ddl-auto: validate`
- les migrations Flyway de `cybermanager` vivent dans `configuration/backends/cybermanager/db/migration`
- les migrations Flyway de `auth` vivent dans `configuration/backends/auth/db/migration`
- les migrations SQL restent externes aux JAR ; fournir `CM_FLYWAY_LOCATIONS` avec un emplacement `filesystem:` absolu, ou surcharger `spring.flyway.locations` au demarrage
- les deux applications utilisent des tables d'historique Flyway distinctes lorsqu'elles partagent un schema
- chaque evolution d'entite JPA ayant un impact SQL produit dans la meme feature une nouvelle migration Flyway versionnee
- les migrations creees pendant le developpement doivent rester utilisables pour la future production
- aucune migration Flyway ne contient de donnee par defaut, de seed metier ou de reference data, sauf la migration idempotente explicitement prevue pour le premier administrateur et son role `ADMIN`
- aucun `CommandLineRunner`, `ApplicationRunner`, composant bootstrap, `data.sql` ou `import.sql` ne doit injecter des donnees au demarrage
- les donnees fonctionnelles sont creees exclusivement par les cas d'usage exposes par l'application
- une configuration metier absente reste absente : aucun adapter ne doit retourner de fallback code en dur donnant l'illusion de donnees persistees
- les grilles, tarifs et autres configurations metier doivent avoir des tables explicites et retourner un etat vide tant qu'elles n'ont pas ete creees par l'application
- ne jamais modifier une migration Flyway deja livree ; ajouter une nouvelle version
- ne pas casser la compatibilite avec les modules backend existants
- toute recherche metier potentiellement volumique est accompagnee d'une verification d'index
- privilegier les filtrages en base plutot qu'un `findAll()` suivi d'un filtrage en memoire
- pour l'archivage, le reporting ou la purge :
  - identifier les colonnes de filtre exactes
  - indexer les colonnes de filtre et de tri frequentes
  - verifier les suppressions batch et l'ordre des dependances
  - preferer des operations atomiques et transactionnelles

## Steps

1. Lire la feature et identifier le bounded context, les entites et les requetes impactees.
2. Mettre a jour les entites JPA et adapters dans le module infrastructure concerne.
3. Creer une nouvelle migration versionnee dans le repertoire externe du backend concerne sous `configuration/backends/<backend>/db/migration`.
4. Limiter la migration au schema, contraintes, index et transformations necessaires ; la seule donnee initiale autorisee est le premier administrateur explicitement demande avec son role `ADMIN`.
5. Verifier le mode `hibernate` avec `CM_DB_MODE=hibernate` et confirmer que le schema est recree sans conserver les donnees du lancement precedent.
6. Verifier le mode `flyway` sur une base vide avec `CM_DB_MODE=flyway` et confirmer que `ddl-auto: validate` passe.
7. Documenter les variables, le lancement, la migration, les index et les points de vigilance.
8. Rechercher toute nouvelle injection de donnees (`CommandLineRunner`, `ApplicationRunner`, `data.sql`, `import.sql`, `INSERT`) et la refuser hors migration idempotente du premier administrateur.

## Output format

Toujours fournir :
- objectif de l'evolution BDD
- module et migrations Flyway crees ou modifies
- entites JPA impactees
- variables d'environnement et mode `CM_DB_MODE`
- commandes de verification Hibernate et Flyway
- index ou optimisations ajoutes
- confirmation de l'absence de donnees par defaut hors administrateur initial explicitement autorise
- points de vigilance
