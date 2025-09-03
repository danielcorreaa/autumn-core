package com.autumn.jpa;

import com.autumn.anotation.Repository;

import java.util.List;
import java.util.Optional;


public interface CrudRepository<T, ID>{
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    void remove(T entity);
    T update(T entity);
    Optional<T> findOne(ID id);
    boolean exists(ID id) ;
}
