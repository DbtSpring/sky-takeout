package com.sky.service;

import com.sky.entity.AddressBook;
import com.sky.result.Result;

import java.util.List;

public interface AddressBookService {

    List<AddressBook> list();

    List<AddressBook> defaultList();

    void add(AddressBook addressBook);

    void setDefaultById(AddressBook addressBook);

    void update(AddressBook addressBook);

    AddressBook getById(Long id);

    void deleteById(Long id);
}
