package com.linmt;

import com.linmt.service.ITestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * User: Linmt
 * Date: 2023/5/4
 * Time: 13:02
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTest {

    @Autowired
    private ITestService testService;

    @Test
    public void test() {
        List list1 = testService.queryList1();
        List list2 = testService.queryList2();
        List list3 = testService.queryList3();
        List list4 = testService.queryList4();
    }
}
