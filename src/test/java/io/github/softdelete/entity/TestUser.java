package io.github.softdelete.entity;

import io.github.softdelete.annotation.SoftDeletable;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
@SoftDeletable
public class TestUser extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    protected TestUser() {}

    public TestUser(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public Long getId()        { return id; }
    public String getUsername() { return username; }
    public String getEmail()    { return email; }

    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email)       { this.email = email; }
}
