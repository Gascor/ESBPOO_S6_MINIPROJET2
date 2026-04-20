package com.leadelmarche.persistence;

import com.leadelmarche.domain.inventory.InventoryItem;
import java.util.List;
import java.util.Optional;

public class InventoryRepository extends AbstractTextRepository<InventoryItem> {
    public InventoryRepository(TextFileDatabase database) {
        super(database, "inventory.txt");
    }

    @Override
    protected InventoryItem fromFields(List<String> fields) {
        InventoryItem item = new InventoryItem();
        // Lecture du bloc commun (id, active, createdAt, updatedAt), puis des colonnes metier.
        int offset = readBaseFields(item, fields);
        item.setProductId(field(fields, offset++));
        item.setStoreId(field(fields, offset++));
        item.setQuantity(parseBigDecimal(field(fields, offset++)));
        item.setReservedQuantity(parseBigDecimal(field(fields, offset)));
        return item;
    }

    @Override
    protected List<String> toFields(InventoryItem entity) {
        // Ecriture dans le meme ordre que fromFields pour garder une serialisation stable.
        List<String> fields = withBaseFields(entity);
        fields.add(entity.getProductId());
        fields.add(entity.getStoreId());
        fields.add(entity.getQuantity().toPlainString());
        fields.add(entity.getReservedQuantity().toPlainString());
        return fields;
    }

    @Override
    protected String nameOf(InventoryItem entity) {
        // Libellé lisible utilisé par la couche repository pour les logs/erreurs.
        return entity.getProductId() + " " + entity.getStoreId();
    }

    public Optional<InventoryItem> findByProductAndStore(String productId, String storeId) {
        // On cherche sur toutes les lignes (actives/inactives), le stock historique peut rester consultable.
        return findAll(false).stream()
            .filter(i -> i.getProductId().equals(productId) && i.getStoreId().equals(storeId))
            .findFirst();
    }
}
