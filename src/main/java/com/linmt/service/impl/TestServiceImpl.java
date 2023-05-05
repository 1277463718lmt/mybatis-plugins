package com.linmt.service.impl;

import com.linmt.mapper.TestMapper;
import com.linmt.service.ITestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestServiceImpl implements ITestService {

    @Autowired
    private TestMapper testMapper;

    @Override
    public List queryList1() {
        return testMapper.queryList1();
    }

    @Override
    public List queryList2() {
        return testMapper.queryList2();
    }

    @Override
    public List queryList3() {
        return testMapper.queryList3();
    }

    @Override
    public List queryList4() {
        return testMapper.queryList4();
    }
}
