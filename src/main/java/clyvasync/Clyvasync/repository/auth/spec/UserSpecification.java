package clyvasync.Clyvasync.repository.auth.spec;

import clyvasync.Clyvasync.enums.auth.RoleName;
import clyvasync.Clyvasync.enums.user.UserStatus;
import clyvasync.Clyvasync.modules.auth.entity.Role;
import clyvasync.Clyvasync.modules.auth.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> filterUsers(String keyword, String role, String status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Avoid N+1 by fetching roles when the query is not a count query
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("roles", JoinType.LEFT);
            }
            // Distinct is necessary when joining one-to-many or many-to-many
            query.distinct(true);

            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchPattern = "%" + keyword.toLowerCase().trim() + "%";
                Predicate fullNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), searchPattern);
                Predicate emailPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchPattern);
                Predicate phonePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("phoneNumber")), searchPattern);
                Predicate usernamePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), searchPattern);
                
                predicates.add(criteriaBuilder.or(fullNamePredicate, emailPredicate, phonePredicate, usernamePredicate));
            }

            if (role != null && !role.equalsIgnoreCase("ALL")) {
                Join<User, Role> rolesJoin = root.join("roles", JoinType.INNER);
                try {
                    RoleName roleName = RoleName.valueOf(role.toUpperCase());
                    predicates.add(criteriaBuilder.equal(rolesJoin.get("name"), roleName));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid role filter
                }
            }

            if (status != null && !status.equalsIgnoreCase("ALL")) {
                if (status.equalsIgnoreCase("ACTIVE")) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), UserStatus.ACTIVE));
                } else if (status.equalsIgnoreCase("LOCKED")) {
                    // Match any non-active statuses mapped to LOCKED on the frontend
                    predicates.add(criteriaBuilder.notEqual(root.get("status"), UserStatus.ACTIVE));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
