package com.amoibeojt.api.filter;

import java.io.IOException;
import java.util.UUID;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TransactionIdFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionIdFilter.class);
    private static final String HEADER_TRANSACTION_ID = "Transaction-Id";

    @PostConstruct
    public void init() {
        LOGGER.info("✅ TransactionIdFilter initialized and registered in Spring context");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String transactionId = request.getHeader(HEADER_TRANSACTION_ID);

        if (transactionId == null || transactionId.isBlank() || !isValidUuidV4(transactionId)) {
            transactionId = UUID.randomUUID().toString();
            LOGGER.debug("Transaction-Id ヘッダーが不正または未指定のため新規生成: {}", transactionId);
        }

        response.setHeader(HEADER_TRANSACTION_ID, transactionId);
        MDC.put(HEADER_TRANSACTION_ID, transactionId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(HEADER_TRANSACTION_ID);
        }
    }

    private boolean isValidUuidV4(String uuidStr) {
        try {
            UUID uuid = UUID.fromString(uuidStr);
            return uuid.version() == 4;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}