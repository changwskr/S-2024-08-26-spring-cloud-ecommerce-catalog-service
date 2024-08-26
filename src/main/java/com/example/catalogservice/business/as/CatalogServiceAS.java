package com.example.catalogservice.business.as;

import com.example.catalogservice.transfer.dto.CatalogDto;
import com.example.catalogservice.business.dc.dao.model.CatalogEntity;

public interface CatalogServiceAS {
    Iterable<CatalogEntity> getAllCatalogs();
    CatalogEntity updateCatalog(CatalogDto catalogDto) throws Exception;

}
