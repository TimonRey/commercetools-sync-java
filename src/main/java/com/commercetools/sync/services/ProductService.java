package com.commercetools.sync.services;

import com.commercetools.sync.services.impl.ProductServiceImpl;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.products.Product;
import io.sphere.sdk.products.ProductDraft;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface ProductService {
    static ProductService of() {
        return new ProductServiceImpl();
    }

    CompletionStage<Optional<Product>> fetch(String productKey);

    CompletionStage<Void> create(ProductDraft productDraft);

    CompletionStage<Void> update(Product product, List<UpdateAction<Product>> updateActions);
}