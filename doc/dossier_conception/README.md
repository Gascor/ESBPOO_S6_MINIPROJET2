# Dossier de conception - LeadelMarche

## 1) Objectif
Concevoir une application Java orientee objet, user friendly, qui remplace les process papier du magasin LeadelMarche avec une architecture evolutive et maintenable.

Note, A NE PAS OUBLIER (voir partie 8 svp)

Le fichier [uml.txt](/c:/Users/lucas/Desktop/ISTY/Projet%20ISTY/ESBPOO_S6_MINIPROJET2/doc/dossier_conception/uml.txt) contient les diagrammes PlantUML complets:
- diagramme de classes global (metier + services + persistance + MVC)
- diagramme de communication du process de vente (incluant caisse rapide)

Pour les renderers qui n'acceptent pas plusieurs blocs `@startuml` dans une meme source string, utiliser les fichiers separes:
- [class_diagram.puml](class_diagram.puml)
- [communication_diagram.puml](communication_diagram.puml)

## 2) Perimetre fonctionnel
Les modules couverts sont:
- stock/inventaire
- points de vente (plusieurs caisses / plusieurs points de vente)
- management du personnel
- management des clients
- promotions
- statistiques
- base de donnees fichier texte

## 3) Choix d architecture
Architecture que j'ai retenue:
- `MVC` pour l interface graphique (Views, Controllers, Services metier)
- `Service + Repository` pour separer logique metier et acces donnees
- `TextFileDatabase` pour la persistance (conforme contrainte fichier texte :p ))
- `ApplicationContext` singleton pour injecter les instances uniques (inventaire, RH, BD)

Regles de modelisation:
- toute entite metier herite de `BaseEntity` avec `active` (delete logique)
- recherche partielle insensible aux accents et a la casse via `SearchNormalizer`
- gestion explicite des enums metier (`ProductType`, `ContractType`, `PaymentMode`, etc.)

## 4) Couverture des exigences principales
- CRUD complet sur produits, personnel, clients, promotions, ventes
- point de vente: vente, panier modifiable, changement mode paiement, ticket
- client anonyme force par `CustomerService.findOrAnonymous(...)`
- reduction du stock apres vente via `SalesService -> InventoryService`
- ticket texte imprimable ou envoyable par mail via `ReceiptService`
- recherche par nom/prenom/id avec matching partiel
- conservation historique grace a `active=false` (pas de suppression physique)

## 5) Couverture des ameliorations (5 a 11)
5. Codes-barres:
- `Product.barcode`
- `SalesService.addLineByBarcode(...)`

6. Alimentaire a la piece sans code-barres:
- `Product.soldByPieceWithoutBarcode`
- `SalesService.addWeightedLine(...)` + quantite/piece

7. Caisses rapides:
- `FastCheckoutService.validateScannedWeight(...)`
- `FastCheckoutService.requestCashierHelp(...)`
- process inclus dans le diagramme de communication

8. RH et planning optimise:
- `WorkContract`, `LeaveRequest`, `Shift`
- `StaffService.optimizeSchedule(...)`
- `StaffService.forecastUnderstaffing(...)` pour alerte anticipee

9. Stock avance:
- seuil alerte (`Product.lowStockThresholdPercent`)
- `InventoryService.detectLowStock()`
- stock inter-magasins via `getStockAcrossStores(...)`

10. Promotions:
- abstraction `Promotion`
- strategies type "X achetes Y offerts" et "pourcentage sur Neme"
- effet immediate ou cagnotte (`PromotionEffect`)

11. Statistiques:
- `StatisticProvider` extensible (ajout de stats personnalisables)
- comparaison de periodes
- sortie tableau/graphe + envoi mail

## 6) Scalabilite et maintenabilite
Points cles:
- extension par nouvelles strategies de promotion sans casser les services existants
- extension par nouveaux providers de statistiques via `registerCustom(...)`
- isolation de la persistance par repository pour permettre evolution future vers SGBD
- gestion multi-points de vente deja prete (`Store`, `POS`, stock par magasin)

## 7) Donnees initiales
`SeedDataService.loadDefaultData()` est prevu pour charger automatiquement un jeu de test:
- minimum 10 enregistrements sur produits, clients, personnel
- au moins 1 client anonyme
- promotions actives de demonstration

## 8) Guide de rendu PlantUML
Pour generer les images des diagrammes:
1. Ouvrir [uml.txt](/c:/Users/lucas/Desktop/ISTY/Projet%20ISTY/ESBPOO_S6_MINIPROJET2/doc/dossier_conception/uml.txt)
2. Coller le contenu dans un rendu PlantUML (plugin IDE ou serveur local)
3. Exporter en PNG/SVG pour le rapport final

Conseil export:
- pour eviter les images coupees, preferer l'export `SVG` et recoller les different png par morceau en version screenshot zoomé (pas pratique mais a force je devais en arriver là)