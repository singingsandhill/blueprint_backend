package com.chapter1.blueprint.member.controller;

import com.chapter1.blueprint.member.domain.PolicyAlarm;
import com.chapter1.blueprint.member.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @Test
    public void testGetNotifications() throws Exception {
        PolicyAlarm mockAlarm = new PolicyAlarm();
        mockAlarm.setIdx(1L);
        mockAlarm.setUid(1L);
        mockAlarm.setAlarmCategory("Test Category");
        mockAlarm.setAlarmType("Test Type");
        mockAlarm.setNotificationEnabled(true);

        List<PolicyAlarm> mockNotifications = List.of(mockAlarm);

        when(memberService.getUidByMemberId("testMemberId")).thenReturn(1L);
        when(memberService.getNotificationsByUid(1L)).thenReturn(mockNotifications);

        mockMvc.perform(get("/member/notifications")
                        .with(SecurityMockMvcRequestPostProcessors.user("testMemberId").roles("MEMBER")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"success\":true,\"response\":{\"data\":[{\"idx\":1,\"uid\":1,\"alarmCategory\":\"Test Category\",\"alarmType\":\"Test Type\",\"notificationEnabled\":true}]}}"));
    }

}
