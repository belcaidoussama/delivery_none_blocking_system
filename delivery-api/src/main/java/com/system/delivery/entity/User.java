package com.system.delivery.entity;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class User implements Serializable{
    @Id
    private Long id;
    private String email;
    private String username;
    private String password;
    @Column("role")  
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) 
    private Role role;
}
