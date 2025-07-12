package com.example.demo.dto.signDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class CodeProofResponseEmail {

    boolean success;
    String email;

}
