package com.sustbbgz.virtualspringbootbackend;
import com.sustbbgz.virtualspringbootbackend.common.Result;
import com.sustbbgz.virtualspringbootbackend.controller.UserController;
import com.sustbbgz.virtualspringbootbackend.controller.dto.UserDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class VirtualSpringbootBackendApplicationTests {


    @Autowired
    public UserController userController;

    // 测试用户控制器的查找所有用户方法
    @Test
    public void test() {
        // 打印用户控制器的findAll方法的结果
        System.out.println(userController.findAll());
    }

// 测试UserController中的findAll方法，验证其基本功能
@Test
public void test_findAll(){
    // 此处调用UserController的findAll方法，获取所有用户信息
//    Result result = UserController.findAll();
    // 打印查询结果，用于调试和验证方法的正确性
//    System.out.println(result);
}


    @Test
    public void test_login(){

        UserDTO dto = new UserDTO();
        dto.setPassword("113");
        dto.setUsername("xxx");
//        System.out.println(UserController.login(dto));
    }

    @Test
    public void test_findpage(){
//        System.out.println(UserController.findPage(0, 5, "", "", ""));
    }

}
