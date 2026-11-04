# Skill: frontend-feature

## Purpose

Implementer une fonctionnalite frontend Angular CyberManager de maniere maintenable, typee et coherente avec l'application existante.

## When to use

Utiliser cette skill lorsqu'une demande impacte principalement le frontend :
- nouvel ecran Angular
- refonte fonctionnelle d'une feature existante
- ajout de formulaire, filtre, pagination ou panneau lateral
- branchement d'un service API sur une page ou un composant
- evolution de contrat frontend liee a une API REST JSON

Utiliser aussi `frontend-design-system` des qu'il faut construire ou refondre un composant, une page, une liste CRUD, un panneau, une modale, un formulaire ou des styles CSS.

## Required inputs

- `AGENTS.md`
- le fichier de feature dans `documentations/features` s'il existe
- les bounded contexts frontend concernes
- les contrats API consommes ou a consommer
- la strategie de journalisation attendue si la feature ajoute ou modifie des flux critiques
- la skill `frontend-design-system` pour les directives de composants, UX, layout et CSS generiques

## Rules

- respecter l'architecture Angular par feature : `pages`, `components`, `services`, `models`, `routes` si necessaire
- garder les composants simples, fortement types et orientes interaction UI
- placer les appels HTTP dans un service Angular dedie
- isoler les interfaces d'entree et de sortie dans `models`
- ne pas mettre de logique metier lourde dans les composants ou templates
- ne pas stocker cache applicatif, jeton, utilisateur courant, donnee metier ou reglage de grille dans `localStorage`, `sessionStorage` ou IndexedDB
- conserver l'etat ephemere dans des services Angular a base de `signals` ou dans un state local explicite
- ne jamais remplacer une donnee metier absente de l'API par un fallback code en dur

## Steps

1. Lire la feature et identifier :
   - objectif utilisateur
   - donnees affichees
   - actions utilisateur
   - erreurs a rendre visibles
   - comportement attendu des listes, details, formulaires et confirmations
   - points de journalisation utiles pour les appels API, anomalies et traitements complexes

2. Identifier la feature Angular concernee :
   - page principale dans `features/<feature>/pages`
   - sous-composants reutilisables dans `features/<feature>/components`
   - appels HTTP dans `features/<feature>/services`
   - interfaces et types dans `features/<feature>/models`
   - emplacement du logging partage (`core`, interceptor, service dedie)

3. Implementer ou completer les modeles TypeScript :
   - typage strict
   - contrat aligne sur l'API
   - separation claire entre requetes, reponses, vues locales et commandes UI
   - aucun type implicite dans les composants

4. Implementer ou completer les services Angular :
   - un service dedie par domaine d'appels API
   - mapping minimal et explicite entre DTO API et modeles frontend
   - messages d'erreur HTTP lisibles pour l'utilisateur
   - journalisation utile pour les flux sensibles ou difficiles a diagnostiquer
   - logs de requetes sortantes avec methode, URL, contexte fonctionnel et charge utile nettoyee si necessaire
   - logs de reponses entrantes avec statut, duree et resume de resultat si cela aide le diagnostic
   - aucune donnee sensible dans les logs : mot de passe, token, cookie, secret ou donnees personnelles non necessaires

5. Implementer ou completer la page et les composants :
   - appliquer `frontend-design-system` pour la structure, les interactions, le layout, les boutons, les filtres, les panneaux, les modales et les styles
   - preserver explicitement les interactions existantes lors d'une refonte : drag and drop, modales, toggles, panneaux de detail, raccourcis de navigation
   - pour les formulaires reactifs, ne pas baser un `computed` sur `form.getRawValue()` si l'on attend une reaction immediate
   - utiliser `signals`, `valueChanges` ou un state local explicite pour les filtres instantanes
   - faire reagir les filtres operationnels des les premieres lettres quand le flux l'exige
   - normaliser les valeurs avant sauvegarde sur les pages de configuration
   - afficher un retour utilisateur apres enregistrement ou action critique
   - journaliser les traitements UI difficiles : transformation de donnees, synchronisation d'etat, normalisation, calculs derives

6. Connecter le frontend au backend :
   - verifier les routes Angular
   - verifier les guards si l'ecran est reserve a certains roles
   - verifier les noms de champs JSON et les formats de date, montant, statut ou enum
   - afficher un etat vide quand l'API ne retourne aucune configuration ou donnee metier
   - rendre visibles les erreurs bloquantes sans masquer les actions de recuperation utiles

7. Tester ou verifier :
   - typage TypeScript
   - appels API coherents
   - comportements de formulaire et de filtre
   - absence de regression sur les interactions critiques existantes
   - absence de stockage navigateur pour les donnees applicatives interdites
   - logs utiles et sans donnee sensible quand la feature l'exige
   - build Angular si possible

8. Mettre a jour la documentation de feature si le comportement utilisateur livre a evolue.

## Output format

Toujours terminer par :
- modules impactes
- ecrans impactes
- fichiers crees / modifies
- choix d'architecture frontend
- points de vigilance
