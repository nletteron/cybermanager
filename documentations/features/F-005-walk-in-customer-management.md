# Feature ID: F-005

## Nom

Gestion des clients specifiques

## Contexte

Le cybercafe doit disposer d'un ecran client permettant de retrouver les clients connus du systeme, de creer des clients specifiques et des clients abonnes, et de consulter leur situation commerciale.

## Acteurs

- Employe du cybercafe

## Objectif

Permettre la consultation, la recherche, la creation et la mise a jour des clients specifiques et abonnes, sans obliger l'utilisateur a passer par le flux session.

## Bounded context

- customer
- sales
- subscription

## Regles metier

- un client journalier est cree naturellement depuis le flux session
- un client specifique peut etre cree directement depuis l'ecran client
- un client abonne peut etre cree directement depuis l'ecran client
- la creation d'un client abonne doit etre associee a une offre d'abonnement initiale
- la fiche client doit afficher l'abonnement en cours si applicable
- la fiche client doit afficher l'historique des achats du client
- la fiche client doit afficher les dettes ouvertes du client en ligne rouge
- la liste des clients doit permettre de distinguer les `Client` et les `Abonne`
- les abonnements du client sont cumulables
- les dates et heures visibles dans la fiche client suivent le format `JJ/MM/AAAA HH:mm`
- un commentaire facultatif de 2000 caracteres maximum est rattache au client, et non a une session
- le commentaire est normalise avant sauvegarde ; une saisie vide ou uniquement composee d'espaces efface le commentaire
- le commentaire est conserve lors des changements de nom, achats d'abonnement, conversions et consommations de credit, et reste disponible pour les futures sessions du client

## Backend

### Cas d'usage
- CreateCustomer
- SearchCustomers
- GetCustomerDetails
- UpdateCustomer

### Domain
- Customer
- CustomerId
- CustomerType
- CustomerName
- CustomerStatus

### Application
- CreateCustomerUseCase
- SearchCustomersUseCase
- GetCustomerDetailsUseCase
- UpdateCustomerUseCase
- GetCustomerCommentUseCase
- UpdateCustomerCommentUseCase
- CustomerCommentView
- CustomerView
- CustomerDetailsView

### Infrastructure
- CustomerJpaEntity
- CustomerJpaRepository
- CustomerRepositoryAdapter
- SaleRepositoryAdapter

### API
- GET /api/customers
- GET /api/customers/{id}
- POST /api/customers
- PUT /api/customers/{id}
- GET /api/customers/{id}/comment : lecture authentifiee du commentaire client
- PATCH /api/customers/{id}/comment : enregistrement authentifie de `{ "comment": "..." }`, ou `null` pour effacer

### Persistance du commentaire

- colonne nullable `cybermanager.cm_customers.comment`, de type `VARCHAR(2000)`
- migration externe `configuration/backends/cybermanager/db/migration/V6__add_customer_comment.sql`, sans donnees initiales
- les modes Hibernate et Flyway utilisent le meme mapping ; les donnees existantes restent intactes et leur commentaire est absent tant qu'il n'est pas saisi

## Frontend

### Ecrans
- liste des clients
- creation d'un client specifique
- creation d'un client abonne
- fiche client detaillee
- navigation depuis le shell principal vers l'ecran clients
- panneau de droite creation / edition refermable

### Composants
- customers-table
- customer-search-box
- customer-create-form
- customer-details-panel
- customer-purchases-table

### Navigation et comportements UI
- l'ecran charge les clients connus du systeme
- toutes les zones de recherche possedent un label explicite
- la recherche client par nom filtre la liste immediatement
- la taille de page est configurable et la pagination reste dans la page
- le tableau client reste compact pour afficher plus de lignes avec moins d'espace vertical
- la creation permet de choisir entre `Client` et `Abonne`
- si le type choisi est abonne, une offre d'abonnement doit etre selectionnee
- le bouton `Nouveau client` ouvre un panneau de droite avec animation puis disparait tant que le panneau est ouvert
- l'ouverture d'une fiche client affiche l'abonnement en cours, l'historique des achats et les dettes ouvertes en rouge
- la fiche client rappelle que les abonnements se cumulent
- le resume du detail signale l'existence de dettes par une icone rouge de non-paiement, sans repeter toute la liste a droite
- la fiche detaillee reutilise le meme panneau que la creation et l'edition
- depuis le detail d'une session, une troisieme colonne permet de consulter, modifier et enregistrer le commentaire de sa fiche client
- l'enregistrement affiche une confirmation ; une erreur de chargement interdit d'ecraser silencieusement un commentaire, et une erreur de sauvegarde conserve la saisie

## Criteres d'acceptation

- un employe peut creer un client specifique depuis l'ecran client
- un employe peut creer un client abonne depuis l'ecran client
- la fiche client affiche l'abonnement en cours
- la fiche client affiche les achats de produits, d'abonnements et autres ventes
- la fiche client affiche les dettes non reglees en rouge
- le commentaire retrouve apres reouverture du detail est celui du client selectionne, y compris dans une autre session
- enregistrer ou effacer le commentaire ne modifie ni le credit ni le type du client
