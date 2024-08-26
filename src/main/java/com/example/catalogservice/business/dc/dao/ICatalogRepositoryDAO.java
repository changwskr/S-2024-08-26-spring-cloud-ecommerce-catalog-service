package com.example.catalogservice.business.dc.dao;

import com.example.catalogservice.business.dc.dao.model.CatalogEntity;
import org.springframework.data.repository.CrudRepository;

public interface ICatalogRepositoryDAO extends CrudRepository<CatalogEntity, Long> {
    CatalogEntity findByProductId(String productId);
}
