package com.example.cqrs.domain.api.rental;

import java.util.UUID;

public record LoanRemovedFromReaderEvent(
        UUID loandId,
        UUID readerId) {
}
