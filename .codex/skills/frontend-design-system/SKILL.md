# Skill: frontend-design-system

## Purpose

Construire ou refondre des composants Angular CyberManager avec une structure UI coherente, des interactions previsibles et des CSS generiques reutilisables.

## When to use

Utiliser cette skill lorsqu'une demande frontend implique :
- creation ou refonte d'un composant Angular
- page de gestion, CRUD, liste, tableau, formulaire, panneau lateral ou modale
- harmonisation visuelle, responsive ou ergonomique
- extraction de styles inline vers `*.component.css`
- ajout ou modification de CSS generique dans `frontend/src/styles.css`

## Required inputs

- `AGENTS.md`
- la page ou le composant Angular concerne
- les styles globaux `frontend/src/styles.css`
- les composants voisins de la meme feature
- la contrainte de budget Angular `anyComponentStyle` si elle est configuree

## Component Construction Rules

- placer la page principale dans `features/<feature>/pages`
- placer les sous-composants reutilisables dans `features/<feature>/components`
- garder les appels HTTP dans `features/<feature>/services`
- garder les interfaces et types dans `features/<feature>/models`
- utiliser des composants simples, fortement types et faciles a scanner
- eviter la logique metier lourde dans les composants et templates
- privilegier `signals`, `valueChanges` ou un state local explicite pour les interactions immediates
- ne pas attendre de reactivite d'un `computed` base directement sur `form.getRawValue()`
- exposer les etats utilisateur utiles : chargement, erreur, vide, succes, action impossible
- preserver les interactions existantes lors d'une refonte : drag and drop, modales, toggles, panneaux, raccourcis, selection
- normaliser les valeurs avant sauvegarde sur toute page de configuration
- afficher une confirmation visible apres enregistrement ou action critique
- si une modale simule un calcul, reprendre la logique backend de reference ou indiquer clairement que le resultat est indicatif

## UI Structure Rules

### Pages

- utiliser une structure `page` avec un entete court, un groupe d'actions et le contenu principal
- eviter les textes d'aide visibles qui expliquent les fonctionnalites evidentes
- garder les ecrans de gestion denses, calmes et faciles a scanner
- ne pas creer de landing page pour une fonctionnalite applicative
- pour les ecrans hauts, preferer `dvh`, `clamp` et les hauteurs derivees du conteneur disponible
- si l'application est rendue dans un shell borne, confiner le scroll dans les panneaux internes
- eviter le debordement vertical global du document

### CRUD Lists

- placer le bouton `Filtres` avec icone dans le bandeau d'actions sous le titre de page
- placer l'action principale a cote du bouton `Filtres` avec le libelle `Creer un nouvel ...` ou `Creer une nouvelle ...`
- masquer les filtres par defaut sauf justification fonctionnelle forte
- afficher les filtres dans un bloc `filters-grid collapsible`
- ouvrir et fermer les filtres avec une animation rapide et discrete
- garder les champs de filtre compacts, alignes a gauche et calibres selon leur contenu
- ne pas etirer un filtre en pleine largeur si son contenu attendu est court
- ne pas dupliquer un bouton `Nouveau` dans le panneau de droite si l'action de creation existe deja en haut
- utiliser une liste a gauche et un panneau creation / edition a droite quand le flux s'y prete
- fermer par defaut un panneau secondaire qui sert seulement a de l'ajout ponctuel

### Results And Pagination

- utiliser un cadre de resultats avec compteur visible
- si une pagination existe, afficher `Precedent` / `Suivant` et `Page X / Y`
- placer `Precedent` / `Suivant` dans le cadre de la liste de resultats, a cote du compteur
- ne jamais placer la pagination dans le bandeau d'actions global quand elle pilote le tableau `Resultats`
- placer les filtres de taille de page dans `filters-grid`, pas dans le bandeau d'actions
- activer la taille de page configurable quand le volume peut croitre

### Forms

- grouper les champs dans une grille compacte
- aligner labels et controles pour une saisie rapide
- afficher les erreurs pres du champ ou dans un bloc erreur proche de l'action
- desactiver les actions impossibles pendant chargement ou validation
- ne pas masquer l'action de sauvegarde dans une zone secondaire difficile a trouver
- ne pas melanger formulaire de creation et detail readonly sans separation visuelle claire

### Buttons And Icons

- utiliser des boutons texte pour les actions claires et des boutons icone pour les outils repetitifs
- si une bibliotheque d'icones existe deja, l'utiliser au lieu de dessiner des SVG manuels
- ne pas ajouter une dependance d'icones uniquement pour un bouton isole
- nommer les boutons d'action avec des verbes : `Creer`, `Enregistrer`, `Restaurer`, `Exporter`
- garder les variantes stables : primaire, secondaire, ghost, danger
- ne pas utiliser deux styles primaires concurrents sur le meme groupe d'actions

### Panels And Modals

- utiliser les panneaux pour edition, detail ou ajout lie a la liste courante
- garder les panneaux secondaires fermes par defaut quand ils ajoutent de la charge visuelle
- utiliser une modale pour confirmation, calcul ponctuel ou action bloquante
- rendre la fermeture explicite et garder l'action principale visible
- eviter les modales pour des formulaires longs si un panneau ou une page dediee convient mieux

### Tables And Dense Lists

- utiliser un conteneur scrollable dedie (`table-shell`) pour les tables larges
- garder les cellules compactes et lisibles
- aligner montants, dates et actions de facon stable
- utiliser des badges (`inline-state`, `type-chip`) pour les statuts courts
- prevoir un etat vide sans inventer de donnee metier

## CSS Organization Rules

- verifier `frontend/src/styles.css` avant d'ajouter une classe generique
- mettre dans `styles.css` seulement les styles reutilisables par plusieurs features
- mettre les styles propres a une page dans son `*.component.css`
- sortir les gros blocs `styles:` inline vers un fichier CSS dedie
- ne pas laisser un composant depasser le budget Angular `anyComponentStyle`
- si un CSS de composant grossit, deplacer les styles generiques vers `styles.css` ou decouper le composant
- garder les selectors simples et proches des classes du template
- ne pas imbriquer des cartes dans des cartes
- ne pas creer de decorations abstraites inutiles : orbes, blobs, fonds purement decoratifs
- eviter les palettes dominees par une seule famille de couleur
- verifier mobile et desktop avant de livrer une refonte visuelle

## Generic CSS Patterns

Avant de coller un bloc, verifier s'il existe deja dans `frontend/src/styles.css`. Ajouter ou completer uniquement ce qui manque.

### Layout Tokens

```css
:root {
  --cm-app-height: 100dvh;
  --cm-page-padding: clamp(0.75rem, 1.2dvh, 2rem);
  --cm-panel-padding: clamp(0.75rem, 1dvh, 1rem);
  --cm-compact-gap: clamp(0.45rem, 0.8dvh, 0.75rem);
  --cm-fixed-shell-height: clamp(20rem, calc(100dvh - 14rem), 48rem);
  --cm-btn-radius: 999px;
}

html,
body {
  margin: 0;
  min-height: 100dvh;
  height: 100dvh;
  overflow: hidden;
}

* {
  box-sizing: border-box;
}
```

### Page And Sections

```css
.page {
  display: grid;
  gap: var(--cm-compact-gap);
  min-height: 0;
}

.hero,
.section-head {
  display: flex;
  justify-content: space-between;
  gap: var(--cm-compact-gap);
  align-items: flex-start;
  flex-wrap: wrap;
}

.hero-actions,
.section-tools,
.actions,
.pager {
  display: flex;
  align-items: center;
  gap: 0.62rem;
  flex-wrap: wrap;
}

.section-tools {
  margin-left: auto;
}

.panel {
  padding: var(--cm-panel-padding);
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 0.85rem;
}
```

### Filters Grid

```css
.filters-grid {
  display: grid;
  gap: 0.58rem;
  align-items: start;
  justify-content: start;
  width: fit-content;
  max-width: 100%;
  margin: 0.3rem 0 0.7rem;
  padding: 0.72rem 0.82rem;
  border: 1px solid #e2e8f0;
  border-radius: 1rem;
  background: #f8fafc;
  transform-origin: top;
  transition:
    opacity 180ms ease,
    transform 200ms ease,
    max-height 220ms ease,
    margin 180ms ease,
    padding 180ms ease,
    border-color 180ms ease;
}

.filters-grid.collapsible {
  overflow: hidden;
  max-height: 14rem;
  opacity: 1;
  transform: translateY(0);
}

.filters-grid.collapsible.is-collapsed {
  max-height: 0;
  opacity: 0;
  transform: translateY(-6px);
  margin-top: 0;
  margin-bottom: 0;
  padding-top: 0;
  padding-bottom: 0;
  border-color: transparent;
  pointer-events: none;
}

.filters-grid .field input,
.filters-grid .field select {
  width: 100%;
}
```

### Fields And Tables

```css
.field {
  display: grid;
  gap: 0.28rem;
  align-content: start;
}

.field span {
  font-size: 0.78rem;
  font-weight: 700;
  color: #334155;
  line-height: 1.1;
}

.table-shell {
  overflow: auto;
  border: 1px solid #e2e8f0;
  border-radius: 0.85rem;
  background: #fff;
}

.fixed-shell {
  height: var(--cm-fixed-shell-height);
  min-height: min(18rem, var(--cm-fixed-shell-height));
  max-height: var(--cm-fixed-shell-height);
}

.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.88rem;
}

.table th,
.table td {
  padding: 0.42rem 0.58rem;
  border-bottom: 1px solid #e2e8f0;
  line-height: 1.15;
  text-align: left;
}

.action-cell,
.actions-head {
  text-align: right;
  white-space: nowrap;
}
```

### Buttons And Status

```css
button,
.link-button {
  appearance: none;
  border: 0;
  border-radius: var(--cm-btn-radius);
  padding: 0.6rem 0.92rem;
  font: inherit;
  font-weight: 700;
  line-height: 1.1;
  cursor: pointer;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.45rem;
}

button:disabled,
.link-button:disabled {
  opacity: 0.48;
  cursor: default;
}

.inline-state,
.type-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.12rem 0.48rem;
  border-radius: 999px;
  font-size: 0.68rem;
  font-weight: 800;
  line-height: 1.1;
  border: 1px solid transparent;
  white-space: nowrap;
}
```

### Modals

```css
.modal-backdrop {
  position: fixed;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 1rem;
  background: rgba(15, 23, 42, 0.46);
  z-index: 1000;
}

.confirm-modal {
  width: min(32rem, 100%);
  padding: var(--cm-panel-padding);
  background: #ffffff;
  border-radius: 1rem;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.26);
}
```

### Responsive Adjustments

```css
@media (max-width: 1100px) {
  .filters-grid {
    width: 100%;
  }

  .hero-actions,
  .section-tools {
    width: 100%;
  }
}
```

## Verification Checklist

- composant dans le bon dossier Angular
- modele TypeScript explicite
- etats vide, chargement, erreur et succes visibles si necessaires
- filtres CRUD masques par defaut avec bouton `Filtres`
- action principale de creation visible en haut
- pagination dans le cadre `Resultats`
- panneau secondaire ferme par defaut si seulement ponctuel
- CSS generique dans `styles.css` uniquement si reutilisable
- CSS specifique dans `*.component.css`
- aucun debordement vertical global
- aucun texte ou bouton qui deborde sur mobile
- aucun warning de budget Angular sur les styles de composant
