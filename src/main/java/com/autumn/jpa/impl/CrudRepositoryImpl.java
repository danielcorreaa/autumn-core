package com.autumn.jpa.impl;

import com.autumn.anotation.Configuration;
import com.autumn.anotation.Inject;
import com.autumn.config.EntityManagerConfig;
import com.autumn.enums.ConfigurationTypes;
import com.autumn.jpa.CrudRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

//@Configuration(type = ConfigurationTypes.DATA_SOURCE)
public class CrudRepositoryImpl<T, ID> implements CrudRepository<T, ID> {
    private Class<T> clazz;
    protected final EntityManager em;

    @Inject
    public CrudRepositoryImpl(EntityManagerConfig entityManagerConfig, Class<T> type) {
        this.clazz = type;
        this.em = entityManagerConfig.entityManager();
    }

    @Override
    public T save(T entity) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(entity);
        tx.commit();
        return entity;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(em.find(clazz, id));
    }

    @Override
    public List<T> findAll() {
        String jpql = "SELECT e FROM " + clazz.getSimpleName() + " e";
        return em.createQuery(jpql, clazz).getResultList();
    }

    @Override
    public void remove(T entity) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.remove(em.contains(entity) ? entity : em.merge(entity));
        tx.commit();
    }

    @Override
    public T update(T entity) {
        EntityTransaction tx = em.getTransaction();
        if(!tx.isActive()) {
            tx.begin();
        }
        T merged = em.merge(entity);
        tx.commit();
        return merged;
    }

    @Override
    public Optional<T> findOne(ID id) {
        T entity = em.find(clazz, id);
        return Optional.ofNullable(entity);
    }

    @Override
    public boolean exists(ID id) {
        return em.find(clazz, id) != null;
    }
}
