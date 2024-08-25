package com.fh.scms.services.implement;

import com.fh.scms.dto.customer.CustomerDTO;
import com.fh.scms.pojo.Customer;
import com.fh.scms.pojo.User;
import com.fh.scms.repository.CustomerRepository;
import com.fh.scms.repository.UserRepository;
import com.fh.scms.services.CustomerService;
import com.fh.scms.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@Transactional
public class CustomerServiceImplement implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public CustomerDTO getCustomerResponse(@NotNull Customer customer) {
        return CustomerDTO.builder()
                .firstName(customer.getFirstName())
                .middleName(customer.getMiddleName())
                .lastName(customer.getLastName())
                .address(customer.getAddress())
                .phone(customer.getPhone())
                .gender(customer.getGender())
                .dateOfBirth(customer.getDateOfBirth())
                .build();
    }

    @Override
    public Customer getProfileCustomer(String username) {
        User user = this.userRepository.findByUsername(username);

        return this.customerRepository.findByUser(user);
    }

    @Override
    public CustomerDTO updateProfileCustomer(String username, CustomerDTO customerDTO) {
        User user = this.userRepository.findByUsername(username);

        if (!user.getConfirm()) {
            throw new IllegalArgumentException("Tài khoản chưa được xác nhận");
        }

        Customer customer = this.customerRepository.findByUser(user);

        Field[] fields = CustomerDTO.class.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(customerDTO);

                if (value != null && !value.toString().isEmpty()) {
                    Field supplierField = Customer.class.getDeclaredField(field.getName());
                    supplierField.setAccessible(true);
                    Object convertedValue = Utils.convertValue(supplierField.getType(), value.toString());
                    supplierField.set(customer, convertedValue);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                Logger.getLogger(UserServiceImplement.class.getName()).log(Level.SEVERE, null, e);
            }
        }

        this.customerRepository.update(customer);

        return this.getCustomerResponse(customer);
    }

    @Override
    public Customer findById(Long id) {
        return this.customerRepository.findById(id);
    }

    @Override
    public void save(Customer customer) {
        this.customerRepository.save(customer);
    }

    @Override
    public void update(Customer customer) {
        this.customerRepository.update(customer);
    }

    @Override
    public void delete(Long id) {
        this.customerRepository.delete(id);
    }

    @Override
    public Long count() {
        return this.customerRepository.count();
    }

    @Override
    public List<Customer> findAllWithFilter(Map<String, String> params) {
        return this.customerRepository.findAllWithFilter(params);
    }
}
