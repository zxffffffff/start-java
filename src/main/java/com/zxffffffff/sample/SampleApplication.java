/****************************************************************************
 ** MIT License
 **
 ** Author	: xiaofeng.zhu
 ** Support	: zxffffffff@outlook.com, 1337328542@qq.com
 **
 ****************************************************************************/

package com.zxffffffff.sample;

import com.zxffffffff.sample_service.ChatContactService;
import com.zxffffffff.sample_service.UserAccountService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SampleApplication {
    public ChatContactService chatContactService = new ChatContactService();
    public UserAccountService userAccountService = new UserAccountService();

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }

    /**
     * 接口：http://localhost:8080/hello?name=xxx
     *
     * @param name
     * @return Hello xxx!
     */
    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return String.format("Hello %s!", name);
    }
}
