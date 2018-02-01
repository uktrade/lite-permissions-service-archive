package uk.gov.bis.lite.permissions.api.view;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.dropwizard.jackson.Jackson;
import org.junit.Before;
import org.junit.Test;
import uk.gov.bis.lite.permissions.Util;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.SubmissionServiceImpl;

import java.time.LocalDateTime;

public class OgelSubmissionViewTest {
  private ObjectMapper mapper;

  @Before
  public void setUp() throws Exception {
    mapper = Jackson.newObjectMapper();
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  @Test
  public void mappingFromOgelSubmissionTest() {
    OgelSubmission sub = Util.getMockOgelSubmission();
    sub.setFirstFail(LocalDateTime.now());
    sub.setLastFail(LocalDateTime.now());
    sub.setCreated(LocalDateTime.now());
    sub.setJson("{}");

    OgelSubmissionView subView = SubmissionServiceImpl.getOgelSubmissionView(sub);

    assertThat(subView.getJson()).isEqualTo(sub.getJson());
    assertThat(subView.getId()).isEqualTo(Long.toString(sub.getId()));
    assertThat(subView.getUserId()).isEqualTo(sub.getUserId());
    assertThat(subView.getOgelType()).isEqualTo(sub.getOgelType());
    assertThat(subView.getMode()).isEqualTo(sub.getMode().toString());
    assertThat(subView.getStatus()).isEqualTo(sub.getStatus().toString());
    assertThat(subView.getSubmissionRef()).isEqualTo(sub.getSubmissionRef());
    assertThat(subView.getCustomerRef()).isEqualTo(sub.getCustomerRef());
    assertThat(subView.getSiteRef()).isEqualTo(sub.getSiteRef());
    assertThat(subView.getFirstFail()).isEqualTo(sub.getFirstFail());
    assertThat(subView.getLastFailMessage()).isEqualTo(sub.getLastFailMessage());
    assertThat(subView.getFailReason()).isEqualTo(sub.getFailReason());
    assertThat(subView.getCallbackUrl()).isEqualTo(sub.getCallbackUrl());
    assertThat(subView.getCreated()).isEqualTo(sub.getCreated());
    assertThat(subView.getLastFail()).isEqualTo(sub.getLastFail());
  }

  @Test
  public void serializationToJsonTest () throws Exception {
    OgelSubmission sub = Util.getMockOgelSubmission();
    sub.setFirstFail(LocalDateTime.now());
    sub.setLastFail(LocalDateTime.now());
    sub.setCreated(LocalDateTime.now());
    sub.setJson("{}");

    OgelSubmissionView subView = SubmissionServiceImpl.getOgelSubmissionView(sub);

    String json = mapper.writeValueAsString(subView);
    OgelSubmissionView newSubView = mapper.readValue(json, OgelSubmissionView.class);

    assertThat(newSubView.getJson()).isEqualTo(subView.getJson());
    assertThat(newSubView.getId()).isEqualTo(subView.getId());
    assertThat(newSubView.getUserId()).isEqualTo(subView.getUserId());
    assertThat(newSubView.getOgelType()).isEqualTo(subView.getOgelType());
    assertThat(newSubView.getMode()).isEqualTo(subView.getMode());
    assertThat(newSubView.getStatus()).isEqualTo(subView.getStatus());
    assertThat(newSubView.getSubmissionRef()).isEqualTo(subView.getSubmissionRef());
    assertThat(newSubView.getCustomerRef()).isEqualTo(subView.getCustomerRef());
    assertThat(newSubView.getSiteRef()).isEqualTo(subView.getSiteRef());
    assertThat(newSubView.getFirstFail()).isEqualTo(subView.getFirstFail());
    assertThat(newSubView.getLastFailMessage()).isEqualTo(subView.getLastFailMessage());
    assertThat(newSubView.getFailReason()).isEqualTo(subView.getFailReason());
    assertThat(newSubView.getCallbackUrl()).isEqualTo(subView.getCallbackUrl());
    assertThat(newSubView.getCreated()).isEqualTo(subView.getCreated());
    assertThat(newSubView.getLastFail()).isEqualTo(subView.getLastFail());
  }
}
