package uk.ac.open.kmi.basil.swagger;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class SwaggerUIBuilderTest {
   @Test
    public void test() throws URISyntaxException {
        String response = SwaggerUIBuilder.build(new URI("http://127.0.0.1:8080/basil/5134r243t/api-docs"));
        Assert.assertTrue(response.contains("\"/basil/5134r243t/api-docs\""));
//        System.err.println(response);
    }

}
