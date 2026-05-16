package ru.flux.android.core.network;

import lombok.AllArgsConstructor;
import lombok.Setter;

@Setter
@AllArgsConstructor
public class AddContactRequest {
    private String phone;
    private String firstName;
    private String lastName;
}
