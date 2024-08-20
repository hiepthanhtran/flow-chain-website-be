package com.fh.scm.services;

import com.fh.scm.pojo.Category;

import java.util.List;
import java.util.Map;

public interface CategoryService {

    Category get(Long id);

    void insert(Category category);

    void update(Category category);

    void delete(Long id);

    void softDelete(Long id);

    Long count();

    List<Category> getAll(Map<String, String> params);
}
