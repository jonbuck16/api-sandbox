package com.example.api.sandbox;

import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;

public class OAS3MediaTypes {

    public static MediaType APPLICATION_JSON() {
        MediaType applicationJsom = new MediaType();
        Schema<?> schema = new Schema<>();
        schema.setType(Constants.OBJECT);
        applicationJsom.setSchema(schema);
        return applicationJsom;
    }
}
