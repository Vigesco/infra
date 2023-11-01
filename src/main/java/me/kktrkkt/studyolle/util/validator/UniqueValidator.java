package me.kktrkkt.studyolle.util.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class UniqueValidator implements ConstraintValidator<Unique, String> {

    @Autowired
    private EntityManager entityManager;

    private String tableName;
    private String columnName;

    @Override
    public void initialize(Unique unique) {
        this.tableName = unique.table();
        this.columnName = unique.column();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        Long singleResult = entityManager
                .createQuery("select count(*) " +
                                "from " + tableName +
                                " where " + columnName + " = '" + value + "'"
                        , Long.class)
                .getSingleResult();
        return 0 == singleResult;
    }
}
