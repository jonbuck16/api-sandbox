package com.example.api.sandbox.utils;

import java.util.regex.Pattern;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.PathParameter;

/**
 * 
 * @since v1
 */
public class OpenApiPathUtilsTest {
    private PathParameter categoryPathParameter = Mockito.mock(PathParameter.class);
    private Operation getOperation = Mockito.mock(Operation.class);
    private PathParameter idPathParameter = Mockito.mock(PathParameter.class);
    private PathItem pathItem = Mockito.mock(PathItem.class);
    private PathParameter pathParameter = Mockito.mock(PathParameter.class);
    private Schema<?> categorySchema = Mockito.mock(Schema.class);
    private Schema<?> idSchema = Mockito.mock(Schema.class);

    @Test
    public void basicPath() throws Exception {
        Pattern pattern = OpenApiUtils.pathToRegex("/basic/path", pathItem, HttpMethod.GET);

        Assert.assertThat(pattern.pattern(), CoreMatchers.equalTo("/basic/path"));
    }

    @Test
    public void pathWithIntegerVariable() throws Exception {
        Mockito.when(pathItem.readOperationsMap()).thenReturn(ImmutableMap.of(HttpMethod.GET, getOperation));
        Mockito.when(getOperation.getParameters()).thenReturn(ImmutableList.of(pathParameter));
        Mockito.when(pathParameter.getName()).thenReturn("id");
        Mockito.when(pathParameter.getIn()).thenReturn("path");
        Mockito.when(pathParameter.getSchema()).thenReturn(idSchema);
        Mockito.when(pathParameter.getSchema().getType()).thenReturn("integer");
        
        Pattern pattern = OpenApiUtils.pathToRegex("/basic/{id}", pathItem, HttpMethod.GET);

        Assert.assertThat(pattern.pattern(), CoreMatchers.equalTo("/basic/[0-9]*"));
    }
    
    @Test
    public void pathWithIntegerVariableInTheMiddle() throws Exception {
        Mockito.when(pathItem.readOperationsMap()).thenReturn(ImmutableMap.of(HttpMethod.GET, getOperation));
        Mockito.when(getOperation.getParameters()).thenReturn(ImmutableList.of(pathParameter));
        Mockito.when(pathParameter.getName()).thenReturn("id");
        Mockito.when(pathParameter.getIn()).thenReturn("path");
        Mockito.when(pathParameter.getSchema()).thenReturn(idSchema);
        Mockito.when(pathParameter.getSchema().getType()).thenReturn("integer");
        
        Pattern pattern = OpenApiUtils.pathToRegex("/basic/{id}/uploadImage", pathItem, HttpMethod.GET);

        Assert.assertThat(pattern.pattern(), CoreMatchers.equalTo("/basic/[0-9]*/uploadImage"));
    }

    @Test
    public void pathWithIntegerVariableInTheMiddleButNoParamater() throws Exception {
        Mockito.when(pathItem.readOperationsMap()).thenReturn(ImmutableMap.of(HttpMethod.GET, getOperation));
        Mockito.when(getOperation.getParameters()).thenReturn(ImmutableList.of());
        
        Pattern pattern = OpenApiUtils.pathToRegex("/basic/{id}/uploadImage", pathItem, HttpMethod.GET);

        Assert.assertThat(pattern.pattern(), CoreMatchers.equalTo("/basic/\\{id\\}/uploadImage"));
    }
    
    @Test
    public void pathWithMultipleVariablesAndParameters() throws Exception {
        Mockito.when(pathItem.readOperationsMap()).thenReturn(ImmutableMap.of(HttpMethod.GET, getOperation));
        Mockito.when(getOperation.getParameters()).thenReturn(ImmutableList.of(categoryPathParameter, idPathParameter));
        Mockito.when(categoryPathParameter.getName()).thenReturn("category");
        Mockito.when(categoryPathParameter.getIn()).thenReturn("path");
        Mockito.when(categoryPathParameter.getSchema()).thenReturn(categorySchema);
        Mockito.when(categoryPathParameter.getSchema().getType()).thenReturn("string");
        Mockito.when(idPathParameter.getName()).thenReturn("id");
        Mockito.when(idPathParameter.getIn()).thenReturn("path");
        Mockito.when(idPathParameter.getSchema()).thenReturn(idSchema);
        Mockito.when(idPathParameter.getSchema().getType()).thenReturn("integer");
        
        Pattern pattern = OpenApiUtils.pathToRegex("/basic/{category}/{id}", pathItem, HttpMethod.GET);
        
        Assert.assertThat(pattern.pattern(), CoreMatchers.equalTo("/basic/[a-zA-Z]*/[0-9]*"));
    }
    
    @Test
    public void pathWithMultipleVariablesNoParameters() throws Exception {
        Mockito.when(pathItem.readOperationsMap()).thenReturn(ImmutableMap.of(HttpMethod.GET, getOperation));
        Mockito.when(getOperation.getParameters()).thenReturn(ImmutableList.of());
        
        Pattern pattern = OpenApiUtils.pathToRegex("/basic/{category}/{id}", pathItem, HttpMethod.GET);
        
        Assert.assertThat(pattern.pattern(), CoreMatchers.equalTo("/basic/\\{category\\}/\\{id\\}"));
    }

    @Test
    public void pathWithStringVariable() throws Exception {
        Mockito.when(pathItem.readOperationsMap()).thenReturn(ImmutableMap.of(HttpMethod.GET, getOperation));
        Mockito.when(getOperation.getParameters()).thenReturn(ImmutableList.of(pathParameter));
        Mockito.when(pathParameter.getName()).thenReturn("id");
        Mockito.when(pathParameter.getIn()).thenReturn("path");
        Mockito.when(pathParameter.getSchema()).thenReturn(idSchema);
        Mockito.when(pathParameter.getSchema().getType()).thenReturn("string");
        
        Pattern pattern = OpenApiUtils.pathToRegex("/basic/{id}", pathItem, HttpMethod.GET);

        Assert.assertThat(pattern.pattern(), CoreMatchers.equalTo("/basic/[a-zA-Z]*"));
    }
    
    @Test
    public void pathWithVariableNoParameter() throws Exception {
        Mockito.when(pathItem.readOperationsMap()).thenReturn(ImmutableMap.of(HttpMethod.GET, getOperation));
        Mockito.when(getOperation.getParameters()).thenReturn(ImmutableList.of());
        
        Pattern pattern = OpenApiUtils.pathToRegex("/basic/{id}", pathItem, HttpMethod.GET);
        
        Assert.assertThat(pattern.pattern(), CoreMatchers.equalTo("/basic/\\{id\\}"));
    }
}
