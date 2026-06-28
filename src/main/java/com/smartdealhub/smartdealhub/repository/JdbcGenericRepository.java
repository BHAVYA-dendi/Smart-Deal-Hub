package com.smartdealhub.smartdealhub.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import java.util.List;

public abstract class JdbcGenericRepository<T, ID> {

    protected final JdbcTemplate jdbcTemplate;

    public JdbcGenericRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Each repo provides the row mapper
    protected abstract RowMapper<T> getRowMapper();

    // Each repo provides table name
    protected abstract String getTableName();

    // Each repo provides primary key column
    protected abstract String getIdColumn();

    // Each repo implements insert
    public abstract int insert(T entity);

    // Each repo implements update
    public abstract int update(T entity);

    // Common methods
    public List<T> findAll() {
        return jdbcTemplate.query("SELECT * FROM " + getTableName(), getRowMapper());
    }

    public T findById(ID id) {
        List<T> results = jdbcTemplate.query(
                "SELECT * FROM " + getTableName() + " WHERE " + getIdColumn() + "=?",
                getRowMapper(),
                id
        );
        return results.isEmpty() ? null : results.get(0);
    }

    public int deleteById(ID id) {
        return jdbcTemplate.update(
                "DELETE FROM " + getTableName() + " WHERE " + getIdColumn() + "=?",
                id
        );
    }
}