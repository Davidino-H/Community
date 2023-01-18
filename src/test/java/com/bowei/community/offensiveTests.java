package com.bowei.community;

import com.bowei.community.util.OffensiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class offensiveTests {
    @Autowired
    private OffensiveFilter offensiveFilter;
    @Test
    public void tessOffensiveFilter() {
        String text = "你是bitch，还是nigger。ahahaah";
        text = offensiveFilter.filter(text);
        System.out.println(text);

        String text1 = "你是$bitch，还&是nig&g*er。aha&haah";
        text1 = offensiveFilter.filter(text1);
        System.out.println(text1);
    }

}
