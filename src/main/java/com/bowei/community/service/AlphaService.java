package com.bowei.community.service;

import com.bowei.community.dao.AlphaDao;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
//@Scope("prototype")
public class AlphaService {
    @Autowired
    private AlphaDao alphaDao;


    public AlphaService() {
        System.out.println("instantiate AlphaService");
    }
    @PostConstruct
    public void init() {
        System.out.println("Initialize AlphaService");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("Destroy AlphaService");
    }


    public String find() {
        return alphaDao.select();
    }
}
