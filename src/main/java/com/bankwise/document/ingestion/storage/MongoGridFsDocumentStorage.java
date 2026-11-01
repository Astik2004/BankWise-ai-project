package com.bankwise.document.ingestion.storage;

import com.bankwise.common.exception.BusinessException;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class MongoGridFsDocumentStorage implements DocumentStorage {

    private final GridFsTemplate gridFsTemplate;
    private final GridFsOperations gridFsOperations;

    @Override
    public String store(MultipartFile file, String storageKey) {
        try (InputStream inputStream = file.getInputStream()) {
            // Remove any existing file with same storageKey to prevent orphaned duplicates
            gridFsTemplate.delete(Query.query(Criteria.where("filename").is(storageKey)));
            gridFsTemplate.store(inputStream, storageKey, file.getContentType());
            log.info("Stored document in MongoDB GridFS with storageKey: {}", storageKey);
            return storageKey;
        } catch (IOException e) {
            log.error("Failed to store document in MongoDB GridFS: {}", e.getMessage(), e);
            throw new BusinessException("Failed to store document in cloud database");
        }
    }

    @Override
    public InputStream read(String storageKey) {
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("filename").is(storageKey)));
        if (gridFSFile == null) {
            throw new BusinessException("Document not found in storage: " + storageKey);
        }
        try {
            GridFsResource resource = gridFsOperations.getResource(gridFSFile);
            return resource.getInputStream();
        } catch (IOException e) {
            log.error("Failed to read document from MongoDB GridFS: {}", e.getMessage(), e);
            throw new BusinessException("Failed to read document from cloud database");
        }
    }

    @Override
    public void delete(String storageKey) {
        gridFsTemplate.delete(Query.query(Criteria.where("filename").is(storageKey)));
        log.info("Deleted document from MongoDB GridFS with storageKey: {}", storageKey);
    }

    @Override
    public boolean exists(String storageKey) {
        return gridFsTemplate.findOne(Query.query(Criteria.where("filename").is(storageKey))) != null;
    }
}

