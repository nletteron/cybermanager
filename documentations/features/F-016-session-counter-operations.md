# Feature ID: F-016

## Nom

Operations comptoir avancees sur les sessions

## Contexte

L'ecran `Sessions` est devenu le coeur operationnel du comptoir. Il doit gerer la creation de session, la pause, les ventes additionnelles, les dettes, l'encaissement a l'arret et les preferences d'affichage sans perdre en lisibilite.

## Acteurs

- Employe du cybercafe

## Objectif

Fournir un ecran `Sessions` compacte, orientee comptoir, qui centralise les actions rapides et le detail financier de chaque client.

## Bounded context

- session
- customer
- sales
- subscription

## Regles metier

- un seul champ `Client` permet de rechercher et selectionner un client connu, journalier ou abonne, ou de saisir un nouveau nom
- une pause est une vraie pause : aucun decompte de credit ni de temps pendant la pause
- apres reprise, le temps de pause n'est pas ajoute au temps consomme
- pour un client standard, le decompte visuel se fait a la seconde et l'arrondi a la minute superieure n'intervient qu'a l'arret
- pour un abonne, le credit temps se decompte a la seconde
- toute minute entamee est consommee au moment du calcul final de connexion
- la modale d'arret distingue ce que le client paie maintenant de ses dettes deja ouvertes
- une session ne peut pas etre dans l'etat `Paye` tant qu'il reste des dettes ouvertes
- les dettes ouvertes d'un client doivent reapparaitre meme lors d'une nouvelle session plus tard
- une vente de produit ne doit jamais effacer les dettes deja existantes
- chaque vente et chaque dette conserve une ligne distincte en base, meme lorsque le produit, le montant et la date sont identiques ; sans choix enregistre, les lignes sont affichees separement ; la fusion visuelle est optionnelle et reversible, avec un choix persistant par utilisateur pour toutes ses sessions
- le lien entre une vente et sa dette repose sur leurs identifiants persistants, jamais sur le libelle ou une date arrondie
- supprimer une ligne retire uniquement la vente ou la dette selectionnee et son eventuelle contrepartie liee, sans toucher aux autres lignes similaires
- les abonnements d'un meme client sont cumulables et augmentent le credit disponible
- dans la modale de paiement, un abonnement ajoute pendant le reglement augmente d'abord le credit, reduit le cout du depassement eventuel, puis ajoute son propre prix au montant final a regler
- la colonne `A payer` d'une session doit suivre la meme regle que la modale de paiement pour les abonnements deja rattaches a la session
- l'action `Fin de journee` cloture aussi les sessions encore actives
- lors de la `Fin de journee`, le cout de connexion restant et les achats rattaches a chaque session non soldee sont passes en dette
- la modale de paiement affiche les dettes ouvertes separement et ne les additionne pas silencieusement au reglement de la session
- pendant le paiement, l'ajout ou le retrait d'un abonnement met a jour immediatement le montant a regler
- la modale de paiement peut proposer l'ajout d'un abonnement meme pour un client journalier si une offre est disponible
- tant qu'une session arretee n'est pas payee, un abonnement vendu dans son detail peut etre supprime

## Backend

### Cas d'usage

- StartSession
- PauseSession
- ResumeSession
- StopSession
- CreateProductSale
- CreateSubscriptionSale
- SettleDebt
- GetCustomerDetails
- GetSessionDisplayPreference
- ConfigureSessionDisplayPreference

### Application

- SessionApplicationService
- SalesApplicationService
- CustomerApplicationService
- SessionView

### Infrastructure

- CafeSessionRepositoryAdapter
- SessionDisplayPreferenceRepositoryAdapter
- migrations Flyway externes dans `configuration/backends/cybermanager/db/migration`

### API

- GET /api/sessions/current
- GET /api/sessions/day
- GET /api/sessions/display-preference : preference de l'utilisateur authentifie, ou 204 s'il n'a encore fait aucun choix
- PUT /api/sessions/display-preference : enregistre `{ "groupEntries": true }` ou `false` pour cet utilisateur ; son identite provient exclusivement du jeton, jamais de la requete
- POST /api/sessions/start
- POST /api/sessions/{id}/pause
- POST /api/sessions/{id}/resume
- POST /api/sessions/{id}/stop
- POST /api/sales/products
- POST /api/sales/subscriptions
- POST /api/customers/debts/{debtId}/settle

## Frontend

### Ecrans

- ecran `Sessions`
- ecran `Reglages sessions`

### Navigation et comportements UI

- la colonne de gauche expose uniquement la recherche client et le demarrage ; les blocs abonnes et le panneau de conversion sont retires
- les resultats de recherche affichent les noms sans cartes encadrees, alignes a gauche ; le type de client et son credit eventuel sont places a droite du nom sur la meme ligne, sans centrage du contenu ; un nom tronque reste consultable dans son infobulle
- la colonne de droite affiche le tableau operationnel `Sessions en cours`, sans second tableau des sessions du jour
- la recherche client reste dans une colonne de gauche compacte de 180 a 220 px, a cote de `Sessions en cours` ; chaque suggestion utilise un espacement vertical reduit pour le nom et les informations du client, et la liste est limitee a 8 rem de hauteur avec scroll interne ; la disposition passe sur une colonne sur petit ecran
- les filtres sont visibles par defaut, peuvent etre replies avec `Filtres` et combinent nom du client, date de debut minimale et etat ; leur zone grandit lorsque le choix d'etat est ouvert
- les en-tetes `Client`, `Etat` et `Debut` pilotent le tri du tableau ; un second clic sur la meme colonne inverse son sens
- leurs commandes sont des textes avec de petites fleches, de meme typographie que `Type`, `Credit` et `A payer`, sans habillage de bouton bleu
- le tri initial est `Debut` decroissant ; les nouveaux tris `Client` et `Etat` commencent dans le sens croissant
- le tri d'etat croissant suit le cycle `En cours`, `Pause`, `A payer`, `Terminee` ; les egalites sont departagees par date de debut decroissante puis par nom du client et identifiant de session
- le filtre d'etat propose les quatre etats et `Tous les etats`, avec une recherche immediate insensible a la casse et aux accents et un message si aucun etat ne correspond
- le choix d'un etat ferme la liste et applique le filtre ; `Echap` ferme la liste sans modifier le choix, et la recherche est effacee a la fermeture
- la recherche dans les etats, le tri, les filtres et les en-tetes sont utilisables au clavier ; la direction de tri est exposee avec `aria-sort`
- le tri et les filtres restent actifs apres les rechargements et les actions de session ; le compteur de resultats suit la liste filtree
- les tris et filtres restent en memoire pendant le chargement courant ; le choix de fusion est enregistre en base pour l'utilisateur authentifie puis recharge a chaque ouverture de la page
- les reglages ne sont jamais ecrits dans `localStorage`, `sessionStorage` ou IndexedDB
- chaque ligne de session en cours affiche `Client`, `Type`, `Etat`, `Credit`, `A payer`, `Consomme`, `Debut`
- `En cours`, `Pause`, `A payer` et `Terminee` sont distingues visuellement
- le dernier controle a droite est un petit `+` qui ouvre le detail et devient `-` une fois ouvert ; il expose son etat aux technologies d'assistance
- cliquer sur une zone sans bouton de la ligne ouvre ou ferme le meme detail ; `Entree` et `Espace` permettent aussi cette action au clavier, tandis que les boutons conservent leur action propre sans declencher un second basculement
- plusieurs details de sessions peuvent rester ouverts simultanement ; fermer, trier ou actualiser une ligne ne replie pas les autres
- chaque ligne d'achat ou de dette du detail dispose d'un bouton `-` pour supprimer uniquement cette operation
- les lignes produit et abonnement affichent les quantites reelles enregistrees pour chaque article (par exemple `3 x Produit` ou `2 x Forfait`) ; elles ne sont pas deduites du nombre de ventes ni du montant
- si l'API client en cours d'execution ne fournit pas encore les lignes d'achat, l'ecran charge leurs quantites et prix reels depuis `GET /api/sales/{saleId}`, avec au plus quatre lectures simultanees ; les donnees completes ne sont pas rechargees et un echec reste visible au lieu d'inventer une quantite ou de rendre la fusion silencieusement inactive
- un petit bouton de fusion pres de `Achats de la session et dettes` regroupe uniquement l'affichage des lignes de meme type, de meme composition et de meme prix unitaire ; achats et dettes restent dans des groupes distincts
- l'affichage fusionne additionne les quantites enregistrees de chaque article et les montants en centimes ; il conserve les identifiants des ventes et des dettes d'origine sans les modifier ; seul le choix d'affichage est enregistre en base
- le bouton de fusion enregistre le dernier choix explicite, fusionne ou separe, et l'applique a toutes les sessions de l'utilisateur ; un effacement des caches, une deconnexion ou un redemarrage du serveur ne supprime pas ce choix ; les autres utilisateurs conservent leur propre preference
- pendant le chargement ou l'enregistrement du choix, le bouton est desactive ; un echec est visible et ne change pas le mode courant ; une sauvegarde reussie affiche une confirmation
- le compteur d'un groupe permet de retrouver temporairement les lignes individuelles pour une suppression ou une bascule en dette precise ; il ne change pas la preference enregistree, retrouvee a la prochaine ouverture du detail ; une action sur un groupe ne declenche pas de suppression ou de transformation en masse
- des dettes autonomes de meme libelle peuvent aussi etre regroupees visuellement avec leur solde cumule ; aucune quantite de produit n'est inventee quand aucune ligne de vente n'est rattachee
- le detail presente les achats et dettes, puis le commentaire client ; le resume abonnement est retire
- le commentaire est initialement affiche dans une zone d'une ligne, redimensionnable verticalement et partagee entre les sessions du meme client ; il est enregistre automatiquement a la perte du focus uniquement si sa valeur normalisee a change depuis le chargement ou le dernier enregistrement reussi, sans bouton `Enregistrer`
- une confirmation d'enregistrement ou une erreur reste visible ; en cas d'echec, le texte saisi est conserve et une nouvelle sortie du champ retente l'enregistrement ; une requete deja lancee termine meme si le detail est replie, sans afficher sa reponse sur un autre client
- les actions `Vendre un produit` et `Vendre un abonnement` sont directement sur la ligne de session, avant le `+`, sous forme de boutons icones panier et carte d'abonnement, avec libelles accessibles et infobulles ; elles sont utilisables sans ouvrir le detail et ne le font pas basculer
- ces actions de vente utilisent un fond neutre et un texte sombre ; le rouge reste reserve aux dettes et erreurs
- aucune action `Convertir` n'est affichee sur les lignes
- les lignes d'achats du jour sont compactes, alignees verticalement sur la date et le prix
- la vente d'un produit depuis une session se fait dans une modale
- la vente d'un abonnement depuis une session se fait dans une modale
- l'arret d'une session ouvre une modale de validation du paiement avec detail `Connexion`, `Achats`, `Total du jour`, `Dettes ouvertes`
- la modale de paiement peut ajouter un ou plusieurs abonnements et recalculer immediatement le montant `A payer`
- le bouton `Fin de journee` cloture toutes les sessions encore visibles dans `Sessions en cours` et les retire ensuite de cette liste
- le nom du client dans la liste ouvre une modale de fiche client complete, alignee sur l'ecran `Clients`
- dans cette modale client, les dettes restent rouges comme dans la fiche client classique
- cette modale client affiche les `10` derniers achats puis permet de charger `10` achats de plus avec `+` et de replier avec `-`
- l'ecran `Sessions` n'affiche plus de bouton `Configurer l'affichage`
- la liste `Sessions en cours` est affichee en continu, sans pagination visible, et exploite toute la hauteur disponible avant scroll interne

## Criteres d'acceptation

- l'utilisateur peut piloter une session complete sans quitter l'ecran
- les choix de tri et de filtre restent dans l'etat ephemere de la page sans ecriture dans `localStorage`, `sessionStorage` ou IndexedDB
- l'etat d'une session passe visuellement de `En cours` a `Pause`, `A payer` ou `Terminee` selon le cas
- le tri par client respecte l'ordre alphabetique francais, le tri par debut respecte la chronologie et le tri par etat respecte l'ordre fonctionnel documente, chacun dans les deux sens
- les filtres de nom, date et etat se cumulent ; si aucune session ne correspond, un message explicite remplace les lignes
- les actions de pause, reprise, paiement et detail restent disponibles et liees a la bonne session apres un tri ou un filtrage
- la recherche client reutilise la fiche selectionnee quel que soit son type et refuse un doublon de session ouverte
- le commentaire est persistant en base, facultatif et limite a 2000 caracteres, sans donnees prechargees
- la modale d'arret affiche distinctement le paiement du jour et les dettes deja ouvertes
- les dettes et achats du jour restent lisibles, compacts et coherents apres ventes, pauses et arrets
- deux ventes identiques ajoutees successivement restent distinctes apres suppression ou bascule en dette de l'une d'elles
- apres activation de la fusion, une nouvelle connexion du meme utilisateur retrouve les achats et dettes fusionnes, sans dependance aux caches du navigateur ; une desactivation explicite est aussi conservee

## Persistance des liens vente / dette

La migration externe `V7__link_debts_to_distinct_sales.sql` ajoute `cm_customer_debts.sale_id` et ses index. Une seule dette ouverte peut etre associee a une vente. Les anciennes dettes ouvertes qui correspondent aux anciens libelles sont associees une a une aux ventes par ordre chronologique, avec l'identifiant comme critere de departage ; aucune vente ni dette n'est fusionnee ou supprimee. Une dette autonome conserve un lien absent. Aucun rapprochement par libelle ou montant n'est effectue pendant les actions applicatives.

La migration conserve les lignes et les montants existants et n'ajoute aucune donnee metier par defaut. En mode `flyway`, appliquer V7 avant de charger la nouvelle API. En mode `hibernate`, le champ est genere avec le schema de developpement.

## Persistance du choix de fusion

La migration externe `V8__create_session_display_preferences.sql` cree `cybermanager.cm_session_display_preferences`, avec l'adresse courriel normalisee de l'utilisateur comme cle primaire et `group_entries` comme choix explicite. Aucune preference n'est prechargee. L'absence de ligne reste exposee par un statut 204 et correspond a l'affichage initial separe. La cle primaire indexe la lecture par utilisateur ; aucun lien de schema avec le contexte Auth n'est necessaire.

En mode `flyway`, la nouvelle API applique V8 puis valide le schema. Le mode `hibernate` recree les tables de developpement : sa suppression de schema efface aussi les preferences, comme les autres donnees ; utiliser `flyway` pour conserver les donnees entre redemarrages.
