package com.example.catalogservice.business.dc;

import com.example.catalogservice.business.dc.dao.ICatalogRepositoryDAO;
import com.example.catalogservice.business.dc.dao.model.CatalogEntity;
import com.example.catalogservice.transfer.dto.CatalogDto;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
@Slf4j
@Component
public class CatalogServiceDC {
	ICatalogRepositoryDAO catalogRepository;

	@Autowired
	public CatalogServiceDC(ICatalogRepositoryDAO catalogRepository) {
		this.catalogRepository = catalogRepository;
	}

	public Iterable<CatalogEntity> getAllCatalogs() {
		return catalogRepository.findAll();
	}

	public CatalogEntity updateCatalog(CatalogDto catalogDto) throws Exception {
		 ModelMapper mapper = new ModelMapper();

		// 생산품의 ID를 확인한다.
		CatalogEntity entity = catalogRepository.findByProductId((String) catalogDto.getProductId());
		if (entity != null) {
			// 수량을 감소시킨다.
			entity.setStock(entity.getStock() - catalogDto.getQty());

			// 데이타베이스에 저장한다.
			catalogRepository.save(entity);
		} else {
			log.error("CatalogEntity 상품이 존재하지 않습니다.");
			throw new Exception("CatalogEntity 상품이 존재하지 않습니다.");
		}

		CatalogEntity catalogEntity = catalogRepository.findByProductId(catalogDto.getProductId());
		log.debug("★★★★★★★★CatalogEntity:" + catalogEntity);
		return catalogEntity;
	}
}
