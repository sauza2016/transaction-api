package com.spin.transaction.application.port.out;

import com.spin.transaction.domain.model.ProviderTransactionResult;
import com.spin.transaction.domain.model.Transaction;

public interface TransactionProviderPort {

    ProviderTransactionResult process(Transaction transaction);
}