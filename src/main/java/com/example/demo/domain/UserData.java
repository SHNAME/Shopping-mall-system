package com.example.demo.domain;

import com.example.demo.en.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserData implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false,unique = true,nullable = false)
    private  long id;

    @Column(nullable = false,unique = true)
    private String email;

    @Setter
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    //JPA가 해당 Collection을 별도로 관리
   @ElementCollection(fetch = FetchType.EAGER)
   @Enumerated(EnumType.STRING)
   @Column(nullable = false)
   private List<Role> roles;

    //1:1 관계 Mapping User가 주인으로, User 데이터가 저장, 삭제, 갱신할 때 Address도 같은 연산 처리
    @OneToOne(mappedBy = "user",cascade = CascadeType.ALL,orphanRemoval = true)
    @Enumerated(EnumType.STRING)
    private Address address;

    //사용자가 삭제된다고 해서 주문이 삭제는 x, 주문은 서버의 중요 Data
    @Builder.Default
    @OneToMany(mappedBy = "user")
    private Set<UserOrder> orderList = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinColumn(name ="cart_id",nullable = false)
    @Builder.Default
    @Setter
    //장바구니 조회에 사용
    private Cart cart =new Cart();


    public void setAddress(Address address){
        this.address = address;
        address.setUser(this);
    }

    public void addOrder(UserOrder userOrder){
        orderList.add(userOrder);
        userOrder.setUser(this);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream().map(role ->new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return String.valueOf(this.id);
    }
}
