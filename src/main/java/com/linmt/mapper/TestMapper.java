package com.linmt.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TestMapper {
    List queryList1();

    List queryList2();

    List queryList3();

    List queryList4();
}
