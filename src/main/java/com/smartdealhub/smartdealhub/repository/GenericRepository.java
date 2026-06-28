package com.smartdealhub.smartdealhub.repository;

import java.util.List;

public interface GenericRepository<T, ID> {
    List<T> findAll();
    T findById(ID id);
    int insert(T entity);
    int update(T entity);
    int deleteById(ID id);
}