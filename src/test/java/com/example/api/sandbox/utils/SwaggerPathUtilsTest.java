package com.example.api.sandbox.utils;

import java.util.regex.Pattern;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.parameters.PathParameter;

/**
 * Ensures that the SwaggerPathUtils returns the correct values.
 * 
 * @since v1
 */
public class SwaggerPathUtilsTest {

    private PathParameter categoryPathParameter = Mockito.mock(PathParameter.class);
    private Operation getOperation = Mockito.mock(Operation.class);
    private PathParameter idPathParameter = Mockito.mock(PathParameter.class);
    private Path path = Mockito.mock(Path.class);
    private PathParameter pathParameter = Mockito.mock(PathParameter.class);

    @Test
    public void basicPath() throws Exception {
        Pattern pattern = SwaggerPathUtils.pathToRegex("/basic/path", path, HttpMethod.GET);

        Assert.assertThat(pattern.pattern(), CoreMatchers.equalTo("/basic/path"));
    }

    @Test
    public void pathWithIntegerVariable() throws Exception {
        Mockito.when(path.getOperationMap()).thenReturn(ImmutableMap.of(HttpMethod.GET, getOperation));
        Mockito.when(getOperation.getParameters()).thenReturn(ImmutableList.of(pathParameter));
        Mockito.when(pathParameter.getName()).thenReturn("id");
        Mockito.when(pathParameter.getIn()).thenReturn("path");
        Mockito.when(pathParameter.getType()).thenReturn("integer");
        Pattern pattern = SwaggerPathUtils.pathToRegex("/basic/{id}", path, HttpMethod.GET);

        Assert.assertThat(pattern.pattern(), CoreMatchers.equalTo("/basic/[0-9]*"));
    }
    
    @Test
    public void pathWithIntegerVariableInTheMiddle() throws Exception {
        Mockito.when(path.getOperationMap()).thenReturn(ImmutableMap.of(HttpMethod.GET, getOperation));
        Mockito.when(getOperation.getParameters()).thenReturn(ImmutableList.of(pathParameter));
        Mockito.when(pathParameter.getName()).thenReturn("id");
        Mockito.when(pathParameter.getIn()).thenReturn("path");
        Mockito.when(pathParameter.getType()).thenReturn("integer");
        Pattern pattern = SwaggerPathUtils.pathToRegex("/basic/{id}/uploadImage", path, HttpMethod.GET);

        Assert.assertThat(pattern.pattern(), CoreMatchers.equalTo("/basic/[0-9]*/uploadImage"));
    }

    @Test
    public void pathWithIntegerVariableInTheMiddleButNoParamater() throws Exception {
        Mockito.when(path.getOperationMap()).thenReturn(ImmutableMap.of(HttpMethod.GET, getOperation));
        Mockito.when(getOperation.getParameters()).thenReturn(ImmutableList.of());
        Pattern pattern = SwaggerPathUtils.pathToRegex("/basic/{id}/uploadImage", path, HttpMethod.GET);

        Assert.assertThat(pattern.pattern(), CoreMatchers.equalTo("/basic/\\{id\\}/uploadImage"));
    }
    
    @Test
    public void pathWithMultipleVariablesAndParameters() throws Exception {
        Mockito.when(path.getOperationMap()).thenReturn(ImmutableMap.of(HttpMethod.GET, getOperation));
        Mockito.when(getOperation.getParameters()).thenReturn(ImmutableList.of(categoryPathParameter, idPathParameter));
        Mockito.when(categoryPathParameter.getName()).thenReturn("category");
        Mockito.when(categoryPathParameter.getIn()).thenReturn("path");
        Mockito.when(categoryPathParameter.getType()).thenReturn("string");
        Mockito.when(idPathParameter.getName()).thenReturn("id");
        Mockito.when(idPathParameter.getIn()).thenReturn("path");
        Mockito.when(idPathParameter.getType()).thenReturn("integer");
        
        Pattern pattern = SwaggerPathUtils.pathToRegex("/basic/{category}/{id}", path, HttpMethod.GET);
        
        Assert.assertThat(pattern.pattern(), CoreMatchers.equalTo("/basic/[a-zA-Z]*/[0-9]*"));
    }
    
    @Test
    public void pathWithMultipleVariablesNoParameters() throws Exception {
        Mockito.when(path.getOperationMap()).thenReturn(ImmutableMap.of(HttpMethod.GET, getOperation));
        Mockito.when(getOperation.getParameters()).thenReturn(ImmutableList.of());
        Pattern pattern = SwaggerPathUtils.pathToRegex("/basic/{category}/{id}", path, HttpMethod.GET);
        
        Assert.assertThat(pattern.pattern(), CoreMatchers.equalTo("/basic/\\{category\\}/\\{id\\}"));
    }

    @Test
    public void pathWithStringVariable() throws Exception {
        Mockito.when(path.getOperationMap()).thenReturn(ImmutableMap.of(HttpMethod.GET, getOperation));
        Mockito.when(getOperation.getParameters()).thenReturn(ImmutableList.of(pathParameter));
        Mockito.when(pathParameter.getName()).thenReturn("id");
        Mockito.when(pathParameter.getIn()).thenReturn("path");
        Mockito.when(pathParameter.getType()).thenReturn("string");
        Pattern pattern = SwaggerPathUtils.pathToRegex("/basic/{id}", path, HttpMethod.GET);

        Assert.assertThat(pattern.pattern(), CoreMatchers.equalTo("/basic/[a-zA-Z]*"));
    }
    
    @Test
    public void pathWithVariableNoParameter() throws Exception {
        Mockito.when(path.getOperationMap()).thenReturn(ImmutableMap.of(HttpMethod.GET, getOperation));
        Mockito.when(getOperation.getParameters()).thenReturn(ImmutableList.of());
        Pattern pattern = SwaggerPathUtils.pathToRegex("/basic/{id}", path, HttpMethod.GET);
        
        Assert.assertThat(pattern.pattern(), CoreMatchers.equalTo("/basic/\\{id\\}"));
    }
}
