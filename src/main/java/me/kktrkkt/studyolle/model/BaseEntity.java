package me.kktrkkt.studyolle.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.springframework.data.domain.AbstractAggregateRoot;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Objects;

@MappedSuperclass
@Getter @Setter
public abstract class BaseEntity<T extends AbstractAggregateRoot<T>> extends AbstractAggregateRoot<T> implements Serializable {

    @Id
    @GeneratedValue
    protected Long id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        BaseEntity<?> that = (BaseEntity<?>) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
