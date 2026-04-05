package com.hospedaje.notification.repository;

import com.hospedaje.notification.document.InvoiceSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface InvoiceSnapshotRepository extends MongoRepository<InvoiceSnapshot, String> {
    Optional<InvoiceSnapshot> findByOrderId(String orderId);
}
