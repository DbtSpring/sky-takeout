package com.sky.service.impl;

import com.sky.context.BaseContext1;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressBookServiceImpl1 implements AddressBookService {
    @Autowired
    private AddressBookMapper addressBookMapper;



    @Override
    public void add(AddressBook addressBook) {
        addressBook.setIsDefault(0);
        addressBook.setUserId(BaseContext1.getThreadLocal());
        addressBookMapper.insert(addressBook);
    }

    @Override
    public void setDefaultById(AddressBook addressBook) {
        //设置其他所有为非默认
        addressBook.setUserId(BaseContext1.getThreadLocal());
        Long id = addressBook.getId();
        addressBook.setId(null);
        addressBook.setIsDefault(0);
        addressBookMapper.update(addressBook);
        //设置本地址为默认
        addressBook.setUserId(null);
        addressBook.setId(id);
        addressBook.setIsDefault(1);
        addressBookMapper.update(addressBook);
    }

    @Override
    public void update(AddressBook addressBook) {
        addressBookMapper.update(addressBook);
    }

    @Override
    public AddressBook getById(Long id) {
        return addressBookMapper.getById(id);
    }

    @Override
    public void deleteById(Long id) {
        addressBookMapper.deleteById(id);
    }

    @Override
    public List<AddressBook> list(){
        AddressBook addressBook = AddressBook.builder().userId(BaseContext1.getThreadLocal()).build();
        return addressBookMapper.list(addressBook);
    }

    @Override
    public List<AddressBook> defaultList(){
        AddressBook addressBook = AddressBook.builder()
                .userId(BaseContext1.getThreadLocal())
                .isDefault(1)
                .build();
        return addressBookMapper.list(addressBook);
    }
}
