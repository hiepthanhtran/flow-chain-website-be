package com.fh.scm.services;

import com.fh.scm.pojo.Customer;

import java.util.List;
import java.util.Map;

public interface CustomerService {

    Customer get(Long id);

    Customer getByPhone(String phone);

    void insert(Customer customer);

    void update(Customer customer);

    void delete(Long id);

    void softDelete(Long id);

    Long count();

    List<Customer> getAll(Map<String, String> params);
}
