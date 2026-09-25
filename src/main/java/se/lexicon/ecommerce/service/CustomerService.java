package se.lexicon.ecommerce.service;

import se.lexicon.ecommerce.dto.CustomerRequest;
import se.lexicon.ecommerce.dto.CustomerResponse;

public interface CustomerService {

    CustomerResponse register(CustomerRequest request);

    CustomerResponse findById(Long id);

    CustomerResponse update(Long id, CustomerRequest request);
}
