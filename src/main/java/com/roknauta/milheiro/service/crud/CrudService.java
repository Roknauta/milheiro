package com.roknauta.milheiro.service.crud;

import com.roknauta.milheiro.dto.BaseDTO;

import java.util.List;

public interface CrudService<T extends BaseDTO> {

    T findById(Long id);

    List<T> find(T dto);

    List<T> findAll();

    T save(T dto);

    void delete(Long id);

    default void delete(T dto) {
        delete(dto.getId());
    }
}
