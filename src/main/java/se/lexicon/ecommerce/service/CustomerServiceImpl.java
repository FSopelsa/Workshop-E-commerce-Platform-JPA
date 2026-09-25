package se.lexicon.ecommerce.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.lexicon.ecommerce.domain.Customer;
import se.lexicon.ecommerce.dto.CustomerRequest;
import se.lexicon.ecommerce.dto.CustomerResponse;
import se.lexicon.ecommerce.exception.DuplicateResourceException;
import se.lexicon.ecommerce.exception.ResourceNotFoundException;
import se.lexicon.ecommerce.mapper.CustomerMapper;
import se.lexicon.ecommerce.repository.CustomerRepository;

import java.util.Objects;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerServiceImpl(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = Objects.requireNonNull(customerRepository, "customerRepository must not be null");
        this.customerMapper = Objects.requireNonNull(customerMapper, "customerMapper must not be null");
    }

    @Override
    @Transactional
    public CustomerResponse register(CustomerRequest request) {
        Objects.requireNonNull(request, "customer request must not be null");
        if (customerRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("customer email is already registered: " + request.email());
        }

        Customer customer = customerMapper.toEntity(request);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        return customerMapper.toResponse(findRequired(id));
    }

    @Override
    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Objects.requireNonNull(request, "customer request must not be null");
        Customer customer = findRequired(id);

        customerRepository.findByEmail(request.email())
                .filter(existing -> !isSameCustomer(existing, customer))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("customer email is already registered: " + request.email());
                });

        customer.updateDetails(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.street(),
                request.city(),
                request.zipCode()
        );
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    private Customer findRequired(Long id) {
        Objects.requireNonNull(id, "customer id must not be null");
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("customer not found: " + id));
    }

    private boolean isSameCustomer(Customer candidate, Customer target) {
        if (candidate == target) {
            return true;
        }
        return candidate.getId() != null && candidate.getId().equals(target.getId());
    }
}
