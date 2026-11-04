package com.cybermanager.domain.ports.session;

import com.cybermanager.domain.businessobjects.session.CafeSession;
import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.session.SessionId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CafeSessionRepository {

    CafeSession save(CafeSession session);

    Optional<CafeSession> findById(SessionId sessionId);

    List<CafeSession> findByDay(LocalDate date);

    List<CafeSession> findAll();

    List<CafeSession> findActive();

    List<CafeSession> findByCustomerIds(List<CustomerId> customerIds);

    void deleteByCustomerIds(List<CustomerId> customerIds);

}

