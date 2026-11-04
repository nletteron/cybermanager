# Feature ID: F-007

## Nom

Gestion du temps de connexion

## Contexte

Le cybercafe doit suivre le temps de connexion utilise par les clients journaliers et par les abonnes avec une interface fluide, orientee comptoir, sans liste exhaustive inefficace.

## Acteurs

- Employe du cybercafe

## Objectif

Permettre de demarrer, suivre et terminer une session de connexion a partir d'une recherche client unique, incluant les clients journaliers et les abonnes, ou d'un nouveau nom.

## Bounded context

- session
- customer
- subscription
- sales
- debt

## Regles metier

- une session possede une heure de debut
- une session peut posseder une heure de fin
- une session est liee a un client
- un client journalier peut etre cree directement dans l'ecran sessions
- un client existant, journalier ou abonne, doit etre retrouvable par nom et selectionnable dans la meme liste
- la selection reutilise l'identifiant et la fiche du client ; elle ne cree pas de doublon
- un nouveau nom peut demarrer une session avec creation d'un client journalier ; un nom correspondant exactement a un client propose exige de selectionner ce client
- un abonne actif avec du credit doit pouvoir demarrer et terminer une session
- un abonne sans credit disponible ne peut pas demarrer une nouvelle session
- un abonne deja present dans `Sessions en cours` ne peut pas etre lance une seconde fois tant que sa session n'est pas terminee
- un abonnement peut toujours etre vendu depuis le detail d'une session ; le bouton et le panneau de conversion dedies sont retires de cet ecran
- l'affichage des sessions du jour doit etre centre sur le nom du client
- la gestion des postes n'est pas exposee dans l'interface utilisateur
- la vue de detail d'une session doit afficher les achats du jour, le resume abonnement et les dettes ouvertes du client
- l'arret d'une session client journalier facturee et non reglee cree une dette ouverte
- les abonnements d'un client abonne sont cumulables
- l'arret d'une session doit restituer immediatement le total a payer pour cette session
- une session en cours peut etre mise en pause puis reprise sans quitter la liste des sessions en cours
- le detail d'une session peut declencher une vente produit ou une vente d'abonnement en modale
- le detail `A payer` inclut uniquement la connexion et les achats de la session non mis en dette
- la modale d'arret distingue ce qui est encaisse maintenant des dettes deja ouvertes
- la fiche client complete doit etre consultable directement depuis le nom du client dans l'ecran sessions, via une modale
- le detail d'une session contient en troisieme colonne un commentaire enregistre sur le client et conserve pour ses prochaines sessions

## Backend

### Cas d'usage
- StartSession
- StopSession
- GetCurrentSession
- SearchSessionsOfDay
- DeductSubscriberTime
- CreateCustomer
- ConvertCustomerToSubscriber

### Domain
- Session
- SessionId
- SessionStartTime
- SessionEndTime
- ConsumedDuration
- RemainingSubscriptionTime

### Application
- StartSessionUseCase
- StopSessionUseCase
- SearchSessionsOfDayUseCase
- SessionView
- CurrentSessionsView

### Infrastructure
- SessionJpaEntity
- SessionJpaRepository
- SessionRepositoryAdapter

### API
- GET /api/sessions/day
- GET /api/sessions/current
- POST /api/sessions/start
- POST /api/sessions/{id}/pause
- POST /api/sessions/{id}/resume
- POST /api/sessions/{id}/stop
- POST /api/customers
- POST /api/customers/{id}/convert-to-subscriber

## Frontend

### Ecrans
- creation d'un client journalier et demarrage de session
- recherche unique d'un client existant, incluant les abonnes, puis demarrage de session
- vue des sessions en cours
- detail developpable par session en cours

### Composants
- walk-in-session-form
- customer-search-box
- active-sessions-table
- customer-comment

### Navigation et comportements UI
- l'ecran permet de saisir directement le nom d'un client journalier
- le champ `Client` recherche tous les types de clients des les premieres lettres, sans blocs `Abonne existant` ou `Nouvel abonne`
- les resultats affichent le nom, le type et le credit si applicable, sous forme de lignes sans encadre individuel
- un client inactif ou ayant deja une session ouverte reste identifiable dans les resultats mais ne peut pas etre lance
- modifier le nom annule la selection et les recherches precedentes afin d'eviter des resultats obsoletes
- toutes les zones de recherche possedent un label explicite
- le tableau operationnel `Sessions en cours` affiche le nom du client, son type, son etat et sa date de debut ; il n'y a pas de second tableau `Sessions du jour` sur cet ecran
- les en-tetes `Client`, `Etat` et `Debut` sont cliquables et permettent de trier dans les deux sens, avec une fleche indiquant le tri actif
- les commandes de tri conservent la typographie des autres en-tetes, sans fond, contour ou ombre de bouton ; les noms des clients sont des liens sans encadre
- le tri initial affiche les dates de debut les plus recentes ; le premier clic sur `Client` trie de A a Z, le premier clic sur `Etat` suit l'ordre `En cours`, `Pause`, `A payer`, `Terminee`
- les filtres masques par defaut combinent le nom du client, la date de debut minimale et un etat
- le filtre `Etat` propose une liste avec recherche immediate, insensible a la casse et aux accents, ainsi qu'un choix `Tous les etats`
- rechercher dans la liste des etats ne modifie pas le filtre deja selectionne ; une selection applique immediatement le filtre au tableau
- les filtres et le tri restent actifs pendant les mises a jour des sessions et ne sont conserves que pour la page courante, sans stockage navigateur
- la zone `Sessions en cours` garde une hauteur stable, meme quand peu de lignes sont affichees
- un petit `+` en derniere colonne ouvre sous la session le detail des achats, l'abonnement et le commentaire client ; il devient `-` pour replier le detail
- le detail est organise en trois colonnes sur grand ecran et empile sur petit ecran ; les actions de vente utilisent des couleurs neutres
- la vente produit pour une session en cours se declenche depuis la colonne de gauche sans quitter l'ecran
- la vente produit depuis session permet aussi de cocher la creation d'une dette
- le tableau des achats du detail session doit rester tres compact pour supporter de nombreuses lignes
- les achats du jour et les dettes creees le jour meme sont affiches dans une chronologie unique
- une dette issue d'une vente apparait sur la meme ligne que la vente correspondante, avec une icone rouge de non-paiement
- le resume du detail signale seulement la presence de dettes par une icone a droite
- cliquer sur le nom d'un client dans `Sessions en cours` ouvre une modale de fiche client avec abonnement, credit, achats et dettes
- dans cette modale client, seuls les `10` derniers achats sont affiches initialement, avec chargement progressif par paquets de `10`
- les dates et heures visibles dans la session suivent le format `JJ/MM/AAAA HH:mm`
- l'arret d'une session affiche immediatement le total a payer calcule pour cette session
- chaque ligne de session en cours propose une action `Pause` ou `Reprendre` sans sortir la session de la liste
- le resume abonnement rappelle que les abonnements se cumulent
- l'interface privilegie des formulaires courts, compacts et des actions immediates pour un usage comptoir
- les colonnes et tableaux sont compactes et scrollables pour supporter un grand nombre de sessions
- les sessions en cours sont affichees sans pagination fonctionnelle cote ecran et utilisent toute la hauteur disponible avant scroll interne
- le formulaire unique `Client` reste compact dans la colonne de gauche avec son action `Demarrer la session`

## Criteres d'acceptation

- un client journalier peut etre cree et demarre depuis l'ecran sessions
- un client existant, journalier ou abonne, peut etre selectionne par nom et demarrer une session sans duplication de sa fiche
- un abonne deja present dans `Sessions en cours` ne peut pas etre relance et un message d'erreur explicite est affiche
- le tableau des sessions affiche les noms des clients et permet un tri croissant et decroissant depuis les colonnes `Client`, `Etat` et `Debut`
- la selection d'un etat se combine aux filtres de nom et de date ; `Tous les etats` retire uniquement le filtre d'etat
- une recherche d'etat sans resultat affiche un etat vide ; un filtrage sans session affiche un message explicite dans le tableau
- les anciens blocs abonnes et l'action `Convertir` ne sont plus presents dans la page sessions
- le commentaire client peut etre enregistre et retrouve apres reouverture du detail, sans modifier les montants de session
- le detail d'une session affiche les dettes ouvertes du client en rouge
- le detail des achats reste lisible sans occuper excessivement de hauteur
- l'utilisateur voit le montant total a payer apres l'arret d'une session
- le detail d'une session reste rattache a sa ligne lorsque le tri change
- les operations comptoir avancees sont detaillees dans `F-016-session-counter-operations.md`
