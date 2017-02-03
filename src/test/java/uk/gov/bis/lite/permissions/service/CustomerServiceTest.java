package uk.gov.bis.lite.permissions.service;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.bis.lite.permissions.mocks.FailServiceMock;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class CustomerServiceTest {

  @ClassRule
  public static final ResourceTestRule resources = ResourceTestRule.builder()
      .addResource(new CreateSiteWebEndpoint())
      .build();

  private CustomerService customerService;

  @Before
  public void before() {
    FailService failService = new FailServiceMock();
    //customerService = new CustomerServiceImpl(failService, "/");
  }

  @Test
  public void testCreateSite() throws Exception {

  }

  @Path("/")
  public static class CreateSiteWebEndpoint {

    @GET
    @Path("/customer-sites/{customerId}")
    public Response get(@PathParam("customerId") String customerId) {

      return Response.ok("[]", MediaType.APPLICATION_JSON_TYPE.withCharset("utf-8")).build();
    }
  }
}
