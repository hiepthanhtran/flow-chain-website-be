package com.fh.scm.services.implement;

import com.fh.scm.pojo.Customer;
import com.fh.scm.repository.CustomerRepository;
import com.fh.scm.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class CustomerServiceImplement implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public Customer get(UUID id) {
        return this.customerRepository.get(id);
    }

    @Override
    public void insert(Customer customer) {
        this.customerRepository.insert(customer);
    }

    @Override
    public void update(Customer customer) {
        this.customerRepository.update(customer);
    }

    @Override
    public void delete(UUID id) {
        this.customerRepository.delete(id);
    }

    @Override
    public void softDelete(UUID id) {
        this.customerRepository.softDelete(id);
    }

    @Override
    public void insertOrUpdate(Customer customer) {
        this.customerRepository.insertOrUpdate(customer);
    }

    @Override
    public Long count() {
        return this.customerRepository.count();
    }

    @Override
    public Boolean exists(UUID id) {
        return this.customerRepository.exists(id);
    }

    @Override
    public List<Customer> getAll(Map<String, String> params) {
        return this.customerRepository.getAll(params);
    }
}
