package com.system.delivery.entity;

import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table("client")
@Getter
@Setter
@NoArgsConstructor
public class Client extends User {
    private String address;
    public Client(String email, String username, String password, String address) {
        super(null, email, username, password, Role.CLIENT);
        this.address = address;
    }
}
