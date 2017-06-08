package com.commercetools.sync.products.actions;

import com.commercetools.sync.products.ProductSyncOptions;
import io.sphere.sdk.categories.Category;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.models.Reference;
import io.sphere.sdk.products.Product;
import io.sphere.sdk.products.ProductData;
import io.sphere.sdk.products.ProductDraft;
import io.sphere.sdk.products.commands.updateactions.AddToCategory;
import io.sphere.sdk.products.commands.updateactions.RemoveFromCategory;
import io.sphere.sdk.products.commands.updateactions.SetCategoryOrderHint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.commercetools.sync.products.actions.ActionUtils.actionsOnProductData;
import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;

final class Categories {
    private Categories() {
    }

    static List<UpdateAction<Product>> mapCategories(final Product product, final ProductDraft draft,
                                                     final ProductSyncOptions syncOptions) {
        return actionsOnProductData(product, syncOptions,
            ProductData::getCategories, draft.getCategories(), (oldCategories, newCategories) -> {

                List<UpdateAction<Product>> updateActions = new ArrayList<>();

                subtract(newCategories, oldCategories).forEach(c ->
                    updateActions.add(AddToCategory.of(c, syncOptions.isUpdateStaged())));

                subtract(oldCategories, newCategories).forEach(c ->
                    updateActions.add(RemoveFromCategory.of(c, syncOptions.isUpdateStaged())));

                return updateActions;
            });
    }

    static List<UpdateAction<Product>> setCategoryOrderHints(final Product product, final ProductDraft draft,
                                                             final ProductSyncOptions syncOptions) {
        return actionsOnProductData(product, syncOptions,
            ProductData::getCategoryOrderHints, draft.getCategoryOrderHints(), (oldHints, newHints) -> {

                List<UpdateAction<Product>> updateActions = new ArrayList<>();

                Map<String, String> newMap = nonNull(newHints) ? newHints.getAsMap() : emptyMap();
                Map<String, String> oldMap = nonNull(oldHints) ? oldHints.getAsMap() : emptyMap();

                // remove category hints present in old product if they are absent in draft
                oldMap.forEach((key, value) -> {
                    if (!newMap.containsKey(key)) {
                        updateActions.add(
                            SetCategoryOrderHint.of(key, null, syncOptions.isUpdateStaged()));
                    }
                });

                // add category hints present in draft if they are absent or changed in old product
                newMap.forEach((key, value) -> {
                    if (!oldMap.containsKey(key) || !Objects.equals(oldMap.get(key), value)) {
                        updateActions.add(SetCategoryOrderHint.of(key, value, syncOptions.isUpdateStaged()));
                    }
                });

                return updateActions;
            });
    }

    private static Set<Reference<Category>> subtract(final Set<Reference<Category>> set1,
                                                     final Set<Reference<Category>> set2) {
        Set<Reference<Category>> difference = new HashSet<>(set1);
        difference.removeAll(set2);
        return difference;
    }
}