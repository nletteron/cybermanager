package com.cybermanager.api.controllers.customer;

import com.cybermanager.api.abstracts.ApiSupport;
import com.cybermanager.api.dtos.customer.CustomerCommentRequest;
import com.cybermanager.api.dtos.customer.CustomerCommentResponse;
import com.cybermanager.application.usecases.customer.GetCustomerCommentUseCase;
import com.cybermanager.application.usecases.customer.UpdateCustomerCommentUseCase;
import com.cybermanager.domain.commands.customer.UpdateCustomerCommentCommand;
import com.cybermanager.domain.queries.customer.GetCustomerCommentQuery;
import com.cybermanager.infrastructure.out.adapters.security.users.JwtAccessTokenReader;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customers/{id}/comment")
public class CustomerCommentController {

    private final GetCustomerCommentUseCase getComment;
    private final UpdateCustomerCommentUseCase updateComment;
    private final JwtAccessTokenReader tokenReader;

    public CustomerCommentController(GetCustomerCommentUseCase getComment, UpdateCustomerCommentUseCase updateComment, JwtAccessTokenReader tokenReader) {
        this.getComment = getComment;
        this.updateComment = updateComment;
        this.tokenReader = tokenReader;
    }

    @GetMapping
    public CustomerCommentResponse get(@RequestHeader("Authorization") String authorization, @PathVariable("id") UUID id) {
        ApiSupport.actor(authorization, tokenReader);
        var view = getComment.execute(new GetCustomerCommentQuery(id));
        return new CustomerCommentResponse(view.customerId(), view.comment());
    }

    @PatchMapping
    public CustomerCommentResponse update(@RequestHeader("Authorization") String authorization, @PathVariable("id") UUID id, @Valid @RequestBody CustomerCommentRequest request) {
        ApiSupport.actor(authorization, tokenReader);
        var view = updateComment.execute(new UpdateCustomerCommentCommand(id, request.comment()));
        return new CustomerCommentResponse(view.customerId(), view.comment());
    }

}
