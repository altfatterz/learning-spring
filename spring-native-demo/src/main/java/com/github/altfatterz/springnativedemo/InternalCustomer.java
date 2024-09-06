package com.github.altfatterz.springnativedemo;

class InternalCustomer {
    private String firstName;
    private String lastName;

    public InternalCustomer(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
