package com.spin.transaction.infrastructure.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import com.spin.transaction.application.port.out.TransactionRepositoryPort;
import com.spin.transaction.domain.model.Transaction;
import com.spin.transaction.infrastructure.persistence.entity.TransactionEntity;
import com.spin.transaction.infrastructure.persistence.repository.SpringDataTransactionRepository;
import com.spin.transaction.mapper.TransactionMapper;

/**
 * Persistence adapter that implements the transaction repository output port
 * using Spring Data JPA.
 *
 * This class isolates the application layer from JPA-specific components.
 */
@Component
public class TransactionPersistenceAdapter implements TransactionRepositoryPort {

	private final SpringDataTransactionRepository repository;
	private final TransactionMapper mapper;

	/**
	 * Creates the persistence adapter with its required dependencies.
	 *
	 * Constructor injection makes dependencies explicit and facilitates testing.
	 *
	 * @param repository Spring Data JPA repository
	 * @param mapper     transaction domain/entity mapper
	 */
	public TransactionPersistenceAdapter(SpringDataTransactionRepository repository, TransactionMapper mapper) {

		this.repository = repository;
		this.mapper = mapper;
	}

	/**
	 * Persists a domain transaction through Spring Data JPA.
	 *
	 * @param transaction domain transaction
	 * @return persisted transaction converted back to the domain model
	 */
	@Override
	public Transaction save(Transaction transaction) {

		TransactionEntity entity = mapper.toEntity(transaction);
		TransactionEntity savedEntity = repository.save(entity);

		return mapper.toDomain(savedEntity);
	}

	/**
	 * Retrieves all persisted transactions ordered from newest to oldest.
	 *
	 * @return domain transactions
	 */
	@Override
	public List<Transaction> findAll() {
		return repository.findAllByOrderByCreatedAtDesc().stream().map(mapper::toDomain).toList();
	}

}