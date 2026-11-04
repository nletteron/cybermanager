# F-025 - Gestion des catégories produit

## Objectif

Permettre à un administrateur de créer et consulter les catégories utilisées par le catalogue produit.

## Règles fonctionnelles

- Une catégorie possède un identifiant UUID généré, un nom unique, une description et un taux de TVA obligatoire compris entre 0 et 100.
- Aucune catégorie ni valeur de TVA n'est créée automatiquement au démarrage ou par migration.
- L'écran `Gestion Produit > Catégories` propose la liste, un filtre et un panneau de création fermé par défaut.
- La création et la modification d'un produit exigent la sélection d'une catégorie existante ; aucune saisie libre de catégorie n'est acceptée.
- Les anciennes lignes produit sans relation de catégorie restent lisibles pendant la transition de schéma, mais doivent recevoir une catégorie avant modification.

## API

- `GET /api/categories?term=` : recherche des catégories.
- `POST /api/categories` : création réservée à un administrateur.
- Les requêtes de création et modification produit utilisent `categoryId`.

## Persistance

- Table `cybermanager.cm_product_categories`.
- Colonne relationnelle `cybermanager.cm_products.category_id` et clé étrangère vers la catégorie.
- Migration Flyway `V4__create_product_categories.sql`, sans donnée initiale.
